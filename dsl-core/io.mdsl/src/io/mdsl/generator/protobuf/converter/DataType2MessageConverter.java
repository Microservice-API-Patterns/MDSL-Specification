package io.mdsl.generator.protobuf.converter;

import static io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType.BOOL;
import static io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType.BYTES;
import static io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType.DOUBLE;
import static io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType.INT32;
import static io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType.INT64;
import static io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType.STRING;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.github.microserviceapipatterns.protobufgen.model.AnyType;
import io.github.microserviceapipatterns.protobufgen.model.FieldType;
import io.github.microserviceapipatterns.protobufgen.model.Message;
import io.github.microserviceapipatterns.protobufgen.model.MessageField;
import io.github.microserviceapipatterns.protobufgen.model.ProtoSpec;
import io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.ParameterForest;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.generator.AnonymousFieldNameGenerator;
import io.mdsl.utils.CardinalityHelper;

/**
 * Converts MDSL data contracts to Protocol Buffer messages.
 */
public class DataType2MessageConverter {

	private static final FieldType FALLBACK_PRIMITIVE_TYPE = new AnyType();

	private ProtoSpec.Builder protoSpec;
	private AnonymousFieldNameGenerator fieldNameGenerator;
	private Message unspecifiedTypeMessage;
	private Set<String> generatedTypeNames;

	public DataType2MessageConverter(ProtoSpec.Builder protoSpec) {
		this.protoSpec = protoSpec;
		this.fieldNameGenerator = new AnonymousFieldNameGenerator();
		this.generatedTypeNames = Sets.newHashSet();
	}

	public Message convert(DataContract contract) {
		Message.Builder message = new Message.Builder(contract.getName());
		mapElementStructure(contract.getStructure(), message);
		return message.build();
	}

	public void mapElementStructure(ElementStructure elementStructure, Message.Builder message) {
		if (elementStructure.getPf() != null) {
			mapParameterForest(elementStructure.getPf(), message);
		} else if (elementStructure.getPt() != null) {
			mapParameterTree(elementStructure.getPt(), message);
		} else if (elementStructure.getApl() != null) {
			mapAtomicParameterList(elementStructure.getApl(), message);
		} else {
			mapSingleParameterNode(elementStructure.getNp(), message);
		}
	}

	private void mapParameterForest(ParameterForest pf, Message.Builder message) {
		List<ParameterTree> trees = Lists.newLinkedList();
		trees.add(pf.getPtl().getFirst());
		trees.addAll(pf.getPtl().getNext());
		for (ParameterTree tree : trees) {
			String attrName = fieldNameGenerator.getUniqueName(tree.getName());
			String typeName = deriveMessageNameFromParameterName(attrName);
			Message.Builder nestedMessageBuilder = new Message.Builder(typeName);
			mapParameterTree(tree, nestedMessageBuilder);
			Message nestedMessage = nestedMessageBuilder.build();
			message.withNestedMessage(nestedMessage);
			MessageField.Builder field = new MessageField.Builder(nestedMessage, attrName, getNextFieldNumber(message));
			addField2Message(message, field, tree.getCard());
		}
	}

	private void mapParameterTree(ParameterTree pt, Message.Builder message) {
		List<TreeNode> treeNodes = Lists.newLinkedList();
		treeNodes.add(pt.getFirst());
		treeNodes.addAll(pt.getNexttn());
		for (TreeNode treeNode : treeNodes) {
			mapTreeNode(treeNode, message);
		}
	}

	private void mapTreeNode(TreeNode treeNode, Message.Builder message) {
		if (treeNode.getPn() != null && treeNode.getPn().getGenP() != null) {
			message.withField(getUnspecifiedTypeMessage(),
					fieldNameGenerator.getUniqueName(treeNode.getPn().getGenP().getName()));
		} else if (treeNode.getPn() != null) {
			mapSingleParameterNode(treeNode.getPn(), message);
		} else if (treeNode.getApl() != null) {
			AtomicParameterList list = treeNode.getApl();
			String attrName = fieldNameGenerator.getUniqueName(list.getName());
			String typeName = deriveMessageNameFromParameterName(attrName);
			Message.Builder nestedMessageBuilder = new Message.Builder(typeName);
			mapAtomicParameterList(list, nestedMessageBuilder);
			Message nestedMessage = nestedMessageBuilder.build();
			message.withNestedMessage(nestedMessage);
			MessageField.Builder field = new MessageField.Builder(nestedMessage, attrName, getNextFieldNumber(message));
			addField2Message(message, field, list.getCard());
		} else if (treeNode.getChildren() != null) {
			ParameterTree subTree = treeNode.getChildren();
			String attrName = fieldNameGenerator.getUniqueName(subTree.getName());
			String typeName = deriveMessageNameFromParameterName(attrName);
			Message.Builder nestedMessageBuilder = new Message.Builder(typeName);
			mapParameterTree(subTree, nestedMessageBuilder);
			Message nestedMessage = nestedMessageBuilder.build();
			message.withNestedMessage(nestedMessage);
			MessageField.Builder field = new MessageField.Builder(nestedMessage, attrName, getNextFieldNumber(message));
			addField2Message(message, field, subTree.getCard());
		}
	}

	private void mapAtomicParameterList(AtomicParameterList apl, Message.Builder message) {
		List<AtomicParameter> parameters = new LinkedList<>();
		parameters.add(apl.getFirst());
		parameters.addAll(apl.getNextap());
		for (AtomicParameter ap : parameters) {
			MessageField.Builder field = new MessageField.Builder(mapBasicType(ap.getRat().getBtype()),
					fieldNameGenerator.getUniqueName(ap.getRat().getName()), getNextFieldNumber(message));
			addField2Message(message, field, ap.getCard());
		}
	}

	private void mapSingleParameterNode(SingleParameterNode spn, Message.Builder message) {
		if (spn.getAtomP() != null) {
			MessageField.Builder field = new MessageField.Builder(mapBasicType(spn.getAtomP().getRat().getBtype()),
					fieldNameGenerator.getUniqueName(spn.getAtomP().getRat().getName()), getNextFieldNumber(message));
			addField2Message(message, field, spn.getAtomP().getCard());
		} else if (spn.getTr() != null) {
			String attrName = fieldNameGenerator.getUniqueName(spn.getTr().getName());
			MessageField.Builder field = new MessageField.Builder(convert(spn.getTr().getDcref()), attrName,
					getNextFieldNumber(message));
			addField2Message(message, field, spn.getTr().getCard());
		} else if (spn.getGenP() != null) {
			// nothing to do in this case; we just create empty message without fields
		}
	}

	private Message getUnspecifiedTypeMessage() {
		if (unspecifiedTypeMessage == null) {
			unspecifiedTypeMessage = new Message.Builder("UnspecifiedType").build();
			protoSpec.withMessage(unspecifiedTypeMessage);
		}
		return unspecifiedTypeMessage;
	}

	private String deriveMessageNameFromParameterName(String parameterName) {
		String notNullOrEmptyName = fieldNameGenerator.getUniqueName(parameterName);
		String derivedName = notNullOrEmptyName.substring(0, 1).toUpperCase() + notNullOrEmptyName.substring(1)
				+ "Message";
		if (generatedTypeNames.contains(derivedName)) {
			String baseName = derivedName;
			int counter = 2;
			while (generatedTypeNames.contains(derivedName)) {
				derivedName = baseName + "_" + counter;
				counter++;
			}
		}
		generatedTypeNames.add(derivedName);
		return derivedName;
	}

	private FieldType mapBasicType(String mdslType) {
		if (mdslType == null)
			return FALLBACK_PRIMITIVE_TYPE;
		switch (mdslType) {
		case "bool":
			return BOOL;
		case "int":
			return INT32;
		case "long":
			return INT64;
		case "double":
			return DOUBLE;
		case "string":
			return STRING;
		case "raw":
			return BYTES;
		default:
			return FALLBACK_PRIMITIVE_TYPE;
		}
	}

	private void addField2Message(Message.Builder message, MessageField.Builder field, Cardinality card) {
		if (CardinalityHelper.isList(card))
			message.withField(field.repeated().build());
		else if (CardinalityHelper.isOptional(card))
			message.withField(field).withField(SimpleFieldType.BOOL, "has_" + field.build().getName()).build();
		else
			message.withField(field.build());
	}

	private int getNextFieldNumber(Message.Builder message) {
		return message.build().getFields().size() + 1;
	}

}

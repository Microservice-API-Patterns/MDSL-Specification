package io.mdsl.generator.jolie.converter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterForest;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.AnonymousFieldNameGenerator;

/**
 * Converts MDSL ServiceSpecification to Jolie port and interface
 * 
 */
public class MDSL2JolieConverter {

	private ServiceSpecificationAdapter mdslSpecification;

	private static final String DEFAULT_TYPE = "msg: string";
	private static final String UNDEFINED = "undefined";
	private AnonymousFieldNameGenerator fieldNameGenerator;

	public MDSL2JolieConverter(ServiceSpecification mdslSpecification) {
		this.mdslSpecification = new ServiceSpecificationAdapter(mdslSpecification);
		this.fieldNameGenerator = new AnonymousFieldNameGenerator();
	}

	public void convertOperations(EndpointContract endpoint, HashMap<String, OperationModel> result) {
		for (Operation operation : endpoint.getOps()) {
			String mep = operation.getMep();
			String requestSignature = convertMessageStructure(operation.getRequestMessage());
			String responseSignature = convertMessageStructure(operation.getResponseMessage());
			OperationModel om = new OperationModel(operation.getName(), requestSignature, responseSignature, convertResponsibility(operation.getResponsibility()), mep);
			result.put(operation.getName(), om);
		}
	}

	private String convertMessageStructure(DataTransferRepresentation dtr) {
		if (dtr == null)
			return DEFAULT_TYPE;

		// TODO handle headers (future work for all generators)

		StringBuffer result = new StringBuffer();
		ElementStructure payload = dtr.getPayload();
		convertElementStructure(payload, result);

		return result.toString();
	}

	private void convertElementStructure(ElementStructure representationElement, StringBuffer result) {
		if (representationElement.getNp() != null) {
			SingleParameterNode simpleParameter = representationElement.getNp();
			convertSingleParameterNode(simpleParameter, result);
			// TODO remove (left over after refactoring), test more systematically
//			if(simpleParameter.getAtomP() != null) {
//				AtomicParameter ap = simpleParameter.getAtomP();
//				convertAtomicParameter(ap, result);
//			}
//			else if (simpleParameter.getGenP()!=null) {
//				GenericParameter genericParameter = simpleParameter.getGenP();
//				String parameterName = genericParameter.getName();
//				 
//				parameterName = createNameIfEmpty(parameterName);
//				
//				result.append(parameterName + ": void /* placeholder parameter */");
//			}
//			else if (simpleParameter.getTr()!=null) {
//				TypeReference typeRef = simpleParameter.getTr();
//				convertTypeReference(typeRef, result);
//			}
//			else {
//				// can/should not get here, but you never know 
//				throw new MDSLException("Unexpected spn type");
//			}
		} else if (representationElement.getApl() != null) {
			convertAtomicParameterList(representationElement.getApl(), result);
		} else if (representationElement.getPt() != null) {
			ParameterTree pt = representationElement.getPt();
			convertParameterTree(pt, result);
		} else if (representationElement.getPf() != null) {
			convertParameterForest(representationElement.getPf(), result);
		} else {
			// can/should not get here, but you never know (PragProg hint)
			throw new MDSLException("Unexpected type of element structure");
		}
	}

	private void convertTypeReference(TypeReference typeRef, StringBuffer result) {
		String parameterName = typeRef.getName();
		parameterName = createNameIfEmpty(parameterName);
		parameterName = handleCardinality(typeRef.getCard(), parameterName);
		result.append(parameterName + ": " + typeRef.getDcref().getName() + " "); // name always there?
	}

	private void convertParameterForest(ParameterForest pf, StringBuffer result) {
		List<ParameterTree> trees = Lists.newLinkedList();
		trees.add(pf.getPtl().getFirst());
		trees.addAll(pf.getPtl().getNext());
		for (ParameterTree tree : trees) {
			String attrName = fieldNameGenerator.getUniqueName(tree.getName());
			String typeName = deriveTypeNameFromParameterName(attrName);
			typeName = handleCardinality(tree.getCard(), typeName);
			StringBuffer nestedResult = new StringBuffer();
			convertParameterTree(tree, nestedResult);
			result.append(typeName);
			result.append(": void { ");
			result.append(nestedResult);
			result.append("} ");
		}
	}

	private void convertParameterTree(ParameterTree pt, StringBuffer result) {

		String attrName = fieldNameGenerator.getUniqueName(pt.getName());
		String typeName = deriveTypeNameFromParameterName(attrName);
		typeName = handleCardinality(pt.getCard(), typeName);
		result.append(typeName);
		result.append(": void { ");

		List<TreeNode> treeNodes = Lists.newLinkedList();
		treeNodes.add(pt.getFirst());
		treeNodes.addAll(pt.getNexttn());
		for (TreeNode treeNode : treeNodes) {
			convertTreeNode(treeNode, result);
			// result.append("\n\t"); // TODO not a clean solution for indentation
		}

		result.append("} ");
	}

	private void convertTreeNode(TreeNode treeNode, StringBuffer result) {
		if (treeNode.getPn() != null && treeNode.getPn().getGenP() != null) {
			// add unspecified message/type/parameter (see protogen)
			if (treeNode.getPn().getGenP().getName() != null && !"".equals(treeNode.getPn().getGenP().getName()))
				result.append(new AnonymousFieldNameGenerator().getUniqueName(treeNode.getPn().getGenP().getName()) + ": void ");
			else
				result.append("unspecifiedType: void ");
		} else if (treeNode.getPn() != null) {
			StringBuffer nestedResult = new StringBuffer();
			convertSingleParameterNode(treeNode.getPn(), nestedResult);
			result.append(nestedResult);
		} else if (treeNode.getApl() != null) {
			AtomicParameterList list = treeNode.getApl();
			StringBuffer nestedResult = new StringBuffer(); // needed here?
			convertAtomicParameterList(list, nestedResult);
			result.append(nestedResult);
		} else if (treeNode.getChildren() != null) {
			ParameterTree subTree = treeNode.getChildren();
			StringBuffer nestedResult = new StringBuffer();
			convertParameterTree(subTree, nestedResult);
			result.append(nestedResult);
		}
	}

	private void convertSingleParameterNode(SingleParameterNode spn, StringBuffer result) {
		if (spn.getAtomP() != null) {
			convertAtomicParameter(spn.getAtomP(), result);
		} else if (spn.getGenP() != null) {
			GenericParameter genericParameter = spn.getGenP();
			String parameterName = genericParameter.getName();
			parameterName = createNameIfEmpty(parameterName);

			result.append(parameterName + ": void /* placeholder parameter */");
		} else if (spn.getTr() != null) {
			convertTypeReference(spn.getTr(), result);
		} else
			// can/should not get here, but you never know
			throw new MDSLException("Unknown type of spn");
	}

	private void convertAtomicParameterList(AtomicParameterList apl, StringBuffer result) {
		String attrName = fieldNameGenerator.getUniqueName(apl.getName());
		String typeName = deriveTypeNameFromParameterName(attrName);
		StringBuffer nestedResult = new StringBuffer(); // needed here?

		typeName = handleCardinality(apl.getCard(), typeName);
		nestedResult.append(typeName);
		nestedResult.append(": void { ");

		List<AtomicParameter> parameters = new LinkedList<>();
		parameters.add(apl.getFirst());
		parameters.addAll(apl.getNextap());
		for (AtomicParameter ap : parameters) {
			StringBuffer nextResult = new StringBuffer();
			convertAtomicParameter(ap, nextResult);
			nestedResult.append(nextResult);
			nestedResult.append("\n\t"); // would be nice not add this for last AP
		}

		nestedResult.append("} ");
		result.append(nestedResult.toString());
	}

	private void convertAtomicParameter(AtomicParameter ap, StringBuffer result) {
		RoleAndType rat = ap.getRat();
		String name = rat.getName();
		name = createNameIfEmpty(name);

		String role = rat.getRole();

		String type = rat.getBtype();
		if (type != null) {
			type = convertBaseType(type); // no conversion needed (type systems aligned)
		} else {
			type = "string"; // default assumption
		}

		// handle cardinalities
		name = handleCardinality(ap.getCard(), name);

		// add role as comment
		result.append(name + ": " + type + " /* data type role: " + role + " */ ");
	}

	private String createNameIfEmpty(String parameterName) {
		return fieldNameGenerator.getUniqueName(parameterName);
	}

	private String handleCardinality(Cardinality card, String name) {
		if (card == null)
			return name; // [1,1] not needed (default in Jolie)

		if (card.getExactlyOne() != null)
			return name;
		else if (card.getAtLeastOne() != null) {
			return name + "[1,*]";
		} else if (card.getZeroOrMore() != null) {
			return name + "[0,*]"; // could also add "*" only
		} else if (card.getZeroOrOne() != null) {
			return name + "[0,1]";
		} else
			throw new MDSLException("Unknown cardinality of " + name);
	}

	private String convertBaseType(String type) {
		return type;
	}

	private String deriveTypeNameFromParameterName(String attrName) {
		// could implement Jolie naming conventions here (capitalization?)
		return attrName;
	}

	private String convertResponsibility(OperationResponsibility responsibility) {
		if (responsibility == null) {
			return UNDEFINED;
		}
		if (responsibility.getCf() != null) {
			return responsibility.getCf();
		} else if (responsibility.getSco() != null) {
			return responsibility.getSco();
		} else if (responsibility.getSto() != null) {
			return responsibility.getSto();
		} else if (responsibility.getRo() != null) {
			return responsibility.getRo();
		} else if (responsibility.getOther() != null) {
			return responsibility.getOther();
		}
		return UNDEFINED;
	}

	public HashMap<String, OperationModel> convertEndpoints() {
		HashMap<String, OperationModel> result = new HashMap<String, OperationModel>();
		for (EndpointContract endpoint : mdslSpecification.getEndpointContracts()) {
			this.convertOperations(endpoint, result);
		}
		return result;
	}

	public HashMap<String, TypeModel> convertDataTypes() {
		HashMap<String, TypeModel> result = new HashMap<String, TypeModel>();
		for (DataContract dc : this.mdslSpecification.getTypes()) {
			StringBuffer convertedType = new StringBuffer();
			this.convertElementStructure(dc.getStructure(), convertedType);
			String tname = dc.getName();
			TypeModel tdefinition = new TypeModel(tname, convertedType.toString());
			result.put(tname, tdefinition);
		}
		return result;
	}
}

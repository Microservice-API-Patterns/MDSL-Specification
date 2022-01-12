package io.mdsl.generator.openapi.converter;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.ParameterForest;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.generator.AnonymousFieldNameGenerator;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

/**
 * Converts an MDSL datatype to an OpenAPI schema
 * 
 * @author ska, socadk
 *
 */
@SuppressWarnings("rawtypes")
public class DataType2SchemaConverter {

	private static final String GENERIC_PARAMETER_DESCRIPTION = "Generic parameter";
	private static final String ID_ROLE = "ID";
	private static final String IDENTIFIER_ROLE = "Identifier"; // identical to ID_ROLE semantically
	private static final String L_ROLE = "L"; // short for Link
	private static final String LINK_ROLE = "Link";
	
	public final static String REF_PREFIX = "#/components/schemas/";
	private static final String TREE_IDENTIFIER_PREFIX = "tree";

	private AnonymousFieldNameGenerator fieldNameGenerator;

	public DataType2SchemaConverter() {
		this.fieldNameGenerator = new AnonymousFieldNameGenerator();
	}

	public Schema convert(DataContract dataType) {
		return convert(dataType.getStructure()).name(dataType.getName());
	}

	public Schema convert(ElementStructure structure) {
		ObjectSchema object = new ObjectSchema();
		Map<String, Schema> propertySchemas = convertProperties(structure);
		if (propertySchemas != null && !propertySchemas.isEmpty())
			object.setProperties(propertySchemas);

		if (structure.getApl() != null) {
			return mapCardinalities(structure.getApl().getCard(), object);
		} else if (structure.getPt() != null) {
			return mapCardinalities(structure.getPt().getCard(), object);
		} else {
			return object;
		}
	}

	public Schema convert(AtomicParameter atomicParameter) {
		RoleAndType rat = atomicParameter.getRat();
		Schema schema = getSchema4ParameterType(atomicParameter);
		if(schema!=null) {
			schema.setName(fieldNameGenerator.getUniqueName(rat.getName()));
			if (atomicParameter.getCard() != null) {
				schema = mapCardinalities(atomicParameter.getCard(), schema);
			}
			String description = MDSLSpecificationWrapper.getClassifierAndElementStereotype(atomicParameter.getClassifier(), rat);
			schema.setDescription(description);
		}
		return schema;
	}
	
	public Schema<?> convert(GenericParameter genP) {
		// note: no pattern stereotype classifier or cardinality in genP grammar 
		Schema schema = new StringSchema();
		schema.setName(fieldNameGenerator.getUniqueName(genP.getName()));
		schema.setDescription(GENERIC_PARAMETER_DESCRIPTION);
		return schema;
	}
	
	public Schema convertAndCreateSchema4ParameterTreeAndItsNodes(ParameterTree pt, boolean externalCardinality) {
		if(pt==null) {
			return null;
		}
		
		Schema treeWrapperSchema = new ObjectSchema();
		treeWrapperSchema.setName(fieldNameGenerator.getUniqueName(pt.getName()));
		treeWrapperSchema.setProperties(convertProperties(pt));

		String description = MDSLSpecificationWrapper.getClassifierAndElementStereotype(pt.getClassifier(), null);
		if(description!=null&&!description.equals("")) {
			treeWrapperSchema.setDescription(description);
		}
		
		if(externalCardinality)
			return getArrayWrapperSchema(treeWrapperSchema);
		else
			return mapCardinalities(pt.getCard(), treeWrapperSchema);
	}

	public Schema mapCardinalities(Cardinality card, Schema schema) {
		if (card == null)
			return schema;

		if (card.getZeroOrMore() != null) {
			return getArrayWrapperSchema(schema);
		} else if (card.getAtLeastOne() != null) {
			return getArrayWrapperSchema(schema).minItems(1);
		} else if (card.getExactlyOne() != null) {
			return schema.nullable(false);
		} else if (card.getZeroOrOne() != null) {
			return schema.nullable(true);
		}
		return schema;
	}

	private Map<String, Schema> convertProperties(ElementStructure dataTypeStructure) {
		if (dataTypeStructure.getApl() != null) {
			return convertProperties(dataTypeStructure.getApl());
		} else if (dataTypeStructure.getNp() != null) {
			return convertProperties(dataTypeStructure.getNp());
		} else if (dataTypeStructure.getPf() != null) {
			return convertProperties(dataTypeStructure.getPf());
		} else if (dataTypeStructure.getPt() != null) {
			return convertProperties(dataTypeStructure.getPt());
		}
		return null;
	}

	private Map<String, Schema> convertProperties(AtomicParameterList parameterList) {
		Map<String, Schema> map = new LinkedHashMap<>();
		Schema firstParameterSchema = convert(parameterList.getFirst());
		if(firstParameterSchema!=null) 
			map.put(firstParameterSchema.getName(), firstParameterSchema);
		for (AtomicParameter nextParameter : parameterList.getNextap()) {
			Schema nextParameterSchema = convert(nextParameter);
			if(nextParameterSchema!=null) 
				map.put(nextParameterSchema.getName(), nextParameterSchema);
		}
		return map;
	}

	private Map<String, Schema> convertProperties(SingleParameterNode parameterNode) {
		if (parameterNode.getGenP() != null) {
			// do not create attribute node in case it is a P type (on root level)
			if(parameterNode.getGenP().getName()==null||parameterNode.getGenP().getName().equals("")) {
				return null;
			}
		}

		Map<String, Schema> map = new LinkedHashMap<>();
		Schema schema = createSchema4SingleParameterNode(parameterNode);
		if (schema != null)
			map.put(schema.getName(), schema);
		return map;
	}

	private Map<String, Schema> convertProperties(ParameterForest parameterForest) {
		Map<String, Schema> map = new LinkedHashMap<>();
		if (parameterForest.getPtl() == null)
			return map;
		int treeCounter = 1;
		List<ParameterTree> trees = Lists.newLinkedList();
		trees.add(parameterForest.getPtl().getFirst());
		trees.addAll(parameterForest.getPtl().getNext());
		for (ParameterTree tree : trees) {
			Schema treeWrapperSchema = new ObjectSchema().name(TREE_IDENTIFIER_PREFIX + treeCounter);
			treeWrapperSchema.setProperties(convertProperties(tree));
			map.put(treeWrapperSchema.getName(), treeWrapperSchema);
			treeCounter++;
		}
		return map;
	}

	private Map<String, Schema> convertProperties(ParameterTree parameterTree) {
		Map<String, Schema> map = new LinkedHashMap<>();
		Schema firstTreeNodeSchema = createSchema4TreeNode(parameterTree.getFirst());
		if (firstTreeNodeSchema != null)
			map.put(firstTreeNodeSchema.getName(), firstTreeNodeSchema);
		for (TreeNode nextNode : parameterTree.getNexttn()) {
			Schema nextSchema = createSchema4TreeNode(nextNode);
			if (nextSchema != null)
				map.put(nextSchema.getName(), nextSchema);
		}
		return map;
	}

	public Schema createSchema4SingleParameterNode(SingleParameterNode singleNode) {
		MDSLLogger.reportDetailedInformation("Entering createSchema4SingleParameterNode");
		if (singleNode.getAtomP() != null) {
			Schema atomPSchema = convert(singleNode.getAtomP());
			if (atomPSchema != null)
				return atomPSchema;
		} else if (singleNode.getTr() != null) {
			return createSchemaForTypeReference(singleNode.getTr());
		} else if (singleNode.getGenP() != null) {
			Schema genPSchema = convert(singleNode.getGenP());
			if (genPSchema != null) {
				return genPSchema;
			}
		}
		return null;
	}
	
	public Schema createSchemaForTypeReference(TypeReference typeRef) {
		String un = typeRef.getName(); // can be null
		MDSLLogger.reportDetailedInformation("createSchemaForTypeReference " + un);
		String refn = REF_PREFIX + typeRef.getDcref().getName();
		// TODO v55 "mapCardinalities" is called, but does not always seems to have desired effect due to constraints of OAS spec. 
		Schema result = mapCardinalities(typeRef.getCard(),
			new Schema<>().name(fieldNameGenerator.getUniqueName(un)).$ref(refn)); // null un causes new name to be generated
		if(un!=null&&!"".equals(un)) {
			; // result.setDescription("Type reference " + un); // TODO (future work) activate feature
		}
		return result;
	}

	private Schema createSchema4TreeNode(TreeNode node) {
		if (node.getApl() != null) {
			AtomicParameterList list = node.getApl();
			Schema listWrapperSchema = new ObjectSchema();
			listWrapperSchema.setName(fieldNameGenerator.getUniqueName(list.getName()));
			listWrapperSchema.setProperties(convertProperties(list));
			return mapCardinalities(node.getApl().getCard(), listWrapperSchema);
		} else if (node.getPn() != null) {
			return createSchema4SingleParameterNode(node.getPn());
		} else if (node.getChildren() != null) {
			ParameterTree subTree = node.getChildren();
			Schema treeWrapperSchema = new ObjectSchema();
			treeWrapperSchema.setName(fieldNameGenerator.getUniqueName(subTree.getName()));
			treeWrapperSchema.setProperties(convertProperties(subTree));
			return mapCardinalities(subTree.getCard(), treeWrapperSchema);
		}
		return null;
	}
	
	private Schema getArrayWrapperSchema(Schema schema) {
		return new ArraySchema().items(schema).name(schema.getName());
	}

	private Schema getSchema4ParameterType(AtomicParameter parameter) {
		if (parameter.getRat().getBtype() != null && !"".equals(parameter.getRat().getBtype()))
			return getSchema4BasicType(parameter.getRat().getBtype());

		if (parameter.getRat().getRole() != null
				&& (ID_ROLE.equals(parameter.getRat().getRole()) || IDENTIFIER_ROLE.equals(parameter.getRat().getRole())))
			return new UUIDSchema();

		if (parameter.getRat().getRole() != null
				&& (L_ROLE.equals(parameter.getRat().getRole()) || LINK_ROLE.equals(parameter.getRat().getRole())))
			return new URISchema();

		return new StringSchema();
	}

	private Schema getSchema4BasicType(String basicType) {
		switch (basicType) {
		case "bool":
			return new BooleanSchema();
		case "int":
			return new IntegerSchema();
		case "long":
			return new NumberSchema();
		case "double":
			return new NumberSchema();
		case "string":
			return new StringSchema();
		case "raw":
			return new BinarySchema();
		case "void": {
			// decided to ignore D<void> and not map to string:
			MDSLLogger.reportInformation("Not mapping a <void> parameter.");
			return null; // new VoidSchema(); // Schema<Void> is not appreciated by OAS and other tools
			// return new ObjectSchema();
		}
		default: {
			MDSLLogger.reportWarning("Unknown basic type " + basicType);
			return null; 
			}
		}
	}

	private class VoidSchema extends Schema<Void> {
		private static final String VOID_SCHEMA = "void";

		public VoidSchema() {
			super(VOID_SCHEMA, null);
		}
	}

	private class URISchema extends Schema<URI> {
		private static final String STRING_SCHEMA_NAME = "string";

		public URISchema() {
			super(STRING_SCHEMA_NAME, "uri");
		}
	}
	
	public Schema getSchema4RequestOrResponseStructure(ElementStructure payload) {
		if (payload.getNp() != null && payload.getNp().getTr() != null) {
			// case: reference to 'data type' declaration
			TypeReference tr = payload.getNp().getTr();
			return mapCardinalities(tr.getCard(), new Schema<>().$ref(DataType2SchemaConverter.REF_PREFIX + tr.getDcref().getName()));
		} else {
			// case: data structure defined inline in MDSL
			return convert(payload);
		}
	}
}

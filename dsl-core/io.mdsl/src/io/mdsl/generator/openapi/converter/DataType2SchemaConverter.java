package io.mdsl.generator.openapi.converter;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.atomicParameter;
import io.mdsl.apiDescription.atomicParameterList;
import io.mdsl.apiDescription.dataContract;
import io.mdsl.apiDescription.elementStructure;
import io.mdsl.apiDescription.parameterForest;
import io.mdsl.apiDescription.parameterTree;
import io.mdsl.apiDescription.roleAndType;
import io.mdsl.apiDescription.singleParameterNode;
import io.mdsl.apiDescription.treeNode;
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
 * @author ska, zio
 *
 */
@SuppressWarnings("rawtypes")
public class DataType2SchemaConverter {

	public final static String REF_PREFIX = "#/components/schemas/";

	private int anonymousNameCounter = 1;

	public Schema convert(dataContract dataType) {
		return convert(dataType.getStructure()).name(dataType.getName());
	}

	public Schema convert(elementStructure structure) {
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
	
	public Schema convert(atomicParameter atomicParameter) {
		roleAndType rat = atomicParameter.getRat();
		Schema schema = getSchema4ParameterType(atomicParameter);
		schema.setName(getUniqueName(rat.getName()));
		if (atomicParameter.getCard() != null)
			schema = mapCardinalities(atomicParameter.getCard(), schema);
		return schema;
	}

	/**
	 * Wraps a schema with an ArraySchema according to given cardinalities.
	 */
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

	private Map<String, Schema> convertProperties(elementStructure dataTypeStructure) {
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

	private Map<String, Schema> convertProperties(atomicParameterList parameterList) {
		Map<String, Schema> map = new LinkedHashMap<>();
		Schema firstParameterSchema = convert(parameterList.getFirst());
		map.put(firstParameterSchema.getName(), firstParameterSchema);
		for (atomicParameter nextParameter : parameterList.getNextap()) {
			Schema nextParameterSchema = convert(nextParameter);
			map.put(nextParameterSchema.getName(), nextParameterSchema);
		}
		return map;
	}

	private Map<String, Schema> convertProperties(singleParameterNode parameterNode) {
		// do not create attribute node in case it is a P type (on root level)
		// TODO (low prio) how about mapping P to "unknown" in OAS? or at least warn?
		if (parameterNode.getGenP() != null)
			return null;

		Map<String, Schema> map = new LinkedHashMap<>();
		Schema schema = createSchema4SingleParameterNode(parameterNode);
		if (schema != null)
			map.put(schema.getName(), schema);
		return map;
	}

	private Map<String, Schema> convertProperties(parameterForest parameterForest) {
		Map<String, Schema> map = new LinkedHashMap<>();
		if (parameterForest.getPtl() == null)
			return map;
		int treeCounter = 1;
		List<parameterTree> trees = Lists.newLinkedList();
		trees.add(parameterForest.getPtl().getFirst());
		trees.addAll(parameterForest.getPtl().getNext());
		for (parameterTree tree : trees) {
			Schema treeWrapperSchema = new ObjectSchema().name("tree" + treeCounter);
			treeWrapperSchema.setProperties(convertProperties(tree));
			map.put(treeWrapperSchema.getName(), treeWrapperSchema);
			treeCounter++;
		}
		return map;
	}

	private Map<String, Schema> convertProperties(parameterTree parameterTree) {
		Map<String, Schema> map = new LinkedHashMap<>();
		Schema firstTreeNodeSchema = createSchema4TreeNode(parameterTree.getFirst());
		if (firstTreeNodeSchema != null)
			map.put(firstTreeNodeSchema.getName(), firstTreeNodeSchema);
		for (treeNode nextNode : parameterTree.getNexttn()) {
			Schema nextSchema = createSchema4TreeNode(nextNode);
			if (nextSchema != null)
				map.put(nextSchema.getName(), nextSchema);
		}
		return map;
	}

	private Schema createSchema4SingleParameterNode(singleParameterNode singleNode) {
		// only consider 'atomP'; nothing to generate for 'genP'
		// TODO (low prio) actually we could map genP to a string 
		if (singleNode.getAtomP() != null) {
			Schema atomPSchema = convert(singleNode.getAtomP());
			if (atomPSchema != null)
				return atomPSchema;
		} else if (singleNode.getTr() != null) {
			return mapCardinalities(singleNode.getTr().getCard(),
					new Schema<>().name(getUniqueName(singleNode.getTr().getName()))
							.$ref(REF_PREFIX + singleNode.getTr().getDcref().getName()));
		} else if (singleNode.getGenP() != null) {
			return new ObjectSchema().name(getUniqueName(singleNode.getGenP().getName()));
		}
		return null;
	}

	private Schema createSchema4TreeNode(treeNode node) {
		if (node.getApl() != null) {
			atomicParameterList list = node.getApl();
			Schema listWrapperSchema = new ObjectSchema();
			listWrapperSchema.setName(getUniqueName(list.getName()));
			listWrapperSchema.setProperties(convertProperties(list));
			return mapCardinalities(node.getApl().getCard(), listWrapperSchema);
		} else if (node.getPn() != null) {
			return createSchema4SingleParameterNode(node.getPn());
		} else if (node.getChildren() != null) {
			parameterTree subTree = node.getChildren();
			Schema treeWrapperSchema = new ObjectSchema();
			treeWrapperSchema.setName(getUniqueName(subTree.getName()));
			treeWrapperSchema.setProperties(convertProperties(subTree));
			return mapCardinalities(subTree.getCard(), treeWrapperSchema);
		}
		return null;
	}

	private Schema getArrayWrapperSchema(Schema schema) {
		return new ArraySchema().items(schema).name(schema.getName());
	}

	private Schema getSchema4ParameterType(atomicParameter parameter) {
		if (isStringDefined(parameter.getRat().getBtype()))
			return getSchema4BasicType(parameter.getRat().getBtype());

		if (parameter.getRat().getRole() != null
				&& ("ID".equals(parameter.getRat().getRole()) || "Identifier".equals(parameter.getRat().getRole())))
			return new UUIDSchema();

		if (parameter.getRat().getRole() != null
				&& ("L".equals(parameter.getRat().getRole()) || "Link".equals(parameter.getRat().getRole())))
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
		default:
			// TODO warn about this? 
			return new VoidSchema();
		}
	}
	
	// TODO (high prio) map D<void> (and other voids) properly:
	// [x] ignore? [-] map to string?

	private boolean isStringDefined(String name) {
		return name != null && !"".equals(name);
	}

	private String getUniqueName(String inputName) {
		// in case there is a name, just take it
		if (isStringDefined(inputName))
			return inputName;

		// in case there is no name, generate a unique "anonymous" name
		String genName = "anonymous" + anonymousNameCounter;
		anonymousNameCounter++;
		return genName;
	}

	private class VoidSchema extends Schema<Void> {
		public VoidSchema() {
			super("void", null);
		}
	}

	private class URISchema extends Schema<URI> {
		public URISchema() {
			super("string", "uri");
		}
	}
}

/*
 * Copyright 2020 The MDSL Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mdsl.generator.model.converter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.ParameterForest;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.generator.AnonymousFieldNameGenerator;
import io.mdsl.generator.model.BasicType;
import io.mdsl.generator.model.DataType;
import io.mdsl.generator.model.DataTypeField;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.utils.CardinalityHelper;

/**
 * Converts MDSL data types (AST model) into data types of our generator model.
 *
 */
public class DataTypeConverter {

	private static final String ANONYMOUS_TYPE_NAME = "AnonymousType";
	private AnonymousFieldNameGenerator fieldNameGenerator;
	private MDSLGeneratorModel model;
	
	private String currentDefaultValue = null;

	public DataTypeConverter(MDSLGeneratorModel model) {
		this.model = model;
		this.fieldNameGenerator = new AnonymousFieldNameGenerator();
	}

	/**
	 * Converts an MDSL data contract into a generator model data type.
	 * 
	 * @param dataContract the data contract that shall be converted
	 * @return the generator model data type
	 */
	public DataType convert(DataContract dataContract) {
		currentDefaultValue = null;
		DataType dataType = new DataType(dataContract.getName());
		
		// new in V5.4.4
		String version = dataContract.getSvi();
		if(version!=null ) {
			// is surrounded by double quotes
			dataType.setVersion(version.substring(1, version.length()-1)); 
		}
		if(dataContract.getDefault()!=null ) {
			currentDefaultValue = dataContract.getDefault().getDefault();
			dataType.setDefaultValue(currentDefaultValue); // no quotes
		}
		
		mapElementStructure(dataContract.getStructure(), dataType);
		
		return dataType;
	}

	/**
	 * Maps an element structure to a data type (fills data type object with
	 * corresponding fields etc.)
	 * 
	 * @param elementStructure the input element structure of MDSL
	 * @param dataType         the data type that shall be filled with corresponding
	 *                         fields
	 */
	public void mapElementStructure(ElementStructure elementStructure, DataType dataType) {
		if (elementStructure.getPf() != null) {
			mapParameterForest(elementStructure.getPf(), dataType);
		} else if (elementStructure.getPt() != null) {
			mapParameterTree(elementStructure.getPt(), dataType);
		} else if (elementStructure.getApl() != null) {
			mapAtomicParameterList(elementStructure.getApl(), dataType);
		} else {
			mapSingleParameterNode(elementStructure.getNp(), dataType);
		}
	}

	private void mapParameterForest(ParameterForest pf, DataType dataType) {
		List<ParameterTree> trees = Lists.newLinkedList();
		trees.add(pf.getPtl().getFirst());
		trees.addAll(pf.getPtl().getNext());
		for (ParameterTree tree : trees) {
			String attrName = fieldNameGenerator.getUniqueName(tree.getName());
			String typeName = getUniqueTypeName(attrName);

			DataType nestedType = new DataType(typeName);
			mapParameterTree(tree, nestedType);
			model.addDataType(nestedType);
			DataTypeField field = new DataTypeField(attrName);
			field.setType(nestedType);
			field.isList(CardinalityHelper.isList(tree.getCard()));
			field.isNullable(CardinalityHelper.isOptional(tree.getCard()));
			dataType.addField(field);
		}
	}

	private void mapParameterTree(ParameterTree pt, DataType dataType) {
		List<TreeNode> treeNodes = Lists.newLinkedList();
		treeNodes.add(pt.getFirst());
		treeNodes.addAll(pt.getNexttn());
		for (TreeNode treeNode : treeNodes) {
			mapTreeNode(treeNode, dataType);
		}
	}

	private void mapTreeNode(TreeNode treeNode, DataType dataType) {
		if (treeNode.getPn() != null && treeNode.getPn().getGenP() != null) {
			DataTypeField field = new DataTypeField(
					fieldNameGenerator.getUniqueName(treeNode.getPn().getGenP().getName()));
			field.setType(BasicType.VOID);
			field.isList(false);
			field.isNullable(false); // not sure if this is the correct semantic here
			dataType.addField(field);
		} else if (treeNode.getPn() != null) {
			mapSingleParameterNode(treeNode.getPn(), dataType);
		} else if (treeNode.getApl() != null) {
			AtomicParameterList list = treeNode.getApl();
			String attrName = fieldNameGenerator.getUniqueName(list.getName());
			DataType nestedDataType = new DataType(getUniqueTypeName(attrName));
			mapAtomicParameterList(list, nestedDataType);
			model.addDataType(nestedDataType);
			DataTypeField field = new DataTypeField(attrName);
			field.setType(nestedDataType);
			field.isList(CardinalityHelper.isList(list.getCard()));
			field.isNullable(CardinalityHelper.isOptional(list.getCard()));
			dataType.addField(field);
		} else if (treeNode.getChildren() != null) {
			ParameterTree subTree = treeNode.getChildren();
			String attrName = fieldNameGenerator.getUniqueName(subTree.getName());
			DataType nestedDataType = new DataType(getUniqueTypeName(attrName));
			mapParameterTree(subTree, nestedDataType);
			model.addDataType(nestedDataType);
			DataTypeField field = new DataTypeField(attrName);
			field.setType(nestedDataType);
			field.isList(CardinalityHelper.isList(subTree.getCard()));
			field.isNullable(CardinalityHelper.isOptional(subTree.getCard()));
			dataType.addField(field);
		}
	}

	private void mapAtomicParameterList(AtomicParameterList apl, DataType dataType) {
		// TODO handle defaultValue
		List<AtomicParameter> parameters = new LinkedList<>();
		parameters.add(apl.getFirst());
		parameters.addAll(apl.getNextap());
		for (AtomicParameter ap : parameters) {
			DataTypeField field = new DataTypeField(fieldNameGenerator.getUniqueName(ap.getRat().getName()));
			field.setType(BasicType.byName(ap.getRat().getBtype()));
			field.isList(CardinalityHelper.isList(ap.getCard()));
			field.isNullable(CardinalityHelper.isOptional(ap.getCard()));
			dataType.addField(field);
		}
	}

	private void mapSingleParameterNode(SingleParameterNode spn, DataType dataType) {
		if (spn.getAtomP() != null) {
			DataTypeField field = new DataTypeField(
					fieldNameGenerator.getUniqueName(spn.getAtomP().getRat().getName()));
			field.setType(BasicType.byName(spn.getAtomP().getRat().getBtype()));
			field.isList(CardinalityHelper.isList(spn.getAtomP().getCard()));
			field.isNullable(CardinalityHelper.isOptional(spn.getAtomP().getCard()));
			field.setDefaultValue(currentDefaultValue);
			dataType.addField(field);
		} else if (spn.getTr() != null) {
			DataTypeField field = new DataTypeField(fieldNameGenerator.getUniqueName(spn.getTr().getName()));
			field.setType(convert(spn.getTr().getDcref()));
			field.isList(CardinalityHelper.isList(spn.getTr().getCard()));
			field.isNullable(CardinalityHelper.isOptional(spn.getTr().getCard()));
			dataType.addField(field);
		} else if (spn.getGenP() != null) {
			// nothing to do in this case; we just create empty data type without fields
		}
	}

	private String getUniqueTypeName(String initialName) {
		String name = initialName;
		if (name == null || "".equals(name))
			return getUniqueTypeName(ANONYMOUS_TYPE_NAME);

		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		Set<String> alreadyExistingTypeNames = model.getDataTypes().stream().map(d -> d.getName())
				.collect(Collectors.toSet());
		String baseName = name;
		int counter = 2;
		while (alreadyExistingTypeNames.contains(name)) {
			name = baseName + "_" + counter;
			counter++;
		}
		return name;
	}

}

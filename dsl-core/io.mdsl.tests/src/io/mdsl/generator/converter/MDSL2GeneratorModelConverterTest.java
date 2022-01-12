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
package io.mdsl.generator.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.model.Client;
import io.mdsl.generator.model.DataType;
import io.mdsl.generator.model.DataTypeField;
import io.mdsl.generator.model.EndpointContract;
import io.mdsl.generator.model.JavaBinding;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.Operation;
import io.mdsl.generator.model.OperationParameter;
import io.mdsl.generator.model.Provider;
import io.mdsl.generator.model.ProviderImplementation;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;

public class MDSL2GeneratorModelConverterTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canConvertSingleParameterNodes() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("single-parameter-nodes-model.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals("TestAPI", genModel.getApiName());
		assertEquals(3, genModel.getDataTypes().size());
		DataType type1 = genModel.getDataTypes().stream().filter(d -> d.getName().equals("TestSingleParameterNodeType1")).findFirst().get();
		DataType type2 = genModel.getDataTypes().stream().filter(d -> d.getName().equals("TestSingleParameterNodeType2")).findFirst().get();
		DataType type3 = genModel.getDataTypes().stream().filter(d -> d.getName().equals("TestSingleParameterNodeType3")).findFirst().get();
		assertNotNull(type1);
		assertNotNull(type2);
		assertNotNull(type3);
		assertEquals(0, type1.getFields().size());
		assertEquals(1, type2.getFields().size());
		DataTypeField type2Field = type2.getFields().get(0);
		assertEquals("string", type2Field.getTypeAsString());
		assertEquals("name", type2Field.getName());
		assertEquals(1, type3.getFields().size());
		DataTypeField type3Field = type3.getFields().get(0);
		assertEquals("ref", type3Field.getName());
		assertEquals("TestSingleParameterNodeType1", type3Field.getTypeAsString());
		assertEquals(type1, type3Field.getType());
	}

	@Test
	public void canConvertParameterLists() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("parameter-list-model.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals("TestAPI", genModel.getApiName());
		assertEquals(1, genModel.getDataTypes().size());
		DataType listType = genModel.getDataTypes().stream().filter(d -> d.getName().equals("TestParameterList")).findFirst().get();
		assertNotNull(listType);
		assertEquals(2, listType.getFields().size());
		DataTypeField field1 = listType.getFields().get(0);
		DataTypeField field2 = listType.getFields().get(1);
		assertNotNull(field1);
		assertNotNull(field2);
		assertEquals("attr1", field1.getName());
		assertEquals("string", field1.getTypeAsString());
		assertEquals("attr2", field2.getName());
		assertEquals("int", field2.getTypeAsString());
	}

	@Test
	public void canConvertParameterTrees() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("parameter-tree-model.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals("TestAPI", genModel.getApiName());
		assertEquals(4, genModel.getDataTypes().size());
		DataType refType = genModel.getDataTypes().stream().filter(d -> d.getName().equals("RefType")).findFirst().get();
		DataType treeType = genModel.getDataTypes().stream().filter(d -> d.getName().equals("TestType")).findFirst().get();
		DataType listType = genModel.getDataTypes().stream().filter(d -> d.getName().equals("List")).findFirst().get();
		DataType subTreeType = genModel.getDataTypes().stream().filter(d -> d.getName().equals("SubTree")).findFirst().get();
		assertNotNull(refType);
		assertNotNull(treeType);
		assertNotNull(listType);
		assertNotNull(subTreeType);
		assertEquals(0, refType.getFields().size());
		assertEquals(5, treeType.getFields().size());
		DataTypeField field1 = treeType.getFields().get(0);
		DataTypeField field2 = treeType.getFields().get(1);
		DataTypeField field3 = treeType.getFields().get(2);
		DataTypeField field4 = treeType.getFields().get(3);
		DataTypeField field5 = treeType.getFields().get(4);
		assertNotNull(field1);
		assertNotNull(field2);
		assertNotNull(field3);
		assertNotNull(field4);
		assertNotNull(field5);
		assertEquals("testAttribute1", field1.getName());
		assertEquals("string", field1.getTypeAsString());
		assertEquals("testAttribute2", field2.getName());
		assertEquals("RefType", field2.getTypeAsString());
		assertEquals("testAttribute3", field3.getName());
		assertEquals("void", field3.getTypeAsString());
		assertEquals("list", field4.getName());
		assertEquals("List", field4.getTypeAsString());
		assertEquals("subTree", field5.getName());
		assertEquals("SubTree", field5.getTypeAsString());
		assertEquals(2, listType.getFields().size());
		DataTypeField listTypeField1 = listType.getFields().get(0);
		DataTypeField listTypeField2 = listType.getFields().get(1);
		assertEquals("listAttribute1", listTypeField1.getName());
		assertEquals("string", listTypeField1.getTypeAsString());
		assertEquals("listAttribute2", listTypeField2.getName());
		assertEquals("int", listTypeField2.getTypeAsString());
		assertEquals(1, subTreeType.getFields().size());
		DataTypeField subTreeField1 = subTreeType.getFields().get(0);
		assertEquals("subTreeAttribute", subTreeField1.getName());
		assertEquals("string", subTreeField1.getTypeAsString());
	}

	@Test
	public void canConvertParameterForest() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("parameter-forest-model.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals("TestAPI", genModel.getApiName());
		assertEquals(4, genModel.getDataTypes().size());
		DataType refType = genModel.getDataTypes().stream().filter(d -> d.getName().equals("RefType")).findFirst().get();
		DataType forestType = genModel.getDataTypes().stream().filter(d -> d.getName().equals("TestType")).findFirst().get();
		DataType tree1Type = genModel.getDataTypes().stream().filter(d -> d.getName().equals("Tree1")).findFirst().get();
		DataType tree2Type = genModel.getDataTypes().stream().filter(d -> d.getName().equals("Tree2")).findFirst().get();
		assertNotNull(refType);
		assertNotNull(forestType);
		assertNotNull(tree1Type);
		assertNotNull(tree2Type);
		assertEquals(0, refType.getFields().size());
		assertEquals(2, forestType.getFields().size());
		DataTypeField forestField1 = forestType.getFields().get(0);
		DataTypeField forestField2 = forestType.getFields().get(1);
		assertEquals("tree1", forestField1.getName());
		assertEquals("Tree1", forestField1.getTypeAsString());
		assertEquals("tree2", forestField2.getName());
		assertEquals("Tree2", forestField2.getTypeAsString());
		assertEquals(1, tree1Type.getFields().size());
		DataTypeField tree1Field = tree1Type.getFields().get(0);
		assertEquals("testAttribute1", tree1Field.getName());
		assertEquals("string", tree1Field.getTypeAsString());
		assertEquals(1, tree2Type.getFields().size());
		DataTypeField tree2Field = tree2Type.getFields().get(0);
		assertEquals("testAttribute2", tree2Field.getName());
		assertEquals("string", tree2Field.getTypeAsString());
	}

	@Test
	public void canConvertEndpoint() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("endpoint-model-1.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals("TestAPI", genModel.getApiName());
		assertEquals(1, genModel.getEndpoints().size());
		assertEquals(2, genModel.getDataTypes().size());
		EndpointContract endpoint = genModel.getEndpoints().get(0);
		assertNotNull(endpoint);
		assertEquals("TestEndpoint", endpoint.getName());
		assertEquals(1, endpoint.getOperations().size());
		Operation operation = endpoint.getOperations().get(0);
		assertNotNull(operation);
		assertEquals("TestOperation", operation.getName());
		assertEquals("TestOutput", operation.getResponse().getName());
		assertEquals(1, operation.getParameters().size());
		OperationParameter parameter = operation.getParameters().get(0);
		assertEquals("TestInput", parameter.getType().getName());
	}

	@Test
	public void canConvertProviders() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("provider-and-client-model.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals("TestMDSLSpec", genModel.getApiName());
		assertEquals(1, genModel.getEndpoints().size());
		assertEquals("TestEndpoint", genModel.getEndpoints().get(0).getName());
		assertEquals(1, genModel.getProviders().size());
		assertEquals(1, genModel.getClients().size());

		Provider provider = genModel.getProviders().get(0);
		Client client = genModel.getClients().get(0);
		assertEquals(1, provider.offeredEndpoints().size());
		assertEquals("TestEndpoint", provider.offeredEndpoints().get(0).getName());
		assertEquals(1, client.getConsumedEndpoints().size());
		assertEquals("TestEndpoint", client.getConsumedEndpoints().get(0).getName());
	}

	@Test
	public void canConvertProviderImplementation() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("provider-implementation-model.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals(1, genModel.getProviderImplementations().size());
		ProviderImplementation providerImpl = genModel.getProviderImplementations().get(0);
		assertEquals("TestImpl", providerImpl.getName());
		assertEquals("TestProvider", providerImpl.getProviderName());
		assertEquals("PlainJava", providerImpl.getImplTechnology());
		assertEquals("TestImplClass", providerImpl.getClazz());
	}

	@Test
	public void canAddJavaProtocolBindingIfAvailable() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("java-binding-model-1.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals(1, genModel.getEndpoints().size());
		EndpointContract endpoint = genModel.getEndpoints().get(0);
		assertNotNull(endpoint);
		assertNotNull(endpoint.getProtocolBinding());
		assertEquals("Java", endpoint.getProtocolBinding().getProtocolName());
	}

	@Test
	public void canAddJavaProtocolBindingWithPackageAndOperationMappingIfAvailable() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("java-binding-model-2.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals(1, genModel.getEndpoints().size());
		EndpointContract endpoint = genModel.getEndpoints().get(0);
		assertNotNull(endpoint);
		assertNotNull(endpoint.getProtocolBinding());
		assertEquals("Java", endpoint.getProtocolBinding().getProtocolName());
		assertTrue(endpoint.getProtocolBinding() instanceof JavaBinding);
		JavaBinding javaBinding = (JavaBinding) endpoint.getProtocolBinding();
		assertEquals("io.mdsl.test", javaBinding.getPackage());
		assertEquals("testOperationMethod", javaBinding.getJavaMethodName4Operation("TestOperation"));
	}

	@Override
	protected String testDirectory() {
		return "/test-data/generator-model/";
	}

}

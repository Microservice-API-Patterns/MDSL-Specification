package io.mdsl.generator.protobuf.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.github.microserviceapipatterns.protobufgen.model.Message;
import io.github.microserviceapipatterns.protobufgen.model.MessageField;
import io.github.microserviceapipatterns.protobufgen.model.ProtoSpec;
import io.github.microserviceapipatterns.protobufgen.model.RemoteProcedureCall;
import io.github.microserviceapipatterns.protobufgen.model.Service;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;

public class MDSL2ProtobufConverterTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canConvertEmptyModel() throws IOException {
		// given
		ServiceSpecification mdslModel = getTestSpecification(getTestResource("empty-model.mdsl"));

		// when
		ProtoSpec proto = new MDSL2ProtobufConverter(mdslModel).convert();

		// then
		assertEquals(mdslModel.getName(), proto.getPackage());
		assertEquals(1, proto.getServices().size());
		assertEquals("TestEndpoint", proto.getServices().get(0).getName());
	}

	@Test
	public void canConvertSingleParameterNodes() throws IOException {
		// given
		ServiceSpecification mdslModel = getTestSpecification(getTestResource("single-parameter-nodes-model.mdsl"));

		// when
		ProtoSpec proto = new MDSL2ProtobufConverter(mdslModel).convert();

		// then
		assertEquals(3, proto.getMessages().size());
		Message genericParam = proto.getMessages().stream()
				.filter(m -> m.getName().equals("TestSingleParameterNodeType1")).findAny().get();
		Message atomicParam = proto.getMessages().stream()
				.filter(m -> m.getName().equals("TestSingleParameterNodeType2")).findAny().get();
		Message typeRef = proto.getMessages().stream().filter(m -> m.getName().equals("TestSingleParameterNodeType3"))
				.findAny().get();
		assertNotNull(genericParam);
		assertNotNull(atomicParam);
		assertNotNull(typeRef);

		// atomic param field
		assertEquals(1, atomicParam.getFields().size());
		MessageField field = atomicParam.getFields().iterator().next();
		assertEquals("name", field.getName());
		assertEquals("string", field.getType());

		// param ref
		assertEquals(1, typeRef.getFields().size());
		field = typeRef.getFields().iterator().next();
		assertEquals("ref", field.getName());
		assertEquals("TestSingleParameterNodeType1", field.getType());
	}

	@Test
	public void canConvertAtomicParameterList() throws IOException {
		// given
		ServiceSpecification mdslModel = getTestSpecification(getTestResource("parameter-list-model.mdsl"));

		// when
		ProtoSpec proto = new MDSL2ProtobufConverter(mdslModel).convert();

		// then
		assertEquals(1, proto.getMessages().size());
		Message paramList = proto.getMessages().get(0);
		assertEquals("TestParameterList", paramList.getName());
		assertEquals(2, paramList.getFields().size());
		MessageField attr1 = paramList.getFields().stream().filter(f -> f.getName().equals("attr1")).findAny().get();
		MessageField attr2 = paramList.getFields().stream().filter(f -> f.getName().equals("attr2")).findAny().get();
		assertNotNull(attr1);
		assertNotNull(attr2);
		assertEquals("string", attr1.getType());
		assertEquals("int32", attr2.getType());
	}

	@Test
	public void canConvertParameterTree() throws IOException {
		// given
		ServiceSpecification mdslModel = getTestSpecification(getTestResource("parameter-tree-model.mdsl"));

		// when
		ProtoSpec proto = new MDSL2ProtobufConverter(mdslModel).convert();

		// then
		assertEquals(3, proto.getMessages().size());
		Message refType = proto.getMessages().stream().filter(m -> m.getName().equals("RefType")).findFirst().get();
		Message testTree = proto.getMessages().stream().filter(m -> m.getName().equals("TestType")).findFirst().get();
		Message unspecifiedType = proto.getMessages().stream().filter(m -> m.getName().equals("UnspecifiedType"))
				.findFirst().get();
		assertNotNull(refType);
		assertNotNull(testTree);
		assertNotNull(unspecifiedType);
		assertEquals(0, refType.getFields().size());
		assertEquals(5, testTree.getFields().size());
		assertEquals(2, testTree.getNestedMessages().size());
		MessageField field1 = testTree.getFields().stream().filter(f -> f.getName().endsWith("testAttribute1"))
				.findFirst().get();
		MessageField field2 = testTree.getFields().stream().filter(f -> f.getName().endsWith("testAttribute2"))
				.findFirst().get();
		MessageField field3 = testTree.getFields().stream().filter(f -> f.getName().endsWith("testAttribute3"))
				.findFirst().get();
		MessageField field4 = testTree.getFields().stream().filter(f -> f.getName().endsWith("list")).findFirst().get();
		MessageField field5 = testTree.getFields().stream().filter(f -> f.getName().endsWith("subTree")).findFirst()
				.get();
		assertNotNull(field1);
		assertNotNull(field2);
		assertNotNull(field3);
		assertNotNull(field4);
		assertNotNull(field5);
		assertEquals("string", field1.getType());
		assertEquals("RefType", field2.getType());
		assertEquals("UnspecifiedType", field3.getType());
		assertEquals("TestType.ListMessage", field4.getType());
		assertEquals("TestType.SubTreeMessage", field5.getType());
		Message listMessage = testTree.getNestedMessages().stream().filter(m -> m.getSimpleName().equals("ListMessage"))
				.findFirst().get();
		Message subTreeMessage = testTree.getNestedMessages().stream()
				.filter(m -> m.getSimpleName().equals("SubTreeMessage")).findFirst().get();
		assertNotNull(listMessage);
		assertNotNull(subTreeMessage);
		assertEquals(2, listMessage.getFields().size());
		assertEquals(1, subTreeMessage.getFields().size());
		MessageField listAttribute1 = listMessage.getFields().stream().filter(f -> f.getName().equals("listAttribute1"))
				.findFirst().get();
		MessageField listAttribute2 = listMessage.getFields().stream().filter(f -> f.getName().equals("listAttribute2"))
				.findFirst().get();
		MessageField subTreeAttribute = subTreeMessage.getFields().stream()
				.filter(f -> f.getName().equals("subTreeAttribute")).findFirst().get();
		assertNotNull(listAttribute1);
		assertNotNull(listAttribute2);
		assertNotNull(subTreeAttribute);
		assertEquals("string", listAttribute1.getType());
		assertEquals("int32", listAttribute2.getType());
		assertEquals("string", subTreeAttribute.getType());
	}

	@Test
	public void canConvertParameterForest() throws IOException {
		// given
		ServiceSpecification mdslModel = getTestSpecification(getTestResource("parameter-forest-model.mdsl"));

		// when
		ProtoSpec proto = new MDSL2ProtobufConverter(mdslModel).convert();

		// then
		assertEquals(2, proto.getMessages().size());
		Message refType = proto.getMessages().stream().filter(m -> m.getName().equals("RefType")).findFirst().get();
		Message testForest = proto.getMessages().stream().filter(m -> m.getName().equals("TestType")).findFirst().get();
		assertNotNull(refType);
		assertNotNull(testForest);
		assertEquals(2, testForest.getFields().size());
		assertEquals(2, testForest.getNestedMessages().size());
		MessageField tree1Field = testForest.getFields().stream().filter(f -> f.getName().equals("tree1")).findFirst()
				.get();
		MessageField tree2Field = testForest.getFields().stream().filter(f -> f.getName().equals("tree2")).findFirst()
				.get();
		assertNotNull(tree1Field);
		assertNotNull(tree2Field);
		assertEquals("TestType.Tree1Message", tree1Field.getType());
		assertEquals("TestType.Tree2Message", tree2Field.getType());
		Message tree1 = testForest.getNestedMessages().stream().filter(m -> m.getSimpleName().equals("Tree1Message"))
				.findFirst().get();
		Message tree2 = testForest.getNestedMessages().stream().filter(m -> m.getSimpleName().equals("Tree2Message"))
				.findFirst().get();
		assertNotNull(tree1);
		assertNotNull(tree2);
		assertEquals(1, tree1.getFields().size());
		assertEquals(1, tree2.getFields().size());
	}

	@Test
	public void canConvertRPCWithSingleReferenceParameter() throws IOException {
		// given
		ServiceSpecification mdslModel = getTestSpecification(getTestResource("service-model-1.mdsl"));

		// when
		ProtoSpec proto = new MDSL2ProtobufConverter(mdslModel).convert();

		// then
		assertEquals(1, proto.getServices().size());
		assertEquals(2, proto.getMessages().size());
		Service service = proto.getServices().get(0);
		assertEquals("TestEndpoint", service.getName());
		assertEquals(1, service.getRemoteProcedureCalls().size());
		RemoteProcedureCall rpc = service.getRemoteProcedureCalls().iterator().next();
		assertEquals("TestOperation", rpc.getName());
		assertEquals("TestInput", rpc.getInput().getName());
		assertEquals("TestOutput", rpc.getOutput().getName());
	}

	@Override
	protected String testDirectory() {
		return "/test-data/proto-generation/";
	}

}

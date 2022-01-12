/*
 * Copyright 2021 The MDSL Project Team
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.transformations.EndpointTransformations;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.transformations.MAPDecoratorHelpers;
import io.mdsl.transformations.MessageTransformationHelpers;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.OperationTransformations;
import io.mdsl.transformations.ScenarioTransformations;
import io.mdsl.transformations.TransformationChains;
import io.mdsl.validation.HTTPBindingValidator;

public class MDSL2QuickFixTransformationsTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canLoadStoryModelAndConvertIntoGenModel() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("single-story-model.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel genModel = converter.convert();

		// then
		assertEquals("QuickFixTransformationTestAPI", genModel.getApiName());
	}
	
	// flow tests appear in MDSL2FlowTransformationsTest
	
	@Test
	public void canTransformStoryIntoEndpoint() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("single-story-model.mdsl")).getServiceSpecification();

		// when
		IntegrationScenario scenario1 = apiDescription.getScenarios().get(0);
		IntegrationStory story1 = scenario1.getStories().get(0);
		
		ScenarioTransformations s2et = new ScenarioTransformations();
		s2et.addEndpointForScenario(scenario1, true);

		// then
		assertEquals("Sample_Scenario_1", scenario1.getName());
		assertEquals("Sample_Story_1", story1.getName());
		
		assertEquals(1, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals(scenario1.getName() + "RealizationEndpoint", ec1.getName());
		// check operations: number of them, names, parameters
		assertEquals(5, ec1.getOps().size());
		for(Operation operation : ec1.getOps()) {
			if(operation.getName().equals("doSomething"))
				assertEquals("doSomething", operation.getName());
			if(operation.getName().equals("createSampleBusinessObject"))
				assertEquals("SampleBusinessObjectDTO", operation.getRequestMessage().getPayload().getNp().getTr().getDcref().getName());
			if(operation.getName().equals("readSampleBusinessObject"))
				assertNotNull(operation.getResponsibility().getRo());
		}
	}
	
	@Test
	public void canEnhanceEndpointWithMAPandIRC() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-from-story-model.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		
		// when
		MAPDecoratorHelpers.setRoleToProcessingResource(ec1);
		OperationTransformations ot = new OperationTransformations();
		ot.addOperationsForRole(ec1);
		
		// then
		assertNotNull(ec1.getPrimaryRole());
		assertEquals("PROCESSING_RESOURCE", ec1.getPrimaryRole());
	
		assertEquals(1, apiDescription.getContracts().size());
		assertEquals("Sample_Scenario_1RealizationEndpoint", ec1.getName());
		assertEquals(5, ec1.getOps().size());
		// verify selected operation details: names, request and response message structure
		assertEquals("doSomething", ec1.getOps().get(0).getName());
		assertNotNull(ec1.getOps().get(1).getResponsibility().getSco());
		assertEquals("ID", ec1.getOps().get(1).getResponseMessage().getPayload().getNp().getAtomP().getRat().getRole()); // create op. returns ID
		assertEquals("ID", ec1.getOps().get(2).getRequestMessage().getPayload().getNp().getAtomP().getRat().getRole()); // read op. expects ???
		assertEquals("Sample_Scenario_1RealizationEndpointDTO", ec1.getOps().get(3).getRequestMessage().getPayload().getNp().getTr().getDcref().getName());
		assertEquals("bool", ec1.getOps().get(4).getResponseMessage().getPayload().getNp().getAtomP().getRat().getBtype());
	}
	
	@Test
	public void canDecorateEndpointAsInformationHolderAndCollectionResourceAndUseTheseToGenerateOperations() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("empty-endpoint.mdsl")).getServiceSpecification();
		assertEquals(1, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals(0, ec1.getOps().size());
		assertEquals(0, apiDescription.getTypes().size());
		
		// when
		MAPDecoratorHelpers.setRole(ec1, "COLLECTION_RESOURCE"); // MUTABLE_COLLECTION_RESOURCE
		OperationTransformations ot = new OperationTransformations();
		ot.addOperationsForRole(ec1);
		
		// then
		assertEquals("TestEndpoint", ec1.getName());
		assertNotNull(ec1.getPrimaryRole());
		assertEquals("INFORMATION_HOLDER_RESOURCE", ec1.getPrimaryRole());
		assertNotNull(ec1.getOtherRoles());
		assertEquals("COLLECTION_RESOURCE", ec1.getOtherRoles().get(0));
	
		assertEquals(1, apiDescription.getContracts().size());
		assertEquals(6, ec1.getOps().size());
		// verify selected operation details: names, request and response message structure, cardinalities; external data type
		assertEquals("findAll", ec1.getOps().get(0).getName());
		assertEquals("responseDTO", ec1.getOps().get(1).getResponseMessage().getPayload().getNp().getTr().getName());
		assertEquals("itemId", ec1.getOps().get(2).getResponseMessage().getPayload().getNp().getAtomP().getRat().getName());
		assertEquals("collectionId", ec1.getOps().get(3).getRequestMessage().getPayload().getPt().getFirst().getPn().getAtomP().getRat().getName());
		assertEquals("ID", ec1.getOps().get(4).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getRat().getRole());
		assertEquals("bool", ec1.getOps().get(5).getResponseMessage().getPayload().getNp().getAtomP().getRat().getBtype());

		assertEquals(1, apiDescription.getTypes().size()); // endpoint-level DTO generated
		assertEquals("TestEndpointDTO", apiDescription.getTypes().get(0).getName());
	}
	
	// TODO test with map-all-role-decorators.mdsl
	
	@Test
	public void canGenerateOperationsCommonForMAPRoleDecorators() 	throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoints-with-all-map-role-decorators.mdsl")).getServiceSpecification();
		assertEquals(0, apiDescription.getTypes().size());
		assertEquals(12, apiDescription.getContracts().size());
		for(EObject contract : apiDescription.getContracts()) {
			EndpointContract ept = (EndpointContract) contract;
			assertEquals(0, ept.getOps().size());
		}
	
		// when
		OperationTransformations ot = new OperationTransformations();
		for(EObject contract : apiDescription.getContracts()) {
			EndpointContract ept = (EndpointContract) contract;
			ot.addOperationsForRole(ept);
		}

		// then
		assertEquals(6, apiDescription.getTypes().size());
		EndpointContract ec = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals("Test_Endpoint1", ec.getName());
		assertEquals("PROCESSING_RESOURCE", ec.getPrimaryRole());
		assertEquals(4, ec.getOps().size());
		// TODO check operation role stereotypes 
		ec = (EndpointContract) apiDescription.getContracts().get(1);
		assertEquals("Test_Endpoint2", ec.getName());
		assertEquals("INFORMATION_HOLDER_RESOURCE", ec.getPrimaryRole());
		assertEquals(2, ec.getOps().size());
		// TODO check operation role stereotypes
		ec = (EndpointContract) apiDescription.getContracts().get(2);
		assertEquals("Test_Endpoint3", ec.getName());
		assertEquals(8, ec.getOps().size());
		// TODO check operation role stereotypes
		ec = (EndpointContract) apiDescription.getContracts().get(3);
		assertEquals("Test_Endpoint4", ec.getName());
		assertEquals(6, ec.getOps().size());
		// TODO check operation role stereotypes
		ec = (EndpointContract) apiDescription.getContracts().get(4);
		assertEquals("Test_Endpoint5", ec.getName());
		assertEquals(2, ec.getOps().size());
		// TODO check operation role stereotypes
		ec = (EndpointContract) apiDescription.getContracts().get(5);
		assertEquals("Test_Endpoint6", ec.getName());
		assertEquals(2, ec.getOps().size());
		// TODO check operation role stereotypes 
		ec = (EndpointContract) apiDescription.getContracts().get(6);
		assertEquals("Test_Endpoint7", ec.getName());
		assertEquals(2, ec.getOps().size());
		// TODO check operation role stereotypes 
		ec = (EndpointContract) apiDescription.getContracts().get(7);
		assertEquals("Test_Endpoint8", ec.getName());
		assertEquals(1, ec.getOps().size());
		// TODO check operation role stereotypes 
		ec = (EndpointContract) apiDescription.getContracts().get(8);
		assertEquals("Test_Endpoint9", ec.getName());
		assertEquals(1, ec.getOps().size());
		// TODO check operation role stereotypes 
		ec = (EndpointContract) apiDescription.getContracts().get(9);
		assertEquals("Test_Endpoint10", ec.getName());
		assertEquals(1, ec.getOps().size());
		// TODO check operation role stereotypes 
		ec = (EndpointContract) apiDescription.getContracts().get(10);
		assertEquals("Test_Endpoint11", ec.getName());
		assertEquals(4, ec.getOps().size());
		// TODO check operation role stereotypes 
		ec = (EndpointContract) apiDescription.getContracts().get(11);
		assertEquals("Test_Endpoint12", ec.getName());
		assertEquals(1, ec.getOps().size());
		// TODO check operation role stereotypes 
	}
	
	@Test 
	public void canAddEventManagementOperations() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-receiving-event.mdsl")).getServiceSpecification();
		EndpointContract ec = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals(1, ec.getOps().size());
		
		EventType eventDeclaration = (EventType) apiDescription.getEvents().get(0).getEvents().get(0); 
		Event event = (Event) ec.getEvents().get(0);
		assertEquals(eventDeclaration.getName(), event.getType().getName());
		
		// when
		String type = "EVENT_PROCESSOR";
		EndpointTransformations.createEventProcessorOperation(ec, event, type);
		EndpointTransformations.createRetrievalOperationsForEvent(ec, event, type);
		
		// then 
		assertEquals(3, ec.getOps().size());
		assertEquals("receiveSomethingHappenedEvent", ec.getOps().get(1).getName());
		assertNotNull(ec.getOps().get(1).getResponsibility().getEp());
		assertEquals("EVENT_PROCESSOR", ec.getOps().get(1).getResponsibility().getEp());
		assertEquals("ONE_WAY", ec.getOps().get(1).getMep());
		assertEquals("getSomethingHappenedEvents", ec.getOps().get(2).getName());
		assertNotNull(ec.getOps().get(2).getResponsibility().getRo());
		assertEquals("RETRIEVAL_OPERATION", ec.getOps().get(2).getResponsibility().getRo()); 
		ParameterTree eventResponse = ec.getOps().get(2).getResponseMessage().getPayload().getPt();
		assertEquals("MD", eventResponse.getFirst().getPn().getAtomP().getRat().getRole());
		assertEquals("what", eventResponse.getNexttn().get(0).getPn().getAtomP().getRat().getName());
	}
	
	@Test
	public void canSplitOperation() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("test-split-operation.mdsl")).getServiceSpecification();
		assertEquals(2, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals("SourceEndpoint", ec1.getName());
		assertEquals(1, ec1.getOps().size());
		Operation moveSubject = ec1.getOps().get(0); // "operationToSplit"
		assertEquals("operationToSplit", ec1.getOps().get(0).getName());

		// when
		OperationTransformations mot = new OperationTransformations();
		mot.splitOperation(moveSubject, false); 
		
		// TODO test with compensating/compensated operation, must throw an exception
		
		// then 
		assertEquals(2, apiDescription.getContracts().size());
		assertEquals(4, ec1.getOps().size());
		assertEquals("task1", ec1.getOps().get(0).getName());
		// TODO check other three methods (names, types)
	}
	
	@Test
	public void canMoveOperationInEndpoint() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("two-endpoints-with-operation.mdsl")).getServiceSpecification();
		assertEquals(2, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals("SourceEndpoint", ec1.getName());
		Operation moveSubject = ec1.getOps().get(0);
		assertEquals(1, ec1.getOps().size());
		
		// when
		OperationTransformations mot = new OperationTransformations();
		MDSLResource targetSpec = mot.moveOperation(moveSubject, "TargetEndpoint"); 
		
		// then 
		assertNotNull(targetSpec);
		assertEquals(2, apiDescription.getContracts().size());
		assertEquals(0, ec1.getOps().size());
		EndpointContract ec2 = (EndpointContract) apiDescription.getContracts().get(1);
		assertEquals("TargetEndpoint", ec2.getName());
		assertEquals(1, ec2.getOps().size());
		// could check properties of moved/extracted operation, including number of providers and their bindings
	}
	
	@Test
	public void canMoveOperationInEndpointWithHTTPBinding() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("two-endpoints-with-operation.mdsl")).getServiceSpecification();
		assertEquals(2, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals("SourceEndpoint", ec1.getName());
		Operation moveSubject = ec1.getOps().get(0);
		assertEquals(1, ec1.getOps().size());
		HTTPBindingTransformations httpbt = new HTTPBindingTransformations();
		httpbt.addBinding(ec1);
		Provider p1 = (Provider) apiDescription.getProviders().get(0);
		assertEquals("SourceEndpointProvider", p1.getName());
		EndpointInstance ei1 = p1.getEpl().get(0).getEndpoints().get(0);
		assertEquals(1, ei1.getPb().get(0).getProtBinding().getHttp().getEb().get(0).getOpsB().size());
		
		// when
		OperationTransformations mot = new OperationTransformations();
		EndpointContract ec2 = (EndpointContract) apiDescription.getContracts().get(1);
		assertEquals("TargetEndpoint", ec2.getName());
		httpbt.addBinding(ec2);
		Provider p2 = (Provider) apiDescription.getProviders().get(1);
		assertEquals("TargetEndpointProvider", p2.getName());
		EndpointInstance ei2 = p2.getEpl().get(0).getEndpoints().get(0);
		assertEquals(0, ei2.getPb().get(0).getProtBinding().getHttp().getEb().get(0).getOpsB().size());
		
		MDSLResource targetSpec = mot.moveOperation(moveSubject, "TargetEndpoint"); 
		// TODO also test Extract Endpoint, or Move to Endpoint that has no binding yet
		
		// then 
		assertNotNull(targetSpec);
		assertEquals(2, apiDescription.getContracts().size());
		assertEquals(0, ec1.getOps().size());
		assertEquals(1, ec2.getOps().size());
		// check properties of moved/extracted operation, including number of providers and their bindings
		assertEquals(2, apiDescription.getProviders().size());
		ei1 = p1.getEpl().get(0).getEndpoints().get(0);
		assertEquals(0, ei1.getPb().get(0).getProtBinding().getHttp().getEb().get(0).getOpsB().size());
		assertEquals(1, ei2.getPb().get(0).getProtBinding().getHttp().getEb().get(0).getOpsB().size());
	}
	
	@Test
	public void canExtractEndpoint() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-operations-from-map-decorator.mdsl")).getServiceSpecification();
		assertEquals(1, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		Operation moveSubject = ec1.getOps().get(0);
		assertEquals(5, ec1.getOps().size());
		
		// when
		OperationTransformations mot = new OperationTransformations();
		MDSLResource targetSpec = mot.moveOperation(moveSubject, "ExtractedEndpoint_" + moveSubject.getName());
		
		// then 
		assertNotNull(targetSpec);
		assertEquals(2, apiDescription.getContracts().size());
		assertEquals(4, ec1.getOps().size());
		EndpointContract ec2 = (EndpointContract) apiDescription.getContracts().get(1);
		assertEquals("ExtractedEndpoint_" + moveSubject.getName(), ec2.getName());
		assertEquals(1, ec2.getOps().size());
		// could check properties of moved/extracted operation
	}
	
	//"apply CQRS" is a special case of "Split Endpoint"
	@Test
	public void canApplyCQRS() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-operations-from-map-decorator.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		
		// when
		EndpointTransformations eptt = new EndpointTransformations();
		eptt.separateCommandsFromQueries(ec1);
		
		// then
		assertEquals(2, apiDescription.getContracts().size());
		EndpointContract ec2 = (EndpointContract) apiDescription.getContracts().get(1);
		assertEquals("Sample_Scenario_1RealizationEndpointCommands", ec1.getName());
		assertEquals("Sample_Scenario_1RealizationEndpointQueries", ec2.getName());
		assertEquals(4, ec1.getOps().size());
		assertEquals(1, ec2.getOps().size());
		// verify operation responsibilities: second endpoint only should have only (and all) retrieval operations:
		for(Operation operation : ec1.getOps()) 
			if(operation.getResponsibility()!=null)
				assertNull(operation.getResponsibility().getRo()); // must be sco, sto, sro, sdo 
		for(Operation operation : ec2.getOps())
			assertEquals("RETRIEVAL_OPERATION", operation.getResponsibility().getRo());
	}
	
	@Test
	public void canExtractInformationHolder() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("test-extract-information-holder.mdsl")).getServiceSpecification();
		assertEquals(1, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals("TestEndpointWithEmbeddedEntities", ec1.getName());
		assertEquals(2, ec1.getOps().size());
		assertEquals("testExtractInformationHolderSPN", ec1.getOps().get(0).getName());
		assertEquals("testExtractInformationHolderPT", ec1.getOps().get(1).getName());
		assertEquals(1, apiDescription.getTypes().size());
		assertNotNull(ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getTr()); // has type reference
		assertEquals("Embedded_Entity", ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getTr().getClassifier().getPattern());
		assertNotNull(ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getChildren()); // has PT
		assertEquals("Embedded_Entity", ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getChildren().getClassifier().getPattern());
		
		// when
		// TODO also test helper methods in isolation (stereotype finders etc.)
		MessageTransformations.extractInformationHolder(ec1.getOps().get(0), true); // extract from request
		MessageTransformations.extractInformationHolder(ec1.getOps().get(1), false); // extract from response
		
		// then 
		assertEquals(3, apiDescription.getTypes().size());
		assertEquals(3, apiDescription.getContracts().size());
		// TODO validate number of operations and name of new endpoints
		assertEquals(2, ec1.getOps().size());
		assertEquals("Linked_Information_Holder", ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getClassifier().getPattern());
		assertEquals("L", ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getRat().getRole());
		assertEquals("Linked_Information_Holder", ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getClassifier().getPattern());
		assertEquals("L", ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getRat().getRole());
	}
	
	@Test
	public void canInlineInformationHolder() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("test-inline-information-holder.mdsl")).getServiceSpecification();
		assertEquals(3, apiDescription.getContracts().size());
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals("TestEndpointWithLinkedInformationHolders", ec1.getName());
		assertEquals(2, ec1.getOps().size());
		assertEquals("testInlineInformationHolderSPN", ec1.getOps().get(0).getName());
		assertEquals("testInlineInformationHolderPT", ec1.getOps().get(1).getName());
		assertEquals("Linked_Information_Holder", ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getClassifier().getPattern());
		assertEquals("L", ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getRat().getRole());
		assertEquals("Linked_Information_Holder", ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getClassifier().getPattern());
		assertEquals("L", ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getPn().getAtomP().getRat().getRole());

		// when
		// TODO also test helper methods in isolation (stereotype finders etc.)
		MessageTransformations.inlineInformationHolder(ec1.getOps().get(0), true); // extract from request
		MessageTransformations.inlineInformationHolder(ec1.getOps().get(1), false); // extract from response
		
		// then 
		assertEquals(2, ec1.getOps().size());
		assertEquals(3, apiDescription.getTypes().size());
		assertNotNull(ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getTr()); // has type reference
		assertEquals("Embedded_Entity", ec1.getOps().get(0).getRequestMessage().getPayload().getPt().getNexttn().get(0).getPn().getTr().getClassifier().getPattern());
		assertNotNull(ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getPn().getTr()); // // has type reference 
		assertEquals("Embedded_Entity", ec1.getOps().get(1).getResponseMessage().getPayload().getPt().getNexttn().get(0).getPn().getTr().getClassifier().getPattern());
	}
	
	@Test
	public void canCompleteOperationSpecificationWithCompensationReportsPolicy() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-operations-from-map-decorator.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		OperationTransformations ot = new OperationTransformations();
		Operation operation = ec1.getOps().get(0); // first operation is "initializeResource"

		// when
		ot.completeOperationWithCompensation(operation, "deleteResourceState");
		ot.completeOperationWithErrorReport(operation);
		ot.completeOperationWithSecurityPolicy(operation);
		
		// then 
		assertEquals("deleteResourceState", operation.getUndo().getName());
		assertNotNull(operation.getReports()); // TODO could validate more
		assertNotNull(operation.getPolicies()); // TODO could validate more
	}
	
	@Test
	public void canCompleteIncompleteDataTypes() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-external-data-types.mdsl")).getServiceSpecification();
		DataContract placeholderType = (DataContract) apiDescription.getTypes().get(0);
		DataContract idOnlyType = (DataContract) apiDescription.getTypes().get(1);
		DataContract idAndPlaceholderType = (DataContract) apiDescription.getTypes().get(2);
		DataContract dataRoleType = (DataContract) apiDescription.getTypes().get(3);
		// TODO test PT wrapping here (on type 5 in input)
		
		// when
		DataTypeTransformations.convertToStringType(placeholderType.getStructure().getNp().getGenP());		
		DataTypeTransformations.convertToStringType(idOnlyType.getStructure().getNp().getGenP());
		DataTypeTransformations.convertToStringType(idAndPlaceholderType.getStructure().getNp().getGenP());
		DataTypeTransformations.completeDataType(dataRoleType.getStructure().getNp().getAtomP().getRat(), "int");
		
		// then 
		assertEquals("anonymous", placeholderType.getStructure().getNp().getAtomP().getRat().getName());
		assertEquals("D", placeholderType.getStructure().getNp().getAtomP().getRat().getRole());
		assertEquals("string", placeholderType.getStructure().getNp().getAtomP().getRat().getBtype());
		
		assertEquals("idOnly", idOnlyType.getStructure().getNp().getAtomP().getRat().getName());
		assertEquals("D", idOnlyType.getStructure().getNp().getAtomP().getRat().getRole());
		assertEquals("string", idOnlyType.getStructure().getNp().getAtomP().getRat().getBtype());
		
		assertEquals("id", idAndPlaceholderType.getStructure().getNp().getAtomP().getRat().getName());
		assertEquals("D", idAndPlaceholderType.getStructure().getNp().getAtomP().getRat().getRole());
		assertEquals("string", idAndPlaceholderType.getStructure().getNp().getAtomP().getRat().getBtype());
		
		assertNull(dataRoleType.getStructure().getNp().getAtomP().getRat().getName());
		assertEquals("D", dataRoleType.getStructure().getNp().getAtomP().getRat().getRole());
		assertEquals("int", dataRoleType.getStructure().getNp().getAtomP().getRat().getBtype());
	}
	
	@Test
	public void canExtractDataTypeDefinition() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-external-data-types.mdsl")).getServiceSpecification();
		Operation op = ((EndpointContract)apiDescription.getContracts().get(0)).getOps().get(3);
		
		// when
		DataTypeTransformations.convertInlineTypeToTypeReference(op.getRequestMessage(), "ExtractedType");
		
		// then
		DataContract newType = apiDescription.getTypes().get(7);
		assertEquals("ExtractedType", newType.getName());
		// could check that previously inlined element structure made it to new type
		assertEquals("ExtractedType", op.getRequestMessage().getPayload().getNp().getTr().getDcref().getName());
		assertEquals("ExtractedType", op.getRequestMessage().getPayload().getNp().getTr().getName());
	}
	
	@Test
	public void canWrapDataTypeInKeyValueMap() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("cqrs-endpoints-with-operations.mdsl")).getServiceSpecification();
		ElementStructure es1 = apiDescription.getTypes().get(0).getStructure();
		AtomicParameter ap1 = es1.getNp().getAtomP();
		
		// when
		MessageTransformations.addKeyValueMapWrapper(ap1);
		
		// then 
		assertNotNull(es1.getPt(), "Expected DTO to be a Parameter Tree now.");
		assertEquals("mapOfSampleBusinessObject", es1.getPt().getName());
		assertEquals("key", es1.getPt().getFirst().getPn().getAtomP().getRat().getName());
		assertEquals("sampleBusinessObject", es1.getPt().getNexttn().get(0).getPn().getAtomP().getRat().getName());
	}
	
	@Test
	public void canTransformDataTypeAndAddPagination() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("cqrs-endpoints-with-operations.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		Operation op1 = ec1.getOps().get(0); // working with first operation "doSomething" in this test
		AtomicParameter ap2 = op1.getResponseMessage().getPayload().getNp().getAtomP();
		
		// when
		MessageTransformations.addParameterTreeWrapper(ap2);
		MessageTransformations.addPagination(op1.getResponseMessage().getPayload(), "cursorFromOperation");
		
		// then
		assertNotNull(op1.getResponseMessage().getPayload().getPt(), "Expected response of operation to be a Parameter Tree now.");
		assertEquals("doSomethingResponseBody", op1.getResponseMessage().getPayload().getPt().getFirst().getPn().getAtomP().getRat().getName());
		// TODO also check metadata in request
		ParameterTree paginatedResponse = op1.getResponseMessage().getPayload().getPt();
		assertEquals("Pagination", paginatedResponse.getClassifier().getPattern());
		EList<TreeNode> paginationMetadata = paginatedResponse.getNexttn();
		assertEquals("pageSize", paginationMetadata.get(0).getPn().getAtomP().getRat().getName());
		assertEquals("self", paginationMetadata.get(1).getPn().getAtomP().getRat().getName());
		assertEquals("nextCursor", paginationMetadata.get(2).getPn().getAtomP().getRat().getName());
	}
	
	@Test
	public void canAddWishList() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("cqrs-endpoints-with-operations.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		Operation op1 = ec1.getOps().get(0); // working with first operation "doSomething" in this test
		ElementStructure requestMessage = op1.getRequestMessage().getPayload();
		assertNotNull(requestMessage.getPt());
				
		// when
		MessageTransformations.addWishList(op1);
		
		// then		
		ParameterTree requestWithWishList = op1.getRequestMessage().getPayload().getPt();
		AtomicParameter wish = requestWithWishList.getNexttn().get(0).getPn().getAtomP();
		assertEquals("Wish_List", wish.getClassifier().getPattern());
		assertEquals("desiredElements", wish.getRat().getName());
		assertEquals("MD", wish.getRat().getRole());
		assertEquals("string", wish.getRat().getBtype());
	}
	
	@Test
	public void canUpgradeWishListIntoWishTemplate() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("cqrs-endpoints-with-operations.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		Operation op1 = ec1.getOps().get(0); // working with first operation "doSomething" in this test
		ElementStructure requestMessage = op1.getRequestMessage().getPayload();
		assertNotNull(requestMessage.getPt());
		MessageTransformations.addWishList(op1);
		MessageTransformations.addParameterTreeWrapper(op1.getResponseMessage().getPayload().getNp().getAtomP());
		
		// when
		MessageTransformations.addWishTemplate(requestMessage, op1.getResponseMessage().getPayload());
		// TODO test on different operation that does not contain a Wish List  
		
		// then		
		assertNotNull(requestMessage.getPt());
		ParameterTree requestWithWishList = op1.getRequestMessage().getPayload().getPt();
		ParameterTree wish = requestWithWishList.getNexttn().get(0).getChildren();
		assertEquals("Wish_Template", wish.getClassifier().getPattern());
		// TODO check that template structure matches response structure
		assertNotNull(op1.getResponseMessage().getPayload().getPt());
	}
	
	@Test
	public void canMakeRequestConditional() throws IOException {
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-external-data-types.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		Operation op = ec1.getOps().get(4); // working with first operation "doSomething" in this test
		ElementStructure requestMessage = op.getRequestMessage().getPayload();
		assertNotNull(requestMessage.getPt());
				
		// when
		MessageTransformations.makeRequestConditional(op, "fingerprint");
		
		// then		 
		assertEquals("doSomethingInContext", op.getName());
		ParameterTree pt = op.getRequestMessage().getPayload().getPt();
		assertEquals("Request_Condition", pt.getNexttn().get(1).getPn().getAtomP().getClassifier().getPattern());
		assertEquals("fingerprint", pt.getNexttn().get(1).getPn().getAtomP().getRat().getName());
		assertEquals("MD", pt.getNexttn().get(1).getPn().getAtomP().getRat().getRole());
		assertEquals("string", pt.getNexttn().get(1).getPn().getAtomP().getRat().getBtype());
	}
	
	@Test
	public void canAddContextRepresentation() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-external-data-types.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		Operation op = ec1.getOps().get(4); 
		ElementStructure requestMessage = op.getRequestMessage().getPayload();
		assertNotNull(requestMessage.getPt());
		DataContract contextDTO = apiDescription.getTypes().get(6);
				
		// when
		MessageTransformations.addContextRepresentation(requestMessage, contextDTO);
		
		// then		
		ParameterTree pt = op.getRequestMessage().getPayload().getPt();
		// TODO when CR moves to start: assertEquals("SampleContextRepresentation", pt.getFirst().getPn().getTr().getDcref().getName());
		int indexOfContextTree = pt.getNexttn().size();
		assertEquals("SampleContextRepresentation", pt.getNexttn().get(indexOfContextTree-1).getPn().getTr().getDcref().getName());
		assertEquals("Context_Representation", pt.getNexttn().get(indexOfContextTree-1).getPn().getTr().getClassifier().getPattern());
	}

	@Test
	public void canBundleRequests() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("cqrs-endpoints-with-operations.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		Operation op1 = ec1.getOps().get(0); // working with first operation "doSomething" in this test
		ElementStructure requestMessage = op1.getRequestMessage().getPayload();
		assertNotNull(requestMessage.getPt());
		String requestTypeName = requestMessage.getPt().getName();
		if(requestTypeName==null||requestTypeName.equals(""))
			requestTypeName = "anonymous";
				
		// when
		MessageTransformations.addRequestBundle(requestMessage, true);
		
		// then		
		ParameterTree bundledRequests = op1.getRequestMessage().getPayload().getPt();
		validateThatElementIsABundle(bundledRequests);
		// assertEquals(requestTypeName+"Wrapper", bundledRequests.getName());
		assertEquals(null, bundledRequests.getName()); // changed in v544
		// could also check that inner type survived
		// note: there might be room for improvement in implementation: do not add wrapper if inner type is PT already?
	}

	private void validateThatElementIsABundle(ParameterTree bundledRequests) {
		assertNotNull(bundledRequests);
		assertNotNull(bundledRequests.getClassifier());
		assertNotNull(bundledRequests.getClassifier().getPattern());
		assertEquals("Request_Bundle", bundledRequests.getClassifier().getPattern());
		assertNotNull(bundledRequests.getCard().getAtLeastOne(), "Request Bundle should be a parameter tree with a '+' cardinality");
	}
	
	@Test
	public void canAddHTTPBindingForEndpoint() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("cqrs-endpoints-with-operations.mdsl")).getServiceSpecification();
		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		
		// when
		for(EObject ept : apiDescription.getContracts()) {
			hbts.addBinding((EndpointContract) ept);
		}
		
		// then
		assertEquals(2, apiDescription.getProviders().size());
		for(int i=0; i<apiDescription.getProviders().size();i++) {
			Provider typedProvider = (Provider) apiDescription.getProviders().get(i);
			EndpointContract boundContract = typedProvider.getEpl().get(0).getContract();
			assertEquals(((EndpointContract) (apiDescription.getContracts()).get(i)).getName(), boundContract.getName());
			EList<Operation> ops = ((EndpointContract) (apiDescription.getContracts()).get(i)).getOps();
			assertEquals(ops.size(), boundContract.getOps().size());
			// check that verbs are bound according to heuristics for POST, PUT, GET, etc. (better setup: check op names explicitly)
			for(int j=0;j<ops.size();j++) {
				Operation nextOp = ops.get(j);
				HTTPBinding httpBinding = typedProvider.getEpl().get(0).getEndpoints().get(0).getPb().get(0).getProtBinding().getHttp();
				assertNotNull(httpBinding);
				EList<HTTPOperationBinding> opsBinding = httpBinding.getEb().get(0).getOpsB();
				HTTPOperationBinding nextOpBinding = opsBinding.get(j);
				assertEquals(nextOp.getName(), nextOpBinding.getBoundOperation());
			    if(nextOp.getResponsibility()!=null&&nextOp.getResponsibility().getSco()!=null)
					assertEquals("PUT", nextOpBinding.getMethod().getName());
				if(nextOp.getResponsibility()!=null&&nextOp.getResponsibility().getRo()!=null)
					assertEquals("GET", nextOpBinding.getMethod().getName()); 
				if(nextOp.getResponsibility()!=null&&nextOp.getResponsibility().getSto()!=null)
					assertEquals("PATCH", nextOpBinding.getMethod().getName(), "Binding of " + nextOp.getName() + " does not meet expectation."); 
				if(nextOp.getResponsibility()!=null&&nextOp.getResponsibility().getSro()!=null)
					assertEquals("PATCH", nextOpBinding.getMethod().getName()); 
				if(nextOp.getResponsibility()!=null&&nextOp.getResponsibility().getCf()!=null)
					assertEquals("POST", nextOpBinding.getMethod().getName()); 
				if(nextOp.getResponsibility()!=null&&nextOp.getResponsibility().getSdo()!=null)
					assertEquals("DELETE", nextOpBinding.getMethod().getName());
				if(nextOp.getName().equals("doSomething")&&nextOp.getResponsibility().getOther().equals("PATCH")) 
					assertEquals("PATCH", nextOpBinding.getMethod().getName());
			}
			// could add more checks (element/parameter level)
		}
	}
	
	@Test
	public void canFixHTTPBindingForOASGeneration() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-bindings.mdsl")).getServiceSpecification();
		Provider provider1 = (Provider) apiDescription.getProviders().get(0);
		EndpointInstance epi1 = provider1.getEpl().get(0).getEndpoints().get(0);
		EList<HTTPResourceBinding> resources = epi1.getPb().get(0).getProtBinding().getHttp().getEb();		
		assertEquals(1, resources.size());
		assertEquals(5, resources.get(0).getOpsB().size());
		HTTPOperationBinding hob = resources.get(0).getOpsB().get(2);
		assertEquals("getResourceState", hob.getBoundOperation());
		HTTPOperationBinding hob2 = resources.get(0).getOpsB().get(4);
		assertEquals("deleteResourceState", hob2.getBoundOperation());
		
		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		
		// when
		hbts.addHttpResourceDuringBindingSplit(resources.get(0));
		// hbts.addURITemplateToExistingHttpResource(hob, HTTPBindingValidator.URI_TEMPLATE_MISSING_TEXT + ": {id1}", "{na}");
		hbts.addURITemplateToExistingHttpResource(hob, HTTPBindingValidator.URI_TEMPLATE_MISSING_TEXT + ": {id1}");
		// hbts.addHttpResourceForURITemplate(hob2, "Message with a uri template {idx} that does not match validation error text", "{id2}");
		hbts.addHttpResourceForURITemplate(hob2, "{id2}"); 

		// then
		assertEquals(3, resources.size());
		assertEquals(3, resources.get(0).getOpsB().size());
		// TODO check that resource nn now has {id} in location
		assertTrue(resources.get(0).getUri().contains("{id1}")); // URI template added
		assertEquals(1, resources.get(1).getOpsB().size());
		assertTrue(!resources.get(1).getUri().contains("{")); // No URI template {...}
		assertEquals(1, resources.get(2).getOpsB().size());
		assertTrue(resources.get(2).getUri().contains("{id2}")); // URI template added

		// TODO check that all three resource bindings have unique verbs (count POSTs etc.)  
	}
	
	// TODO (M) test more data type QFs, e.g. APL -> PT, support PF -> PT QF

	@Test
	public void canApplyTransformationChain1() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("single-story-model.mdsl")).getServiceSpecification();

		// when
		IntegrationScenario scenario1 = apiDescription.getScenarios().get(0);
		TransformationChains tc = new TransformationChains();
		tc.fromStoryToEndpointTypeWithHTTPBinding(apiDescription, scenario1.getName());

		// then
		assertEquals("Sample_Scenario_1", scenario1.getName());
		assertEquals(2, scenario1.getStories().size()); // two in input (one unnamed)
		assertEquals(1, apiDescription.getContracts().size());
		assertTrue(apiDescription.getContracts().get(0) instanceof EndpointContract);
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		assertEquals("Sample_Scenario_1RealizationEndpoint", ec1.getName());
		assertEquals(9, ec1.getOps().size());
		assertEquals("doSomething", ec1.getOps().get(0).getName());		
		assertEquals("createSampleBusinessObject", ec1.getOps().get(1).getName());	
		assertEquals("readSampleBusinessObject", ec1.getOps().get(2).getName());
		assertEquals("updateSampleBusinessObject", ec1.getOps().get(3).getName());			
		assertEquals("deleteSampleBusinessObject", ec1.getOps().get(4).getName());		
		assertEquals("initializeResource", ec1.getOps().get(5).getName());
		assertEquals("getResourceState", ec1.getOps().get(6).getName());
		assertEquals("updateResourceState", ec1.getOps().get(7).getName());
		assertEquals("deleteResourceState", ec1.getOps().get(8).getName());
		
		assertEquals(1, apiDescription.getProviders().size());
		Provider ep1 = (Provider) apiDescription.getProviders().get(0);
		assertEquals("Sample_Scenario_1RealizationEndpointProvider", ep1.getName());
		// TODO further HTTP binding checks (see other test) 
	}
	
	@Test
	public void canApplyTransformationChain2() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-operations-from-map-decorator.mdsl")).getServiceSpecification();
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		TransformationChains tc = new TransformationChains();
		assertEquals(2, apiDescription.getTypes().size());
		
		// when
		tc.addQosManagement(apiDescription, ec1.getName()); // touches all operations in endpoint (if possible)
		
		// then
		assertEquals(1, apiDescription.getContracts().size()); // CQRS applied, so 2
		assertEquals("Sample_Scenario_1RealizationEndpoint", ec1.getName());
		assertEquals(5, ec1.getOps().size()); 
		Operation dsop = ec1.getOps().get(0);
		assertEquals("doSomething", dsop.getName());

		assertNotNull(dsop.getReports()); // could validate more
		assertNotNull(dsop.getPolicies()); // could validate more
		
		ParameterTree pt = dsop.getRequestMessage().getPayload().getPt();
		int indexOfContextTree = pt.getNexttn().size();
		assertEquals("SampleContextDTO", pt.getNexttn().get(indexOfContextTree-1).getPn().getTr().getDcref().getName());
		assertEquals("Context_Representation", pt.getNexttn().get(indexOfContextTree-1).getPn().getTr().getClassifier().getPattern());
		
		assertEquals(3, apiDescription.getTypes().size());
		assertEquals("SampleContextDTO", apiDescription.getTypes().get(2).getName());
		assertNotNull(apiDescription.getTypes().get(2).getStructure().getPt());
		assertEquals("sampleContext", apiDescription.getTypes().get(2).getStructure().getPt().getName());
	}

	@Test
	public void canApplyTransformationChain3() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-operations-from-map-decorator.mdsl")).getServiceSpecification();

		// when
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		TransformationChains tc = new TransformationChains();
		tc.improveQueryPerformanceWithPaginationAndWishList(apiDescription, ec1.getName());

		// then
		assertEquals(1, apiDescription.getContracts().size()); // CQRS applied, so 2
		assertEquals("Sample_Scenario_1RealizationEndpoint", ec1.getName());
		assertEquals(5, ec1.getOps().size()); 
		Operation readOp = ec1.getOps().get(4);
		assertEquals("getResourceState", readOp.getName());
		// TODO also check Pagination metadata in request
		ParameterTree paginatedResponse = readOp.getResponseMessage().getPayload().getPt();
		assertEquals("Pagination", paginatedResponse.getClassifier().getPattern());
		EList<TreeNode> paginationMetadata = paginatedResponse.getNexttn();
		assertEquals("offset-out", paginationMetadata.get(0).getPn().getAtomP().getRat().getName());
		assertEquals("limit-out", paginationMetadata.get(1).getPn().getAtomP().getRat().getName());
		assertEquals("size", paginationMetadata.get(2).getPn().getAtomP().getRat().getName());
		// TODO check Wish List content further, see other tests
		assertTrue(readOp.getRequestMessage().getPayload().getPt()!=null);
		assertTrue(MessageTransformationHelpers.findWishList(readOp.getRequestMessage().getPayload().getPt())!=null);
	}
	
	@Test
	public void canApplyTransformationChain4() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("endpoint-with-operations-from-map-decorator.mdsl")).getServiceSpecification();

		// when
		EndpointContract ec1 = (EndpointContract) apiDescription.getContracts().get(0);
		TransformationChains tc = new TransformationChains();
		tc.improveCommandPerformanceWithRequestBundleAndCQRS(apiDescription, ec1.getName());

		// then
		assertEquals(2, apiDescription.getContracts().size()); // CQRS applied, so 2
		assertEquals("Sample_Scenario_1RealizationEndpointCommands", ec1.getName());
		assertEquals(4, ec1.getOps().size()); 
		// assert request bundle is there via helper (extracted from other test)
		validateThatElementIsABundle(ec1.getOps().get(0).getRequestMessage().getPayload().getPt());
		
		EndpointContract ec2 = (EndpointContract) apiDescription.getContracts().get(1);
		assertEquals("Sample_Scenario_1RealizationEndpointQueries", ec2.getName());
		assertEquals(1, ec2.getOps().size());
		Operation readOp = ec2.getOps().get(0);
		assertEquals("getResourceState", readOp.getName());
	}
	
	/*
	@Test
	public void canApplyTransformationChain5() throws IOException {
		// TODO NYI
	}
	*/

	@Override
	protected String testDirectory() {
		return "/test-data/quickfix-transformations/";
	}
}

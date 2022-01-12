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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.eclipse.emf.common.util.EList;
import org.junit.jupiter.api.Test;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;

import io.mdsl.transformations.FlowTransformations;

public class MDSL2FlowTransformationsTest extends AbstractMDSLInputIntegrationTest {

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
	
	@Test 
	public void canTransformStoryIntoFlow() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("single-story-model.mdsl")).getServiceSpecification();
		IntegrationScenario scenario = apiDescription.getScenarios().get(0);
		
		// when
		FlowTransformations ft = new FlowTransformations();
		ft.addApplicationFlowForScenario(scenario);	
		
		// then
		Orchestration of = apiDescription.getOrchestrations().get(0);
		assertEquals("Sample_Scenario_1Flow", of.getName());
		assertEquals("Sample_Scenario_1", of.getScenario().getName());
		assertEquals(2, apiDescription.getEvents().size());
		assertEquals(2, apiDescription.getCommands().size());
		assertEquals(2, of.getSteps().size());
		// TODO validate more flow content
	}

	@Test 
	public void canAddStepsToSimpleFlow() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("flow-from-story-model.mdsl")).getServiceSpecification();
		Orchestration flow = apiDescription.getOrchestrations().get(0);
		assertEquals(3, flow.getSteps().size());
		FlowTransformations ft = new FlowTransformations();
		
		// when
		ft.addDepStep(flow.getSteps().get(0).getCisStep());
		ft.addCisStep(flow.getSteps().get(1).getDepStep());
		// third step already there (from story) but not used here
		
		// then
		assertEquals(5, flow.getSteps().size());
		// TODO validate details of added flow steps
	}
	
	@Test 
	public void canAddStepsToComplexFlow() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("flow-with-branches.mdsl")).getServiceSpecification();
		// TODO also test n event variation
		Orchestration flow = apiDescription.getOrchestrations().get(0);
		assertEquals(3, flow.getSteps().size());
		FlowTransformations ft = new FlowTransformations();
		
		// when
		ft.addDepStep(flow.getSteps().get(0).getCisStep());
		assertEquals(5, flow.getSteps().size());
		ft.addCisStep(flow.getSteps().get(1).getDepStep());
		// third step already there (from story) but not used here
		
		// then
		assertEquals(7, flow.getSteps().size());
		// TODO validate details of added flow steps
	}
	
	@Test 
	public void canAddBranchingChoiceMergeSteps() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("flow-from-story-model.mdsl")).getServiceSpecification();
		Orchestration flow = apiDescription.getOrchestrations().get(0);
		assertEquals(3, flow.getSteps().size());
		// third step already there (from story) but not used here
		FlowTransformations ft = new FlowTransformations();
		
		// when
		ft.addBranchesWithMerge(flow.getSteps().get(0).getCisStep(), "OR");
		assertEquals(5, flow.getSteps().size());
		ft.addBranchesWithMerge(flow.getSteps().get(1).getDepStep(), "AND");
		
		// then
		assertEquals(9, flow.getSteps().size());
		assertNotNull(flow.getSteps().get(3).getDepStep().getEventProduction().getEaep());
		assertEquals(2, flow.getSteps().get(3).getDepStep().getEventProduction().getEaep().getEvents().size());
		assertEquals(2, flow.getSteps().get(8).getCisStep().getEvents().size());
		// TODO validate more flow content once correct position is used
	}
	
	@Test 
	public void canConsolidateSimpleDomainEventSteps() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("flow-with-dep-step-variations.mdsl")).getServiceSpecification();
		Orchestration flow = apiDescription.getOrchestrations().get(0);
		assertEquals(5, flow.getSteps().size());
		FlowTransformations ft = new FlowTransformations();
		
		// when
		ft.consolidateFlowSteps(flow.getSteps().get(0).getDepStep(), "OR");
		
		// then
		assertEquals(4, flow.getSteps().size());
		assertEquals(2, flow.getSteps().get(0).getDepStep().getEventProduction().getIaep().getEvents().size());
	}
	
	@Test 
	public void canSplitCompositeFlowStep() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("flowtest5a-syntaxvariations-canonicalgenmodel.mdsl")).getServiceSpecification();
		Orchestration flow = apiDescription.getOrchestrations().get(2);
		assertEquals("SampleFlowWithSyntaxVariations3", flow.getName());
		
		assertEquals(1, flow.getSteps().size());
		assertNotNull(flow.getSteps().get(0).getEceStep(), "Step 1 should be an ECE step");
		
		// when
		FlowTransformations ft = new FlowTransformations();
		ft.splitCombinedFlowStep(flow.getSteps().get(0).getEceStep());
		
		// then
		assertEquals(2, flow.getSteps().size());
		assertNotNull(flow.getSteps().get(0).getCisStep(), "Step 1 should be a CIS step now");
		assertNotNull(flow.getSteps().get(1).getDepStep(), "Step 2 should be a DEP step now");
	}
	
	// ** endpoint creation
	
	@Test 
	public void canTransformSimpleFlowIntoEndpoint() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("flow-from-story-model.mdsl")).getServiceSpecification();
		assertEquals(0, apiDescription.getContracts().size());
		Orchestration flow = apiDescription.getOrchestrations().get(0);
		FlowTransformations ft = new FlowTransformations();

		// when
		ft.addEndpointTypeSupportingFlow(flow);
		// included: ft.addOperationForFlowStep(flow.getSteps().get(0).getCisStep().getAction(), "STATE_TRANSITION_OPERATION");
		
		// then
		assertEquals(1, apiDescription.getContracts().size());
		// TODO validate endpoint name and flow reference and MAP decorator
		// TODO validate received event, flow initiation event
		// TODO validate operation details
		EList<Operation> operations = ((EndpointContract)apiDescription.getContracts().get(0)).getOps();
		assertEquals(3, operations.size());
		assertEquals(flow.getSteps().get(0).getCisStep().getAction().getCi().getSci().getCommands().get(0).getName(), operations.get(0).getName());
		assertNotNull(operations.get(0).getResponsibility().getSto());
		assertNotNull(operations.get(0).getSt()); // TODO validate to and from parts
		// TODO validate more endpoint type content (operation details such as event emission)
	}
	
	@Test 
	public void canTransformAdvancedFlowIntoEndpoint() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("flow-from-story-model.mdsl")).getServiceSpecification();
		assertEquals(0, apiDescription.getContracts().size());
		Orchestration flow = apiDescription.getOrchestrations().get(0);
		FlowTransformations ft = new FlowTransformations();
		ft.addBranchesWithMerge(flow.getSteps().get(0).getCisStep(), "OR");
		ft.addBranchesWithMerge(flow.getSteps().get(1).getDepStep(), "AND");
		assertEquals(9, flow.getSteps().size());

		// when
		ft.addEndpointTypeSupportingFlow(flow);
		
		// then
		assertEquals(1, apiDescription.getContracts().size());
		EndpointContract ec = (EndpointContract)apiDescription.getContracts().get(0);
		EList<Operation> operations = (ec.getOps());
		assertEquals(11, operations.size()); // 11 and not 10 because of "event doSomethingSampleDataChoice1 x doSomethingSampleDataChoice2"
		assertEquals(8, ec.getEvents().size()); //  only CIS steps lead to event reception; has one duplicate that should be removed "CRUDSampleBusinessObjectTrigger"
		assertEquals("PROCESSING_RESOURCE", ec.getPrimaryRole());
		assertEquals(flow.getName(), ec.getFlow().getName());
		// TODO validate more endpoint type and operation content
	}
	
	@Test 
	public void canTransformFlowWithCombinedStepIntoEndpoint() throws IOException {
		// given
		ServiceSpecification apiDescription = new MDSLResource(getTestResource("test-combinedsteps-and-alloperators.mdsl")).getServiceSpecification();
		assertEquals(0, apiDescription.getContracts().size());
		Orchestration flow = apiDescription.getOrchestrations().get(0); // first flow in file has a combined step
		FlowTransformations ft = new FlowTransformations();
		assertEquals(4, flow.getSteps().size());
		assertNotNull(flow.getSteps().get(3).getEceStep()); // step 4 is the ece one
		
		// when
		ft.addEndpointTypeSupportingFlow(flow);
		
		// TODO (M) v55 resolve warning "[W] Trying to add a null command to flow" 
		
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(apiDescription);
		MDSLGeneratorModel mdslGenModel = converter.convert();

		// then
		assertEquals(6, mdslGenModel.getOrchestrationFlows().get(0).getEvents().size()); // four atomic events, one AND join
		
		assertEquals(1, apiDescription.getContracts().size());
		EndpointContract ec = (EndpointContract)apiDescription.getContracts().get(0);
		EList<Operation> operations = (ec.getOps());
		assertEquals(7, operations.size()); 
		assertEquals("c2", operations.get(4).getName()); 
		assertEquals(4, ec.getEvents().size()); // three events for/from cis steps in flow, one to initiate flow processing
		assertEquals("e2", ec.getEvents().get(1).getType().getName()); 
		assertEquals("PROCESSING_RESOURCE", ec.getPrimaryRole());
		assertEquals(flow.getName(), ec.getFlow().getName());
		// TODO validate more endpoint type and operation content
	}
	
	@Override
	protected String testDirectory() {
		return "/test-data/quickfix-transformations/";
	}
}

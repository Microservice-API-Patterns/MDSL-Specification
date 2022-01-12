package io.mdsl.generator.model.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.composition.views.PathCollection;
import io.mdsl.generator.model.composition.views.Process;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.utils.MDSLLogger;

public class FlowGenModelFromMDSLFileTest extends AbstractMDSLInputIntegrationTest {
	
	private static final int INDEX_OF_FIRST_FLOW = 0;

	// ** flow gen model construction and access tests 
	
	@Test
	public void canTurnBasicFlowGenModelIntoString0a() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest0a-hello-one-flow.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		
		// then
		assertEquals("FlowTest0", mdslGenModel.getApiName());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().size());
		assertEquals(2, mdslGenModel.getOrchestrationFlows().get(0).getEvents().size());
		assertEquals(2, mdslGenModel.getOrchestrationFlows().get(0).getCommands().size());
		
		// file comparison (template-based) happening in ProcessFlowFreemarkerTemplateTest
	}

	@Test
	public void canAccessBasicOrchestrationGenModel0b() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest0bc-hello-flow-world.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		
		// then
		assertEquals("FlowTest0", mdslGenModel.getApiName());
		assertEquals(2, mdslGenModel.getOrchestrationFlows().size());
		assertEquals("HelloWorldFlow0", mdslGenModel.getOrchestrationFlows().get(0).getName());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).getCommands().size());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).getEvents().size());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).initEvents().size());
		Event event1 = mdslGenModel.getOrchestrationFlows().get(0).getEvents().get("FlowInitiated");
		assertEquals("FlowInitiated", event1.getName()); // events in hash map
		Command command1 = mdslGenModel.getOrchestrationFlows().get(0).getCommands().get(0);
		assertEquals("FlowStep1", command1.getName()); // commands are in a list
	}
	
	@Test
	public void canAccessBasicOrchestrationGenModel0c() throws IOException {
		// given
		// second flow in MDSL file; file also used in canAccessBasicOrchestrationGenModel0b
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest0bc-hello-flow-world.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		
		// then
		assertEquals("FlowTest0", mdslGenModel.getApiName());
		assertEquals(2, mdslGenModel.getOrchestrationFlows().size());
		
		dumpFlow(mdslGenModel, 1);
		Flow flow2 = mdslGenModel.getOrchestrationFlows().get(1);
		assertEquals("HelloWorldFlow1", flow2.getName());
		assertEquals(2, flow2.getCommands().size());
		assertEquals(2, flow2.getEvents().size());
		assertEquals(1, flow2.initEvents().size());
		// assert that C1 triggers E2
		Command command1 = flow2.getCommands().get(0);	
		assertEquals("FlowStep1",command1.getName());
		assertEquals(1, command1.emits().size());
		assertEquals("Event2",command1.emits().get(0).getName());
	}

	@Test
	public void canAccessSequentialOrchestrationGenModel() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest1-sequence.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);

		// then
		assertEquals("FlowTest1", mdslGenModel.getApiName());
		assertEquals(2, mdslGenModel.getOrchestrationFlows().size());
		assertEquals("SequentialFlowStartingWithEvent", mdslGenModel.getOrchestrationFlows().get(0).getName());
		Flow testFlow = mdslGenModel.getOrchestrationFlows().get(0);
		assertEquals(3, testFlow.getCommands().size()); // lists and hash maps differ in their presence checks
		assertEquals(1,testFlow.initEvents().size());
		assertEquals(4, testFlow.getEvents().size());
		assertEquals(1, testFlow.terminationEvents().size());
		// TODO could check more even genmodel content (event/command relations etc.)
		assertEquals("FlowStep3", testFlow.getCommands().get(2).getName());
		assertEquals("FlowInitiated", testFlow.getEvents().get("FlowInitiated").getName());
		assertEquals(1, testFlow.getCommands().get(0).emits().size());
		assertEquals(1, testFlow.getEvents().get("FlowInitiated").triggeredCommands().size());
		assertEquals("FlowStep1",testFlow.getEvents().get("FlowInitiated").triggeredCommands().get(0).getName());
		assertEquals("FlowStep1Completed",testFlow.getCommands().get(0).emits().get(0).getName());
		Collection<Event> events = testFlow.processView().triggeredBy(testFlow.getCommands().get(2));
		StringBuffer eventNames = new StringBuffer();
		events.forEach(event->eventNames.append(event.getName()));
		assertEquals("FlowStep2Completed", eventNames.toString()); // only 1
		assertEquals("FlowTerminated", testFlow.getCommands().get(2).emits().get(0).getName());
		
		// second flow in file does not have a start event:
		assertEquals(0, mdslGenModel.getOrchestrationFlows().get(1).initEvents().size());
	}
	
	@Test
	public void canCreateGenModelForAndMCIStep() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		dumpFlow(mdslGenModel, 1);
		
		// then
		assertEquals("FlowTest2", mdslGenModel.getApiName());
		assertEquals(2, mdslGenModel.getOrchestrationFlows().size()); 
		
		Flow testFlow = mdslGenModel.getOrchestrationFlows().get(0);
		assertEquals("ParallelSplit", testFlow.getName());
		assertEquals(4, testFlow.getCommands().size()); // three basic commands, one composite
		assertEquals(3, testFlow.getEvents().size());
		assertEquals(1, testFlow.initEvents().size());
		assertEquals(1, testFlow.terminationEvents().size());
		assertEquals("FlowStep3", testFlow.getCommands().get(2).getName());
		assertEquals("AND_FlowStep2_FlowStep3", testFlow.getCommands().get(3).getName());
		assertTrue(testFlow.getCommands().get(3) instanceof CompositeCommand);
		assertEquals("FlowInitiated", testFlow.getEvents().get("FlowInitiated").getName());
		assertEquals(1, testFlow.getCommands().get(0).emits().size());
		assertEquals("FlowStep1Completed", testFlow.getCommands().get(0).emits().get(0).getName());
		assertEquals(1, testFlow.getEvents().get("FlowInitiated").triggeredCommands().size());
		assertEquals("FlowStep1",testFlow.getEvents().get("FlowInitiated").triggeredCommands().get(0).getName());	
		
		testFlow = mdslGenModel.getOrchestrationFlows().get(1);
		assertEquals("ParallelSplitWithSynchronization", testFlow.getName());
		assertEquals(6, testFlow.getEvents().size()); // one join
		assertEquals(5, testFlow.getCommands().size()); // four basic commands, one composite
		assertEquals(1, testFlow.initEvents().size());
		assertEquals(1, testFlow.terminationEvents().size());
	}
	
	// test files 3a not used here but in FlowGenModelFromScratchTest and in Freemarker template tests
	
	@Test
	public void canCreateGenModelForChoiceWithMergeStep() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest3b-inclusivechoice-andmerge.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);

		// then
		assertEquals("FlowTest3b", mdslGenModel.getApiName());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().size());
		Flow testFlow = mdslGenModel.getOrchestrationFlows().get(0);
		assertEquals("InclusiveChoiceWithAndMerge", testFlow.getName());
		assertEquals(4, testFlow.getCommands().size()); // OR and XOR do not yield composite commands
		assertEquals(6+1, testFlow.getEvents().size()); // six plus AND join event
		assertEquals(1, testFlow.initEvents().size());
		assertEquals(1, testFlow.terminationEvents().size()); 
		assertEquals("FlowStep3", testFlow.getCommands().get(2).getName());
		assertNotNull(testFlow.getEvents().get("AND_FlowStep2Completed_FlowStep3Completed"));
		assertTrue(testFlow.getEvents().get("AND_FlowStep2Completed_FlowStep3Completed") instanceof JoinEvent);
		assertEquals("FlowStep4", testFlow.getEvents().get("AND_FlowStep2Completed_FlowStep3Completed").triggeredCommands().get(0).getName());
	}
	
	// test files 4a not used here but in Freemarker template tests

	@Test
	public void canAccessGenModelInFlowsWithAllAndAndOrBranchingOptions() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest4b-allbranchingoptions.mdsl")).getServiceSpecification();
		// note that process view tests further down work with other file "flowtest4b-andbranching-with-overlaps.mdsl"
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();

		// then
		assertEquals("FlowTest4", mdslGenModel.getApiName());
		assertEquals(2, mdslGenModel.getOrchestrationFlows().size()); 
		dumpFlow(mdslGenModel, 0);

		// AND flow
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(0);
		assertEquals("AllAndOptionsInMDSLWithOverlap", flow1.getName());
		assertEquals(6, flow1.getCommands().size()); 
		assertEquals(9, flow1.getEvents().size()); // seven atomic events and one AND event and one OR event
		assertEquals(1, flow1.initEvents().size()); 
		assertEquals(1, flow1.terminationEvents().size()); 

		Command command1 = flow1.getCommands().get(0);
		assertEquals("FlowStep1", command1.getName());
		assertEquals(1, command1.emits().size());// emits a join event
		assertEquals("AND_FlowStep1OutEvent1_FlowStep1OutEvent2", command1.emits().get(0).getName());
		Command command5 = flow1.getCommands().get(5);
		assertEquals("FlowStep5", command5.getName());
		assertEquals(1, command5.emits().size());
		assertEquals("FlowTerminated", command5.emits().get(0).getName());
		Command andCommand = flow1.getCommands().get(2); // why at position 3?
		assertEquals("AND_FlowStep1_FlowStep2", andCommand.getName()); // what does this command say in trigger/emit? 
		assertEquals(Flow.AND_OPERATOR, andCommand.getType());
		assertTrue(andCommand instanceof CompositeCommand);
		assertEquals(2, andCommand.containedCommands().size());
		assertEquals("FlowStep1", andCommand.containedCommands().get(0).getName());
		assertEquals(2, ((CompositeCommand) andCommand).containedCommands().size());
		assertEquals("FlowStep2", ((CompositeCommand) andCommand).containedCommands().get(1).getName());
	
		// TODO work with second flow in test file 4b (OR with overlap)
	}
	
	// TODO flow 4c not tested here, but further down in test "canWorkWithProcessViewOfFlow2InModel4c"  

	@Test
	public void canAccessGenModelForVariationTest5Flow1() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest5a-syntaxvariations-canonicalgenmodel.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();

		// then
		assertEquals("FlowTest5", mdslGenModel.getApiName());
		assertEquals(4, mdslGenModel.getOrchestrationFlows().size()); 
		
		dumpFlow(mdslGenModel, 0); // genmodel dump validated via FTL generator, no duplicates 
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);
		
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(0);
		assertEquals("SampleFlowWithSyntaxVariations1", flow1.getName());
		assertEquals(4, flow1.getEvents().size());
		assertEquals(3, flow1.getCommands().size()); 
		// two simple CIS consolidated into one: 
		assertNull(flow1.getEvents().get("Event0").joinedEvents());
		assertEquals("Command1", flow1.getEvents().get("Event0").triggeredCommands().get(0).getName()); 
		assertEquals("Command2", flow1.getEvents().get("Event0").triggeredCommands().get(1).getName()); 
		Command command1 = flow1.getCommands().get(0);
		assertEquals("Command1", command1.getName());
		assertEquals(2, command1.emits().size());
		// two simple CIS consolidated into one (both have ORed events) 
		assertEquals(2, flow1.getEvents().get("AND_Event1_Event2").joinedEvents().size()); 
		assertEquals(1, flow1.getEvents().get("AND_Event1_Event2").triggeredCommands().size()); 
	}
	
	@Test
	public void canAccessGenModelForVariationTest5Flow2() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest5a-syntaxvariations-canonicalgenmodel.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();

		// then
		assertEquals("FlowTest5", mdslGenModel.getApiName());
		assertEquals(4, mdslGenModel.getOrchestrationFlows().size()); 
		assertEquals("SampleFlowWithSyntaxVariations2", mdslGenModel.getOrchestrationFlows().get(1).getName()); 
		dumpFlow(mdslGenModel, 1);
		assertEquals(4, mdslGenModel.getOrchestrationFlows().size());
		assertEquals(6, mdslGenModel.getOrchestrationFlows().get(1).getEvents().size());
		assertEquals(8, mdslGenModel.getOrchestrationFlows().get(1).getCommands().size());
		// TODO v55 validate genmodel content for Event1, Event2; Command 1, Command 2
		// Event 2 triggers Command1 and Command 2 multiple times (due to overlap in MSDL)
		// same test file also used in Sketch Miner story FTL test, which dumps genmodel and paths too
	}

	@Test
	public void canAccessGenModelForVariationTest5Flow3() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest5a-syntaxvariations-canonicalgenmodel.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();

		// then
		assertEquals("FlowTest5", mdslGenModel.getApiName());
		assertEquals(4, mdslGenModel.getOrchestrationFlows().size()); 
		dumpFlow(mdslGenModel, 2);
		
		Flow flow3 = mdslGenModel.getOrchestrationFlows().get(2);
		assertEquals("SampleFlowWithSyntaxVariations3", flow3.getName()); // combined ece command (most simple one)
		assertEquals(2, flow3.getEvents().size()); 
		assertEquals(1, flow3.getCommands().size()); 
	}
	
	@Test
	public void canAccessGenModelForVariationTest5Flow4() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest5a-syntaxvariations-canonicalgenmodel.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();

		// then
		assertEquals("FlowTest5", mdslGenModel.getApiName());
		assertEquals(4, mdslGenModel.getOrchestrationFlows().size()); 
		dumpFlow(mdslGenModel, 3);
		
		/* 
		// correct (duplicate command trigger for Event 1 now removed):
		Number of events: 5
		Number of commands: 3
		 Event: Event1 (Event) triggers 2 command(s): Command1 (Command) AND_Command1_Command2 (CompositeCommand) (CompositeCommand) 
		 Event: Command1Done (Event) triggers 0 command(s): 
		 Event: FlowTerminated (Event) triggers 0 command(s): 
		 Event: AND_Command1Done_FlowTerminated (CompositeEvent) triggers 0 command(s): 
		 Event: Event2 (Event) triggers 1 command(s): AND_Command1_Command2 (CompositeCommand) 
		 Command Command1 emits 2 event(s): Event1 (Event) AND_Command1Done_FlowTerminated (CompositeEvent) 
		 Command Command2 emits 1 event(s): AND_Command1Done_FlowTerminated (CompositeEvent) 
		 Command AND_Command1_Command2 emits 0 event(s): 
		*/
		
		Flow flow4 = mdslGenModel.getOrchestrationFlows().get(3);
		assertEquals("SampleFlowWithSyntaxVariations4", flow4.getName());
		PathCollection pc = flow4.processView().getAllPaths();
		dumpPathCollection(pc);
		
		assertEquals(5, flow4.getEvents().size()); 
		assertEquals(3, flow4.getCommands().size()); 
		assertEquals(2, flow4.getCommands().get(0).emits().size()); 
		assertEquals(1, flow4.getCommands().get(1).emits().size()); 
		assertEquals(2, flow4.getEvents().get("Event1").triggeredCommands().size());
		assertEquals(1, flow4.getEvents().get("Event2").triggeredCommands().size());
		assertEquals(0, flow4.getEvents().get("FlowTerminated").triggeredCommands().size());
	}
	
	/*
	@Test
	public void canAccessGenModelForFlowAfterQuickFixApplications() throws IOException {
	}
	*/
	
	/*
	@Test
	public void canAccessGenModelForFlowFromEventStorming() throws IOException {
		// this genmodel test is not yet available here, but Camel java generation tests this flow (files "esoad...")
	}
    */
	
	@Test 
	public void canAccessGenmodelOfFlowWithCombinedSteps() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest6a-combinedsteps-and-alloperators.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);
	
		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		
		// then
		assertEquals("TestCombinedStepsAndAllOptionsBrief", mdslGenModel.getApiName());
		assertEquals(6, mdslGenModel.getOrchestrationFlows().get(0).getEvents().size()); // 5 + 1 AND
		assertEquals(3, mdslGenModel.getOrchestrationFlows().get(0).getCommands().size()); 
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).getEvents().get("e1").triggeredCommands().size()); 
		assertEquals(2, mdslGenModel.getOrchestrationFlows().get(0).getCommands().get(2).emits().size()); // command c2
		assertEquals(0, mdslGenModel.getOrchestrationFlows().get(0).initEvents().size());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).initCommands().size());
	}
	
	// ** process view tests
	
	@Test 
	public void canWorkWithProcessViewOfModel1() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest1-sequence.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		// String result = processViewOnGenModel.dumpAllPaths();
		// System.out.println(result);
		dumpProcessView(processViewOnGenModel);
		PathCollection pc = processViewOnGenModel.getAllPaths();
		
		// then
		assertEquals("FlowTest1", mdslGenModel.getApiName());
		assertEquals(7, processViewOnGenModel.numberOfNodes()); // 4 events, 3 commands
		assertEquals(4, processViewOnGenModel.numberOfEdges());
		assertEquals(1, pc.size());
		assertEquals(4, pc.getPath(0).length()); // three event-command pairs plus termination "event"
		assertEquals("FlowTerminated", pc.getPath(0).getEventAt(3));
		assertEquals("FlowStep2", pc.getPath(0).getCommandAt(1));
		assertEquals("FlowStep3", pc.getPath(0).getCommandAt(2));
	}
	
	@Test 
	public void canWorkWithProcessViewOfModel2Flow1() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		// String result = processViewOnGenModel.dumpAllPaths();
		// System.out.println(result);
		dumpProcessView(processViewOnGenModel);
		PathCollection pc = processViewOnGenModel.getAllPaths();
				
		// then
		assertEquals("FlowTest2", mdslGenModel.getApiName());;
		assertEquals(7, processViewOnGenModel.numberOfNodes()); // 3 events, 4 commands
		assertEquals(6, processViewOnGenModel.numberOfEdges()); // 2 paths, each of length 3
		assertEquals(2, pc.size());
		assertEquals(3, pc.getPath(0).length()); 
		
		// path 1:
		assertEquals("FlowStep1Completed", pc.getPath(0).getEventAt(1));
		assertEquals("FlowStep2", pc.getPath(0).getCommandAt(1));
		assertEquals("FlowTerminated", pc.getPath(0).getEventAt(2));
		assertEquals("done", pc.getPath(0).getCommandAt(2));
		
		// path 2:
		assertEquals(3, pc.getPath(1).length()); 
		assertEquals("FlowStep1Completed", pc.getPath(1).getEventAt(1));
		assertEquals("FlowStep3", pc.getPath(1).getCommandAt(1));
		assertEquals("FlowTerminated", pc.getPath(1).getEventAt(2));
		assertEquals("done", pc.getPath(1).getCommandAt(2));
	}
	
	@Test 
	public void canWorkWithProcessViewOfModel3a() throws IOException {
		// given
		ServiceSpecification mdsl3a = new MDSLResource(getTestResource("flowtest3a-exclusivechoice-implicitmerge.mdsl")).getServiceSpecification();
		ServiceSpecification mdsl3b = new MDSLResource(getTestResource("flowtest3b-inclusivechoice-andmerge.mdsl")).getServiceSpecification(); // TODO 
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl3a);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc); 
		
		// then
		assertEquals("FlowTest3a", mdslGenModel.getApiName());
		assertEquals(10, processViewOnGenModel.numberOfNodes()); // 6 events, 4 commands
		assertEquals(8, processViewOnGenModel.numberOfEdges()); // 2 paths, each of length 4
		assertEquals(2, pc.size());
		assertEquals(4, pc.getPath(0).length()); 
		assertEquals(4, pc.getPath(1).length());
		
		assertEquals("FlowStep1CompletedOptionA", pc.getPath(0).getEventAt(1));
		assertEquals("FlowStep2", pc.getPath(0).getCommandAt(1));
		assertEquals("FlowStep1CompletedOptionB", pc.getPath(1).getEventAt(1));
		assertEquals("FlowStep3", pc.getPath(1).getCommandAt(1));
		assertEquals("FlowStep4", pc.getPath(0).getCommandAt(2));
		assertEquals("done", pc.getPath(1).getCommandAt(3));
	}
	
	@Test 
	public void canWorkWithProcessViewOfModel3c() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest3c-exclusivechoiceviacommand-implicitmerge.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);
		
		// then
		assertEquals("FlowTest3c", mdslGenModel.getApiName());
		assertEquals(2, pc.size());
		assertEquals(9, processViewOnGenModel.numberOfNodes()); // 5 events, 4 commands
		assertEquals(7, processViewOnGenModel.numberOfEdges()); // 2 paths, of length 3 and 4
		assertEquals(4, pc.getPath(0).length()); 
		assertEquals(3, pc.getPath(1).length());
		// path 1:
		assertEquals("FlowInitiated", pc.getPath(0).getEventAt(0));
		assertEquals("FlowStep1", pc.getPath(0).getCommandAt(0));
		assertEquals("FlowStep1CompletedOptionA", pc.getPath(0).getEventAt(1));
		assertEquals("FlowStep3", pc.getPath(0).getCommandAt(1));
		assertEquals("FlowStep3Completed", pc.getPath(0).getEventAt(2));
		assertEquals("FlowStep4", pc.getPath(0).getCommandAt(2));
		assertEquals("FlowTerminated", pc.getPath(0).getEventAt(3));
		assertEquals("done", pc.getPath(0).getCommandAt(3));
		// path 2 (shorter):
		assertEquals("FlowStep2Completed", pc.getPath(1).getEventAt(1));
		assertEquals("FlowStep4", pc.getPath(1).getCommandAt(1));
		assertEquals("FlowTerminated", pc.getPath(1).getEventAt(2));
		assertEquals("done", pc.getPath(1).getCommandAt(2));
	}
	
	@Test 
	public void canWorkWithProcessViewOfModel4aFlow1() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest4a-alloptionsmodel1.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		// String result = processViewOnGenModel.dumpAllPaths();
		// System.out.println(result);
		dumpProcessView(processViewOnGenModel);
		
		// then
		assertEquals("FlowTest4a", mdslGenModel.getApiName());
		assertEquals("SampleFlowWithCisStepOr", mdslGenModel.getOrchestrationFlows().get(0).getName());
		assertEquals(3, pc.size());
		assertEquals(12, processViewOnGenModel.numberOfNodes()); // 5 events (one AND), 5 commands
		assertEquals(15, processViewOnGenModel.numberOfEdges()); // 3 paths, of length 5
		assertEquals(5, pc.getPath(0).length());
		assertEquals(5, pc.getPath(1).length());
		assertEquals(5, pc.getPath(2).length());
		
		/*
		/// correct (from output):
		(FlowInitiated)
		StartFlowCommand
		(Event1)
		Command1
		(Command1Done1)
		AND_Command1Done1_Command2Done_Command3Done
		(AND_Command1Done1_Command2Done_Command3Done)
		TerminateFlowCommand
		(FlowTerminated)
		done

		(FlowInitiated)
		StartFlowCommand
		(Event1)
		Command2
		(Command2Done)
		AND_Command1Done1_Command2Done_Command3Done
		(AND_Command1Done1_Command2Done_Command3Done)
		TerminateFlowCommand
		(FlowTerminated)
		done

		(FlowInitiated)
		StartFlowCommand
		(Event1)
		Command3
		(Command3Done)
		AND_Command1Done1_Command2Done_Command3Done
		(AND_Command1Done1_Command2Done_Command3Done)
		TerminateFlowCommand
		(FlowTerminated)
		done
		*/
	}
	
	@Test 
	public void canWorkWithProcessViewOfModel4aFlow2() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest4a-alloptionsmodel1.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 1);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(1).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);
		
		// could also test flow 3, "SampleFlowWithDepStepAnd"
		
		// then
		assertEquals("FlowTest4a", mdslGenModel.getApiName());
		assertEquals("SampleFlowWithCisStepAnd", mdslGenModel.getOrchestrationFlows().get(1).getName());

		assertEquals(3, pc.size());
		assertEquals(5, pc.getPath(0).length());
		assertEquals(5, pc.getPath(1).length());
		assertEquals(5, pc.getPath(2).length());
		/*
		/// correct (from output):
		(FlowInitiated)
		StartFlowCommand
		(Event1)
		Command1
		(Command1Done1)
		AND_Command1Done1_Command2Done_Command3Done
		(AND_Command1Done1_Command2Done_Command3Done)
		TerminateFlowCommand
		(FlowTerminated)
		done

		(FlowInitiated)
		StartFlowCommand
		(Event1)
		Command2
		(Command2Done)
		AND_Command1Done1_Command2Done_Command3Done
		(AND_Command1Done1_Command2Done_Command3Done)
		TerminateFlowCommand
		(FlowTerminated)
		done

		(FlowInitiated)
		StartFlowCommand
		(Event1)
		Command3
		(Command3Done)
		AND_Command1Done1_Command2Done_Command3Done
		(AND_Command1Done1_Command2Done_Command3Done)
		TerminateFlowCommand
		(FlowTerminated)
		done
		*/
	}
	
	@Test 
	public void canWorkWithProcessViewOfFlow1InModel4b() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest4b-andbranching-with-overlaps.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0); // first flow in file, second one used different syntax (same model)
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);;
		// String result = processViewOnGenModel.dumpAllPaths();
		// System.out.println(result); 
		
		// then
		assertEquals("FlowTest4", mdslGenModel.getApiName());
		assertEquals("AllAndOptionsInMDSLWithOverlap", mdslGenModel.getOrchestrationFlows().get(0).getName());
		assertEquals(3, pc.size());

		/*
		/// correct (from output)
		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent1)
		FlowStep3
		(FlowStep3Completed)
		AND_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep3Completed_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent2)
		FlowStep4
		(FlowStep4Completed)
		AND_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep3Completed_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep2
		(FlowStep2Completed)
		FlowStep3
		(FlowStep3Completed)
		AND_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep3Completed_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done
 		*/	
	}
	
	@Test 
	public void canWorkWithProcessViewOfFlow2InModel4c() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest4c-allbranchingoptions.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 2); // third flow in file, OR (first one has AND, second one has workaround)
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(2).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);

		// then
		assertEquals("FlowTest4", mdslGenModel.getApiName());
		
		// then
		assertEquals("FlowTest4", mdslGenModel.getApiName());
		assertEquals("AllOrOptionsInMDSLWithOverlapWorks", mdslGenModel.getOrchestrationFlows().get(2).getName());
		assertEquals(4, pc.size()); 
		assertEquals(5, pc.getPath(0).length());
		assertEquals(5, pc.getPath(1).length());
		assertEquals(5, pc.getPath(2).length());
		assertEquals(4, pc.getPath(3).length());
		assertEquals(8, mdslGenModel.getOrchestrationFlows().get(2).getEvents().size());
		assertEquals(5, mdslGenModel.getOrchestrationFlows().get(2).getCommands().size());
		assertEquals("FlowInitiated->FlowStep2;FlowStep2Completed->FlowStep3;FlowStep3Completed->AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed;AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed->FlowStep5;FlowTerminated->done;", 
				pc.getPath(2).dump()); // this is the path that was missing (overlap) 
		/*
		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent1)
		FlowStep3
		(FlowStep3Completed)
		AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent2)
		FlowStep4
		(FlowStep4Completed)
		AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep2
		(FlowStep2Completed)
		FlowStep3
		(FlowStep3Completed)
		AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep2
		(FlowStep2Completed)
		AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep2Completed_FlowStep3Completed_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done
		*/
	}
	
	@Test 
	public void canWorkWithProcessViewOfFlow2InModel4cWithWorkaround() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest4c-allbranchingoptions.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 1); // second flow in file, OR (first one has AND)
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(1).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);

		// then
		assertEquals("FlowTest4", mdslGenModel.getApiName());
		
		// then
		assertEquals("FlowTest4", mdslGenModel.getApiName());
		assertEquals("AllOrOptionsInMDSLWithOverlapWithFix", mdslGenModel.getOrchestrationFlows().get(1).getName());
		assertEquals(3, pc.size()); // 3 is correct
		
		/*
		// correct (from output):
		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent1)
		FlowStep3
		(FlowStep3Completed)
		InterimCommand
		(Steps1And2Done)
		AND_Steps1And2Done_FlowStep4Completed
		(AND_Steps1And2Done_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent2)
		FlowStep4
		(FlowStep4Completed)
		AND_Steps1And2Done_FlowStep4Completed
		(AND_Steps1And2Done_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep2
		(FlowStep2Completed)
		FlowStep3
		(FlowStep3Completed)
		InterimCommand
		(Steps1And2Done)
		AND_Steps1And2Done_FlowStep4Completed
		(AND_Steps1And2Done_FlowStep4Completed)
		FlowStep5
		(FlowTerminated)
		done
		*/
	}
	
	@Test
	public void canAccessGenModelForVariationTest5a() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest5a-syntaxvariations-canonicalgenmodel.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);
		
		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, INDEX_OF_FIRST_FLOW);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();

		// then
		dumpPathCollection(pc);
		
		assertEquals("FlowTest5", mdslGenModel.getApiName());
		assertEquals("SampleFlowWithSyntaxVariations1", mdslGenModel.getOrchestrationFlows().get(0).getName());
		assertEquals(4, pc.size()); 
		
		/*
		/// correct (from output);
		(Event0)
		Command1
		(Event1)
		AND_Event1_Event2
		(AND_Event1_Event2)
		TerminateCommand

		(Event0)
		Command1
		(Event2)
		AND_Event1_Event2
		(AND_Event1_Event2)
		TerminateCommand

		(Event0)
		Command2
		(Event1)
		AND_Event1_Event2
		(AND_Event1_Event2)
		TerminateCommand

		(Event0)
		Command2
		(Event2)
		AND_Event1_Event2
		(AND_Event1_Event2)
		TerminateCommand
		*/
		
		// could test more of the process view helpers here
		Process processViewOnFlow4 = mdslGenModel.getOrchestrationFlows().get(3).processView();
		Command andCommmand = mdslGenModel.getOrchestrationFlows().get(3).getCommands().get(2);
		assertEquals("AND_Command1_Command2", andCommmand.getName());
		assertEquals(2, processViewOnFlow4.getEventsThatTrigger(andCommmand).size());
	}
	
	@Test 
	public void canWorkWithProcessViewOfFlow1InModel5bHasSimpleLoopInDepStep() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest5b-simpleloops.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();
		// String result = processViewOnGenModel.dumpAllPaths();
		// System.out.println(result); 
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);

		// then
		assertEquals("FlowTest5b", mdslGenModel.getApiName());
		assertEquals("SampleFlowWithLoop", mdslGenModel.getOrchestrationFlows().get(0).getName());
		assertEquals(2, pc.size());

		/*
		/// correct (from output);
		(Event0)
		Command1
		(Event1)
		Command2
		(Command2)
		GOTO-Event1

		(Event0)
		Command1
		(Event1)
		Command3
		(FlowTerminated)
		done
		*/
	}
	
	@Test
	public void canAccessGenModelForLoopTest5bHasSimpleLoopInCisStep() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest5b-simpleloops.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);
		
		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 1);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(1).processView();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		dumpPathCollection(pc);

		// then
		assertEquals("FlowTest5b", mdslGenModel.getApiName());
		assertEquals("SampleFlowWithLoopInCisStep", mdslGenModel.getOrchestrationFlows().get(1).getName());
		assertEquals(2, pc.size()); 	
		/*
		/// ok (from output):
		(Event0)
		Command1
		(Event1)
		Command2
		(Event2)
		Command1
		(Command1)
		GOTO-Event1

		(Event0)
		Command1
		(Event1)
		Command2
		(Event2)
		StopFlowCommand
		*/
	}

	@Test 
	public void canWorkWithProcessViewOfModel7() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest7-branchingoptions-withnesting.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = mdslGenModel.getOrchestrationFlows().get(0).processView();

		// then
		assertEquals("FlowTest7", mdslGenModel.getApiName());
		String result = processViewOnGenModel.dumpAllPaths();
		PathCollection pc = processViewOnGenModel.getAllPaths();
		assertEquals(3, pc.size());
		assertEquals(7, pc.getPath(0).length());
		assertEquals(7, pc.getPath(1).length());
		assertEquals(5, pc.getPath(2).length());
		dumpPathCollection(pc);
		/*
		/// correct (from output): 
		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent1)
		FlowStep3
		(FlowStep3Completed)
		AND_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep3Completed_FlowStep4Completed)
		FlowStep6
		(FlowStep6Completed)
		AND_FlowStep5Completed_FlowStep6Completed
		(AND_FlowStep5Completed_FlowStep6Completed)
		FlowStep7
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep1
		(FlowStep1OutEvent2)
		FlowStep4
		(FlowStep4Completed)
		AND_FlowStep3Completed_FlowStep4Completed
		(AND_FlowStep3Completed_FlowStep4Completed)
		FlowStep6
		(FlowStep6Completed)
		AND_FlowStep5Completed_FlowStep6Completed
		(AND_FlowStep5Completed_FlowStep6Completed)
		FlowStep7
		(FlowTerminated)
		done

		(FlowInitiated)
		FlowStep2
		(FlowStep2Completed)
		FlowStep5
		(FlowStep5Completed)
		AND_FlowStep5Completed_FlowStep6Completed
		(AND_FlowStep5Completed_FlowStep6Completed)
		FlowStep7
		(FlowTerminated)
		done
 		*/
	}

	private void dumpFlow(MDSLGeneratorModel flogenmodel, int index) {
		if(MDSLLogger.logLevel<2) {
			return;
		}
		String flowDump = flogenmodel.getOrchestrationFlows().get(index).toString();
		System.out.println("----------------------------------------------------------------------------");
		System.out.print(flowDump);
		System.out.println("----------------------------------------------------------------------------");
	}
	
	private void dumpProcessView(Process processViewOnGenModel) {
		if(MDSLLogger.logLevel<2) {
			return;
		}
		
		System.out.println("----------------------------------------------------------------------------");
		System.out.println("TestFlow paths collection:"); 
		System.out.println(processViewOnGenModel.dumpAllPaths());
		System.out.println("----------------------------------------------------------------------------");
	}
	
	
	private void dumpPathCollection(PathCollection pc) {
		if(MDSLLogger.logLevel<2) {
			return;
		}
		
		System.out.println("----------------------------------------------------------------------------");
		if(pc!=null)
			System.out.println(pc.toString());
		else
			MDSLLogger.reportWarning("Empty path collection.");
		System.out.println("----------------------------------------------------------------------------");
	}
	
	@Override
	protected String testDirectory() {
		return "/test-data/flowgenmodel-tests/";
	}
}

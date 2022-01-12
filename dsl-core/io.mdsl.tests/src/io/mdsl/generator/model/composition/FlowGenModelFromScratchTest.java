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
package io.mdsl.generator.model.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.composition.views.Path;
import io.mdsl.generator.model.composition.views.PathCollection;
import io.mdsl.generator.model.composition.views.Process;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;
import io.mdsl.utils.MDSLLogger;

public class FlowGenModelFromScratchTest extends AbstractMDSLInputIntegrationTest {
	
	// ** building models from scratch
	
	@Test
	public void canCreateOrchestrationFlowModelWithSingleEventAndSingleCommand() {
		// given
		MDSLGeneratorModel mdslGenModel = new MDSLGeneratorModel("TestAPI");
		Flow flow = new io.mdsl.generator.model.composition.Flow("TestFlow");
		Event event = new io.mdsl.generator.model.composition.Event("TestEvent");
		Command command = new io.mdsl.generator.model.composition.Command("TestCommand");
		List<Command> triggeredCommands = new ArrayList<Command>();
		
		// when
		flow.addCommand(command);
		flow.addEvent(event);
		triggeredCommands.add(command);
		event.addTriggeredCommands(triggeredCommands, Flow.NO_OPERATOR);
		mdslGenModel.addOrchestration(flow);
		dumpFlow(mdslGenModel, 0);

		// then
		assertEquals(1, mdslGenModel.getOrchestrationFlows().size());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).initEvents().size()); // 0 termination events
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).getCommands().size());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).getEvents().size());
		Event event1 = mdslGenModel.getOrchestrationFlows().get(0).getEvents().get("TestEvent");
		assertEquals("TestEvent", event1.getName()); // events in hash map
		
		assertEquals(1, event1.triggeredCommands().size());
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).getCommands().size());
		Command command1 = mdslGenModel.getOrchestrationFlows().get(0).getCommands().get(0);
		assertEquals("TestCommand", command1.getName()); // commands are in a list
		assertEquals("TestCommand", event1.triggeredCommands().get(0).getName());
		
		// System.out.println("TestFlow paths: " + flow.getProcessView().dumpAllPaths());
		dumpProcessView(flow.processView());
	}
	
	@Test
	public void canCreateOrchestrationFlowModelAndViewItAsProcess() {
		// given
		MDSLGeneratorModel mdslGenModel = new MDSLGeneratorModel("TestAPI");
		Flow flow = new io.mdsl.generator.model.composition.Flow("TestFlow");
		Event event = new io.mdsl.generator.model.composition.Event("TestEvent");
		Command command1 = new io.mdsl.generator.model.composition.Command("TestCommand1");
		Command command2 = new io.mdsl.generator.model.composition.Command("TestCommand2");
		List<Command> composedCommands = new ArrayList<Command>();
		
		// when
		flow.addEvent(event);
		flow.addCommand(command1);
		flow.addCommand(command2);
		composedCommands.add(command1);
		composedCommands.add(command2);
		// command composition done inside event.addTriggeredCommands(composedCommands, Flow.AND_OPERATOR):
		Command andCommand = event.addTriggeredCommands(composedCommands, Flow.AND_OPERATOR);
		flow.addCommand(andCommand);
		mdslGenModel.addOrchestration(flow);
		
		// then
		assertEquals(1, mdslGenModel.getOrchestrationFlows().get(0).getEvents().size());
		assertEquals(3, mdslGenModel.getOrchestrationFlows().get(0).getCommands().size());
		
		// System.out.println("TestFlow paths: " + flow.getProcessView().dumpAllPaths());
		dumpProcessView(flow.processView());
	}
	
	// ** starting with models in files:
	
	@Test
	public void canWorkWithProcessViewPathCollectors() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest1-sequence.mdsl")).getServiceSpecification();
		// ServiceSpecification mdsl2 = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(0);
		dumpFlow(mdslGenModel, 0);
		
		Process processViewOnGenModel = flow1.processView();
		Event finalEvent = flow1.getEvents().get("FlowTerminated");
		PathCollection pcte = processViewOnGenModel.getDownstreamPathsOf(finalEvent);
		PathCollection pccmd = processViewOnGenModel.getDownstreamPathsOf(flow1.getCommands().get(2));
		PathCollection pcFromInitEvent = processViewOnGenModel.getDownstreamPathsOf(flow1.getEvents().get("FlowInitiated"));
		
		// then
		assertTrue(processViewOnGenModel.terminatesFlow(finalEvent));
		
		dumpPathCollection(pcte);
		assertEquals(1, pcte.size());
		assertEquals(1, pcte.getPath(0).length());
		assertEquals(1, pcte.size());
		assertEquals(1, pcte.getPath(0).length());
		
		dumpPathCollection(pccmd);
		
		dumpPathCollection(pcFromInitEvent);
		assertEquals(1, pcFromInitEvent.size());
		assertEquals(4, pcFromInitEvent.getPath(0).length());
		assertEquals(
			"FlowInitiated->FlowStep1;FlowStep1Completed->FlowStep2;FlowStep2Completed->FlowStep3;FlowTerminated->done;", 
			pcFromInitEvent.getPath(0).dump()
		);
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsJoinEvent() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(1);
		
		dumpFlow(mdslGenModel, 1);
		Process processViewOnGenModel = flow1.processView();
		PathCollection pcFromJoinEvent = processViewOnGenModel.getDownstreamPathsOf(flow1.getEvents().get("AND_FlowStep2Completed_FlowStep3Completed"));
		
		// then	
		assertEquals(1, pcFromJoinEvent.size()); // was 2
		assertEquals(2, pcFromJoinEvent.getPath(0).length()); // was 3
		
		dumpPath(pcFromJoinEvent.getPath(0));
		
		assertEquals(
			"AND_FlowStep2Completed_FlowStep3Completed->FlowStep4;FlowTerminated->done;", 
			pcFromJoinEvent.getPath(0).dump()
		);
	}
	


	@Test
	public void canWorkWithProcessViewPathCollectorsJoinEvent2() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(1);
		// System.out.println(flow1.toString());
		dumpFlow(flow1);
		
		dumpFlow(mdslGenModel, 1);
		Process processViewOnGenModel = flow1.processView();
		assertEquals("FlowStep2", flow1.getCommands().get(1).getName());
		PathCollection pcStartingAtStep2 = processViewOnGenModel.getDownstreamPathsOf(flow1.getCommands().get(1));
		
		// then	
		assertEquals(1, pcStartingAtStep2.size()); // was 2
		dumpPath(pcStartingAtStep2.getPath(0));
		
		assertEquals(3, pcStartingAtStep2.getPath(0).length());
		assertEquals(
			"FlowStep2Completed->AND_FlowStep2Completed_FlowStep3Completed;AND_FlowStep2Completed_FlowStep3Completed->FlowStep4;FlowTerminated->done;", 
			pcStartingAtStep2.getPath(0).dump()
		);

		// System.out.println(pcStartingAtStep2.toString());
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsJoinEvent3() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(1);
		dumpFlow(flow1);
		
		dumpFlow(mdslGenModel, 1);
		Process processViewOnGenModel = flow1.processView();
		assertEquals("FlowStep2", flow1.getCommands().get(1).getName());
		PathCollection pcFromAndTriggerEvent = processViewOnGenModel.getDownstreamPathsOf(flow1.getEvents().get("FlowStep1Completed"));

		// System.out.println(pcFromAndTriggerEvent.getPath(0).dump());
		dumpPathCollection(pcFromAndTriggerEvent);
		
		// then	
		assertEquals(2, pcFromAndTriggerEvent.size());
		assertEquals(4, pcFromAndTriggerEvent.getPath(0).length());
		assertEquals(
			"FlowStep1Completed->FlowStep2;FlowStep2Completed->AND_FlowStep2Completed_FlowStep3Completed;AND_FlowStep2Completed_FlowStep3Completed->FlowStep4;FlowTerminated->done;", 
			pcFromAndTriggerEvent.getPath(0).dump()
		);
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsJoinEvent4() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(1);
		dumpFlow(flow1);
		// dumpFlow(mdslGenModel, 1);
		
		Process processViewOnGenModel = flow1.processView();
		assertEquals("FlowStep2", flow1.getCommands().get(1).getName());
		PathCollection pcFromInitEvent = processViewOnGenModel.getDownstreamPathsOf(flow1.getEvents().get("FlowInitiated"));

		dumpPath(pcFromInitEvent.getPath(0));
		dumpPathCollection(pcFromInitEvent);
		
		// then	
		assertEquals(2, pcFromInitEvent.size());
		assertEquals(
			// "FlowInitiated->FlowStep1;FlowStep1Completed->AND_FlowStep2_FlowStep3;FlowStep2Completed->AND_FlowStep2Completed_FlowStep3Completed;AND_FlowStep2Completed_FlowStep3Completed->FlowStep4;FlowTerminated->done;", 
			"FlowInitiated->FlowStep1;FlowStep1Completed->FlowStep2;FlowStep2Completed->AND_FlowStep2Completed_FlowStep3Completed;AND_FlowStep2Completed_FlowStep3Completed->FlowStep4;FlowTerminated->done;", 
			pcFromInitEvent.getPath(0).dump()
		);
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsEventOr1() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest3a-exclusivechoice-implicitmerge.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow0 = mdslGenModel.getOrchestrationFlows().get(0);
		dumpFlow(flow0);
		// dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = flow0.processView();
		PathCollection pcFromEventFS3C= processViewOnGenModel.getDownstreamPathsOf(flow0.getEvents().get("FlowStep3Completed"));

		dumpPath(pcFromEventFS3C.getPath(0));
		dumpPathCollection(pcFromEventFS3C);
		
		// then	
		assertEquals(1, pcFromEventFS3C.size());
		assertEquals(
			"FlowStep3Completed->FlowStep4;FlowTerminated->done;", 
			pcFromEventFS3C.getPath(0).dump()
		);
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsEventOr2() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest3a-exclusivechoice-implicitmerge.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow0 = mdslGenModel.getOrchestrationFlows().get(0);
		dumpFlow(flow0);
		
		dumpFlow(mdslGenModel, 0);
		Process processViewOnGenModel = flow0.processView();
		PathCollection pcFromEventFS3C= processViewOnGenModel.getDownstreamPathsOf(flow0.getEvents().get("FlowStep1CompletedOptionA"));

		dumpPath(pcFromEventFS3C.getPath(0));
		dumpPathCollection(pcFromEventFS3C);
		
		// then	
		assertEquals(1, pcFromEventFS3C.size());
		assertEquals(
			"FlowStep1CompletedOptionA->FlowStep2;FlowStep2Completed->FlowStep4;FlowTerminated->done;", 
			pcFromEventFS3C.getPath(0).dump()
		);
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsEventOr3() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest3a-exclusivechoice-implicitmerge.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow0 = mdslGenModel.getOrchestrationFlows().get(0);
		dumpFlow(mdslGenModel, 0);
		
		Process processViewOnGenModel = flow0.processView();
		PathCollection pcFromC1= processViewOnGenModel.getDownstreamPathsOf(flow0.getCommands().get(0));
		PathCollection pcFromEI= processViewOnGenModel.getDownstreamPathsOf(flow0.getEvents().get("FlowInitiated"));
		assertEquals("FlowStep1", flow0.getCommands().get(0).getName());

		dumpPath(pcFromEI.getPath(0));
		dumpPathCollection(pcFromEI);
		
		// then	
		assertEquals(2, pcFromEI.size());
		assertEquals(
			"FlowInitiated->FlowStep1;FlowStep1CompletedOptionA->FlowStep2;FlowStep2Completed->FlowStep4;FlowTerminated->done;", 
			pcFromEI.getPath(0).dump()
		);
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsAllOptions4a1() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest4a-alloptionsmodel1.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow5 = mdslGenModel.getOrchestrationFlows().get(4);
		dumpFlow(flow5);
		
		dumpFlow(mdslGenModel, 4);
		Process processViewOnGenModel = flow5.processView();
		PathCollection pc = processViewOnGenModel.getDownstreamPathsOf(flow5.getEvents().get("Event3"));
		 
		dumpPath(pc.getPath(0));
		dumpPathCollection(pc);
		
		// then	
		assertEquals(2, pc.size());
		assertEquals(
			"Event3->Command5;Command5Done->AND_Command3Done_Command4Done_Command5Done;AND_Command3Done_Command4Done_Command5Done->TerminateFlowCommand;",
			pc.getPath(0).dump()
		);
	}
	
	@Test
	public void canWorkWithProcessViewPathCollectorsAllOptions4a2() throws IOException {
		// given
		ServiceSpecification mdsl1 = new MDSLResource(getTestResource("flowtest4a-alloptionsmodel1.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl1);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow5 = mdslGenModel.getOrchestrationFlows().get(4);
		dumpFlow(mdslGenModel, 4);
		
		Process processViewOnGenModel = flow5.processView();
		PathCollection pc = processViewOnGenModel.getDownstreamPathsOf(flow5.getEvents().get("FlowInitiated"));
		 
		// dumpPath(pc.getPath(0));
		dumpPathCollection(pc);
		
		// then	
		assertEquals(7, pc.size()); 
		assertEquals("SampleFlowWithAllOptions", flow5.getName()); // not all options, actually (dep step and?)
		assertEquals(
			// "FlowInitiated->StartFlowCommand;Event1->Command1;AND_Command1Done1_Command1Done2_Command2Done->TerminateFlowCommand;", 
			// "FlowInitiated->StartFlowCommand;Event1->AND_Command1_Command2;Command1Done1->AND_Command1Done1_Command1Done2_Command2Done;AND_Command1Done1_Command1Done2_Command2Done->TerminateFlowCommand;",
			"FlowInitiated->StartFlowCommand;Event1->Command1;Command1Done1->AND_Command1Done1_Command1Done2_Command2Done;AND_Command1Done1_Command1Done2_Command2Done->TerminateFlowCommand;",
			pc.getPath(0).dump()
		);
	}
	
	@Test
	public void canIdentifyInitationAndTerminationStatiAndCommandTriggersViaProcessView() throws IOException {
		// given
		ServiceSpecification mdsl = new MDSLResource(getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl")).getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);

		// when
		MDSLGeneratorModel mdslGenModel = converter.convert();
		Flow flow1 = mdslGenModel.getOrchestrationFlows().get(1);
		dumpFlow(mdslGenModel, 1);
		Process processViewOnGenModel = flow1.processView();
		
		// then
		assertEquals(6, flow1.getEvents().size());
		assertFalse(processViewOnGenModel.participatesInJoin(flow1.getEvents().get("FlowInitiated")));
		assertFalse(processViewOnGenModel.participatesInJoin(flow1.getEvents().get("FlowStep1Completed")));
		assertTrue(processViewOnGenModel.participatesInJoin(flow1.getEvents().get("FlowStep2Completed")));
		assertTrue(processViewOnGenModel.participatesInJoin(flow1.getEvents().get("FlowStep3Completed")));
		Event joinEvent = flow1.getEvents().get("AND_FlowStep2Completed_FlowStep3Completed");
		assertTrue(joinEvent.isJoin());
		assertFalse(processViewOnGenModel.participatesInJoin(joinEvent));
		assertFalse(processViewOnGenModel.participatesInJoin(flow1.getEvents().get("FlowTerminated")));
		
		assertTrue(processViewOnGenModel.initiatesFlow(flow1.getEvents().get("FlowInitiated")));
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getEvents().get("AND_FlowStep2Completed_FlowStep3Completed")));
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getEvents().get("FlowStep3Completed")));
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getEvents().get("FlowTerminated")));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getEvents().get("FlowInitiated")));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getEvents().get("AND_FlowStep2Completed_FlowStep3Completed")));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getEvents().get("FlowStep3Completed")));
		assertTrue(processViewOnGenModel.terminatesFlow(flow1.getEvents().get("FlowTerminated")));

		assertEquals(5, flow1.getCommands().size());
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getCommands().get(0)));
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getCommands().get(1)));
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getCommands().get(2)));
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getCommands().get(3)));
		assertFalse(processViewOnGenModel.initiatesFlow(flow1.getCommands().get(4)));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getCommands().get(0)));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getCommands().get(1)));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getCommands().get(2)));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getCommands().get(3)));
		assertFalse(processViewOnGenModel.terminatesFlow(flow1.getCommands().get(4)));
		
		assertEquals(1, (processViewOnGenModel.triggeredBy(flow1.getCommands().get(0))).size());
		assertEquals(0, (processViewOnGenModel.triggeredBy(flow1.getCommands().get(1))).size()); // TODO part of composite command only
		assertEquals(0, (processViewOnGenModel.triggeredBy(flow1.getCommands().get(2))).size()); // TODO part of composite command only
		assertEquals(1, (processViewOnGenModel.triggeredBy(flow1.getCommands().get(3))).size());
		assertEquals("AND_FlowStep2_FlowStep3", flow1.getCommands().get(3).getName()); // triggered by AND join event
		assertEquals(1, (processViewOnGenModel.triggeredBy(flow1.getCommands().get(3))).size());
		Event andCommandTrigger = (Event) processViewOnGenModel.triggeredBy(flow1.getCommands().get(3)).toArray()[0];
		assertEquals("FlowStep1Completed", andCommandTrigger.getName());
		assertEquals("FlowStep4", flow1.getCommands().get(4).getName()); // triggered by AND join event
		assertEquals(1, (processViewOnGenModel.triggeredBy(flow1.getCommands().get(4))).size());
		Event andEvent = (Event) processViewOnGenModel.triggeredBy(flow1.getCommands().get(4)).toArray()[0];
		assertEquals("AND_FlowStep2Completed_FlowStep3Completed", andEvent.getName());
		
		// System.out.println("TestFlow paths: " + processViewOnGenModel.dumpAllPaths());
		dumpProcessView(processViewOnGenModel);
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
	
	private void dumpFlow(Flow flow) {
		if(MDSLLogger.logLevel<2) {
			return;
		}
		
		String flowDump = flow.toString();
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
		// System.out.println(pc.toString());
		System.out.println(pc.dump(true));
		System.out.println("----------------------------------------------------------------------------");
	}
	
	private void dumpPath(Path path) {
		if(MDSLLogger.logLevel<2) {
			return;
		}
		path.dump();	
	}
	
	@Override
	protected String testDirectory() {
		return "/test-data/flowgenmodel-tests/";
	}
}

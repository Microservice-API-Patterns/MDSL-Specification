package io.mdsl.jaamsim;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.junit.jupiter.api.Test;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.jaamsim.JaamSimToMDSL;

public class JaamSim2MDSLConversionTest {

	private static final String CONFIG_FILE_SUFFIX = ".cfg";
	private static final String TEST_DATA_PATH = "src/test/resources/"; // TODO adopt as needed

	@Test
	public void canConvertJaamSimTestCase1SequentialFlow() {
		// given
		String testFileName = TEST_DATA_PATH + "flowtest1-jaamsim" + CONFIG_FILE_SUFFIX;
		JaamSimToMDSL j2m = new JaamSimToMDSL(testFileName);

		// when
		ServiceSpecification mdsl = j2m.convert();

		// then
		assertEquals(7, mdsl.getTypes().size()); // 1+3+1 for flow start/steps/end, 2 in template
		assertEquals(7, mdsl.getEvents().size()); // was 6: 4 in MDSL input, 1 added for sim entity, 1 in template
		assertEquals(6, mdsl.getCommands().size()); // was 6: 3 flow steps, 1 entity generator, 1 in template
		
		assertEquals(1, mdsl.getOrchestrations().size()); 
		assertEquals(9, mdsl.getOrchestrations().get(0).getSteps().size()); // was 7: 3+3 in MDSL input, 1 added for flow start
		assertEquals(0, countAndedEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(0, countOredEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(0, countJoinEventsInFlow(mdsl.getOrchestrations().get(0)));
		
		assertEquals(1, mdsl.getScenarios().size()); 
		assertEquals(1, mdsl.getScenarios().get(0).getStories().size()); 
		
		assertEquals(2, mdsl.getContracts().size()); 
		assertEquals(4, ((EndpointContract) mdsl.getContracts().get(0)).getOps().size()); // servers, statistics	
		assertEquals(4, ((EndpointContract) mdsl.getContracts().get(1)).getOps().size()); // sim entity IHR CRUD
		assertEquals("SequentialFlowStartingWithEventSimEntityResource", ((EndpointContract) mdsl.getContracts().get(1)).getName());
		assertEquals("createSequentialFlowStartingWithEventSimEntity", ((EndpointContract) mdsl.getContracts().get(1)).getOps().get(0).getName());
	}

	@Test
	public void canConvertJaamSimTestCase2ParallelSplit() {
		// given
		String testFileName = TEST_DATA_PATH + "flowtest2-jaamsim" + CONFIG_FILE_SUFFIX;
		JaamSimToMDSL j2m = new JaamSimToMDSL(testFileName);

		// when
		ServiceSpecification mdsl = j2m.convert();

		// then
		assertEquals(1, mdsl.getOrchestrations().size()); 
		assertEquals(1, countAndedEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(0, countOredEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(1, countJoinEventsInFlow(mdsl.getOrchestrations().get(0)));
		
		assertEquals(2, mdsl.getContracts().size()); 
		assertEquals(6, ((EndpointContract) mdsl.getContracts().get(0)).getOps().size()); // 4 + 1 + 1
		assertEquals(4, ((EndpointContract) mdsl.getContracts().get(1)).getOps().size()); // sim entity IHR CRUD
	}
	
	@Test
	public void canConvertJaamSimTestCase3a() {
		// given
		String testFileName = TEST_DATA_PATH + "flowtest3a-jaamsim" + CONFIG_FILE_SUFFIX;
		JaamSimToMDSL j2m = new JaamSimToMDSL(testFileName);

		// when
		ServiceSpecification mdsl = j2m.convert();
		try {
			j2m.writeToFile(mdsl, TEST_DATA_PATH + "flowtest3a-jaamsim.mdsl");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// then
		assertEquals(1, mdsl.getOrchestrations().size()); 
		assertEquals(16, mdsl.getOrchestrations().get(0).getSteps().size()); // was 14: 7 DEP steps, 7 CIS steps
		assertEquals(0, countAndedEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(1, countOredEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(0, countJoinEventsInFlow(mdsl.getOrchestrations().get(0)));
		
		assertEquals(2, mdsl.getContracts().size()); 
		assertEquals(7, ((EndpointContract) mdsl.getContracts().get(0)).getOps().size()); // TODO 5 + 1 + 1
		assertEquals(4, ((EndpointContract) mdsl.getContracts().get(1)).getOps().size()); // sim entity IHR CRUD
	}

	@Test
	public void canConvertPSOADDemoFlow() {
		// note that the PSOAD demo works with two MDSLs, step 1 and step 5 (this test)
		
		// given
		String testFileName = TEST_DATA_PATH + "process-driven-SOAD-final-jaamsim" + CONFIG_FILE_SUFFIX;

		// when
		JaamSimToMDSL j2m = new JaamSimToMDSL(testFileName);
		ServiceSpecification mdsl = j2m.convert();

		// then
		
		assertEquals(1, mdsl.getOrchestrations().size()); 
		assertEquals(1, countAndedEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(1, countOredEventsInFlow(mdsl.getOrchestrations().get(0)));
		assertEquals(1, countJoinEventsInFlow(mdsl.getOrchestrations().get(0)));
		
		assertEquals(1, mdsl.getScenarios().size()); 
		assertEquals(1, mdsl.getScenarios().get(0).getStories().size()); 
		
		assertEquals(2, mdsl.getContracts().size()); 
		assertEquals(13, ((EndpointContract) mdsl.getContracts().get(0)).getOps().size());
		assertEquals(4, ((EndpointContract) mdsl.getContracts().get(1)).getOps().size()); // sim entity IHR
	}
	
	private Integer countAndedEventsInFlow(Orchestration orchestration) {
		int splitCount = 0;
		for(FlowStep step : orchestration.getSteps()) {
			// not counting ece steps ending with + (not generated)
			if(step.getDepStep()!=null) {
				if(step.getDepStep().getEventProduction().getMep()!=null) {
					splitCount++;
				}
			}
		}
		return splitCount;
	}
	
	private Integer countOredEventsInFlow(Orchestration orchestration) {
		int choiceCount = 0;
		for(FlowStep step : orchestration.getSteps()) {
			// not counting ece steps ending with + (not generated)
			if(step.getDepStep()!=null) {
				if(step.getDepStep().getEventProduction().getIaep()!=null||step.getDepStep().getEventProduction().getEaep()!=null) {
					choiceCount++;
				}
			}
		}
		return choiceCount;
	}
	
	private Integer countJoinEventsInFlow(Orchestration orchestration) {
		int joinCount = 0;
		for(FlowStep step : orchestration.getSteps()) {
			// not counting ece steps starting with join (not generated)
			if(step.getCisStep()!=null) {
				EList<EventType> events = step.getCisStep().getEvents();
				if(events.size()>1) {
					joinCount++;
				}
			}
		}
		return joinCount;
	}
}

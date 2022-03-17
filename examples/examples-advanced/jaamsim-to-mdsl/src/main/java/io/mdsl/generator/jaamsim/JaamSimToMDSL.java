package io.mdsl.generator.jaamsim;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import io.mdsl.APIDescriptionStandaloneSetup;
import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.Action;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DefaultValue;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EitherCommandOrOperation;
import io.mdsl.apiDescription.EitherCommandOrOperationInvokation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.EventProduction;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.InclusiveAlternativeEventProduction;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.MultipleEventProduction;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleCommandInvokation;
import io.mdsl.apiDescription.SingleEventProduction;
import io.mdsl.apiDescription.StateTransition;
import io.mdsl.apiDescription.StoryObject;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.transformations.FlowTransformations;
import io.mdsl.transformations.OperationTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;

// Known limitation: control flow mapping is not complete (for instance, see [W]arnings in console output)
// 
// TODO (L) map more concepts from Table 16-1 ("Process Flow Palette") in Users Guide
// [O, R] could map ExpressionEntity to Computation Function
// [O, R] could map EntityContainer to ParameterTree; leverage Arrays and Maps in Attribute Definition Lists for data type cardinalities
// [O, R] could map EntityGate to AND join event with aggregating command behind it
// [Q] map EntitySignal and EntityDelay?

// TODO (bug?) should use flow name (not API descr.) in m2j and find it here in j2m
// TODO m2j: check order of lines 2 and 4 in layout

public class JaamSimToMDSL {

	private static final String API_DESCRIPTION_SUFFIX = "Contract"; // "APIDescription";
	private static final String COMMAND_SUFFIX = ""; // "Command";
	private static final String EMITTER_SUFFIX = ""; // "Emitter";
	private static final String TRIGGER_SUFFIX = ""; // "Trigger";
	private static final String REFERENCE_SUFFIX = "Reference";
	
	private static final String MDSL_TOOLS_SKELETON_FILE = "src/main/resources/MDSLTools-SkeletonOfAPIWithFlow.mdsl";

	private JaamSimConfigurationWrapper jsmWrapper;  
		
	public static void main(String[] args) {
		if(args.length==1) {
			String jaamSimConfigurationFile = args[0];
			JaamSimToMDSL converter = new JaamSimToMDSL(jaamSimConfigurationFile);
			ServiceSpecification targetMDSL = converter.convert();
			try {
				converter.writeToFile(targetMDSL, jaamSimConfigurationFile.replace(".cfg",  "") + ".mdsl");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
		else {
			System.err.println("JaamSim2MDSL requires a parameter: JaamSim2MDSL <path to input (configuration file)>");
		}
	}

	public JaamSimToMDSL(String configFileName) {
		this.jsmWrapper= new JaamSimConfigurationWrapper(configFileName);
	}

	public ServiceSpecification convert() {
		try {
			ServiceSpecification apiDescription = new MDSLResource(getTestResource(MDSL_TOOLS_SKELETON_FILE)).getServiceSpecification();

			String description = findDescriptionInConfigFile("Simulation Description");  
			apiDescription.setDescription(description);

			String flowName = jsmWrapper.findModelNameInConfigFile();  
			apiDescription.setName(JaamSimConfigurationWrapper.normalizeName(flowName) + API_DESCRIPTION_SUFFIX);

			// convert SimEntities to data types
			List<String> simEntities = jsmWrapper.findObjectDefinitionInConfigFile("SimEntity");
			convertSimEntitiesToMDSLTypes(apiDescription, simEntities);

			// get sample flow from sample/template model and create MDSL flow and scenario with story for it
			IntegrationScenario scenario = addStoryforSimulationFlow(apiDescription, flowName, description, simEntities);
			Orchestration flow = apiDescription.getOrchestrations().get(0);
			flow.setName(flowName);
			flow.setScenario(scenario);

			EndpointContract ec = createEndpointContract(apiDescription);

			// convert resources and entity processors to endpoint operations (less frequently used concepts in examples) 
			List<String> entityProcessors = jsmWrapper.findObjectDefinitionInConfigFile("EntityProcessor");
			convertEntityProcessorsToEndpointOperations(ec, entityProcessors);
			List<String> resources = jsmWrapper.findObjectDefinitionInConfigFile("Resource");
			convertResourcesToEndpointOperations(ec, resources);

			// convert simEntities to IHR with CRUD operations 			
			convertSimEntitiesToInformationHolderResources(apiDescription, simEntities);
						
			// convert entity generators to init events
			List<String> entityGenerators = jsmWrapper.findObjectDefinitionInConfigFile("EntityGenerator");
			convertToMDSLCommandTypesAndFlowSteps(flow, entityGenerators);

			// convert servers to command invocation steps and command types:
			List<String> servers = jsmWrapper.findObjectDefinitionInConfigFile("Server");
			convertToMDSLCommandTypesAndFlowSteps(flow,servers);
			convertServersToEndpointOperations(ec, servers, "run");
			
			// added for test case n:
			// convert servers to command invocation steps and command types:
			List<String> assigns = jsmWrapper.findObjectDefinitionInConfigFile("Assign");
			convertToMDSLCommandTypesAndFlowSteps(flow, assigns);
			
			// convert servers to command invocation steps and command types:
			List<String> conveyors= jsmWrapper.findObjectDefinitionInConfigFile("EntityConveyor");
			convertToMDSLCommandTypesAndFlowSteps(flow, conveyors);

			// convert queues to domain event emission steps and event types (not done here)
			List<String> queues = jsmWrapper.findObjectDefinitionInConfigFile("Queue");
			// TODO (M) bring back
			// convertQueuesToMDSLEvents(flow, queues);  

			// convert control flow constructs: Branch, Duplicate, Combine
			List<String> branches = jsmWrapper.findObjectDefinitionInConfigFile("Branch");
			convertBranchesToMDSLOrSteps(flow, branches);

			List<String> duplicates = jsmWrapper.findObjectDefinitionInConfigFile("Duplicate");
			convertDuplicatesToMDSLAndSteps(flow, duplicates);

			List<String> combines = jsmWrapper.findObjectDefinitionInConfigFile("Combine");
			convertCombinesToMDSLJoinEventsAndSteps(flow, combines);
			
			// convert statistics to flow steps and command types:
			List<String> statistics = jsmWrapper.findObjectDefinitionInConfigFile("Statistics");
			convertStatisticsToMDSLComputationFunction(ec, statistics);
			convertToMDSLCommandTypesAndFlowSteps(flow, statistics);

			// convert entity sinks to termination events
			List<String> entitySinks = jsmWrapper.findObjectDefinitionInConfigFile("EntitySink");
			convertToMDSLEvents(flow, entitySinks);
			
			return apiDescription;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		
	}

	// ** converters

	private static IntegrationScenario addStoryforSimulationFlow(ServiceSpecification apiDescription, 
			String flowName, String description, List<String> simEntities) {
		IntegrationScenario is = ApiDescriptionFactory.eINSTANCE.createIntegrationScenario();
		is.setName(JaamSimConfigurationWrapper.normalizeName(flowName)+"Scenario");
		IntegrationStory storyForFlow = ApiDescriptionFactory.eINSTANCE.createIntegrationStory();
		storyForFlow.setName(flowName+"Story");
		Action action = ApiDescriptionFactory.eINSTANCE.createAction();
		action.setPlainAction("run " + description.replaceAll("\'", "") + " for");
		storyForFlow.setAction(action);
		EList<StoryObject> objectList = storyForFlow.getOn(); 
		for(String simEntity : simEntities) {
			StoryObject so = ApiDescriptionFactory.eINSTANCE.createStoryObject();
			so.setObject(simEntity);
			// TODO (L) cannot set preposition, no prefix in grammar rule (ok)
			objectList.add(so);
		}

		storyForFlow.setCondition("SimulationInputsArrive");
		storyForFlow.setClient("SimulationModeller");
		storyForFlow.setOutcome("SimulationOutputs");

		is.getStories().add(storyForFlow);
		apiDescription.getScenarios().add(is);

		return is;
	}
	
	private static EndpointContract createEndpointContract(ServiceSpecification mdsl) {
		Orchestration flow = mdsl.getOrchestrations().get(0); // assumes sample template has at least one, uses first
		FlowTransformations fts = new FlowTransformations();
		fts.addEndpointTypeSupportingFlow(flow);
		EndpointContract ec = (EndpointContract) mdsl.getContracts().get(0); // sample template now has one contract
		return ec;
	}
	
	private static void convertEntityProcessorsToEndpointOperations(EndpointContract ec, List<String> entityProcessors) {
		TypeReference sampleDataTypeReference = createTypeReference((ServiceSpecification)ec.eContainer(), "SampleEntityData", null); // type is in template

		for(String entityProcessor : entityProcessors) {
			Operation entityProcessorOperation = OperationTransformationHelpers.createUpdateOperation(entityProcessor, sampleDataTypeReference);
			entityProcessorOperation.setName("processEntity" + JaamSimConfigurationWrapper.normalizeName(entityProcessor));
			ec.getOps().add(entityProcessorOperation);
		}
	}

	private static void convertResourcesToEndpointOperations(EndpointContract ec, List<String> resources) {
		TypeReference sampleDataTypeReference = createTypeReference((ServiceSpecification)ec.eContainer(), "SimPartAttributes", null); // type is in template;

		for(String resource : resources) {
			Operation entityProcessorOperation = OperationTransformationHelpers.createUpdateOperation(JaamSimConfigurationWrapper.normalizeName(resource), sampleDataTypeReference);
			entityProcessorOperation.setName("workOnResource" + resource);
			ec.getOps().add(entityProcessorOperation);
		}
	}
	
	private void convertServersToEndpointOperations(EndpointContract ec, List<String> elementNames, String operationNamePrefix) {		
		for(String element : elementNames) {
			ElementStructure es = DataTypeTransformations.wrapAtomicParameterAsElementStructure(DataTypeTransformations.createAtomicDataParameter("placeholder", "string"));
			DataContract elementType = (DataContract) DataTypeTransformations.findOrCreateDataType((ServiceSpecification)ec.eContainer(), JaamSimConfigurationWrapper.normalizeName(element), es);
			TypeReference elementTypeReference = ApiDescriptionFactory.eINSTANCE.createTypeReference();
			elementTypeReference.setName(JaamSimConfigurationWrapper.normalizeName(element) + REFERENCE_SUFFIX);
			elementTypeReference.setDcref(elementType);
			Operation operation = OperationTransformationHelpers.createUpdateOperation(JaamSimConfigurationWrapper.normalizeName(element), elementTypeReference);
			operation.setName(operationNamePrefix + JaamSimConfigurationWrapper.normalizeName(element));
			ec.getOps().add(operation);
			
			handleStateAssignment(element, operation);
			handleAttributeDefinitionList(element, operation);
			// TODO (M) not yet: handleCustomOutputList(element, operation);
			
			List<String> nextComponents = jsmWrapper.findAttributeValueInEntity(element, "NextComponent");
			if(nextComponents!=null&&nextComponents.size()>=1) { 
				if(nextComponents.size()>1) {
					System.err.println("[W] More than one nextComponent found for " + element);
				}
				String ncType = jsmWrapper.getSimulationObjectType(nextComponents.get(0));
				if("Queue".equals(ncType)) {
					Event eventForQueue = ApiDescriptionFactory.eINSTANCE.createEvent();
					EventType et = TransformationHelpers.findOrCreateUniqueEventType((ServiceSpecification)ec.eContainer(), JaamSimConfigurationWrapper.normalizeName(nextComponents.get(0)) + TRIGGER_SUFFIX);
					eventForQueue.setType(et);
					operation.getEvents().add(eventForQueue);
				}
			}
		}
	}

	private List<String> handleStateAssignment(String element, Operation operation) {
		List<String> states = jsmWrapper.findAttributeValueInEntity(element, "StateAssignment");
		if(states!=null&&states.size()>=1) {
			if(states.size()>1) {
				System.err.println("[W] More than one state assignment found for " + element);
			}
			StateTransition stv = ApiDescriptionFactory.eINSTANCE.createStateTransition();
			stv.setFrom("anyState");
			stv.setTo(states.get(0));
			operation.setSt(stv);
		}
		return states;
	}
	
	private void handleAttributeDefinitionList(String element, Operation operation) {
		// TODO ADL support
	}
	
	private void handleCustomOutputList(String element, Operation operation) {
		// TODO COL support
	}

	private void convertSimEntitiesToInformationHolderResources(ServiceSpecification mdsl, List<String> simEntities) {
		// alternative mapping/transformation option: story with CRUD action, story to endpoint quick fix  
		for(String simEntity : simEntities) {	
			EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(mdsl, JaamSimConfigurationWrapper.normalizeName(simEntity) + "Resource");
			ec.setPrimaryRole("INFORMATION_HOLDER_RESOURCE");
			ec.getOtherRoles().add("COLLECTION_RESOURCE");
			
			AtomicParameter ap = DataTypeTransformations.createAtomicDataParameter(JaamSimConfigurationWrapper.normalizeName(simEntity) + "Type", "int");
			ElementStructure ts = DataTypeTransformations.wrapAtomicParameterAsElementStructure(ap);
			DataContract simTypeReference = DataTypeTransformations.findOrCreateDataType(mdsl, JaamSimConfigurationWrapper.normalizeName(simEntity), ts); // must be there
			TypeReference simEntityDTOReference = DataTypeTransformations.createTypeReference(simTypeReference);
			Operation cop = OperationTransformationHelpers.createCreateOperation("create" + JaamSimConfigurationWrapper.normalizeName(simEntity), simEntityDTOReference);
			ec.getOps().add(cop);
			Operation rop = OperationTransformationHelpers.createReadOperation("read" + JaamSimConfigurationWrapper.normalizeName(simEntity), "entityId", EcoreUtil.copy(simEntityDTOReference));
			ec.getOps().add(rop);
			Operation uop = OperationTransformationHelpers.createUpdateOperation("udpate" + JaamSimConfigurationWrapper.normalizeName(simEntity), EcoreUtil.copy(simEntityDTOReference));
			ec.getOps().add(uop);
			Operation dop = OperationTransformationHelpers.createDeleteOperation("delete" + JaamSimConfigurationWrapper.normalizeName(simEntity));
			ec.getOps().add(dop);

			mdsl.getContracts().add(ec);
		}
	}

	private void convertStatisticsToMDSLComputationFunction(EndpointContract ec, List<String> statistics) {
		for(String statisticsItem : statistics) {
			AtomicParameter ap = DataTypeTransformations.createAtomicDataParameter(JaamSimConfigurationWrapper.normalizeName(statisticsItem) + "Type", "int");
			ElementStructure ts = DataTypeTransformations.wrapAtomicParameterAsElementStructure(ap);
			TypeReference statisticsDataTypeReference = createTypeReference((ServiceSpecification)ec.eContainer(), JaamSimConfigurationWrapper.normalizeName(statisticsItem) + "Data", ts);
			Operation statisticsOperation = OperationTransformationHelpers.createReadOperation(JaamSimConfigurationWrapper.normalizeName(JaamSimConfigurationWrapper.normalizeName(statisticsItem)), "flowEntitities", statisticsDataTypeReference);
			OperationResponsibility cfRespo = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
			cfRespo.setCf("COMPUTATION_FUNCTION");
			Cardinality set = ApiDescriptionFactory.eINSTANCE.createCardinality();
			set.setAtLeastOne("!");
			statisticsOperation.getRequestMessage().getPayload().getNp().getAtomP().setCard(set);
			statisticsOperation.setResponsibility(cfRespo);
			statisticsOperation.setName("calculate" + JaamSimConfigurationWrapper.normalizeName(statisticsItem));
			ec.getOps().add(statisticsOperation);
		}
	}
	
	private static TypeReference createTypeReference(ServiceSpecification mdsl, String typeName, ElementStructure typeStructure) {
		TypeReference dataTypeReference = ApiDescriptionFactory.eINSTANCE.createTypeReference();
		DataContract newDataType = DataTypeTransformations.findOrCreateDataType(mdsl, JaamSimConfigurationWrapper.normalizeName(typeName), typeStructure); // must exist in sample input/template
		dataTypeReference.setDcref(newDataType);
		dataTypeReference.setName(DataTypeTransformationHelpers.decapitalizeName(JaamSimConfigurationWrapper.normalizeName(typeName)));
		return dataTypeReference;
	}

	// ** accessors

	private String findDescriptionInConfigFile(String elementName) throws IOException {
		return jsmWrapper.findFirstDefinitionInConfigFile(elementName);
	}

	private void convertSimEntitiesToMDSLTypes(ServiceSpecification mdsl, List<String> simEntities) {
		// TODO ADL support
	}

	// TODO (L) group the events and commands by type/mapping rule (MDSL has that capability)
	

	// ** creation helpers

	private Orchestration convertToMDSLCommandTypesAndFlowSteps(Orchestration flow, List<String> modelObjectList) throws IOException {
		ServiceSpecification mdsl = (ServiceSpecification) flow.eContainer();

		for(String modelObject : modelObjectList) {

			// step 1: CIS from waitQueue 

			// do not create a step for a new event, but only for those in WaitQueue
			List<String> eventName = jsmWrapper.findAttributeValueInEntity(modelObject, "WaitQueue");
			
			// servers have exactly one wait queue; other elements do not have one (no step generated in that case)
			if(eventName!=null&&eventName.size()==1) {
				createSimpleCisStep(
						flow, 
						mdsl, 
						JaamSimConfigurationWrapper.normalizeName(eventName.get(0)), 
						JaamSimConfigurationWrapper.normalizeName(JaamSimConfigurationWrapper.normalizeName(modelObject) + COMMAND_SUFFIX));
			}
			else if(eventName!=null&&eventName.size()==0) {
				System.out.println("[I] Empty WaitQueue found for " + modelObject);
			}
			else if(eventName!=null&&eventName.size()>=1) {
				System.err.println("[W] More than one WaitQueue found for " + modelObject);
			}
			else { // must be null
				System.out.println("[I] No WaitQueue found for " + modelObject);
			}
			
			// step 2: DEP or DEP/CIS pair from next component
			List<String> nextComponentNames = jsmWrapper.findAttributeValueInEntity(modelObject, "NextComponent");
			// servers only have one wait queue at max.; other elements might not have one (no step generated in that case)

			if(nextComponentNames.size()==1) {
				String componentType = jsmWrapper.getSimulationObjectType(nextComponentNames.get(0));
				String normalizedComponentName = JaamSimConfigurationWrapper.normalizeName(nextComponentNames.get(0));

				if("Queue".equals(componentType)) {
					createSimpleDepStep(flow, mdsl, JaamSimConfigurationWrapper.normalizeName(modelObject), normalizedComponentName);
				}
				else if("Branch".equals(componentType)) { 
					createSimpleDepStep(flow, mdsl, JaamSimConfigurationWrapper.normalizeName(modelObject), normalizedComponentName);			
				}
				// "Combines" cannot be reached here has WaitQueueList as input
				else if("Duplicate".equals(componentType)) { 
					createSimpleDepStep(flow, mdsl, JaamSimConfigurationWrapper.normalizeName(modelObject), normalizedComponentName);
				}
				else if("Assign".equals(componentType)) {
					createSimpleDepStep(flow, mdsl, JaamSimConfigurationWrapper.normalizeName(modelObject), normalizedComponentName);
				}
				else if("EntityConveyor".equals(componentType)) {
					createSimpleDepStep(flow, mdsl, JaamSimConfigurationWrapper.normalizeName(modelObject), normalizedComponentName);
				}
				else if("Statistics".equals(componentType)) {
					createSimpleDepStep(flow, mdsl, JaamSimConfigurationWrapper.normalizeName(modelObject), normalizedComponentName + "Initiated");
					createEventAndIntermediateCisStep(flow, mdsl, normalizedComponentName, "Initiated");
				}
				else if("EntitySink".equals(componentType)) {
					createSimpleDepStep(flow, mdsl, JaamSimConfigurationWrapper.normalizeName(modelObject), normalizedComponentName);
				}
				else {
					System.out.println("[I] Not creating a DEP step for " + modelObject + ", nextComponent: " + normalizedComponentName + " type is " + componentType);
				}
			}
			else if((nextComponentNames.size()==0)) {
				System.out.println("[I] " + modelObject + " does not have a next component.");
			}
			else {
				String candidateServerType = jsmWrapper.getSimulationObjectType(modelObject);
				System.err.println("[W] Not creating a DEP step for " + modelObject + " (of type " + candidateServerType + "), has multiple NextComponent entries: " + nextComponentNames.size());
				for(String nextComponentElement : nextComponentNames) {
					System.err.print(nextComponentElement + ", ");
				}
			}
		}

		mdsl.getOrchestrations().add(flow);
		return flow;
	}

	private void createSimpleCisStep(Orchestration flow, ServiceSpecification mdsl, String from, String to) {

		// copied from stepForStory, could use shared helper (in FlowTransformations?): 
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		CommandInvokationStep cis = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();
		EitherCommandOrOperationInvokation coi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
		CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
		SingleCommandInvokation sci = ApiDescriptionFactory.eINSTANCE.createSingleCommandInvokation();

		// add event type (on service specification level), suggesting a name that might have to be modified
		EventType de = TransformationHelpers.findOrCreateUniqueEventType(mdsl, from);

		// add event reference to flow step
		cis.getEvents().add(de);

		// add command type (on service specification level)
		CommandType ct = TransformationHelpers.findOrCreateUniqueCommandType(
				mdsl, DataTypeTransformationHelpers.replaceSpacesWithUnderscores(to));

		// finally, prepare command and flow step
		sci.getCommands().add(ct);
		ci.setSci(sci);
		coi.setCi(ci);
		cis.setAction(coi);
		fs.setCisStep(cis);

		flow.getSteps().add(fs);
	}

	private void createSimpleDepStep(Orchestration flow, ServiceSpecification mdsl, String from, String to) {
		FlowStep fs2 = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		DomainEventProductionStep depStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep();
		EitherCommandOrOperation eco = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
		CommandType command = TransformationHelpers.findOrCreateUniqueCommandType(
				mdsl, DataTypeTransformationHelpers.replaceSpacesWithUnderscores(from));
		eco.setCommand(command);
		depStep.setAction(eco);
		EventProduction ep = ApiDescriptionFactory.eINSTANCE.createEventProduction();
		SingleEventProduction sep = ApiDescriptionFactory.eINSTANCE.createSingleEventProduction();
		EventType et = TransformationHelpers.findOrCreateUniqueEventType(mdsl, to);
		sep.getEvents().add(et);
		ep.setSep(sep);
		depStep.setEventProduction(ep);
		fs2.setDepStep(depStep);
		flow.getSteps().add(fs2);
	}
	
	// note: queues for regular are created in "convertSimulationObjectsToEvents" right now (bring back here?)		
	private void convertToMDSLEvents(Orchestration flow, List<String> elementList) {		
		ServiceSpecification mdsl = (ServiceSpecification) flow.eContainer();
		for(String modelElement : elementList) {
			System.out.println("[D] Processing " + modelElement + " (event creation)");
			convertSingleSimulationObjectToEvent(mdsl, modelElement);
			// TODO (L) also create an IHR for the queue? An AsyncMDSL channel?
		}
	}

	private void convertBranchesToMDSLOrSteps(Orchestration flow, List<String> branches) throws IOException {
		ServiceSpecification mdsl = (ServiceSpecification) flow.eContainer();

		for(String branchName : branches) {			
			List<String> branchComponents = jsmWrapper.findNextComponentsOfEntity(branchName);
			// TODO (L) could also get Choice attribute and map it into an evaluation step/operation

			System.out.println("[D] Processing Branch element " + branchName + ", has " + /* branchInputs.size() + " input(s) and " */ + branchComponents.size() + " outputs.");

			InclusiveAlternativeEventProduction xorBranches = ApiDescriptionFactory.eINSTANCE.createInclusiveAlternativeEventProduction();
			for(String nextChoice : branchComponents) {
				System.out.println("[D] Processing next choice in " + branchName + ": " + nextChoice);
				String componentType = jsmWrapper.getSimulationObjectType(nextChoice);
				if("Queue".equals(componentType)) {
					EventType eventForNextChoice = convertSingleSimulationObjectToEvent(mdsl, nextChoice);
					xorBranches.getEvents().add(eventForNextChoice);
				}
				// TODO (M) extract helper method handling identical remaining cases
				else if("Server".equals(componentType)) {
					System.out.println("[I] Handle type of choice in branch: " + componentType);
					EventType choiceEvent = createEventAndIntermediateCisStep(flow, mdsl, nextChoice, "Chosen");
					xorBranches.getEvents().add(choiceEvent);
				}
				else if("Branch".equals(componentType)) {
					System.out.println("[I] Handle type of choice in branch: " + componentType);
					EventType choiceEvent = createEventAndIntermediateCisStep(flow, mdsl, nextChoice, "Chosen");
					xorBranches.getEvents().add(choiceEvent);
				}
				else if("Duplicate".equals(componentType)) {
					System.out.println("[I] Handle type of choice in branch: " + componentType);
					EventType choiceEvent = createEventAndIntermediateCisStep(flow, mdsl, nextChoice, "Chosen");
					xorBranches.getEvents().add(choiceEvent);
				}
				else if("Statistics".equals(componentType)) {
					System.out.println("[I] TODO handle type of choice in branch: " + componentType);
					EventType choiceEvent = createEventAndIntermediateCisStep(flow, mdsl, nextChoice, "Chosen");
					xorBranches.getEvents().add(choiceEvent);
				}
				// added for test case n:
				else if("Assign".equals(componentType)) {
					System.out.println("[I] TODO handle type of choice in branch: " + componentType);
					EventType choiceEvent = createEventAndIntermediateCisStep(flow, mdsl, nextChoice, "Chosen");
					xorBranches.getEvents().add(choiceEvent);
				}
				else if("EntityConveyor".equals(componentType)) {
					System.out.println("[I] TODO handle type of choice in branch: " + componentType);
					EventType choiceEvent = createEventAndIntermediateCisStep(flow, mdsl, nextChoice, "Chosen");
					xorBranches.getEvents().add(choiceEvent);
				}
				else if("EntitySink".equals(componentType)) {
					System.out.println("[I] handle type of choice in branch: " + componentType);
					EventType choiceEvent = createEventAndIntermediateCisStep(flow, mdsl, nextChoice, "Chosen");
					xorBranches.getEvents().add(choiceEvent);
				}
				else {
					System.err.println("[W] Unexpected type of choice in branch: " + componentType);
				}
			}

			if(xorBranches.getEvents().size() < 2 ) {
				System.err.println("[W] Wrong number of event production elements");
			}
			else {
				EventProduction ep = ApiDescriptionFactory.eINSTANCE.createEventProduction();
				ep.setIaep(xorBranches);
				DomainEventProductionStep depStep = createDepStepForGatewayObject(mdsl, branchName, ep);
				// depStep.setEventProduction(ep); // not needed here, taken out Feb 6
				
				// add flow step to flow
				addStepToFlow(flow, depStep);
			}
		}
	}

	private EventType createEventAndIntermediateCisStep(Orchestration flow, ServiceSpecification mdsl, String nextChoice, String suffix) {
		String choiceEventName = JaamSimConfigurationWrapper.normalizeName(nextChoice) + suffix;
		EventType eventForNextChoice = 
				TransformationHelpers.findOrCreateUniqueEventType(mdsl, choiceEventName);	
		createSimpleCisStep(flow, mdsl, choiceEventName, JaamSimConfigurationWrapper.normalizeName(nextChoice));
		return eventForNextChoice;
	}

	private static DomainEventProductionStep createDepStepForGatewayObject(ServiceSpecification mdsl, String gatewayName, EventProduction ep) {
		DomainEventProductionStep depStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep();
		depStep.setEventProduction(ep);
		CommandType ct = TransformationHelpers.findOrCreateUniqueCommandType(
				mdsl, DataTypeTransformationHelpers.replaceSpacesWithUnderscores(JaamSimConfigurationWrapper.normalizeName(gatewayName)));
		EitherCommandOrOperation coo = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
		coo.setCommand(ct);
		depStep.setAction(coo);
		return depStep;
	}

	private void convertDuplicatesToMDSLAndSteps(Orchestration flow, List<String> duplicates) throws IOException {
		ServiceSpecification mdsl = (ServiceSpecification) flow.eContainer();

		for(String duplicateName : duplicates) {
			// find servers (and SimEntities?) that have this gateway in their NextComponentList
			List<String> targetComponents = jsmWrapper.findTargetComponentListOfEntity(duplicateName);
			targetComponents.add(jsmWrapper.findNextComponentOfEntity(duplicateName));

			MultipleEventProduction andSplits = ApiDescriptionFactory.eINSTANCE.createMultipleEventProduction();
			
			for(String nextTargetComponent : targetComponents) {
				System.out.println("[D] Processing next split way in " + nextTargetComponent + ": " + duplicateName);
				String componentType = jsmWrapper.getSimulationObjectType(nextTargetComponent);
				if("Queue".equals(componentType)) {
					EventType eventForNextSplit = convertSingleSimulationObjectToEvent(mdsl, nextTargetComponent);
					andSplits.getEvents().add(eventForNextSplit);
				}
				// TODO (M) extract helper method handling identical remaining cases
				else if("Server".equals(componentType)) {
					System.out.println("[I] Handle type of choice in duplicate: " + componentType);
					EventType splitEvent = createEventAndIntermediateCisStep(flow, mdsl, nextTargetComponent, "Taken");
					andSplits.getEvents().add(splitEvent);
				}
				else if("Branch".equals(componentType)) {
					System.out.println("[I] Handle type of choice in duplicate: " + componentType);
					EventType splitEvent = createEventAndIntermediateCisStep(flow, mdsl, nextTargetComponent, "Taken");
					andSplits.getEvents().add(splitEvent);
				}
				else if("Duplicate".equals(componentType)) {
					System.out.println("[I] Handle type of choice in duplicate: " + componentType);
					EventType splitEvent = createEventAndIntermediateCisStep(flow, mdsl, nextTargetComponent, "Taken");
					andSplits.getEvents().add(splitEvent);
				}
				// added for test case n:
				else if("Assign".equals(componentType)) {
					System.out.println("[I] TODO handle type of choice in branch: " + componentType);
					EventType splitEvent = createEventAndIntermediateCisStep(flow, mdsl, nextTargetComponent, "Taken");
					andSplits.getEvents().add(splitEvent);
				}
				else if("EntityConveyor".equals(componentType)) {
					System.out.println("[I] TODO handle type of choice in branch: " + componentType);
					EventType splitEvent = createEventAndIntermediateCisStep(flow, mdsl, nextTargetComponent, "Taken");
					andSplits.getEvents().add(splitEvent);
				}
				else if("EntitySink".equals(componentType)) {
					System.out.println("[I] handle type of choice in branch: " + componentType);
					EventType splitEvent = createEventAndIntermediateCisStep(flow, mdsl, nextTargetComponent, "Taken");
					andSplits.getEvents().add(splitEvent);
				}
				else if("Statistics".equals(componentType)) {
					System.out.println("[W] Handle type of choice in duplicate: " + componentType);
					EventType splitEvent = createEventAndIntermediateCisStep(flow, mdsl, nextTargetComponent, "Taken");
					andSplits.getEvents().add(splitEvent);
				}
				else {
					System.err.println("[W] Unexpected type of choice in duplicate: " + componentType);
				}
			}

			if(andSplits.getEvents().size() < 2 ) {
				System.err.println("[W] Wrong number of event production elements");
			}
			else {
				EventProduction ep = ApiDescriptionFactory.eINSTANCE.createEventProduction();
				ep.setMep(andSplits);
				DomainEventProductionStep depStep = createDepStepForGatewayObject(mdsl, duplicateName, ep);
				// add flow step to flow
				addStepToFlow(flow, depStep);
			}
		}
	}
	
	private List<CommandType> convertCombinesToCommands(ServiceSpecification mdsl, List<String> combineNames) {
		List<CommandType> commands = new ArrayList<CommandType>();
		for(String combineComponent : combineNames) {
			String objectType = jsmWrapper.getSimulationObjectType(combineComponent);
			if(objectType.equals("Combine")) {
				CommandType cmd = TransformationHelpers.findOrCreateUniqueCommandType(mdsl, JaamSimConfigurationWrapper.normalizeName(combineComponent) + EMITTER_SUFFIX);
				commands.add(cmd);
			}
			else if(objectType.equals("Queue")) {
				CommandType cmd = TransformationHelpers.findOrCreateUniqueCommandType(mdsl, JaamSimConfigurationWrapper.normalizeName(combineComponent) + EMITTER_SUFFIX);
				commands.add(cmd);
			}
			else {
				System.err.println("[W] " + combineComponent + " is not a Combine or Queue, but a(n) " + objectType);			
			}
		}
		return commands;
	}
	
	private static void addStepToFlow(Orchestration flow, DomainEventProductionStep depStep) {
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setDepStep(depStep);
		flow.getSteps().add(fs);
	}

	private List<EventType> convertSimulationObjectsToEvents(ServiceSpecification mdsl, List<String> simObjects) {
		List<EventType> events = new ArrayList<EventType>();
		for(String eventCandidate : simObjects) {
			EventType et = convertSingleSimulationObjectToEvent(mdsl, eventCandidate);
			if(et!=null ) {
				events.add(et);
			}
		}
		return events;
	}

	private EventType convertSingleSimulationObjectToEvent(ServiceSpecification mdsl, String eventCandidate) {
		String objectType = jsmWrapper.getSimulationObjectType(eventCandidate);

		if(objectType.equals("Queue")) {
			return TransformationHelpers.findOrCreateUniqueEventType(mdsl, JaamSimConfigurationWrapper.normalizeName(eventCandidate) + TRIGGER_SUFFIX);		
		}
		else if(objectType.equals("EntitySink")) {
			return TransformationHelpers.findOrCreateUniqueEventType(mdsl, JaamSimConfigurationWrapper.normalizeName(eventCandidate) + TRIGGER_SUFFIX);	
		}
		else {
			System.err.println("[W] Type of " + eventCandidate + " is unsupported: " + objectType + ". Model object skipped.");
			return null;
		}
	}

	private void convertCombinesToMDSLJoinEventsAndSteps(Orchestration flow, List<String> combines) throws IOException {
		ServiceSpecification mdsl = (ServiceSpecification) flow.eContainer();

		for(String joinName : combines) {			
			// handle "nextComponent" configuration input (tbd: create a multiple command CIS? comment?)
			List<String> combinesComponents = jsmWrapper.findNextComponentsFollowingEntity(joinName);
			List<String> joinedEvents = jsmWrapper.findWaitQueueListOfEntity(joinName);
			List<CommandType> commands = convertCombinesToCommands(mdsl, combinesComponents);			

			CommandInvokationStep cisStep = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();

			List<EventType> events = convertSimulationObjectsToEvents(mdsl, joinedEvents);
			events.forEach(event->cisStep.getEvents().add(event));

			// can only be one due to JaamSim constraint, ok to loop
			SingleCommandInvokation commandAfterJoin = ApiDescriptionFactory.eINSTANCE.createSingleCommandInvokation();
			for(CommandType command : commands) {
				commandAfterJoin.getCommands().add(command);
			}
			
			CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
			ci.setSci(commandAfterJoin);
			EitherCommandOrOperationInvokation ecooi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
			ecooi.setCi(ci);

			cisStep.setAction(ecooi);

			// add flow step to flow
			addStepToFlow(flow, cisStep);
		}
	}

	private static void addStepToFlow(Orchestration flow, CommandInvokationStep cisStep) {
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setCisStep(cisStep);
		flow.getSteps().add(fs);
	}	
	
	// ** helpers
	
	protected static Resource getTestResource(String testMDSLName) throws IOException {
		new APIDescriptionStandaloneSetup().createInjectorAndDoEMFRegistration();
		return new ResourceSetImpl().getResource(URI.createFileURI(getTestFile(testMDSLName).getAbsolutePath()), true);
	}

	private static File getTestFile(String testMDSLName) {
		return new File(Paths.get("").toAbsolutePath().toString(), testMDSLName);
	}
	
	public void writeToFile(ServiceSpecification apiDescription, String targetFileName) throws IOException {
		MDSLResource targetResource = new MDSLResource(apiDescription.eResource());
		String mdslModelAsString = targetResource.getXtextResource().getSerializer().serialize(apiDescription);

		System.out.println("Writing generated MDSL to " + targetFileName);
		FileWriter fileWriter = new FileWriter(targetFileName);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.printf("// Generated with MDSL2JaamSim on %s, at %s\n", java.time.LocalDate.now(), java.time.LocalTime.now());
		printWriter.print(mdslModelAsString);
		printWriter.close();
		System.out.println("Done writing generated MDSL to " + targetFileName);
	}
}

package io.mdsl.transformations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.CombinedInvocationStep;
import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.ConcurrentCommandInvokation;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EitherCommandOrOperation;
import io.mdsl.apiDescription.EitherCommandOrOperationInvokation;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.EventProduction;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.ExclusiveAlternativeEventProduction;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.InclusiveAlternativeEventProduction;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleCommandInvokation;
import io.mdsl.apiDescription.SingleEventProduction;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MDSLLogger;

public class FlowTransformations {

	public static final String DEP_STEP = "DEP_STEP";
	public static final String CIS_STEP = "CIS_STEP";
	
	private static final String FLOW_NAME_SUFFIX = "Flow";
	private static final String COMMAND_NAME_SUFFIX = "Processor";
	private static final String EVENT_NAME_SUFFIX = "Trigger";
	private static final String COMPLETED_SUFFIX = "Completed";


	// ** story to flow transformation(s)
	
	public void addApplicationFlowForScenario(IntegrationScenario scenario) {
		ServiceSpecification ss = (ServiceSpecification) scenario.eContainer();

		Orchestration flow = createFlowIfPossible(ss, scenario);
		for (IntegrationStory story : scenario.getStories()) {
			FlowStep fs = stepForStory(ss, story);
			flow.getSteps().add(fs);
		}

		ss.getOrchestrations().add(flow);
	}
	
	// ** flow to flow transformations
	
	public void consolidateFlowSteps(DomainEventProductionStep depStep, String branchingType) {
		// TODO use branchingType parameter
		CommandType command = depStep.getAction().getCommand(); // there is only one	
		Orchestration flow = (Orchestration) depStep.eContainer().eContainer();
		FlowStep step = (FlowStep) depStep.eContainer(); 
		List<FlowStep> stepsToBeMerged = FlowTransformationHelpers.findAllPeersOfStep(flow, command);
			
		if(stepsToBeMerged.size()>1) {
			int position = flow.getSteps().indexOf(step);
			List<EventType> eventSteps = FlowTransformationHelpers.eventsInSimpleDEPSteps(stepsToBeMerged);
			if(eventSteps.size()>1) {
				FlowStep mergedDepStep = createAlternativeDEPStep(flow, command, eventSteps);
				flow.getSteps().add(position, mergedDepStep); // could use add at position index elsewhere too
				flow.getSteps().removeAll(stepsToBeMerged);
			}
			else {
				// edge case, do not modify flow
			}
		}
		// no no need to merge with self (size==1)
	}
	
	public void splitCombinedFlowStep(CombinedInvocationStep eceStep) {
		FlowStep step = (FlowStep) eceStep.eContainer();
		Orchestration flow = (Orchestration) step.eContainer();
		
		CommandInvokationStep cis = eceStep.getCisStep();
		EventProduction ep = eceStep.getEventProduction();
		
		int position = flow.getSteps().indexOf(step);
		step.setEceStep(null);
		step.setCisStep(cis);
		
		try {
			CommandType commandInEceStep = cis.getAction().getCi().getSci().getCommands().get(0);
			EitherCommandOrOperation simpleCommand = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
			simpleCommand.setCommand(commandInEceStep);
			
			// add new dep step
			DomainEventProductionStep depStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep();
			depStep.setEventProduction(EcoreUtil.copy(ep));

			depStep.setAction(simpleCommand);
			FlowStep depFlowStep = ApiDescriptionFactory.eINSTANCE.createFlowStep();
			depFlowStep.setDepStep(depStep);		
			// TODO v55 formatter still has to be adjusted to add line break:
			flow.getSteps().add(position+1, depFlowStep);
		}
		catch (Exception e) {
			throw new MDSLException("Split Combined Flow Step is only ssupported for simple commands.");
		}
	}
	
	public void addBranchesWithMerge(EObject element, String branchingType) {
		if(branchingType.equals("AND")) // TODO use constant 
			addAndBranchesWithMerge(element);
		else if (branchingType.equals("OR")) // TODO use constant 
			addChoiceBranchesWithMerge(element);
		else 
			throw new MDSLException("Unknown type of transformation: " + branchingType);
	}
	
	private void addChoiceBranchesWithMerge(EObject element) {
		if(!(element instanceof CommandInvokationStep))
			throw new MDSLException("This transformation expects a command invocation step");
		
		CommandInvokationStep cis = (CommandInvokationStep) element;
		
		if(cis.eContainer() instanceof CombinedInvocationStep)
			throw new MDSLException("This transformation cannot be invoked on combined invocation steps.");

		Orchestration flow = (Orchestration) cis.eContainer().eContainer();
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		EList<CommandType> commands = FlowTransformationHelpers.getCommandListOfStep(cis.getAction().getCi());
		if(!(commands.size()==1)) {
			MDSLLogger.reportError("Adding choice branches only works if command(s) in this step emit a single command.");
		}
		
		CommandType trigger = commands.get(0);
		EventType choice1 = TransformationHelpers.findOrCreateUniqueEventType(ss, trigger.getName()+"Choice1");
		EventType choice2 = TransformationHelpers.findOrCreateUniqueEventType(ss, trigger.getName()+"Choice2");
		List<EventType> choices = new ArrayList<EventType>();
		choices.add(choice1);
		choices.add(choice2);
		createExclusiveChoiceDEPStep(flow, trigger, choices);
		
		// could place a command in between the events and the join (in separate QF? in combined event-command-event step?)
		
		// create a cis that serves as an Aggregator
		createAggregator(flow, choices, trigger.getName() + "_Choice1_Choice2_" + "JoinProcessor");
	}

	public void addAndBranchesWithMerge(EObject element) {
		if(!(element instanceof DomainEventProductionStep))
			throw new MDSLException("This transformation expects a domain event production step");
		DomainEventProductionStep depStep = (DomainEventProductionStep) element;
		Orchestration flow = (Orchestration) depStep.eContainer().eContainer();
		// int position = flow.getSteps().indexOf(depStep);
		
		EList<EventType> events = getEventListOfStep(depStep);
		if(!(events.size()==1)) {
			MDSLLogger.reportError("Adding parallel branches only works if command(s) in this step emit a single event.");
		}
		EventType event = events.get(0);
		
		FlowStep newStep = createCISStepWithTwoConcurrentCommandInvocations(flow, event);
		// flow.getSteps().add(position+1, newStep);
		flow.getSteps().add(newStep);

		try {
			CommandType ct1 = newStep.getCisStep().getAction().getCi().getCci().getCommands().get(0); 
			CommandType ct2 = newStep.getCisStep().getAction().getCi().getCci().getCommands().get(1); 

			// get commands from new ciss, create a dep step for each of them
			DomainEventProductionStep branch1Done = createSimpleDEPStepAndAddItToFlow(flow, ct1); // could pass position in
			DomainEventProductionStep branch2Done = createSimpleDEPStepAndAddItToFlow(flow, ct2); // could pass position in
			EventType branchEvent1 = branch1Done.getEventProduction().getSep().getEvents().get(0); // assuming that created CIS is simple
			EventType branchEvent2 = branch2Done.getEventProduction().getSep().getEvents().get(0); // assuming that created CIS is simple

			// get event from new deps and join them
			List<EventType> eventList = new ArrayList<EventType>();
			eventList.add(branchEvent1); // assuming CIS is simple
			eventList.add(branchEvent2);

			// create another cis that serves as an Aggregator
			createAggregator(flow, eventList, event.getName() + "1_" + event.getName() + "2_" + "JoinProcessor");
		}
		catch(Exception e) {
			MDSLLogger.reportError("Unexpected flow and flow step structure, cannot apply transformation");
		}

	}

	private FlowStep createCISStepWithTwoConcurrentCommandInvocations(Orchestration flow, EventType event) {
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		CommandInvokationStep cisStep = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();

		// add event reference to flow step (event type must exist already) 
		cisStep.getEvents().add(event);
			
		String suggestedCommandName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(event.getName()) + COMMAND_NAME_SUFFIX;
		CommandType ct1 = TransformationHelpers.findOrCreateUniqueCommandType(ss, suggestedCommandName + "1");
		CommandType ct2 = TransformationHelpers.findOrCreateUniqueCommandType(ss, suggestedCommandName + "2");
		
		// prepare new flow step that references two new command types
		EitherCommandOrOperationInvokation ecoi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
		CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
		ConcurrentCommandInvokation cci = ApiDescriptionFactory.eINSTANCE.createConcurrentCommandInvokation();
		cci.getCommands().add(ct1);
		cci.getCommands().add(ct2);
		ci.setCci(cci);
		ecoi.setCi(ci);
		cisStep.setAction(ecoi);

		// add new step to flow
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setCisStep(cisStep);
		
		return fs;
	}

	public void addCisStep(EObject element) {
		if(!(element instanceof DomainEventProductionStep))
			throw new MDSLException("This transformation expects a domain event production step");
		DomainEventProductionStep depStep = (DomainEventProductionStep) element;
		
		// dep->flow step->flow->service spec (also needed for new command)
		Orchestration flow = (Orchestration) depStep.eContainer().eContainer();

		EList<EventType> events = getEventListOfStep(depStep);
		events.forEach(event->createCISStep(flow, event, null));
	}

	// TODO (future work) either do more aggregation-specific work here or merge with addCIS 
	private CommandInvokationStep createAggregator(Orchestration flow, List<EventType> eventList, String suggestedCommandName) {
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		CommandInvokationStep cisStep = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();

		// add events reference to flow step (event type must exist already) 
		eventList.forEach(event->cisStep.getEvents().add(event));

		CommandType ct = TransformationHelpers.findOrCreateUniqueCommandType(ss, suggestedCommandName);

		// prepare new flow step that references the new command (type)
		EitherCommandOrOperationInvokation ecoi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
		CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
		SingleCommandInvokation sci = ApiDescriptionFactory.eINSTANCE.createSingleCommandInvokation();
		sci.getCommands().add(ct);
		ci.setSci(sci);
		ecoi.setCi(ci);
		cisStep.setAction(ecoi);

		// add it to flow
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setCisStep(cisStep);
		flow.getSteps().add(fs);

		return cisStep;
	}

	private CommandInvokationStep createCISStep(Orchestration flow, EventType event, String suggestedCommandName) {
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		CommandInvokationStep cisStep = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();

		// add event reference to flow step (event type must exist already) 
		cisStep.getEvents().add(event);
			
		if(suggestedCommandName==null||suggestedCommandName.equals(""))
			suggestedCommandName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(event.getName()) + COMMAND_NAME_SUFFIX;

		CommandType ct = TransformationHelpers.findOrCreateUniqueCommandType(ss, suggestedCommandName);
		
		// prepare new flow step that references the new command (type)
		EitherCommandOrOperationInvokation ecoi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
		CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
		SingleCommandInvokation sci = ApiDescriptionFactory.eINSTANCE.createSingleCommandInvokation();
		sci.getCommands().add(ct);
		ci.setSci(sci);
		ecoi.setCi(ci);
		cisStep.setAction(ecoi);

		// add it to flow
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setCisStep(cisStep);
		flow.getSteps().add(fs);
		
		return cisStep;
	}
	
	public void addDepStep(EObject element) {
		if(!(element instanceof CommandInvokationStep))
			throw new MDSLException("This transformation expects a domain event production step");
		CommandInvokationStep ciStep = (CommandInvokationStep) element;
		
		if(ciStep.eContainer() instanceof CombinedInvocationStep)
			throw new MDSLException("This transformation cannot be invoked on combined invocation steps.");
		
		// cis->flow step->flow->service spec (needed for new event)
		Orchestration flow = (Orchestration) ciStep.eContainer().eContainer();
		CommandInvokation ci = ciStep.getAction().getCi();
		EList<CommandType> commands = getCommandsOfStep(ci);
		commands.forEach(command->createSimpleDEPStepAndAddItToFlow(flow, command));
	}
	
	private DomainEventProductionStep createExclusiveChoiceDEPStep(Orchestration flow, CommandType command, List<EventType> events) {
		DomainEventProductionStep depStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep();
		EventProduction ep = ApiDescriptionFactory.eINSTANCE.createEventProduction();
		depStep.setEventProduction(ep);
		
		EitherCommandOrOperation coo = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
		coo.setCommand(command);
		depStep.setAction(coo);
		
		ExclusiveAlternativeEventProduction orChoices = ApiDescriptionFactory.eINSTANCE.createExclusiveAlternativeEventProduction();
		// add event reference to new flow step
		events.forEach(event->orChoices.getEvents().add(event));
		ep.setEaep(orChoices);
		
		// add flow step to flow
		createAndAddFlowStep(flow,depStep);
		 
		return depStep;
	}

	private DomainEventProductionStep createSimpleDEPStepAndAddItToFlow(Orchestration flow, CommandType ct) {
		DomainEventProductionStep depStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep();
		EitherCommandOrOperation coo = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
		// coo.setActor("AnonymousActor");
		coo.setCommand(ct);
		depStep.setAction(coo);
		
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		String suggestedEventName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(ct.getName()) + COMPLETED_SUFFIX;
		// add event type (on service specification level), suggesting a name that might have to be modified
		EventType de = TransformationHelpers.findOrCreateUniqueEventType(ss, suggestedEventName);

		// add event reference to flow step
		createAndAddSingleEventReference(depStep, de);

		// add flow step to flow
		createAndAddFlowStep(flow, depStep);
		 
		return depStep;
	}

	private void createAndAddSingleEventReference(DomainEventProductionStep depStep, EventType de) {
		EventProduction ep = ApiDescriptionFactory.eINSTANCE.createEventProduction();
		SingleEventProduction sep = ApiDescriptionFactory.eINSTANCE.createSingleEventProduction();
		sep.getEvents().add(de);
		ep.setSep(sep);
		depStep.setEventProduction(ep);
		depStep.getEventProduction().getSep().getEvents().add(de);
	}

	private void createAndAddFlowStep(Orchestration flow, DomainEventProductionStep depStep) {
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setDepStep(depStep);
		flow.getSteps().add(fs);
	}
	
	// ** flow to endpoint transformations

	public void addEndpointTypeSupportingFlow(Orchestration flow) {
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();

		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, flow.getName() + FlowTransformationHelpers.ENDPOINT_NAME_SUFFIX);
		ec.setFlow(flow); // grammar: either flow or scenario can be referenced at present
		ec.setPrimaryRole(MAPDecoratorHelpers.PROCESSING_RESOURCE);

		for (FlowStep step : flow.getSteps()) {
			if (step.getCisStep() != null) {
				FlowTransformationHelpers.createStateTransitionOperationForCommand(step.getCisStep());
			} else if (step.getDepStep() != null) {
				FlowTransformationHelpers.createEventProcessorOperationForEventProduction(step.getDepStep().getEventProduction());
			}
			else if (step.getEceStep() != null) {
				CommandInvokationStep cis = step.getEceStep().getCisStep();
				FlowTransformationHelpers.createStateTransitionOperationForCommand(cis);
				EventProduction ep = step.getEceStep().getEventProduction();
				FlowTransformationHelpers.createEventProcessorOperationForEventProduction(ep);
			}
			else {
				MDSLLogger.reportWarning("Unknown type of flow step.");
			}
		}

		Event re = FlowTransformationHelpers.createFlowInitiationEvent(ss, flow);
		ec.getEvents().add(re);
		ss.getContracts().add(ec);
	}

	public void addOperationForFlowStep(EObject element, String stepType) {
		// could also use STATE_CREATION_OPERATION (for first command in flow)
		if (stepType.equals(CIS_STEP)) {
			if(element instanceof CommandInvokationStep) {
				FlowTransformationHelpers.createStateTransitionOperationForCommand((CommandInvokationStep)element);
			}
			else {
				MDSLLogger.reportError("Unexpected element type: " + element.getClass().getSimpleName());
			}
		}
		else if (stepType.equals(DEP_STEP)) {
			if(element instanceof DomainEventProductionStep) {
				FlowTransformationHelpers.createEventProcessorOperationForEventProduction(((DomainEventProductionStep)element).getEventProduction());
			}
			else {
				MDSLLogger.reportError("Unexpected element type: " + element.getClass().getSimpleName());
			}
		} 
		else {
			MDSLLogger.reportError("Unknown step type: " + stepType);
		}
	}

	// could add a check to validator: scenario already supported by a flow?
	private Orchestration createFlowIfPossible(ServiceSpecification ss, IntegrationScenario scenario) {
		String suggestedName = scenario.getName() + FLOW_NAME_SUFFIX;
		// check that name is not already taken, return existing flow if so (flow steps will then be added to it) 
		for (Orchestration exflow : ss.getOrchestrations()) {
			if (exflow.getName().equals(suggestedName)) {
				return exflow;
			}
		}

		Orchestration flow = ApiDescriptionFactory.eINSTANCE.createOrchestration();
		flow.setName(suggestedName);
		flow.setScenario(scenario);

		return flow;
	}
	
	// ** story to flow helpers 

	private FlowStep stepForStory(ServiceSpecification ss, IntegrationStory story) {
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();

		CommandInvokationStep cis = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();
		EitherCommandOrOperationInvokation coi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
		CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
		SingleCommandInvokation sci = ApiDescriptionFactory.eINSTANCE.createSingleCommandInvokation();
		// depStep has [delegates to] for actor (but cis created here); goals could go to new eval. step 
		// TODO (L) map role and goal too: to comment? or add a new grammar element?
		String storyAction = ScenarioTransformationHelpers.getActionName(story.getAction(), ScenarioTransformationHelpers.getFirstObjectName(story));

		String suggestedEventName = null;
		if (story.getCondition() == null || story.getCondition().isEmpty()) {
			suggestedEventName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(storyAction) + EVENT_NAME_SUFFIX;
		} else {
			suggestedEventName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(story.getCondition());
		}

		// add event type (on service specification level), suggesting a name that might have to be modified
		EventType de = TransformationHelpers.findOrCreateUniqueEventType(ss, suggestedEventName);

		// add event reference to flow step
		cis.getEvents().add(de);

		// add command type (on service specification level)
		CommandType ct = TransformationHelpers.findOrCreateUniqueCommandType(ss, DataTypeTransformationHelpers.replaceSpacesWithUnderscores(storyAction));

		// finally, prepare command and flow step
		sci.getCommands().add(ct);
		ci.setSci(sci);
		coi.setCi(ci);
		cis.setAction(coi);
		fs.setCisStep(cis);

		return fs;
	}
	
	// ** flow to flow helpers
	
	private EList<EventType> getEventListOfStep(DomainEventProductionStep depStep) {
		if(depStep.getEventProduction().getSep()!=null) {
			return depStep.getEventProduction().getSep().getEvents(); // only one
		} 
		else if(depStep.getEventProduction().getMep()!=null) {
			return depStep.getEventProduction().getMep().getEvents();
		}
		else if(depStep.getEventProduction().getEaep()!=null) {
			return depStep.getEventProduction().getEaep().getEvents();
		}
		else if(depStep.getEventProduction().getIaep()!=null) {
			return depStep.getEventProduction().getIaep().getEvents();
		}
		else {
			MDSLLogger.reportError("Unknown type of domain production step.");
		}
		return null;
	}

	private EList<CommandType> getCommandsOfStep(CommandInvokation ci) {
		if(ci.getSci()!=null) {
			return ci.getSci().getCommands();
		}
		else if(ci.getCci()!=null) {
			return ci.getCci().getCommands();
		}
		else if(ci.getEaci()!=null) {
			return ci.getEaci().getCommands();
		}
		else if(ci.getIaci()!=null) {
			return ci.getIaci().getCommands();
		}
		else {
			MDSLLogger.reportError("Unknown type of command invocation step.");
		}
		return null;
	}

	private FlowStep createAlternativeDEPStep(Orchestration flow, CommandType command, List<EventType> events) {
		DomainEventProductionStep depStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep();
		EitherCommandOrOperation coo = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
		coo.setCommand(command);
		depStep.setAction(coo);

		EventProduction ep = ApiDescriptionFactory.eINSTANCE.createEventProduction();
		InclusiveAlternativeEventProduction ieap = ApiDescriptionFactory.eINSTANCE.createInclusiveAlternativeEventProduction();
		events.forEach(event->ieap.getEvents().add(event));

		ep.setIaep(ieap);
		depStep.setEventProduction(ep);
		
		FlowStep flowStep = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		flowStep.setDepStep(depStep);
		return flowStep;
	}
}

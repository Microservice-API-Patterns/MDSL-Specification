package io.mdsl.transformations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.CombinedInvocationStep;
import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.EventProduction;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.StateTransition;
import io.mdsl.utils.MDSLLogger;

public class FlowTransformationHelpers {

	public static final String ENDPOINT_NAME_SUFFIX = "Endpoint";
	private static final String OPERATION_EXECUTED_SUFFIX = "Executed";
	private static final String OPERATION_TRIGGERED_SUFFIX = "Triggered";
	private static final String FLOW_START_EVENT_SUFFIX = "Initiated";
	
	private static final String EVENT_EMISSION_PREFIX = "emit";
	private static final String DEFAULT_OPERATION_NAME = "defaultOperationName";

	public static void createStateTransitionOperationForCommand(CommandInvokationStep cmdis) {
		CommandInvokation ci = cmdis.getAction().getCi();
		EList<EventType> events = cmdis.getEvents();

		EndpointContract ec = findOrCreateEndpointContract(cmdis);
		events.forEach(event->addReceivingEventToEndpointContract(event, ec));

		EList<CommandType> commandList = getCommandListOfStep(ci);	
		commandList.forEach(command->createCommandOperationIfPossible(ec, command.getName()));
	}

	public static void createCommandOperationIfPossible(EndpointContract ec, String opName) {
		if (TransformationHelpers.findOperationInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName)) == null) {
			Operation operation = OperationTransformationHelpers.createStateManipulatingOperation(opName);
			StateTransition stateTransition = ApiDescriptionFactory.eINSTANCE.createStateTransition();
			stateTransition.setFrom(opName + OPERATION_TRIGGERED_SUFFIX);
			stateTransition.setTo(opName + OPERATION_EXECUTED_SUFFIX);
			operation.setSt(stateTransition);
			ec.getOps().add(operation);
		} else {
			MDSLLogger.reportWarning("Not creating command invocation operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken");
		}
	}

	public static void addReceivingEventToEndpointContract(EventType receivedEventType, EndpointContract ec) {
		Event eventReference = ApiDescriptionFactory.eINSTANCE.createEvent();
		eventReference.setType(receivedEventType);
		ec.getEvents().add(eventReference);
	}

	public static void createEventProcessorOperationForEventProduction(EventProduction ep) {
		// ep->dep->flow step->flow->service spec
		ServiceSpecification ss = (ServiceSpecification) ep.eContainer().eContainer().eContainer().eContainer();
		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, FlowTransformationHelpers.endpointNameForFlow((Orchestration) ep.eContainer().eContainer().eContainer()));

		if(ep.getSep()!=null) {
			String opName = FlowTransformationHelpers.turnEventNameIntoOperationName(ep.getSep().getEvents().get(0));
			if (TransformationHelpers.findOperationInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName)) != null) {
				MDSLLogger.reportWarning("Not creating event processor operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken.");
			}
			else { 
				// TODO v55 simplify and align with other cases (see refactorings of other transformations)
				Operation eventEmissionOperation = OperationTransformationHelpers.createEventProductionOperation(opName); // could be "EVENT_EMITTER"
				ec.getOps().add(eventEmissionOperation);
				EventType de = ep.getSep().getEvents().get(0);
				createAndAddEvent(opName, eventEmissionOperation, de);
			}
		} 
		else if(ep.getMep()!=null) {
			for(EventType event : ep.getMep().getEvents()) {
				String opName = FlowTransformationHelpers.turnEventNameIntoOperationName(event);
				if (TransformationHelpers.findOperationInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName)) != null) {
					MDSLLogger.reportWarning("Not creating event processor operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken.");
				}
				else { 
					Operation eventEmissionOperation = OperationTransformationHelpers.createEventProductionOperation(opName); // could be "EVENT_EMITTER"
					ec.getOps().add(eventEmissionOperation);
					createAndAddEvent(opName, eventEmissionOperation, event);
				}
			}
		} 
		else if(ep.getIaep()!=null) {
			for(EventType event : ep.getIaep().getEvents()) {
				String opName = FlowTransformationHelpers.turnEventNameIntoOperationName(event);
				if (TransformationHelpers.findOperationInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName)) != null) {
					MDSLLogger.reportWarning("Not creating event processor operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken");
				}
				else { 
					Operation eventEmissionOperation = OperationTransformationHelpers.createEventProductionOperation(opName); // could be "EVENT_EMITTER"
					ec.getOps().add(eventEmissionOperation);
					createAndAddEvent(opName, eventEmissionOperation, event);
				}
			}
		} 
		else if(ep.getEaep()!=null) {
			for(EventType event : ep.getEaep().getEvents()) {
				String opName = FlowTransformationHelpers.turnEventNameIntoOperationName(event);
				if (TransformationHelpers.findOperationInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName)) != null) {
					MDSLLogger.reportWarning("Not creating event processor operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken");
				}
				else { 
					Operation eventEmissionOperation = OperationTransformationHelpers.createEventProductionOperation(opName); // could be "EVENT_EMITTER"
					ec.getOps().add(eventEmissionOperation);
					createAndAddEvent(opName, eventEmissionOperation, event);
				}
			}
		}
		else {
			MDSLLogger.reportError("Unknown type of ep structure.");
		}

	}

	public static void createAndAddEvent(String opName, Operation eventEmissionOperation, EventType de) {
		if (de != null) {
			Event ev = ApiDescriptionFactory.eINSTANCE.createEvent();
			ev.setType(de);
			eventEmissionOperation.getEvents().add(ev);
		} else {
			System.err.println("Event type not found in flow, not adding event " + opName);
		}
	}

	public static Event createFlowInitiationEvent(ServiceSpecification ss, Orchestration flow) {
		EventType de = TransformationHelpers.findOrCreateEventType(ss, flow.getName() + FLOW_START_EVENT_SUFFIX);
		Event re = ApiDescriptionFactory.eINSTANCE.createEvent();
		re.setType(de);

		return re;
	}

	public static EndpointContract findOrCreateEndpointContract(CommandInvokationStep element) {
		Orchestration flow = null;
		if(element.eContainer() instanceof CombinedInvocationStep) {
			flow = (Orchestration) element.eContainer().eContainer().eContainer();
		}
		else {
			// is a regular CIS step
			flow = (Orchestration) element.eContainer().eContainer();
		}
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, FlowTransformationHelpers.endpointNameForFlow(flow));
		return ec;
	}

	public static List<FlowStep> findAllPeersOfStep(Orchestration flow, CommandType command) {
		List<FlowStep> result = new ArrayList<FlowStep>();
		for(FlowStep step : flow.getSteps()) {
			if(step.getDepStep()!=null&&step.getDepStep().getEventProduction().getSep()!=null) {
				// we are interested in all simple DEP steps with the same action command, irrespective of emitted events 
				if(step.getDepStep().getAction().getCommand().getName().equals(command.getName())) {
					result.add(step);
				}
			}
		}
		return result;
	}

	public static List<EventType> eventsInSimpleDEPSteps(List<FlowStep> stepsToBeMerged) {
		List<EventType> result = new ArrayList<EventType>();
		stepsToBeMerged.forEach((eventStep->result.add(eventStep.getDepStep().getEventProduction().getSep().getEvents().get(0))));
		return result;
	}


	public static String endpointNameForFlow(Orchestration flow) {
		return flow.getName() + ENDPOINT_NAME_SUFFIX;
	}

	public static String turnEventNameIntoOperationName(EventType event) {
		return EVENT_EMISSION_PREFIX + event.getName();
	}

	public static String getSimpleCommandName(DomainEventProductionStep depStep) {
		String opName = DEFAULT_OPERATION_NAME;
		try {
			opName = DataTypeTransformationHelpers.capitalizeName(depStep.getEventProduction().getSep().getEvents().get(0).getName());
		} catch (Exception e) {
			MDSLLogger.reportError("Unsupported flow syntax. Only simple domain event production steps can be transformed at present.");
		}
		return opName;
	}
	
	public static EList<CommandType> getCommandListOfStep(CommandInvokation ci) {
		if(ci.getSci()!=null) {
			// is only one
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
			MDSLLogger.reportError("Unsupported type of command invocation");
		}
		return null;
	}
}

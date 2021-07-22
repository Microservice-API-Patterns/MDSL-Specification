package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EitherCommandOrOperation;
import io.mdsl.apiDescription.EitherCommandOrOperationInvokation;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.StateTransition;
import io.mdsl.apiDescription.impl.EitherCommandOrOperationImpl;
import io.mdsl.apiDescription.impl.EitherCommandOrOperationInvokationImpl;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;

class AddOperationForFlowStep implements ISemanticModification {
	public final static String STATE_CREATION_OPERATION = "STATE_CREATION_OPERATION";
	public final static String EVENT_PROCESSOR = "EVENT_PROCESSOR";
	public final static String RETRIEVAL_OPERATION = "RETRIEVAL_OPERATION";

	private String operationRole; 

	public AddOperationForFlowStep(String operationRole) {
		this.operationRole = operationRole;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		if(operationRole.equals("STATE_TRANSITION_OPERATION")) {
			createStateTransitionOperationForCommand(element); 
		}
		else if(operationRole.equals(EVENT_PROCESSOR)) {
			// dep->flow step->flow->service spec
			DomainEventProductionStep depStep = (DomainEventProductionStep) element;			
			ServiceSpecification ss = (ServiceSpecification) depStep.eContainer().eContainer().eContainer();
			EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, endpointNameForFlow((Orchestration)depStep.eContainer().eContainer()));

			// this only works if depStep has most simple structure that is possible (single event)
			String opName = getCommandNameFromStep(depStep);

			if(!TransformationHelpers.operationExistsInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName))) {	
				// TODO could use new helpers for story to operation now (requires other parameters)
				Operation eventProcessorOperation = TransformationHelpers.createStateManipulatingOperation(opName, this.operationRole);
				ec.getOps().add(eventProcessorOperation);
				// this only works if depStep has most simple structure that is possible (single event)
				EventType de = getEventFromStep(depStep);  
				if(de!=null) {
					Event ev = ApiDescriptionFactory.eINSTANCE.createEvent();
					ev.setType(de);
					eventProcessorOperation.getEvents().add(ev);
				}
				// TODO could add event to "receive" section of endpoint type as well
				else
					System.err.println("Event type not found in flow, not adding event to operation " + opName);
			}
			else
				TransformationHelpers.reportError("Can't create event processor operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken");
		}
		// this one taken out in Quick Fix provider:
		/* else if (operationRole.equals(STATE_CREATION_OPERATION)) {

			// cis->flow step->flow->service spec
			CommandInvokationStep cisStep = (CommandInvokationStep) element;
			FlowStep flowStep = (FlowStep) cisStep.eContainer();
			Orchestration flow = (Orchestration)flowStep.eContainer();
			ServiceSpecification ss = (ServiceSpecification) flow.eContainer();

			EndpointContract ec = TransformationUtilities.findOrCreateEndpointType(ss, endpointNameForFlow(flow));

			// this only works for very simple flow steps, those with single command invocation 
			// TODO handle or catch all options (in helper method; check what happens in generator)
			String opName = cisStep.getAction().getCi().getSci().getCommands().get(0).getName();

			if(!operationExistsInContract(ec, TransformationUtilities.decapitalizeName(opName))) {
				Operation operation = TransformationUtilities.createStateManipulatingOperation(opName, this.operationRole);
				StateTransition stateTransition = ApiDescriptionFactory.eINSTANCE.createStateTransition();
				// PoC/experimental:
				stateTransition.setFrom(opName + "Triggered");
				stateTransition.setTo(opName + "Executed");
				operation.setSt(stateTransition);
				ec.getOps().add(operation);
			}
			else
				TransformationUtilities.reportError("Can't create command invokation operation, name " + TransformationUtilities.decapitalizeName(opName) + " already taken");
		}
			*/
		else 
			TransformationHelpers.reportError("Unknown operation role " + operationRole);
	}

	private void createStateTransitionOperationForCommand(EObject element) {
		if(element.getClass()==EitherCommandOrOperationImpl.class) {
			EitherCommandOrOperation cmd = (EitherCommandOrOperation) element;

			ServiceSpecification ss = (ServiceSpecification) cmd.eContainer().eContainer().eContainer().eContainer();
			EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, endpointNameForFlow((Orchestration)cmd.eContainer().eContainer().eContainer()));
			String opName = cmd.getCommand().getName();
			if(!TransformationHelpers.operationExistsInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName))) {			
				Operation operation = TransformationHelpers.createStateManipulatingOperation(opName, this.operationRole);
				StateTransition stateTransition = ApiDescriptionFactory.eINSTANCE.createStateTransition();
				stateTransition.setFrom(opName + "Triggered");
				stateTransition.setTo(opName + "Executed");
				operation.setSt(stateTransition);
				ec.getOps().add(operation);
				// event production/reception taken out here, would require more navigation
			}
			else
				TransformationHelpers.reportError("Can't create event processor operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken");
		}
		else if(element.getClass()==EitherCommandOrOperationInvokationImpl.class) {
			EitherCommandOrOperationInvokation cmdi = (EitherCommandOrOperationInvokation) element;

			// TODO move to utility shared with previous alternative 
			ServiceSpecification ss = (ServiceSpecification) cmdi.eContainer().eContainer().eContainer().eContainer();
			EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, endpointNameForFlow((Orchestration)cmdi.eContainer().eContainer().eContainer()));

			String opName = getNameOfFirstSimpleCommandInvocation(cmdi);

			if(!TransformationHelpers.operationExistsInContract(ec, DataTypeTransformationHelpers.decapitalizeName(opName))) {			
				Operation operation = TransformationHelpers.createStateManipulatingOperation(opName, this.operationRole);
				StateTransition stateTransition = ApiDescriptionFactory.eINSTANCE.createStateTransition();
				stateTransition.setFrom(opName + "Triggered");
				stateTransition.setTo(opName + "Executed");
				operation.setSt(stateTransition);
				ec.getOps().add(operation);
				// event production/reception taken out here, would require more navigation
			}
			else
				TransformationHelpers.reportError("Can't create event processor operation, name " + DataTypeTransformationHelpers.decapitalizeName(opName) + " already taken");
		}
		else 
			TransformationHelpers.reportError("Unexpected element class : " + element.getClass());
	}
	
	private EventType getEventFromStep(DomainEventProductionStep depStep) {
		EventType de;
		try {
			// TODO (M) support more options
			de = getSimpleEventName(depStep);
			return de;
		} 
		catch (Exception e) {
			System.err.println("Unsupported flow step type, using default event name");
			return null;
		}
	}

	private EventType getSimpleEventName(DomainEventProductionStep depStep) {
		EventType de = null;
		try {
			de = depStep.getEventProduction().getSep().getEvents().get(0);
		} catch (Exception e) {
			TransformationHelpers.reportError("Unsupported flow syntax. Only simple events can be transformed.");
		}
		return de;
	}

	private String getCommandNameFromStep(DomainEventProductionStep depStep) {
		String opName;
		try {
			// TODO (M) support more options
			opName = getSimpleCommandName(depStep);
			return opName;
		} 
		catch (Exception e) {
			System.err.println("Unsupported flow step type, using default command name");
			return "DefaultCommandName";
		}
	}

	private String getSimpleCommandName(DomainEventProductionStep depStep) {
		String opName = null;
		try {
			opName = depStep.getAction().getCommand().getName();
		} catch (Exception e) {
			TransformationHelpers.reportError("Unsupported flow syntax. Only simple commands can be transformed.");
		}
		return opName;
	}

	private String getNameOfFirstSimpleCommandInvocation(EitherCommandOrOperationInvokation cmdi) {
		try {
			// TODO (H) support more options
			String opName = cmdi.getCi().getSci().getCommands().get(0).getName();
			return opName;
		} 
		catch (Exception e) {
			System.err.println("Unsupported flow step type, using default command name");
			return "DefaultCommandName";
		}
	}
	
	private String endpointNameForFlow(Orchestration flow) {
		return flow.getName() + "Endpoint";
	}
}
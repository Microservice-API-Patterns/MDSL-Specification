package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.CommandTypes;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EitherCommandOrOperation;
import io.mdsl.apiDescription.EitherCommandOrOperationInvokation;
import io.mdsl.apiDescription.EventProduction;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleCommandInvokation;
import io.mdsl.apiDescription.SingleEventProduction;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;

class AddApplicationFlowStep implements ISemanticModification {
	private String stepType;

	public AddApplicationFlowStep(String stepType) {
		this.stepType = stepType;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		if(stepType.equals("CIS"))
			addCisStep(element, context);
		else if(stepType.equals("DEP"))
			addDepStep(element, context);
	}
	
	private void addCisStep(EObject element, IModificationContext context) {		
		DomainEventProductionStep depStep = (DomainEventProductionStep) element;
		
		// dep->flow step->flow->service spec (also needed for new command)
		Orchestration flow = (Orchestration) depStep.eContainer().eContainer();
		
		CommandInvokationStep cisStep = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();
		
		EventType ev = getFirstEventInSimpleStep(depStep);
			
		String suggestedCommandName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(ev.getName()) + "Processor"; 
		
		// add event reference to flow step
		cisStep.getEvents().add(ev);
		
		EitherCommandOrOperationInvokation ecoi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
		CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
		SingleCommandInvokation sci = ApiDescriptionFactory.eINSTANCE.createSingleCommandInvokation();
		
		CommandType ct = ApiDescriptionFactory.eINSTANCE.createCommandType();
		ct.setName(suggestedCommandName);
		if(ev.getContent()!=null)
			ct.setSubject(EcoreUtil.copy(ev.getContent()));
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		
		// uniqueness not checked, so resulting MDSL might be invalid temporarily
		CommandTypes cmds = ApiDescriptionFactory.eINSTANCE.createCommandTypes();
		cmds.getCommands().add(ct);
		ss.getCommands().add(cmds);
		
		sci.getCommands().add(ct);
		ci.setSci(sci);
		ecoi.setCi(ci);
		cisStep.setAction(ecoi);
		
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setCisStep(cisStep);
		flow.getSteps().add(fs);
	}

	public void addDepStep(EObject element, IModificationContext context) {
		CommandInvokationStep cisStep = (CommandInvokationStep) element;		

		// cis->flow step->flow->service spec (needed for new event)
		Orchestration flow = (Orchestration) cisStep.eContainer().eContainer();
			
		DomainEventProductionStep depStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep(); 
		
		CommandType ct = getFirstCommandInSimpleStep(cisStep);
		
		EitherCommandOrOperation coo = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
		// coo.setActor("AnonymousActor"); // TODO tbd
		coo.setCommand(ct);
		depStep.setAction(coo);
		
		String suggestedEventName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(ct.getName()) + "Completed";
				
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		// add event type (on service specification level), suggesting a name that might have to be modified
		EventType de = TransformationHelpers.findOrCreateUniqueEventType(ss, suggestedEventName);
		
		// add event reference to flow step
		EventProduction ep = ApiDescriptionFactory.eINSTANCE.createEventProduction();
		SingleEventProduction sep = ApiDescriptionFactory.eINSTANCE.createSingleEventProduction();
		sep.getEvents().add(de);
		ep.setSep(sep);
		depStep.setEventProduction(ep);
		depStep.getEventProduction().getSep().getEvents().add(de);

		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		fs.setDepStep(depStep);
		flow.getSteps().add(fs);
	}
	
	// ** helpers (TODO improve flow navigation in them) 
	
	private EventType getFirstEventInSimpleStep(DomainEventProductionStep depStep) {
		try {
			EventType ev = depStep.getEventProduction().getSep().getEvents().get(0);
			return ev;
		} catch (Exception e) {
			TransformationHelpers.reportError("Unsupported flow syntax. Only simple event productions can be transformed.");
			return null;
		}
	}

	private CommandType getFirstCommandInSimpleStep(CommandInvokationStep cisStep) {
		CommandType ct;
		try {
			ct = cisStep.getAction().getCi().getSci().getCommands().get(0);
			return ct;
		} catch (Exception e) {
			TransformationHelpers.reportError("Unsupported flow syntax. Only simple command invocations can be transformed.");
			return null;
		}
	}
}
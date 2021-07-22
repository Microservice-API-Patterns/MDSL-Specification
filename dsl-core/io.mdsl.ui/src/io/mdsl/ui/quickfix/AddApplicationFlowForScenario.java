package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.EitherCommandOrOperationInvokation;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleCommandInvokation;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.ScenarioTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;

class AddApplicationFlowForScenario implements ISemanticModification {

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		IntegrationScenario scenario = (IntegrationScenario) element;
		ServiceSpecification ss = (ServiceSpecification) scenario.eContainer();
		
		Orchestration flow = createFlowIfPossible(ss, scenario);
		for(IntegrationStory story : scenario.getStories()) { 
			FlowStep fs = stepForStory(ss, story);
			flow.getSteps().add(fs);
		}
				
		// add flow to ServiceSpecification that includes the scenario
		ss.getOrchestrations().add(flow);
	}

	private Orchestration createFlowIfPossible(ServiceSpecification ss, IntegrationScenario scenario) {
		String suggestedName = scenario.getName() + "Flow";
		// check that name is not already taken, return existing flow if so (flow steps will then be added to it) 
		for(Orchestration exflow : ss.getOrchestrations()) {
			if(exflow.getName().equals(suggestedName)) {
				return exflow;
				// TransformationUtilities.reportError("An application orchestration flow " + suggestedName + " already exists. Please rename the scenario.");
			}
		}
		
		Orchestration flow = ApiDescriptionFactory.eINSTANCE.createOrchestration();
		flow.setName(suggestedName);
		flow.setScenario(scenario);
		
		return flow;
	}
	
	private FlowStep stepForStory(ServiceSpecification ss, IntegrationStory story) {
		FlowStep fs = ApiDescriptionFactory.eINSTANCE.createFlowStep();
		
		CommandInvokationStep cis = ApiDescriptionFactory.eINSTANCE.createCommandInvokationStep();
		EitherCommandOrOperationInvokation coi = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperationInvokation();
		CommandInvokation ci = ApiDescriptionFactory.eINSTANCE.createCommandInvokation();
		SingleCommandInvokation sci = ApiDescriptionFactory.eINSTANCE.createSingleCommandInvokation();
		
		// TODO (L) more: map role and goal too? (comment, new grammar element?)
		// depStep has [delegates to] for actor (but we create cis here); goals could go to new eval. step 
		
		String storyAction = ScenarioTransformationHelpers.getActionName(story.getAction(), 
				ScenarioTransformationHelpers.getFirstObjectName(story));
														
		String suggestedEventName = null;
		if(story.getCondition()==null || story.getCondition().equals(""))
			suggestedEventName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(storyAction) + "Trigger"; 
		else
			suggestedEventName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(story.getCondition());
		
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
}
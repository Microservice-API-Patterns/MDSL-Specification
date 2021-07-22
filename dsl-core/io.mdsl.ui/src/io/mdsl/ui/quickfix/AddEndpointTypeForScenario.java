package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.transformations.TransformationHelpers;

public class AddEndpointTypeForScenario implements ISemanticModification {
	
	private boolean generateOperations;
	
	AddEndpointTypeForScenario(boolean withOperations) {
		this.generateOperations = withOperations;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		
		IntegrationScenario scenario = (IntegrationScenario) element;
		ServiceSpecification ss = (ServiceSpecification) scenario.eContainer();
			
		String endpointName = scenario.getName() + "RealizationEndpoint";
		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, endpointName);
		ec.setScenario(scenario); 
		
		AddOperationForScenarioStory ao4ss = new AddOperationForScenarioStory(/*"n/a"*/);
		
		if(generateOperations) {
			for(IntegrationStory story: scenario.getStories()) {
				ao4ss.apply(story, context);
			}
		}
			
		ss.getContracts().add(ec);
	}
}

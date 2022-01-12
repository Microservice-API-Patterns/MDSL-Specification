package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.transformations.TransformationChains;
import io.mdsl.transformations.TransformationHelpers;

public class AddEndpointTypeWithPatternSupport extends QuickfixSemanticModification {

	private String desiredQuality;

	AddEndpointTypeWithPatternSupport(String desiredQuality) {
		this.desiredQuality = desiredQuality;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if (!(element instanceof IntegrationScenario)) {
			TransformationHelpers.reportError("AddEndpointTypeWithPatternSupport expects an IntegrationScenario as input");
		}
		TransformationChains tc = new TransformationChains();
		tc.applyEntireChainToScenariosAndItsStories((IntegrationScenario) element, desiredQuality);
	}
}

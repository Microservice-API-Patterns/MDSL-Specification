package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.transformations.ScenarioTransformations;
import io.mdsl.transformations.TransformationHelpers;

public class AddEndpointTypeForScenario extends QuickfixSemanticModification {

	private boolean generateOperations;

	AddEndpointTypeForScenario(boolean withOperations) {
		this.generateOperations = withOperations;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if (!(element instanceof IntegrationScenario)) {
			TransformationHelpers.reportError("AddEndpointTypeForScenario expects an IntegrationScenario as input");
		}
		IntegrationScenario scenario = (IntegrationScenario) element;
		ScenarioTransformations et = new ScenarioTransformations();
		et.addEndpointForScenario(scenario, generateOperations);
	}
}

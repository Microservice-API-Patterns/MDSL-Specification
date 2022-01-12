package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.transformations.FlowTransformations;

class AddApplicationFlowForScenario extends QuickfixSemanticModification {

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		IntegrationScenario scenario = (IntegrationScenario) element;
		FlowTransformations ft = new FlowTransformations();
		ft.addApplicationFlowForScenario(scenario);
	}
}
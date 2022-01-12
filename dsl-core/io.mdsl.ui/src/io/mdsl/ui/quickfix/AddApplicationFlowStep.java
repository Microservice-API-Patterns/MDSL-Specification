package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.transformations.FlowTransformations;

class AddApplicationFlowStep extends QuickfixSemanticModification {
	private String stepType;

	public AddApplicationFlowStep(String stepType) {
		this.stepType = stepType;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		FlowTransformations ft = new FlowTransformations();

		if (stepType.equals(FlowTransformations.CIS_STEP)) {
			ft.addCisStep(element);
		} else if (stepType.equals(FlowTransformations.DEP_STEP)) {
			ft.addDepStep(element);
		}
	}
}
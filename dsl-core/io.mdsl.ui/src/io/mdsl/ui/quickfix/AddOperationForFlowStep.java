package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.transformations.FlowTransformations;

class AddOperationForFlowStep extends QuickfixSemanticModification {
	private String stepType;

	public AddOperationForFlowStep(String stepType) {
		this.stepType = stepType;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		FlowTransformations ft = new FlowTransformations();
		ft.addOperationForFlowStep(element, stepType);
	}
}
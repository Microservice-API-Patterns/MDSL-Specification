package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Orchestration;
import io.mdsl.transformations.FlowTransformations;;

class AddEndpointTypeSupportingFlow extends QuickfixSemanticModification {
	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		Orchestration flow = (Orchestration) element;
		FlowTransformations ft = new FlowTransformations();
		ft.addEndpointTypeSupportingFlow(flow);
	}
}
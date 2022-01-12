package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.transformations.OperationTransformations;

public class AddOperationsAccordingToMAPDecoration extends QuickfixSemanticModification {

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		EndpointContract ec = (EndpointContract) element;
		OperationTransformations ot = new OperationTransformations();
		ot.addOperationsForRole(ec);
	}
}

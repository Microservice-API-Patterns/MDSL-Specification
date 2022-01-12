package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.OperationTransformations;
import io.mdsl.transformations.TransformationHelpers;

class SplitOperation extends QuickfixSemanticModification {
	private boolean touchResponse=false; 

	public SplitOperation(boolean touchResponse) {
		this.touchResponse = touchResponse;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		if (!(element instanceof Operation)) {
			TransformationHelpers.reportError("This Quick Fix can only be applied if an operation is selected.");
		}
		
		Operation operation = (Operation) element;
		OperationTransformations opt = new OperationTransformations();
		opt.splitOperation(operation, touchResponse);
	}
}
package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddWishTemplate extends QuickfixSemanticModification {
	public AddWishTemplate() {
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if (!(element instanceof Operation)) {
			TransformationHelpers.reportError("This type of Add Wish Template Quick Fix can only be applied if an operation is selected.");
		}
		Operation operation = (Operation) element;
		MessageTransformations.addWishTemplate(operation.getRequestMessage().getPayload(), operation.getResponseMessage().getPayload());
	}
}
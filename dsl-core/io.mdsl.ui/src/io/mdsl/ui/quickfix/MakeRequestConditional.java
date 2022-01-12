package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;

class MakeRequestConditional extends QuickfixSemanticModification {
	private String variant = "lastModifiedAt";

	public MakeRequestConditional(String variant) {
		this.variant = variant;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if (!(element instanceof Operation)) {
			TransformationHelpers.reportError("This type of Add Wish Template Quick Fix can only be applied if an operation is selected.");
		}
		Operation operation = (Operation) element;
		MessageTransformations.makeRequestConditional(operation, variant);	
	}
}
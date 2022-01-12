package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddRequestBundle extends QuickfixSemanticModification {
	private String type;

	public AddRequestBundle(String string) {
		type = string;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if (type.equals("fromSterotype")) {
			// check that element actually always is a element structure
			if (!(element instanceof ElementStructure)) {
				TransformationHelpers.reportError("Internal error: This type of Add Request Bundle Quick Fix can only be applied if an element structure is selected.");
			}
			MessageTransformations.addRequestBundle((ElementStructure) element, true); // not sure about 'true' (but not in use at present)
		} else if (type.equals("fromOperationRequest")) {
			if (!(element instanceof Operation)) {
				TransformationHelpers.reportError("This type of Add Request Bundle Quick Fix can only be applied if an operation is selected.");
			}
			Operation operation = (Operation) element;
			MessageTransformations.addRequestBundle(operation.getRequestMessage().getPayload(), true);
		} else if (type.equals("fromOperationResponse")) {
			if (!(element instanceof Operation)) {
				TransformationHelpers.reportError("This type of Add Request Bundle Quick Fix can only be applied if an operation is selected.");
			}
			Operation operation = (Operation) element;
			MessageTransformations.addRequestBundle(operation.getResponseMessage().getPayload(), false);
		} else {
			TransformationHelpers.reportError("Add Request Bundle Quick Fix of unknown type: " + type);
		}
	}
}
package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddPagination extends QuickfixSemanticModification {
	private String type; // TODO use to distinguish between cursor-based and offset-based

	public AddPagination(String type) {
		this.type = type;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		// TODO sunset the first option "offsetFromSterotype"
		if (type.equals("offsetFromSterotype")) {
			// check that element actually always is a element structure
			if (!(element instanceof ElementStructure)) {
				TransformationHelpers.reportError("This type of Add Pagination Quick Fix can only be applied if an element structure in a response payload is selected.");
			}

			// TODO should pass operation not response payload to improve navigation in helper (impact on MDSL-Web?)
			MessageTransformations.addPagination((ElementStructure) element, "pageFromOperation");
		} else if (type.equals("pageFromOperation") | type.equals("offsetFromOperation") | type.equals("cursorFromOperation")) {
			if (!(element instanceof Operation)) {
				TransformationHelpers.reportError("This type of Add Pagination Quick Fix can only be applied if an operation is selected.");
			}
			Operation operation = (Operation) element;

			// TODO should pass operation not response payload to improve navigation in helper (impact on MDSL-Web?)
			MessageTransformations.addPagination(operation.getResponseMessage().getPayload(), type);
		} else {
			TransformationHelpers.reportError("Add Pagination Quick Fix of unknown type: " + type);
		}
	}
}
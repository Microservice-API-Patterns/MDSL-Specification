package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddWishList extends QuickfixSemanticModification {
	private String type; 

	public AddWishList(String string) {
		type = string;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if(type.equals("fromOperation")) {
			if(!(element instanceof Operation)) {
				TransformationHelpers.reportError("This type of Add Wish List Quick Fix can only be applied if an operation is selected.");
			}
			Operation operation = (Operation) element;
			MessageTransformations.addWishList(operation);
		}
		else {
			TransformationHelpers.reportError("Add Wish List Quick Fix of unknown type: " + type);
		}	
	}
}
package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;

class ExtractInformationHolder extends QuickfixSemanticModification {
	private String type; 

	public ExtractInformationHolder(String type) {
		this.type = type;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
	
		// check that element actually always is an operation
		if(!(element instanceof Operation)) {
			TransformationHelpers.reportError("ExtractInformationHolder Quick Fix can only be applied if an operation with PT in a response payload is selected.");
		}
				
		if(type.equals("fromRequest")) {
			MessageTransformations.extractInformationHolder((Operation)element, true);
		} else if (type.equals("fromResponse")) {
			MessageTransformations.extractInformationHolder((Operation)element, false);
		} else {
			TransformationHelpers.reportError("Invalid type of ExtractInformationHolder Quick Fix: " + type);
		}
	}
}
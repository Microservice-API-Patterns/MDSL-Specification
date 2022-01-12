package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.transformations.MessageTransformations;

class IntroduceParameterTreeDTO extends QuickfixSemanticModification {
	
	@Override
	public void performQuickfix(EObject element, IModificationContext context) {	
		MessageTransformations.addParameterTreeWrapper(element);	
	}
}
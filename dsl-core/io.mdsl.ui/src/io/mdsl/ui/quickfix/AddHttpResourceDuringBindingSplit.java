package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddHttpResourceDuringBindingSplit extends QuickfixSemanticModification {

	/*
	private String resourceName; 
	private static int resourceCounter = 1; 
	*/
	
	public AddHttpResourceDuringBindingSplit(String resourceName) {
		// this.resourceName = resourceName;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		if (!(element instanceof HTTPResourceBinding)) {
			TransformationHelpers.reportError("AddHttpResourceBinding expects an HTTP Resource Binding.");
			return;
		}

		HTTPResourceBinding hrb = (HTTPResourceBinding) element;
		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		hbts.addHttpResourceDuringBindingSplit(hrb);	
	}	
}
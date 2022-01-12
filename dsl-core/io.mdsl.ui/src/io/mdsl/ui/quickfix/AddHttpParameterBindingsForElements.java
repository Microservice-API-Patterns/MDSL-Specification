package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddHttpParameterBindingsForElements extends QuickfixSemanticModification {

	public AddHttpParameterBindingsForElements() {
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		if (!(element instanceof HTTPOperationBinding)) {
			TransformationHelpers.reportError("AddHttpParameterBinding expects an HTTP Operation Binding.");
			return;
		}

		HTTPOperationBinding opb = (HTTPOperationBinding) element;
		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		hbts.addHttpParameterBindingsForElements(opb);
	}
}
package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddHttpProviderAndBindingForContract extends QuickfixSemanticModification {
	// private final static String DEFAULT_LOCATION = "http://localhost:8080";
	// private String defaultResourceName;

	public AddHttpProviderAndBindingForContract(String defaultResourceName) {
		// this.defaultResourceName = defaultResourceName;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		if (!(element instanceof EndpointContract)) {
			TransformationHelpers.reportError("AddHttpProviderAndBindingForContract expects an Endpoint Contract.");
			return;
		}

		EndpointContract contract = (EndpointContract) element;
		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		hbts.addBinding(contract);
	}
}
package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.validation.HTTPBindingValidator;

class AddHttpResourceForURITemplate extends QuickfixSemanticModification {
	private String defaultUriTemplate; 
	private String message; 
	
	public AddHttpResourceForURITemplate(String uriTemplate, String message) {
		this.defaultUriTemplate = uriTemplate;
		this.message = message;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		if (!(element instanceof HTTPOperationBinding)) {
			TransformationHelpers.reportError("Add HTTP Resource For URI Template expects an HTTP Operation Binding.");
			return;
		}
		
		String relativeUri;
		if (message.contains(HTTPBindingValidator.URI_TEMPLATE_MISSING_TEXT)) { // message from HTTPBindingValidator
			relativeUri = message.substring(message.lastIndexOf('{', message.length()));
		} else {
			relativeUri = defaultUriTemplate;
		}
		
		HTTPOperationBinding hob = (HTTPOperationBinding) element;
		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		hbts.addHttpResourceForURITemplate(hob, relativeUri);
	}
}
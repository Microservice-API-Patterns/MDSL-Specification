package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.validation.HTTPBindingValidator;

class AddURITemplateToExistingHttpResource extends QuickfixSemanticModification {
	private String defaultUriTemplate; 
	private String message; 
	
	public AddURITemplateToExistingHttpResource(String defaultUri, String message) {
		this.defaultUriTemplate = defaultUri;
		this.message = message;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		if (!(element instanceof HTTPOperationBinding)) {
			TransformationHelpers.reportError("Add URI Template expects an HTTP Operation Binding.");
			return;
		}

		HTTPOperationBinding hob = (HTTPOperationBinding) element;

		// now parsing "message" here, to avoid tight coupling to it in core project
		String relativeUri;
		if (message.contains(HTTPBindingValidator.URI_TEMPLATE_MISSING_TEXT)) { // message from HTTPBindingValidator!
			relativeUri = message.substring(message.lastIndexOf('{', message.length()));
		} else {
			relativeUri = defaultUriTemplate;
		}
		
		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		hbts.addURITemplateToExistingHttpResource(hob, relativeUri);
	}	
}
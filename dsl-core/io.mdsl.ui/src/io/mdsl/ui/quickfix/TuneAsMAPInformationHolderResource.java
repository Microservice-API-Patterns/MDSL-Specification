package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.EndpointContract;

public class TuneAsMAPInformationHolderResource  implements ISemanticModification  {

	public void apply(EObject element, IModificationContext context) throws Exception {
		EndpointContract etype = (EndpointContract) element;
		// TODO use helper
		etype.setPrimaryRole("INFORMATION_HOLDER_RESOURCE");
	}
}

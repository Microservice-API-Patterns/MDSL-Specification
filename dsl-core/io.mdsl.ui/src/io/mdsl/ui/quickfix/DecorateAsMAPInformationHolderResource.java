package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.transformations.MAPDecoratorHelpers;

public class DecorateAsMAPInformationHolderResource extends QuickfixSemanticModification {

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		EndpointContract etype = (EndpointContract) element;
		MAPDecoratorHelpers.setRoleToInformationHolderResource(etype);
	}
}

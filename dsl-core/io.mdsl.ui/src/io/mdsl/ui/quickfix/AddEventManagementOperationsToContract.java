package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.transformations.EndpointTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddEventManagementOperationsToContract extends QuickfixSemanticModification {
	private String decorator;

	public AddEventManagementOperationsToContract(String mapRole) {
		decorator = mapRole;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		// check that element actually is an event
		if (!(element instanceof Event)) {
			TransformationHelpers.reportError("This Quick Fix can only be applied to events.");
		}

		Event event = (Event) element;
		EndpointContract ec = (EndpointContract) event.eContainer();

		EndpointTransformations.createEventProcessorOperation(ec, event, decorator);
		EndpointTransformations.createRetrievalOperationsForEvent(ec, event, decorator);
	}
}
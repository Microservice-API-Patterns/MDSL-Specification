package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.impl.EventImpl;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.TransformationHelpers;

class AddEventManagementOperationsToContract implements ISemanticModification {
	private String type;

	public AddEventManagementOperationsToContract(String string) {
		type = string;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		// check that element actually is an event
		if(element.getClass()!=EventImpl.class) {
			System.err.println("This Quick Fix can only be applied to events.");
			throw new MDSLException("This Quick Fix can only be applied to events.");
		}
		
		Event event = (Event) element;
		
		// could create in a sibling endpoint ("nnCQRS")? "Split Endpoint IR" can take care of that
		EndpointContract ec = (EndpointContract) event.eContainer();
		
		TransformationHelpers.createEventProcessorOperation(ec, event, type);
		TransformationHelpers.createRetrievalOperations(ec, event, type);
		
		// TODO [R] support snapshotting (additional RO, which should not map to HTTP GET as well)
	}
}
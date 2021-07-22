package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.impl.ElementStructureImpl;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.MessageTransformations;

class AddRequestBundle implements ISemanticModification {
	private String type; // TODO use to define request or request/response bundle?

	public AddRequestBundle(String string) {
		type = string;
	}
	
	// TODO (M) [R] the high-end version of the IR (from TRC repo) should not be a quick fix but a menu option
	
	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
	
		// check that element actually always is a element structure
		if(element.getClass()!=ElementStructureImpl.class) {
			System.err.println("Internal error: Add Request Bundle Quick Fix can only be applied if an element structure is selected.");
			throw new MDSLException("Internal error: Add Request Bundle Quick Fix can only be applied if an element structure is selected.");
		}
		
		MessageTransformations.addRequestBundle((ElementStructure)element);	
	}
}
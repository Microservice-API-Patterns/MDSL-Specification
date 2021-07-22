package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.impl.ElementStructureImpl;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.MessageTransformations;

class AddWishList implements ISemanticModification {
	private String type; 

	public AddWishList(String string) {
		type = string;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
	
		// check that element actually always is a element structure
		if(element.getClass()!=ElementStructureImpl.class) {
			System.err.println("Add Wish List Quick Fix can only be applied if an element structure is selected.");
			throw new MDSLException("Add Wish List Quick Fix can only be applied if an element structure is selected.");
		}
		
		MessageTransformations.addWishList((ElementStructure)element, type);	
	}
}
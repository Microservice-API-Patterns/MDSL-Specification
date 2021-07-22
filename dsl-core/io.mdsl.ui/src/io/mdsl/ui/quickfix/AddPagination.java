package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.impl.DataTransferRepresentationImpl;
import io.mdsl.apiDescription.impl.ElementStructureImpl;
import io.mdsl.apiDescription.impl.OperationImpl;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;

class AddPagination implements ISemanticModification {
	private String type; // TODO use to distinguish between cursor-based and offset-based

	public AddPagination(String string) {
		type = string;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
	
		// check that element actually always is a element structure
		if(element.getClass()!=ElementStructureImpl.class) {
			System.err.println("Add Pagination Quick Fix can only be applied if an element structure in a response payload is selected.");
			throw new MDSLException("Add Pagination Quick Fix can only be applied if an element structure in a response payload is selected.");
		}
				
		MessageTransformations.addPagination((ElementStructure)element);
	}
}
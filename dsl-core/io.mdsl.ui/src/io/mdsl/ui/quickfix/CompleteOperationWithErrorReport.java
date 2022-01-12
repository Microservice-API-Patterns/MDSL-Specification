package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.OperationTransformations;

public class CompleteOperationWithErrorReport extends QuickfixSemanticModification  {

	public void performQuickfix(EObject element, IModificationContext context) {
		// this is a very basic transformation, merely demonstrating the report syntax 
		Operation operation = (Operation) element;
		OperationTransformations ot = new OperationTransformations();
		ot.completeOperationWithErrorReport(operation);
	}
}

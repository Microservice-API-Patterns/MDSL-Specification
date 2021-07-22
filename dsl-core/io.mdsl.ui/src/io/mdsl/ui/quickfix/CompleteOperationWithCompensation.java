package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

public class CompleteOperationWithCompensation implements ISemanticModification  {

	public void apply(EObject element, IModificationContext context) throws Exception {

		Operation operation = (Operation) element;
		String opRefName = "undo";
		
		Shell shell = Display.getCurrent().getActiveShell();
		NamePromptDialog dialog = new NamePromptDialog(shell, "Specify compensating operation", "Please enter the name of an existing operation", true);
		dialog.create();
		if (dialog.open() == Window.OK) {
			if(dialog.getFirstName()==null || dialog.getFirstName().equals(""))
				opRefName = "undo_" + operation.getName();
			else
				opRefName = dialog.getFirstName();
		 } else {
			// user aborted
			System.err.println("No name for compensating operation received from user interface dialog.");
		}
		
		// TODO allow other contracts too (channels)? 
		EndpointContract ec = (EndpointContract) operation.eContainer();
		
		Operation opRef = TransformationHelpers.findOperationInContract(ec, opRefName);
		if(opRef!=null)
			operation.setUndo(opRef);
		else 
			System.err.println("Compensating operation " + opRefName + " not found in contract " + ec.getName());
	}
}

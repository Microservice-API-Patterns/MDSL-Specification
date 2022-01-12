package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.OperationTransformations;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

public class CompleteOperationWithCompensation extends QuickfixSemanticModification {

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		Operation operation = (Operation) element;
		String opRefName = "undo";
	
		Shell shell = Display.getCurrent().getActiveShell();
		NamePromptDialog dialog = new NamePromptDialog(shell, "Specify compensating operation", "Please enter the name of an existing operation", true);
		dialog.create();
		if (dialog.open() == Window.OK) {
			if (dialog.getFirstName() == null || dialog.getFirstName().equals(""))
				opRefName = "undo_" + operation.getName();
			else
				opRefName = dialog.getFirstName();
		} else {
			// user aborted
			System.err.println("No name for compensating operation received from user interface dialog.");
		}
	
		// TODO (L) support other contract types too (channels)?
		OperationTransformations ot = new OperationTransformations();
		ot.completeOperationWithCompensation(operation, opRefName);
	}
}

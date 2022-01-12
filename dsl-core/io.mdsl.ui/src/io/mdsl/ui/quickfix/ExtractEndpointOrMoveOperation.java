package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.OperationTransformations;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

public class ExtractEndpointOrMoveOperation extends QuickfixSemanticModification {
	
	private String targetEndpointName; 

	public ExtractEndpointOrMoveOperation(String target) {
		targetEndpointName = target;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		Operation operation = (Operation) element;
	
		Shell shell = Display.getCurrent().getActiveShell();
		NamePromptDialog dialog = new NamePromptDialog(shell, "Specify target endpoint type", "Endpoint type may or may not exist", true);
		dialog.create();
		if (dialog.open() == Window.OK) {
			if (dialog.getFirstName() == null || dialog.getFirstName().equals("")) {
				// TODO (M) v55 check whether name is suited to identify endpoint (no white spaces etc.)
				targetEndpointName = operation.getName() + "ExtractedEndpoint";
			}
			else {
				targetEndpointName = dialog.getFirstName();
			}
		} else {
			// user aborted
			System.err.println("No name for target endpoint type received from user interface dialog.");
		}
	
		OperationTransformations ot = new OperationTransformations();
		ot.moveOperation(operation, targetEndpointName);
	}
}

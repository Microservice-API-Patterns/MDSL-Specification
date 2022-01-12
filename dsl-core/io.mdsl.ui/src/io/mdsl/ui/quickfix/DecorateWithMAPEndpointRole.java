package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

// import io.mdsl.transformations.TransformationHelper;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.MAPDecoratorHelpers;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

public class DecorateWithMAPEndpointRole extends QuickfixSemanticModification {

	private String role;

	DecorateWithMAPEndpointRole(String role) {
		this.role = role;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		EndpointContract etype = (EndpointContract) element;

		if (role == null || role.equals("") || role.equals("tbd")) {
			// offer UI to select name
			role = "\"Other_Role\"";
			Shell shell = Display.getCurrent().getActiveShell();
			NamePromptDialog dialog = new NamePromptDialog(shell, "Role name", "Please enter a role stereotype (pattern name or any string).", false);
			dialog.create();
			if (dialog.open() == Window.OK) {
				if (dialog.getFirstName() == null || dialog.getFirstName().equals("")) {
					role = "\"Other_Role\"";
				} else {
					role = "\"" + DataTypeTransformationHelpers.replaceSpacesWithUnderscores(dialog.getFirstName()) + "\"";
				}
			} else {
				// user aborted
				System.err.println("No role name received from user interface dialog.");
			}
		}

		MAPDecoratorHelpers.setRole(etype, role);
	}
}

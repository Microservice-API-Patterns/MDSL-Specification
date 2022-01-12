package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

class ConvertInlineTypeToTypeReference extends QuickfixSemanticModification {
	private static final String ANONYMOUS_TYPE_NAME = "AnonymousType";

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		DataTransferRepresentation dtr = (DataTransferRepresentation) element;
		String typeName = obtainTypeNameFromUser();
		if (typeName == null || typeName.equals("")) {
			typeName = DataTypeTransformations.getParameterName(dtr.getPayload());
		}
		if (typeName == null || typeName.equals("")) {
			typeName = ANONYMOUS_TYPE_NAME;
		}
		DataTypeTransformations.convertInlineTypeToTypeReference(dtr, typeName);
	}

	public static String obtainTypeNameFromUser() {
		// offer UI to select type name
		String typeName = ANONYMOUS_TYPE_NAME;
		Shell shell = Display.getCurrent().getActiveShell();
		NamePromptDialog dialog = new NamePromptDialog(shell, "Type name", "Please enter a data type name that is not used yet.", false);
		dialog.create();
		if (dialog.open() == Window.OK) {
			if (dialog.getFirstName() == null || dialog.getFirstName().equals("")) {
				return null;
			} else {
				typeName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(dialog.getFirstName());
			}
		} else {
			// user aborted
			System.err.println("No name for data type received from user interface dialog.");
		}
		return typeName;
	}
}
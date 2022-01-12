package io.mdsl.ui.quickfix;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.exception.MDSLException;
import io.mdsl.ui.internal.MdslActivator;

public abstract class QuickfixSemanticModification implements ISemanticModification {

	public void apply(EObject element, IModificationContext context) {

		try {
			performQuickfix(element, context);
		} catch (MDSLException e) {
			Shell shell = Display.getCurrent().getActiveShell();
			Status status = new Status(IStatus.ERROR, MdslActivator.PLUGIN_ID, e.getMessage(), e);
			ErrorDialog.openError(shell, "An error occurred", "Exception occured applying Quick Fix", status);
		}
	}

	public abstract void performQuickfix(EObject element, IModificationContext context) throws MDSLException;
}
package io.mdsl.ui.editor;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.xtext.ui.editor.XtextEditor;

import io.mdsl.ui.actions.GeneratorsActionGroup;

public class MDSLEditor extends XtextEditor {

	private ActionGroup generatorsGroup;

	@Override
	protected void createActions() {
		super.createActions();

		generatorsGroup = new GeneratorsActionGroup(this);
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);

		ActionContext context = new ActionContext(getSelectionProvider().getSelection());
		generatorsGroup.setContext(context);
		generatorsGroup.fillContextMenu(menu);
		generatorsGroup.setContext(null);
	}

}

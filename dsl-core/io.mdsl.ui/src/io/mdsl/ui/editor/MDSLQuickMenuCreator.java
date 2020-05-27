package io.mdsl.ui.editor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.actions.QuickMenuCreator;

public abstract class MDSLQuickMenuCreator extends QuickMenuCreator {

	public IHandler createHandler() {
		return new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				createMenu();
				return null;
			}
		};
	}
}

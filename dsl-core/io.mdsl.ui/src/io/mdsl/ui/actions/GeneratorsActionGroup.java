package io.mdsl.ui.actions;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.xtext.ui.editor.XtextEditor;

import com.google.common.collect.Lists;

import io.mdsl.exception.MDSLException;
import io.mdsl.ui.editor.MDSLQuickMenuCreator;

public class GeneratorsActionGroup extends ActionGroup {

	private static final String QUICK_MENU_ID = "io.mdsl.ui.edit.generator.quickMenu";

	private IHandlerService handlerService;
	private ICommandService commandService;
	private List<Command> allCommands;

	public GeneratorsActionGroup(XtextEditor editor) {
		this.handlerService = editor.getEditorSite().getService(IHandlerService.class);
		this.commandService = editor.getEditorSite().getService(ICommandService.class);
		allCommands = Lists.newArrayList(commandService.getDefinedCommands());
		installQuickAccessAction();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		addGeneratorSubmenu(menu);
	}

	private void installQuickAccessAction() {
		if (handlerService != null) {
			IHandler handler = new MDSLQuickMenuCreator() {
				@Override
				protected void fillMenu(IMenuManager menu) {
					fillGeneratorMenu(menu);
				}
			}.createHandler();
			handlerService.activateHandler(QUICK_MENU_ID, handler);
		}
	}

	private int fillGeneratorMenu(IMenuManager generatorSubmenu) {
		int added = 0;
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.OpenAPIGenerationCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.ProtocolBuffersGenerationCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.GraphQLGenerationCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.JolieGenerationCommand")); 
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.JavaGenerationCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.ALPSGenerationCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.TextFileGenerationCommand"));
		generatorSubmenu.add(new Separator());
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.GenModelJSONExporterCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.GenModelYAMLExporterCommand"));
		generatorSubmenu.add(new Separator());
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.AsyncMDSLGenerationCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.AsyncAPIGenerationCommand"));
		generatorSubmenu.add(new Separator());
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.SketchMinerGenerationCommand"));
		added += addAction(generatorSubmenu, createAction("io.mdsl.ui.handler.MDSLRefactoringCommand")); // now a QF
		return added;
	}

	private Action createAction(String commandId) {
		Optional<Command> optCommand = allCommands.stream().filter(c -> c.getId().equals(commandId)).findFirst();
		if (!optCommand.isPresent())
			return null;

		Command command = optCommand.get();
		Action action = new Action() {
			public void run() {
				try {
					handlerService.executeCommand(command.getId(), null);
				} catch (Exception e) {
					throw new MDSLException("Could not execute command with id '" + command.getId() + "'.", e);
				}
			};
		};
		try {
			action.setActionDefinitionId(command.getId());
			action.setText(command.getName());
			action.setEnabled(command.isEnabled());
		} catch (NotDefinedException e) {
			throw new MDSLException("The command with the id '" + command.getId() + "' is not properly defined!", e);
		}
		return action;
	}

	private void addGeneratorSubmenu(IMenuManager menu) {
		MenuManager contextMenu = new MenuManager("MDSL", "io.mdsl.ui.generator.menu");
		contextMenu.setActionDefinitionId(QUICK_MENU_ID);
		if (fillGeneratorMenu(contextMenu) > 0)
			menu.insertAfter("additions", contextMenu);
	}

	private int addAction(IMenuManager menu, Action action) {
		if (action != null && action.isEnabled()) {
			menu.add(action);
			return 1;
		}
		return 0;
	}

}

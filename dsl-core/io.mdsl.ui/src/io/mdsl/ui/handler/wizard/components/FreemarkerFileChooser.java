package io.mdsl.ui.handler.wizard.components;

import org.eclipse.swt.widgets.Composite;

/**
 * Component to choose a Freemarker template file.
 */
public class FreemarkerFileChooser extends FileByExtensionChooser {

	public FreemarkerFileChooser(Composite parent) {
		super(parent, "ftl");
	}

	@Override
	protected String getDialogTitle() {
		return "Choose Freemarker (*.ftl) file";
	}

	@Override
	protected String getDialogMessage() {
		return "Select the *.ftl file you want to use for generation:";
	}

}

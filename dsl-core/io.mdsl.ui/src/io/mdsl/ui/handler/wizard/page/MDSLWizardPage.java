package io.mdsl.ui.handler.wizard.page;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.program.Program;

public abstract class MDSLWizardPage extends WizardPage {

	public MDSLWizardPage(String title) {
		super(title);
	}

	@Override
	public void performHelp() {
		Program.launch("https://microservice-api-patterns.github.io/MDSL-Specification/");
	}

}

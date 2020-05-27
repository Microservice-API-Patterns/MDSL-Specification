package io.mdsl.ui.handler.wizard;

import java.util.function.Function;

import org.eclipse.jface.wizard.Wizard;

import io.mdsl.ui.handler.wizard.page.TextFileGenerationWizardPage;

/**
 * Wizard containing Freemarker generator input dialog.
 */
public class TextFileGenerationWizard extends Wizard {

	private TextFileGenerationWizardPage page;
	private Function<TextFileGenerationWizardContext, Boolean> finishFunction;
	private TextFileGenerationWizardContext context;

	public TextFileGenerationWizard(TextFileGenerationWizardContext context, Function<TextFileGenerationWizardContext, Boolean> finishFunction) {
		super();
		setNeedsProgressMonitor(true);
		this.context = context;
		this.finishFunction = finishFunction;
	}

	@Override
	public String getWindowTitle() {
		return "Generate Text File with Freemarker Templating Engine";
	}

	@Override
	public void addPages() {
		page = new TextFileGenerationWizardPage();
		page.setInitialTemplateFile(context.getFreemarkerTemplateFile());
		page.setInitialFilename(context.getTargetFileName());
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		context.setFreemarkerTemplateFile(page.getFile());
		context.setTargetFileName(page.getTargetFileName());
		return finishFunction.apply(context);
	}

}

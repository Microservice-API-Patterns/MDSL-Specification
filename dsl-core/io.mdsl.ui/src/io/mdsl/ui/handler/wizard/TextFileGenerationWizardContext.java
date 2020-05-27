package io.mdsl.ui.handler.wizard;

import org.eclipse.core.resources.IFile;

/**
 * Input data for text file generation (Freemarker generator).
 */
public class TextFileGenerationWizardContext {

	private IFile freemarkerTemplateFile;
	private String targetFileName;

	public void setFreemarkerTemplateFile(IFile freemarkerTemplateFile) {
		this.freemarkerTemplateFile = freemarkerTemplateFile;
	}

	public IFile getFreemarkerTemplateFile() {
		return freemarkerTemplateFile;
	}

	public void setTargetFileName(String targetFileName) {
		this.targetFileName = targetFileName;
	}

	public String getTargetFileName() {
		return targetFileName;
	}

}

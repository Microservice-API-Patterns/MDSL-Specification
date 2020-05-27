package io.mdsl.ui.handler;

import java.io.File;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator2;
import org.osgi.framework.Bundle;

import com.google.inject.Inject;

import io.mdsl.exception.MDSLException;
import io.mdsl.generator.TextFileGenerator;
import io.mdsl.ui.handler.wizard.TextFileGenerationWizard;
import io.mdsl.ui.handler.wizard.TextFileGenerationWizardContext;
import io.mdsl.ui.internal.MdslActivator;

/**
 * This handler calls the TextFileGenerator that uses Freemarker templates to
 * generate text files.
 */
public class TextFileGenerationHandler extends AbstractGenerationHandler {

	private static final String LAST_SELECTED_TEMPLATE_PROPERTY = "lastSelectedTemplate";
	private static final String LAST_TARGET_FILE_NAME_PROPERTY = "lastTargetFileName";

	@Inject
	private TextFileGenerator generator;

	private IFile selectedMDSLFile;

	@Override
	protected IGenerator2 getGenerator() {
		return this.generator;
	}

	@Override
	protected void runGeneration(Resource resource, ExecutionEvent event, IFileSystemAccess2 fsa) {
		selectedMDSLFile = getSelectedFile(event);
		TextFileGenerationWizardContext context = getLastStoredContext(event);
		registerEclipseProjectVariables(event);
		new WizardDialog(HandlerUtil.getActiveShell(event), new TextFileGenerationWizard(context, executionContext -> {
			generator.setFreemarkerTemplateFile(new File(context.getFreemarkerTemplateFile().getLocationURI()));
			generator.setTargetFileName(context.getTargetFileName());
			persistContext(context);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						getGenerator().doGenerate(resource, fsa, new GeneratorContext());
					} catch (MDSLException e) {
						MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Model Input", e.getMessage());
					}
				}
			});
			return true;
		})).open();
	}

	private void registerEclipseProjectVariables(ExecutionEvent event) {
		String projectName = getSelectedFile(event).getProject().getName();
		generator.registerCustomModelProperty("projectName", projectName);
		Bundle mdslBundle = Platform.getBundle("io.mdsl.ui");
		if (mdslBundle != null)
			generator.registerCustomModelProperty("mdslVersion", mdslBundle.getVersion().toString());
	}

	/**
	 * Get last selected values on dialog.
	 */
	private TextFileGenerationWizardContext getLastStoredContext(ExecutionEvent event) {
		TextFileGenerationWizardContext context = new TextFileGenerationWizardContext();
		try {
			Map<QualifiedName, String> properties = selectedMDSLFile.getPersistentProperties();
			if (!properties.containsKey(getQualifiedName4File(selectedMDSLFile, LAST_SELECTED_TEMPLATE_PROPERTY)))
				return context;
			IFile templateFile = findFileInContainer(selectedMDSLFile.getWorkspace().getRoot(),
					properties.get(getQualifiedName4File(selectedMDSLFile, LAST_SELECTED_TEMPLATE_PROPERTY)));
			context.setFreemarkerTemplateFile(templateFile);
			if (properties.containsKey(getQualifiedName4File(selectedMDSLFile, LAST_TARGET_FILE_NAME_PROPERTY)))
				context.setTargetFileName(properties.get(getQualifiedName4File(selectedMDSLFile, LAST_TARGET_FILE_NAME_PROPERTY)));
		} catch (CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, MdslActivator.PLUGIN_ID, "Could not read persisted template file location.", e));
		}
		return context;
	}

	/**
	 * Save last selected values on dialog.
	 */
	private void persistContext(TextFileGenerationWizardContext context) {
		try {
			IFile templateFile = context.getFreemarkerTemplateFile();
			selectedMDSLFile.setPersistentProperty(getQualifiedName4File(selectedMDSLFile, LAST_SELECTED_TEMPLATE_PROPERTY),
					templateFile.getProject().getName() + IPath.SEPARATOR + templateFile.getProjectRelativePath().toString());
			selectedMDSLFile.setPersistentProperty(getQualifiedName4File(selectedMDSLFile, LAST_TARGET_FILE_NAME_PROPERTY), context.getTargetFileName());
		} catch (CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, MdslActivator.PLUGIN_ID, "Could not persist template file location.", e));
		}
	}

	private QualifiedName getQualifiedName4File(IFile file, String property) {
		return new QualifiedName("io.mdsl.textFileGenerator." + property, file.getProjectRelativePath().toString());
	}

}

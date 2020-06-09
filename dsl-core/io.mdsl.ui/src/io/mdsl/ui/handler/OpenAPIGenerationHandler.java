package io.mdsl.ui.handler;

import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.OpenAPIGenerator;

public class OpenAPIGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private OpenAPIGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

	@Override
	protected void postGeneration(ExecutionEvent event) {
		if (!generator.getValidationMessages().isEmpty()) {
			MessageDialog.openWarning(HandlerUtil.getActiveShell(event), "Model Input",
					"The OpenAPI specification has been generated but has validation errors:\n\n"
							+ getErrorsString(generator.getValidationMessages()));
		}
	}

	private String getErrorsString(Set<String> messages) {
		String messageFull = "";
		for (String message : messages) {
			messageFull = " - " + message + "\n";
		}
		return messageFull;
	}

}

package io.mdsl.ui.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.github.microserviceapipatterns.protobufgen.exception.ProtocolBufferBuilderException;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.ProtocolBuffersGenerator;

public class ProtocolBuffersGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private ProtocolBuffersGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

	@Override
	protected void runGeneration(Resource resource, ExecutionEvent event, IFileSystemAccess2 fsa) {
		try {
			getGenerator().doGenerate(resource, fsa, new GeneratorContext());
		} catch (MDSLException e) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Model Input", e.getMessage());
		} catch (ProtocolBufferBuilderException e) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Protocol Buffer Generation Error",
					e.getMessage());
		} catch (Exception e) {
			handleUnexpectedException(event, e);
		}
	}

}

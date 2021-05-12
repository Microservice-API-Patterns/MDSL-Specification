package io.mdsl.ui.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.xtext.generator.IGenerator2;
import com.google.inject.Inject;
import io.mdsl.generator.asyncapi.AsyncApiGenerator;

public class AsyncAPIGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private AsyncApiGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

	@Override
	protected void postGeneration(ExecutionEvent event) {
		
	}
}

package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;
import com.google.inject.Inject;
import io.mdsl.generator.AsyncMDSLGenerator;

public class AsyncMDSLGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private AsyncMDSLGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}
}
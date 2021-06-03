package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.ALPSGenerator;

public class ALPSGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private ALPSGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}
}

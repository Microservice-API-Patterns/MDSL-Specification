package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.JavaGenerator;

public class JavaGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private JavaGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

}

package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.JolieGenerator;

public class JolieGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private JolieGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

}

package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.SketchMinerGenerator;

public class SketchMinerGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private SketchMinerGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}
}

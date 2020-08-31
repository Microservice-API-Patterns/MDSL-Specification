package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.GenModelYAMLExporter;

public class GenModelYAMLExporterHandler extends AbstractGenerationHandler {

	@Inject
	private GenModelYAMLExporter generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

}

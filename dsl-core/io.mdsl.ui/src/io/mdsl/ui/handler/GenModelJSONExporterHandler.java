package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.GenModelJSONExporter;

public class GenModelJSONExporterHandler extends AbstractGenerationHandler {

	@Inject
	private GenModelJSONExporter generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

}

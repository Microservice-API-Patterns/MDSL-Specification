package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.GraphQLGenerator;

public class GraphQLGenerationHandler extends AbstractGenerationHandler {

	@Inject
	private GraphQLGenerator generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}

}

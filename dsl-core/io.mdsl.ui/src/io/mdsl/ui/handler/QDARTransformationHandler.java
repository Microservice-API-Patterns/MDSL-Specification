package io.mdsl.ui.handler;

import org.eclipse.xtext.generator.IGenerator2;

import com.google.inject.Inject;

import io.mdsl.generator.ALPSGenerator;
import io.mdsl.ui.handler.refactoring.MoveOperationRefactoring;

public class QDARTransformationHandler extends AbstractGenerationHandler {

	@Inject
	private MoveOperationRefactoring generator;

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}
}

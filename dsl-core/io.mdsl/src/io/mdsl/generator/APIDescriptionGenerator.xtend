package io.mdsl.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import javax.inject.Inject
import io.mdsl.generator.asyncapi.AsyncApiGenerator

class APIDescriptionGenerator extends AbstractGenerator {

	@Inject AsyncApiGenerator asyncApiGenerator;

	override doGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {
		asyncApiGenerator.doGenerate(input, fsa, context);
	}

}

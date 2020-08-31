package io.mdsl.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.freemarker.FreemarkerEngineWrapper;

/**
 * Generates Java code with Freemarker template
 */
public class JavaGenerator extends AbstractMDSLGenerator {

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {
		// FreemarkerEngineWrapper fmew = new FreemarkerEngineWrapper(JavaGenerator.class, "MDSL2JavaTemplateEmpty.java.ftl");
		FreemarkerEngineWrapper fmew = new FreemarkerEngineWrapper(JavaGenerator.class, "MDSL2JavaTemplateLowLevel.java.ftl");
		fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + ".java", fmew.generate(mdslSpecification));
	}
}

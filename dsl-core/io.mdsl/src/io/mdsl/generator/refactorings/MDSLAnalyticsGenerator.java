package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.AbstractMDSLGenerator;

/**
 * Adds role stereotype PROCESSING_RESOURCE to endpoint type
 */
public class MDSLAnalyticsGenerator extends AbstractMDSLGenerator {
	private String sourceEndpoint = null;
			
	public MDSLAnalyticsGenerator(String sourceEndpoint) {
		super();
		this.sourceEndpoint = sourceEndpoint; // not used yet
	}
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		// TODO (future work) provide meaningful insights
		String result = "// Analytics PoC: This MDSL specification contains " +  mdslSpecification.getContracts().size() + " endpoint type/channel contracts.\n\n";
		RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, mdslSpecification, result);
	}
}

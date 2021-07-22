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
			
	public MDSLAnalyticsGenerator(String sourceEndpoint, String roleDecorator) {
		super();
		this.sourceEndpoint = sourceEndpoint;
	}
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		// TODO [R] just a stub, not yet implemented: provide meaningful data and test
		
		// output file name still ignored as we generate to main memory (string fsa), in download option (seems to be null)
		String fileName = inputFileURI.trimFileExtension().lastSegment() + "-refactored.mdsl";
		System.out.println("Target file name is " + fileName);
		String result = "This MDSL specification contains " +  mdslSpecification.getContracts() + " endpoint type/channel contracts."; 
		fsa.generateFile(fileName, result);
	}
}

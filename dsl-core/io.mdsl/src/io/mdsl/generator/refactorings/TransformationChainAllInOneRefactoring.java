package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.TransformationChains;

public class TransformationChainAllInOneRefactoring extends AbstractMDSLGenerator {

	public TransformationChainAllInOneRefactoring(String desiredQuality) {
		super();
		this.desiredQuality = desiredQuality;
	}

	private String desiredQuality = "all";

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		// TODO iterate and generate for all events found
		TransformationChains tc = new TransformationChains();
		tc.applyEntireChainToAllScenariosAndStories(mdslSpecification); // TODO could use the one that works with single scenario and "desiredQuality" 
		CharSequence result = "// Interface refactoring 'TransformationChainAllInOneRefactoring' applied.\n";
		RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, mdslSpecification, result);		
	}
}

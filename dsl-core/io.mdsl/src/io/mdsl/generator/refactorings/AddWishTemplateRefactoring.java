package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.utils.MDSLLogger;

public class AddWishTemplateRefactoring extends AbstractMDSLGenerator {

	public AddWishTemplateRefactoring(String sourceEndpoint, String sourceOperation) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
	}

	private String sourceEndpoint = "TestName";
	private String sourceOperation = "testOp";
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(sourceOperation)) {
						ElementStructure es = RefactoringHelpers.getRequestPayload(operation);
						MessageTransformations.addWishTemplate(operation.getRequestMessage().getPayload(), operation.getResponseMessage().getPayload());						
						CharSequence result = "// Interface refactoring 'Add WishTemplate' applied.\n";
						RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, es, result);
						return;
					}
				}
			}
		}
		MDSLLogger.reportWarning("Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
}

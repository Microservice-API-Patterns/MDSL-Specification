package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;

import io.mdsl.transformations.OperationTransformations;

public class MoveOperationRefactoring extends AbstractMDSLGenerator {

	public MoveOperationRefactoring(String sourceEndpoint, String sourceOperation, String targetEndpoint) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
		this.targetEndpoint = targetEndpoint;
	}

	private String sourceEndpoint = "TestName";
	private String sourceOperation = "testOp";
	private String targetEndpoint = "NewEndpointName"; // could be null, created then
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(sourceOperation)) {
						
						OperationTransformations moveOp = new OperationTransformations();
						/* MDSLResource targetResource = */ moveOp.moveOperation(operation, targetEndpoint);
						CharSequence result = "// Interface refactoring 'Move Operation' applied.\n";
						RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, operation, result);
						return;
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
}

package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.OperationTransformations;

public class AddOperationsForRoleRefactoring extends AbstractMDSLGenerator {

	public AddOperationsForRoleRefactoring(String sourceEndpoint) {
		super();
		this.sourceEndpoint = sourceEndpoint;
	}

	private String sourceEndpoint = "TestName";
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				CharSequence result = "// Interface refactoring 'Add Operations for MAP Decorator' applied.\n";
				
				OperationTransformations ot = new OperationTransformations();
				ot.addOperationsForRole(endpoint);
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, endpoint, result);
				return;
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " not found in input file.");
	}
}

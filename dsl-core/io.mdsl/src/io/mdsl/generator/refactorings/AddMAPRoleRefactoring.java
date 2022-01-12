package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MAPDecoratorHelpers;

/**
 * Adds role stereotype PROCESSING_RESOURCE to endpoint type
 */
public class AddMAPRoleRefactoring extends AbstractMDSLGenerator {
	private String sourceEndpoint = null;
	private String roleDecorator = "PROCESSING_RESOURCE";
			
	public AddMAPRoleRefactoring(String sourceEndpoint, String roleDecorator) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		if(roleDecorator!=null)
			this.roleDecorator = roleDecorator;
	}
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				CharSequence result = "// Interface refactoring 'Add MAP Decorator' applied.\n";
				
				if(roleDecorator.equals("PROCESSING_RESOURCE"))
					MAPDecoratorHelpers.setRoleToProcessingResource(endpoint);
				else if(roleDecorator.equals("INFORMATION_HOLDER_RESOURCE"))
					MAPDecoratorHelpers.setRoleToInformationHolderResource(endpoint);
				else if(roleDecorator.equals("COLLECTION_RESOURCE"))
					MAPDecoratorHelpers.setRoleToCollectionResource(endpoint);
				else
					System.err.println("MAP role decorator " + roleDecorator + " is not supported yet.");
				
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, endpoint, result);

				return;
			}			
		}
		System.err.println("Endpoint and/or operation not found in input file.");
	}
}

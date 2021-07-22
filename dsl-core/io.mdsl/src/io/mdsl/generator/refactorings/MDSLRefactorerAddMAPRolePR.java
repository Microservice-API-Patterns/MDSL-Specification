package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MAPDecoratorTransformationHelpers;

/**
 * Adds role stereotype PROCESSING_RESOURCE to endpoint type
 */
public class MDSLRefactorerAddMAPRolePR extends AbstractMDSLGenerator {
	private String sourceEndpoint = null;
	private String roleDecorator = "PROCESSING_RESOURCE";
			
	public MDSLRefactorerAddMAPRolePR(String sourceEndpoint, String roleDecorator) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		if(roleDecorator!=null)
			this.roleDecorator = roleDecorator;
	}
	
	// TODO (M) get pattern name from caller (Web UI, CLI), support all MAPs
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				// no need to save, transforming not generating (for CLI/Web)
				CharSequence result = "// Interface refactoring 'Add MAP Processing Resource' done:\n";
				
				if(roleDecorator.equals("PROCESSING_RESOURCE"))
					MAPDecoratorTransformationHelpers.setRoleToProcessingResource(endpoint);
				else if(roleDecorator.equals("INFORMATION_HOLDER_RESOURCE"))
					MAPDecoratorTransformationHelpers.setRoleToProcessingResource(endpoint);
				else
					System.err.println("This MAP decorator is not supported yet.");
				
				MDSLResource targetResource = new MDSLResource(endpoint.eResource());
				result = result + targetResource.getXtextResource().getSerializer().serialize(mdslSpecification);

				// output file name still ignored as we generate to main memory (string fsa), in download option (seems to be null)
				String fileName = inputFileURI.trimFileExtension().lastSegment() + "-refactored.mdsl";
				System.out.println("Target file name is " + fileName);
				fsa.generateFile(fileName, result);
				return;
			}			
		}
		System.err.println("Endpoint and/or operation not found in input file.");
	}
}

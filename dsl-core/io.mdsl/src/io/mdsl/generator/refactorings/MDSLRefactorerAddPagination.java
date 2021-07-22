package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MessageTransformations;

public class MDSLRefactorerAddPagination extends AbstractMDSLGenerator {

	public MDSLRefactorerAddPagination(String sourceEndpoint, String sourceOperation, String targetEndpoint) {
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
						ElementStructure es = findPaginationTarget(operation);
						MessageTransformations.addPagination(es);						
						CharSequence result = "// Interface refactoring 'Add Pagination' done:\n";
						
						result = result + new MDSLResource(es.eResource()).getXtextResource().getSerializer().serialize(mdslSpecification);
							
						// output file name ignored as we generate to main memory (string fsa)?
						fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + "_refactored.mdsl", result);
						System.out.println("Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " refactored.");
						return;
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}

	private ElementStructure findPaginationTarget(io.mdsl.apiDescription.Operation operation) {
		return operation.getResponseMessage().getPayload();
	}
}

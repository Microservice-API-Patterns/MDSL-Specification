package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MessageTransformations;

public class AddRequestBundleRefactoring extends AbstractMDSLGenerator {

	public AddRequestBundleRefactoring(String sourceEndpoint, String sourceOperation, boolean request, boolean response) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
		applyToRequest = request;
		applyToResponse = response;
		
	}

	private String sourceEndpoint = "n/a";
	private String sourceOperation = "n/a";
	boolean applyToRequest = true;
	boolean applyToResponse = false;
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(sourceOperation)) {
						applyRefactorings(mdslSpecification, fsa, inputFileURI, operation);
						return;
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}

	private void applyRefactorings(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI,
			Operation operation) {
		if(applyToRequest) {
			ElementStructure es = RefactoringHelpers.getRequestPayload(operation);
			if(es!=null) {
				applyRefactoring(mdslSpecification, fsa, inputFileURI, es, true);
			}
			else
				System.err.println("Element structure is null, cannot apply refactoring to request");
		}
		if(applyToResponse) {
			ElementStructure es = RefactoringHelpers.getResponsePayload(operation);
			if(es!=null) {
				applyRefactoring(mdslSpecification, fsa, inputFileURI, es, false);
			}
			else
				System.err.println("Element structure is null, cannot apply refactoring to response");
		}
	}

	private void applyRefactoring(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI,
			ElementStructure es, boolean applyToRequest) {
		es = MessageTransformations.addRequestBundle(es, applyToRequest); 
		String result = "// Interface refactoring '" + "BundleRequests" + "' applied.\n";
		RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, es, result);
	}
}

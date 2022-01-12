package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MessageTransformations;

public class AddKeyValueMapWrapperRefactoring extends AbstractMDSLGenerator {
	
	private String sourceEndpoint = "TestName";
	private String sourceOperation = "testOp";
	
	public AddKeyValueMapWrapperRefactoring(String sourceEndpoint, String sourceOperation) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
	}

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(this.sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(this.sourceOperation)) {						
						boolean refactoringTookPlace = false;
						SingleParameterNode spn = RefactoringHelpers.findWrappingTargetInRequest(operation);
						EObject resource;
						String result = "";
						if(spn!=null) {
							AtomicParameter ap = spn.getAtomP();	
							if(ap!=null) {
								result += "// Interface refactoring 'Add Add Key Value Map Wrapper(s)' applied to request message.\n";
								resource = MessageTransformations.addKeyValueMapWrapper(ap);
								RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, resource, result);
								refactoringTookPlace = true;
							}
							else
								System.err.println("[W] This refactoring can only be applied to an AP message payload at present.");
						}
						else 
							System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + ": request message must be SPN/AP.");
						
						spn = RefactoringHelpers.findWrappingTargetInResponse(operation);
						if(spn!=null) {
							AtomicParameter ap = spn.getAtomP();	
							if(ap!=null) {
								result += "// Interface refactoring 'Add Add Key Value Map Wrapper(s)' applied to response message.\n";
								resource = MessageTransformations.addKeyValueMapWrapper(ap);
								RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, resource, result);
								refactoringTookPlace = true;
							}
							else
								System.err.println("[W] This refactoring can only be applied to an AP message payload at present.");
						}
						else 
							System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + ": response message must be SPN/AP.");
				
						// returning unmodified input file if IR could not be applied
						if(!refactoringTookPlace) {
							System.err.println("[W]  Returning (copy of) untouched input.");
							RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, mdslSpecification, result);
						}
						return;
					}	
				}
			}
		}
		System.err.println("[W] Endpoint " + this.sourceEndpoint + " and/or operation " + this.sourceOperation + " not found in input file.");
	}


}

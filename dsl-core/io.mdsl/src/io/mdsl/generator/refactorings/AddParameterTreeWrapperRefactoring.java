package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MessageTransformations;

public class AddParameterTreeWrapperRefactoring extends AbstractMDSLGenerator {

	public AddParameterTreeWrapperRefactoring(String sourceEndpoint, String sourceOperation) {
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
						boolean refactoringTookPlace = false;
						// TODO could decide request/response/both based on input (see AddRequestBundleRefactoring)
						SingleParameterNode spn = RefactoringHelpers.findWrappingTargetInRequest(operation);
						EObject resource;
						String result = "";
						if(spn!=null) {
							AtomicParameter ap = spn.getAtomP();	
							TypeReference tr = spn.getTr();	
							if(ap!=null) {
								result += "// Interface refactoring 'Add Parameter Tree Wrapper(s)' applied to request message.\n";
								resource = MessageTransformations.addParameterTreeWrapper(ap);
								RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, resource, result);
								refactoringTookPlace = true;
							}
							else if(tr!=null) {
								result += "// Interface refactoring 'Add Parameter Tree Wrapper(s)' applied to request message.\n";
								resource = MessageTransformations.addParameterTreeWrapper(tr);
								RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, resource, result);
								refactoringTookPlace = true;
							}
							else
								System.err.println("[W] This refactoring can only be applied to an AP and TR request payloads at present.");
						}
						else 
							System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + ": request message must be SPN/AP.");
						
						SingleParameterNode spn2 = RefactoringHelpers.findWrappingTargetInResponse(operation);
						if(spn2!=null) {
							AtomicParameter ap = spn2.getAtomP();	
							TypeReference tr = spn2.getTr();	
							if(ap!=null) {
								result += "// Interface refactoring 'Add Parameter Tree Wrapper(s)' applied to response message.\n";
								resource = MessageTransformations.addParameterTreeWrapper(ap);
								RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, resource, result);
								refactoringTookPlace = true;
							}
							else if(tr!=null) {
								result += "// Interface refactoring 'Add Parameter Tree Wrapper(s)' applied to response message.\n";
								resource = MessageTransformations.addParameterTreeWrapper(tr);
								RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, resource, result);
								refactoringTookPlace = true;
							}
							else
								System.err.println("[W]  This refactoring can only be applied to an AP and TR response payloads at present.");
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
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
}

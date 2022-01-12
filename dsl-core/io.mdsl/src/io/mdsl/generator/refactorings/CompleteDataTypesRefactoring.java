package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.DataTypeTransformations;

public class CompleteDataTypesRefactoring extends AbstractMDSLGenerator {

	public CompleteDataTypesRefactoring(String sourceEndpoint, String sourceOperation, String type, boolean applyToRequest, boolean applyToResponse) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
		this.type = type;
		this.applyToRequest = applyToRequest;
		this.applyToResponse= applyToResponse;
	}

	private String sourceEndpoint = "TestName";
	private String sourceOperation = "testOp";
	private String type = "string";
	private boolean applyToRequest = true;
	private boolean applyToResponse = true;
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(sourceOperation)) {
						CharSequence result = "";
						applyToRequestAndOrResponse(mdslSpecification, fsa, inputFileURI, operation, result);
						return;
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}

	private void applyToRequestAndOrResponse(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI, Operation operation, CharSequence result) {
		if(applyToRequest) {
			// get AP in operation (known limitation: must be one and only one, on top level)
			if(operation.getRequestMessage().getPayload().getNp()==null){
				System.err.println("[W] Can not complete PTs");
				return;
			}
			SingleParameterNode spn = operation.getRequestMessage().getPayload().getNp();
			if(spn.getAtomP()!=null) {
				DataTypeTransformations.completeDataType(spn.getAtomP().getRat(), this.type);
				// get MDSL resource for/from it
				result = "// Interface refactoring 'Complete Data Types' applied.\n"; // TODO add request/response info
				// ElementStructure es = operation.getRequestMessage().getPayload();
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, spn, result);
			}
			// handle generic parameter as well
			else if(spn.getGenP()!=null) {
				DataTypeTransformations.convertToStringType(spn.getGenP());
				// get MDSL resource for/from it
				result = "// Interface refactoring 'Complete Data Types' applied.\n"; // TODO add request/response info
				// ElementStructure es = operation.getRequestMessage().getPayload();
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, spn, result);
			}
			else
				System.err.println("[W] Can only complete APs and genPs at present");
		}
		if(applyToResponse) {
			// get AP in operation (known limitation: must be one and only one, on top level)
			if(operation.getResponseMessage().getPayload().getNp()==null) {
				System.err.println("[W] Can not complete PTs");
				return;
			}
			SingleParameterNode spn = operation.getResponseMessage().getPayload().getNp();
			if(spn.getAtomP()!=null) {
				DataTypeTransformations.completeDataType(spn.getAtomP().getRat(), this.type);
				// get MDSL resource for/from it
				result = "// Interface refactoring 'Complete Data Types' applied (request).\n"; // TODO add request/response info
				// ElementStructure es = operation.getResponseMessage().getPayload();
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, spn, result);
			}
			// handle generic parameter as well
			else if(spn.getGenP()!=null) {
				DataTypeTransformations.convertToStringType(spn.getGenP());
				// get MDSL resource for/from it
				result = "// Interface refactoring 'Complete Data Types' applied (response).\n"; // TODO add request/response info
				// ElementStructure es = operation.getResponseMessage().getPayload();
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, spn, result);
			}
			else
				System.err.println("Can only complete APs and genPs at present");
		}		
	}
}

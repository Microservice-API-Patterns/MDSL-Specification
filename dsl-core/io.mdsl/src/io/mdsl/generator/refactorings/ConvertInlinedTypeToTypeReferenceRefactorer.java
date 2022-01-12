package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.transformations.MessageTransformations;

public class ConvertInlinedTypeToTypeReferenceRefactorer extends AbstractMDSLGenerator {

	public ConvertInlinedTypeToTypeReferenceRefactorer(String sourceEndpoint, String sourceOperation, String typeName) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
		this.typeName = typeName;
	}

	private String sourceEndpoint = "TestName";
	private String sourceOperation = "testOp";
	private String typeName = "MessagePayload";
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(sourceOperation)) {
						if(operation.getRequestMessage()!=null) { 
							String requestTypeName;
							if(this.typeName!=null) {
								requestTypeName = operation.getName() + "Request" + this.typeName + "Type";
							}
							else {
								requestTypeName = operation.getName() + "Request" + "Type";
							}
							DataTypeTransformations.convertInlineTypeToTypeReference(operation.getRequestMessage(), requestTypeName);						
							CharSequence result = "// Interface refactoring 'Convert Inlined Type to Type Reference' applied to a request payload.\n";
							RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, operation, result);
						}
						else 
							; // throw new MDSLException("Cannot extract type from empty payload");
						
						if(operation.getResponseMessage()!=null) { 					
							String responseTypeName;
							if(this.typeName!=null) {
								responseTypeName = operation.getName() + "Response" + this.typeName + "Type";
							}
							else {
								responseTypeName = operation.getName() + "Response" + "Type";
							}
							DataTypeTransformations.convertInlineTypeToTypeReference(operation.getRequestMessage(), responseTypeName);	
							CharSequence result = "// Interface refactoring 'Convert Inlined Type to Type Reference' applied to a response payload.\n";
							RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, operation, result);
						}
						else 
							; // throw new MDSLException("Cannot extract type from empty payload");
						return;
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
}

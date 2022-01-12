package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.transformations.MessageTransformations;

public class ExternalizeContextRepresentationRefactoring extends AbstractMDSLGenerator {

	public ExternalizeContextRepresentationRefactoring(String sourceEndpoint, String sourceOperation) {
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
						ElementStructure es2 = DataTypeTransformations.wrapAtomicParameterAsElementStructure(
								DataTypeTransformations.createMetadataParameter("qos", "int"));
						DataContract contextDTO = DataTypeTransformations.findOrCreateDataType(mdslSpecification, "SampleContext", es2);
						MessageTransformations.addContextRepresentation(operation.getRequestMessage().getPayload(), contextDTO);						
						CharSequence result = "// Interface refactoring 'Externalize Context Representation' applied.\n";
						RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, operation /*es */, result);
						return;
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
}

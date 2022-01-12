package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.generator.openapi.converter.HTTPBindingConverterHelpers;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.utils.MDSLSpecificationWrapper;

public class AddURITemplateToExistingHttpResourceRefactoring extends AbstractMDSLGenerator {

	public AddURITemplateToExistingHttpResourceRefactoring(String sourceEndpoint, String sourceOperation, String template) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
		this.template = template;
	}

	private String sourceEndpoint = "TestName";
	private String sourceOperation = "testOp";
	private String template = "string";
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(sourceOperation)) {
						MDSLSpecificationWrapper mdslHelper = new MDSLSpecificationWrapper(new ServiceSpecificationAdapter(mdslSpecification)); 
						EndpointInstance httpb = mdslHelper.findFirstProviderAndHttpBindingFor(endpoint);
						HTTPResourceBinding binding = RefactoringHelpers.getFirstOnlyResourceBinding(httpb);
						HTTPOperationBinding opB = HTTPBindingConverterHelpers.findOperationBindingFor(operation.getName(), binding);
						HTTPBindingTransformations hbt = new HTTPBindingTransformations();				
						hbt.addURITemplateToExistingHttpResource(opB, template);
						CharSequence result = "// Interface refactoring 'AddURITemplateToExistingHttpResource' applied.\n";
						RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, endpoint, result);
						return;
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
}

package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.HTTPBindingTransformations;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;

public class AddHttpResourceDuringBindingSplitRefactoring extends AbstractMDSLGenerator {

	public AddHttpResourceDuringBindingSplitRefactoring(String sourceEndpoint, String sourceOperation) {
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
						MDSLSpecificationWrapper mdslHelper = new MDSLSpecificationWrapper(new ServiceSpecificationAdapter(mdslSpecification)); 
						EndpointInstance httpb = mdslHelper.findFirstProviderAndHttpBindingFor(endpoint);
						HTTPResourceBinding binding = RefactoringHelpers.getFirstOnlyResourceBinding(httpb);
						HTTPBindingTransformations hbt = new HTTPBindingTransformations();				
						hbt.addHttpResourceDuringBindingSplit(binding);
						CharSequence result = "// Interface refactoring 'AddHttpResourceDuringBindingSplit' applied.\n";
						RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, endpoint, result);
						return;
					}
				}
			}
		}
		MDSLLogger.reportWarning("Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
}

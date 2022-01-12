package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.EndpointTransformations;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.utils.MDSLLogger;

public class AddEventManagementRefactoring extends AbstractMDSLGenerator {

	public AddEventManagementRefactoring(String sourceEndpoint, String eventName) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.event = eventName;
	}

	private String sourceEndpoint = "TestName";
	private String event = "testEvent";
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				EndpointTransformations.addEventManagementOperations(endpoint, event);
				CharSequence result = "// Interface refactoring 'Add Event Management' applied.\n";
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, endpoint, result);
				return;
			}
		}
		MDSLLogger.reportWarning("Endpoint " + sourceEndpoint + " and/or event " + event + " not found in input.");
	}
}

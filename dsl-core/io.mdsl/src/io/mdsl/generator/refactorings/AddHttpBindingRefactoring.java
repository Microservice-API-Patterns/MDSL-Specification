package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

// import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.HTTPBindingTransformations;

public class AddHttpBindingRefactoring extends AbstractMDSLGenerator {

	private String sourceEndpoint;

	public AddHttpBindingRefactoring(String sourceEndpoint) {
		super();
		this.sourceEndpoint = sourceEndpoint;
	}
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				HTTPBindingTransformations hbts = new HTTPBindingTransformations();
				hbts.addBinding(endpoint);
				CharSequence result = "// Interface refactoring 'Add HTTP Binding' applied.\n";
				RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, endpoint, result);
				return;
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " not found in input file.");
	}
}

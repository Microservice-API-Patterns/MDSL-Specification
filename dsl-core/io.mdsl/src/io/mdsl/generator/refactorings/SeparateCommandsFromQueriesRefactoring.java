package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.EndpointTransformations;

public class SeparateCommandsFromQueriesRefactoring extends AbstractMDSLGenerator {

	private String sourceEndpoint;

	public SeparateCommandsFromQueriesRefactoring(String sourceEndpoint) {
		super();
		this.sourceEndpoint = sourceEndpoint;
	}
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				EndpointTransformations ets = new EndpointTransformations();
				EObject resource = ets.separateCommandsFromQueries(endpoint);
				if(resource!=null) {
					String result = "// Interface refactoring 'Separate Commands from Queries' applied.\n";
					RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, resource, result);
				}
				return;
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " not found in input file.");
	}
}

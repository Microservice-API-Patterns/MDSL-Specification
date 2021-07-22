package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.serializer.ISerializer;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.MessageTransformations;

public class MDSLRefactorerAddRequestBundle extends AbstractMDSLGenerator {

	public MDSLRefactorerAddRequestBundle(String sourceEndpoint, String sourceOperation, String targetEndpoint) {
		super();
		this.sourceEndpoint = sourceEndpoint;
		this.sourceOperation = sourceOperation;
	}

	private String sourceEndpoint = "n/a";
	private String sourceOperation = "n/a";
	
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		// TODO generate MDSL file from last operation in first endpoint

		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			if(endpoint.getName().equals(sourceEndpoint)) {
				for(io.mdsl.apiDescription.Operation operation : endpoint.getOps()) {
					if(operation.getName().equals(sourceOperation)) {
						ElementStructure es = findPatternTarget(operation);
						if(es!=null) {
							System.out.println("Element structure is NOT null!");
							es = MessageTransformations.addRequestBundle(es);
							CharSequence result = "// Interface refactoring 'AddRequestBundle' done (WIP?):\n";
							
							MDSLResource tmp = new MDSLResource(es.eResource());
							XtextResource tmp2 = tmp.getXtextResource();
							ISerializer tmp3 = tmp2.getSerializer();
							result = result + tmp3.serialize(mdslSpecification);
							System.out.println("[D] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " found. Result: " + result);
							
							// output file name ignored as we generate to main memory (string fsa)?
							fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + "_refactored.mdsl", result);
							System.out.println("Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " refactored.");
							return;
						}
						else
							System.err.println("Element structure is null, cannot apply refactoring");
					}
				}
			}
		}
		System.err.println("[W] Endpoint " + sourceEndpoint + " and/or operation " + sourceOperation + " not found in input file.");
	}
	
	private ElementStructure findPatternTarget(io.mdsl.apiDescription.Operation operation) {
		// TODO [R] offer caller to select request vs. response? or both?
		return operation.getRequestMessage().getPayload();
	}
}

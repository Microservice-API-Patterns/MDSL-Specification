package io.mdsl.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.AbstractGenerator;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGeneratorContext;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ServiceSpecification;

/**
 * Abstract generator class for all generators with an MDSL model
 * (ServiceSpecification) as input.
 */
public abstract class AbstractMDSLGenerator extends AbstractGenerator {

	@Override
	public void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		this.generateFromServiceSpecification(new MDSLResource(resource).getServiceSpecification(), fsa,
				resource.getURI());
	}

	/**
	 * Override this method to generate anything with an MDSL model as input.
	 * 
	 * @param mdslSpecification the MDSL input model
	 * @param fsa               the Eclipse file system access to write output into
	 *                          the workspace
	 * @param inputFileURI      the URI of the input file (*.mdsl)
	 */
	protected abstract void generateFromServiceSpecification(ServiceSpecification mdslSpecification,
			IFileSystemAccess2 fsa, URI inputFileURI);

}

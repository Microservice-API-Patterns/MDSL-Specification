package io.mdsl.generator;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.AbstractGenerator;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGeneratorContext;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import com.google.common.collect.Iterators;

import io.mdsl.apiDescription.serviceSpecification;
import io.mdsl.exception.MDSLException;

/**
 * Abstract generator class for all generators with an MDSL model
 * (serviceSpecification) as input.
 */
public abstract class AbstractMDSLGenerator extends AbstractGenerator {

	@Override
	public void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		List<serviceSpecification> serviceSpecifications = IteratorExtensions
				.<serviceSpecification>toList(Iterators.<serviceSpecification>filter(resource.getAllContents(), serviceSpecification.class));

		if (serviceSpecifications.isEmpty())
			throw new MDSLException("The given resource does not contain an MDSL serviceSpecification.");

		// we can assume that a resource (one *.mdsl) only contains one
		// serviceSpecification object here:
		this.generateFromServiceSpecification(serviceSpecifications.get(0), fsa, resource.getURI());
	}

	/**
	 * Override this method to generate anything with an MDSL model as input.
	 * 
	 * @param mdslSpecification the MDSL input model
	 * @param fsa               the Eclipse file system access to write output into
	 *                          the workspace
	 * @param inputFileURI      the URI of the input file (*.mdsl)
	 */
	protected abstract void generateFromServiceSpecification(serviceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI);

}

package io.mdsl.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.freemarker.FreemarkerEngineWrapper;
import io.mdsl.generator.graphql.GraphQLOperationInputTypeNameResolver;
import io.mdsl.generator.graphql.GraphQLSimpleTypeMappingMethod;

/**
 * Generates GraphQL with Freemarker template
 */
public class GraphQLGenerator extends AbstractMDSLGenerator {

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		// generate graphql file per endpoint
		for (EndpointContract endpoint : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			FreemarkerEngineWrapper fmew = new FreemarkerEngineWrapper(GraphQLGenerator.class, "MDSL2GraphQL.ftl");

			// pass endpoint name for which graphql shall be generated
			fmew.registerCustomData("graphQLEndpointName", endpoint.getName());

			// register custom methods for GraphQL template
			fmew.registerCustomData("mapType", new GraphQLSimpleTypeMappingMethod());
			fmew.registerCustomData("resolveOperationInputName", new GraphQLOperationInputTypeNameResolver());

			fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + "_" + endpoint.getName() + ".graphql", fmew.generate(mdslSpecification));
		}
	}
}

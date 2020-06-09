package io.mdsl.generator;

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import com.google.common.collect.Sets;

import io.mdsl.apiDescription.serviceSpecification;
import io.mdsl.generator.openapi.converter.MDSL2OpenAPIConverter;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * Generates an OpenAPI specification with an MDSL model as input.
 * 
 * @author ska
 *
 */
public class OpenAPIGenerator extends AbstractMDSLGenerator {

	private Set<String> validationMessages;

	@Override
	protected void generateFromServiceSpecification(serviceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {
		String fileName = inputFileURI.trimFileExtension().lastSegment() + ".yaml";
		validationMessages = Sets.newHashSet();

		// convert MDSL to OpenAPI model:
		OpenAPI oas = new MDSL2OpenAPIConverter(mdslSpecification).convert();

		// serialize model as YAML file:
		String yaml = Yaml.pretty(oas);

		// validate the generated model:
		SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(yaml);
		validationMessages.addAll(parseResult.getMessages());

		// generate output file
		fsa.generateFile(fileName, yaml);
	}

	/**
	 * Allows clients to get the validation/error messages of the generated OpenAPI
	 * contract. Should ideally be empty.
	 */
	public Set<String> getValidationMessages() {
		return validationMessages;
	}

}

package io.mdsl.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;
// import org.yaml.snakeyaml.DumperOptions.FlowStyle;
// import org.yaml.snakeyaml.Yaml;
// import org.yaml.snakeyaml.constructor.Constructor;
// import org.yaml.snakeyaml.nodes.Tag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;

/**
 * Exports the generator model as JSON file.
 */
public class GenModelYAMLExporter extends AbstractMDSLGenerator {

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {
		MDSLGeneratorModel genModel = new MDSL2GeneratorModelConverter(mdslSpecification).convert();
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));

		try {
			fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + "_GeneratorModel.yaml",
					mapper.writeValueAsString(genModel));
		} catch (JsonProcessingException e) {
			throw new MDSLException("Could not serialize generator model as YAML.", e);
		}
	}
}

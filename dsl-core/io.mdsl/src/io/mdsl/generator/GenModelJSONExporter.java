package io.mdsl.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;
import io.mdsl.utils.MDSLLogger;

/**
 * Exports the generator model as JSON file.
 */
public class GenModelJSONExporter extends AbstractMDSLGenerator {

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {
		MDSLGeneratorModel genModel = new MDSL2GeneratorModelConverter(mdslSpecification).convert();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + "_GeneratorModel.json",
					objectMapper.writeValueAsString(genModel));
		} catch (JsonProcessingException e) {
			MDSLLogger.reportError("Could not serialize generator model as JSON: " + e.getOriginalMessage());
		}
	}
}

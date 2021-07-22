package io.mdsl.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.junit.jupiter.api.Test;

import io.mdsl.tests.AbstractMDSLInputIntegrationTest;

public class GenModelJSONExporterTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canExportGenModelAsJSON() throws IOException {
		// given
		Resource inputModel = getTestResource("simple-generation-input-1.mdsl");
		GenModelJSONExporter generator = new GenModelJSONExporter();

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		assertEquals(
				"{" + System.lineSeparator() + 
				"  \"apiName\" : \"TestAPI\"," + System.lineSeparator() + 
				"  \"dataTypes\" : [ ]," + System.lineSeparator() + 
				"  \"endpoints\" : [ {" + System.lineSeparator() + 
				"    \"name\" : \"TestEndpoint\"," + System.lineSeparator() + 
				"    \"operations\" : [ ]," + System.lineSeparator() +
				"    \"protocolBinding\" : {" + System.lineSeparator() +
				"      \"protocolName\" : \"Undefined\"" + System.lineSeparator() +
				"    }," + System.lineSeparator() +
				"    \"states\" : [ ]," + System.lineSeparator() +
				"    \"transitions\" : [ ]" + System.lineSeparator() +
				"  } ]," + System.lineSeparator() + 
				"  \"providers\" : [ ]," + System.lineSeparator() + 
				"  \"clients\" : [ ]," + System.lineSeparator() + 
				"  \"providerImplementations\" : [ ]," + System.lineSeparator() + 
				"  \"orchestrationFlows\" : [ ]," + System.lineSeparator() + // new in V5.2
				"  \"cuts\" : [ ]" + System.lineSeparator() + // new in V5.2
				"}",
				FileUtils.readFileToString(new File(getGenerationDirectory(), "simple-generation-input-1_GeneratorModel.json"),
						"UTF-8"));
	}

	@Override
	protected String testDirectory() {
		return "/test-data/exporter/";
	}

}

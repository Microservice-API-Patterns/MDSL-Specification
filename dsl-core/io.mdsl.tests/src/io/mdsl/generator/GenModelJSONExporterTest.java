package io.mdsl.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		generateJSONExport(inputModel);

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
				"  \"orchestrationFlows\" : [ ]," + System.lineSeparator() + 
				"  \"cuts\" : [ ]" + System.lineSeparator() + 
				"}",
				FileUtils.readFileToString(new File(getGenerationDirectory(), "simple-generation-input-1_GeneratorModel.json"),
						"UTF-8"));
	}
	
	@Test
	public void canExportSimpleFlowAndBindingInGenModelAsJSON() throws IOException {
		// given
		String testCaseName = "simple-generation-input-2";
		Resource inputModel = getTestResource(testCaseName + ".mdsl");

		// when
		generateJSONExport(inputModel);

		// then
		assertThatGeneratedFileMatchesExpectations(testCaseName + "_GeneratorModel.json");
		
		// assert with string-based helpers (also used in Camel tests)
		// String fileContent = getGeneratedFileContent("simple-generation-input-2_GeneratorModel.json");
		// assertTrue(assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, "\"name\" : \"S1RealizationEndpointHome\"", 2), "Expected 2 resource entries");
	}
	
	@Test
	public void canExportComplexFlowsInGenModelAsJSON() throws IOException {
		// given
		String testCaseName = "flowvariations";
		Resource inputModel = getTestResource(testCaseName + ".mdsl");

		// when
		generateJSONExport(inputModel);

		// then
		assertThatGeneratedFileMatchesExpectations(testCaseName + "_GeneratorModel.json");
	}

	private void generateJSONExport(Resource inputModel) {
		GenModelJSONExporter generator = new GenModelJSONExporter();
		// TODO move next three lines to method in superclass
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());
	}
	
	@Override
	protected String testDirectory() {
		return "/test-data/exporter/";
	}

}

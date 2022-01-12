package io.mdsl.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.junit.jupiter.api.Test;

import io.mdsl.tests.AbstractMDSLInputIntegrationTest;

public class TextFileFreemarkerTemplateGeneratorTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canGenerateTextFile() throws IOException {
		// given
		Resource inputModel = getTestResource("simple-generation-input-1.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("simple-test-template.ftl"));
		generator.setTargetFileName("output.txt");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = FileUtils.readFileToString(new File(getGenerationDirectory(), "output.txt"), "UTF-8");
		assertEquals("TestAPI specified in simple-generation-input-1.mdsl", fileContent);
	}

	@Test
	public void canGenerateUsingSimpleGeneratorModel() throws IOException {
		// given
		Resource inputModel = getTestResource("simple-generation-input-1.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("simple-test-with-genmodel-template.ftl"));
		generator.setTargetFileName("output.txt");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		assertEquals(
				"TestAPI specified in simple-generation-input-1.mdsl" + System.lineSeparator() + System.lineSeparator()
						+ "endpoints:" + System.lineSeparator() + "TestEndpoint" + System.lineSeparator() + "",
				FileUtils.readFileToString(new File(getGenerationDirectory(), "output.txt"), "UTF-8"));
	}
	
	private File getTestFile(String testMDSLName) {
		return new File(Paths.get("").toAbsolutePath().toString(), testDirectory() + testMDSLName);
	}

	protected File getTestInputFile(String testInputFileName) throws IOException {
		return getTestFile(testInputFileName);
	}
	
	@Test
	public void canGenerateALPSYAMLViaGenmodel() throws IOException {
		String baseFilename = "alps-generator-demo";
		
		// given
		Resource inputModel = getTestResource(baseFilename + ".mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("mdsl-to-alps-template.yaml.ftl"));
		generator.setTargetFileName(baseFilename + ".yaml");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		assertEquals(getExpectedTestResult(baseFilename + ".yaml"), getGeneratedFileContent(baseFilename + ".yaml"));
	}
	
	@Override
	protected String testDirectory() {
		return "/test-data/freemarker-generation/";
	}
}

package io.mdsl.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.service.AbstractGenericModule;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.junit.jupiter.api.BeforeEach;

import com.google.common.collect.Iterators;
import com.google.inject.Guice;

import io.mdsl.APIDescriptionStandaloneSetup;
import io.mdsl.apiDescription.ServiceSpecification;

public abstract class AbstractMDSLInputIntegrationTest {

	@BeforeEach
	public void prepare() throws IOException {
		FileUtils.deleteDirectory(getGenerationDirectory());
	}

	protected ServiceSpecification getTestSpecification(Resource resource) throws IOException {
		List<ServiceSpecification> ServiceSpecifications = IteratorExtensions.<ServiceSpecification>toList(
				Iterators.<ServiceSpecification>filter(resource.getAllContents(), ServiceSpecification.class));

		if (ServiceSpecifications.isEmpty())
			throw new RuntimeException("Cannot find MDSL spec in input resource.");
		return ServiceSpecifications.get(0);
	}

	protected Resource getTestResource(String testMDSLName) throws IOException {
		new APIDescriptionStandaloneSetup().createInjectorAndDoEMFRegistration();
		return new ResourceSetImpl().getResource(URI.createFileURI(getTestFile(testMDSLName).getAbsolutePath()), true);
	}

	private File getTestFile(String testMDSLName) {
		return new File(Paths.get("").toAbsolutePath().toString(), testDirectory() + testMDSLName);
	}

	protected File getTestInputFile(String testInputFileName) throws IOException {
		return getTestFile(testInputFileName);
	}

	protected File getGenerationDirectory() {
		return new File(Paths.get("").toAbsolutePath().toString(), "src-test-gen");
	}
	
	protected JavaIoFileSystemAccess getFileSystemAccess() {
		JavaIoFileSystemAccess fsa = new JavaIoFileSystemAccess();
		Guice.createInjector(new AbstractGenericModule() {
			public Class<? extends IEncodingProvider> bindIEncodingProvider() {
				return IEncodingProvider.Runtime.class;
			}
		}).injectMembers(fsa);
		return fsa;
	}

	protected boolean assertThatKeywordAppearsInExpectedNumberOfLines(String fileContent, String keyword, int expectedNumber) throws IOException {
		Pattern pattern = Pattern.compile(Pattern.quote(keyword));
		Matcher matcher = pattern.matcher(fileContent);
		int occurrences = 0;
		while (matcher.find()) {
			occurrences +=1;
		}
		return occurrences==expectedNumber;
	}

	protected String getExpectedTestResult(String fileName) throws IOException {
		return FileUtils.readFileToString(getTestInputFile(fileName), "UTF-8");
	}

	protected String getGeneratedFileContent(String fileName) throws IOException {
		return FileUtils.readFileToString(new File(getGenerationDirectory(), fileName), "UTF-8");
	}
	
	/**
	 * Override this method to define test file directory. Example:
	 * "/test-data/freemarker/"
	 */
	protected abstract String testDirectory();

	/**
	 * Allows testing whether a test input file leads to the expected output
	 */
	protected void assertThatGeneratedFileMatchesExpectations(String testOutputFileName) throws IOException {
		assertEquals(getExpectedTestResult(testOutputFileName), getGeneratedFileContent(testOutputFileName));
	}
}

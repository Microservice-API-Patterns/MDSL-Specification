package io.mdsl.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
// import org.apache.commons.io.FileUtils;
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
import io.mdsl.apiDescription.serviceSpecification;

public abstract class AbstractMDSLInputIntegrationTest {

	@BeforeEach
	public void prepare() throws IOException {
		FileUtils.deleteDirectory(getGenerationDirectory());
	}

	protected serviceSpecification getTestSpecification(Resource resource) throws IOException {
		List<serviceSpecification> serviceSpecifications = IteratorExtensions.<serviceSpecification>toList(
				Iterators.<serviceSpecification>filter(resource.getAllContents(), serviceSpecification.class));

		if (serviceSpecifications.isEmpty())
			throw new RuntimeException("Cannot find MDSL spec in input resource.");
		return serviceSpecifications.get(0);
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

	/**
	 * Override this method to define test file directory. Example:
	 * "/test-data/freemarker/"
	 */
	protected abstract String testDirectory();

	protected static JavaIoFileSystemAccess getFileSystemAccess() {
		JavaIoFileSystemAccess fsa = new JavaIoFileSystemAccess();
		Guice.createInjector(new AbstractGenericModule() {
			public Class<? extends IEncodingProvider> bindIEncodingProvider() {
				return IEncodingProvider.Runtime.class;
			}
		}).injectMembers(fsa);
		return fsa;
	}

}

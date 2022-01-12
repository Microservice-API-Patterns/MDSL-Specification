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

/**
 * Tests for the GraphQL generator.
 * 
 * @author ska
 *
 */
public class GraphQLGeneratorTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canGenerateScalarForPType() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-p-type", "TestEndpoint");
	}

	@Test
	public void canGenerateType4AtomicParameter() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-atomic-parameter-type", "TestEndpoint");
	}

	@Test
	public void canGenerateType4AtomicParameterList() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-atomic-parameter-list-type", "TestEndpoint");
	}

	@Test
	public void canGenerateType4AtomicParameterListWithCardinalities() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-atomic-parameter-list-with-cards-type", "TestEndpoint");
	}

	@Test
	public void canGenerateType4ParameterTree() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-parameter-tree-type", "TestEndpoint");
	}

	@Test
	public void canGenerateTypes4ParameterTreeWithReference() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-parameter-tree-with-reference", "TestEndpoint");
	}

	@Test
	public void canGenerateTypes4ParameterTreeWithSubtree() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-parameter-tree-with-subtree", "TestEndpoint");
	}

	@Test
	public void canGenerateTypes4ParameterForest() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-parameter-forest-type", "TestEndpoint");
	}

	@Test
	public void canGenerateFile4EachEndpoint() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("multiple-endpoints-test", "TestEndpoint1");
		assertThatInputFileGeneratesExpectedOutput("multiple-endpoints-test", "TestEndpoint2");
	}
	
	@Test
	public void canGenerateQueryObject4SimpleOperationWithReturnTypeOnly() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-test-query-with-return-type-only", "TestEndpoint");
	}
	
	@Test
	public void canGenerateMutationObject4SimpleOperationWithInputType() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-test-mutation-with-input-types", "TestEndpoint");
	}

	/**
	 * Allows to test whether a test input file ({baseFilename}.mdsl) leads to the
	 * expected output ({baseFilename}.yaml).
	 */
	private void assertThatInputFileGeneratesExpectedOutput(String baseFilename, String endpointName) throws IOException {
		// given
		Resource inputModel = getTestResource(baseFilename + ".mdsl");
		GraphQLGenerator generator = new GraphQLGenerator();

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		assertEquals(getExpectedTestResult(baseFilename + "_" + endpointName + ".graphql"), getGeneratedFileContent(baseFilename + "_" + endpointName + ".graphql"));
	}

	@Override
	protected String testDirectory() {
		return "/test-data/graphql-generation/";
	}

}

package io.mdsl.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.junit.jupiter.api.Test;

import io.mdsl.exception.MDSLException;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;

/**
 * Tests for the OpenAPI generator.
 * 
 * @author ska, socadk
 *
 */
public class OpenAPIGeneratorTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canGenerateHelloWorldContract() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("simple-generation-input-1");
	}

	@Test
	public void canGenerateSchemaForPType() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-p-type");
	}

	@Test
	public void canGenerateSchemaForSingleParameter() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-single-parameter-type");
	}

	@Test
	public void canGenerateSchemaForParameterList() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-parameter-list-type");
	}

	@Test
	public void canGenerateSchemaForParameterTree() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-parameter-tree-type");
	}

	@Test
	public void canGenerateSchemaForParameterForrest() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-parameter-forrest-type");
	}

	@Test
	public void canGenerateOperationWithPayload() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-test-operation-test-1");	
	}

	@Test
	public void canGenerateOperationWithInlinePayload() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-test-operation-test-2");
	}

	@Test
	public void canGenerateOperationWithReturnValue() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-test-operation-test-3");
	}

    @Test
    public void canGenerateOperationWithInlineReturnValue() throws IOException {
    	assertThatInputFileGeneratesExpectedOutput("endpoint-test-operation-test-4");
    }

	@Test
	public void canHandleCardinality4AtomicParameter() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-cardinalities-1");
	}
	
	@Test
	public void canHandleCardinality4ParameterTree() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-cardinalities-2");
	}
	
	@Test
	public void canHandleCardinality4ParameterList() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-cardinalities-3");
	}
	
	@Test
	public void canHandleCardinality4ParameterListInsideTree() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("datatype-test-cardinalities-4");
	}
	
	@Test
	public void canCreateParameters4GetOperationAndAtomicParameterList() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-parameters-test-1");
	}
	
	@Test
	public void canCreateParameters4GetOperationAndSingleAtomicParameter() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-parameters-test-2");
	}
	
	@Test
	public void canRespectHttpBindingIfSingleProviderExists() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-http-mapping-test-1");
	}
	
	@Test
	public void canRespectOnePATHParameterInHttpBinding1() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-http-mapping-PATH-parameter-test-1");
	}
	
	@Test
	public void canRespectMultiplePATHParameterInHttpBinding() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-http-mapping-PATH-parameter-test-2");
	}
	
	@Test
	public void canRespectOnePATHParameterInHttpBinding2() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-http-mapping-PATH-parameter-test-3");
	}
	
	@Test
	public void canRespectCOOKIEParameterInHttpBinding() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-http-mapping-COOKIE-parameter-test-1");
	}
	
	@Test
	public void cannotHaveDuplicateMappings() throws IOException {
		// given
		Resource inputModel = getTestResource("endpoint-test-duplicate-operations-error.mdsl");
		OpenAPIGenerator generator = new OpenAPIGenerator();

		// when, then
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());

		// TODO use this for other MDSL Exceptions too
		
		assertThrows(MDSLException.class, () -> {
			generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());
		});
	}

	@Test
	public void canGenerateMultipleOperationsAccordingToVerbMapping() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("endpoint-test-operation-test-5");
	}
	
	@Test 
	public void testHTTPBindingVerbsDatatypesInCRUDAPI() throws IOException {
		// TODO add a test with `P`and `D<void>`
		assertThatInputFileGeneratesExpectedOutput("http-binding-verbs-datatypes");
	}
	
	@Test // 
	public void testHTTPBindingVerbMappingHeuristics() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("http-binding-verb-heuristics");
	}

	@Test
	public void testHTTPBindingMAPDecoratorsAndMIMETypes() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("http-binding-map-mimetypes");
	}
	
	@Test
	public void testHTTPBindingMultipleEndpointsAndProvider() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("http-binding-multiple-endpoints-and-providers");
	}
	
	@Test
	public void testHTTPBindingReportsPoliciesHeaders() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("http-binding-reports-policies-headers");
	}
	
	
	@Test
	public void testHTTPBindingHypermediaURITemplates() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("http-binding-hypermedia-uritemplates");
	}
	
	@Test
	public void testHTTPBindingsMaturityLevels12() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("http-binding-restbucks-ml12");
	}
	
	@Test
	public void testHTTPBindingsMaturityLevels3() throws IOException {
		assertThatInputFileGeneratesExpectedOutput("http-binding-restbucks-ml3");
	}
	
	
	/**
	 * Allows testing whether a test input file ({baseFilename}.mdsl) leads to the
	 * expected output ({baseFilename}.yaml).
	 */
	private void assertThatInputFileGeneratesExpectedOutput(String baseFilename) throws IOException {
		
		// System.out.println("[*** Next test ***]: " + baseFilename + ".mdsl");
		
		// given
		Resource inputModel = getTestResource(baseFilename + ".mdsl");
		OpenAPIGenerator generator = new OpenAPIGenerator();

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		assertTrue(generator.getValidationMessages().isEmpty());
		assertEquals(getTestFileContent(baseFilename + ".yaml"), getGeneratedFileContent(baseFilename + ".yaml"));
		
		// System.out.println("[*** Done testing ***]: " + baseFilename + ".mdsl");
	}

	private String getTestFileContent(String fileName) throws IOException {
		return FileUtils.readFileToString(getTestInputFile(fileName), "UTF-8");
	}

	private String getGeneratedFileContent(String fileName) throws IOException {
		return FileUtils.readFileToString(new File(getGenerationDirectory(), fileName), "UTF-8");
	}

	@Override
	protected String testDirectory() {
		return "/test-data/openapi-generation/";
	}

}

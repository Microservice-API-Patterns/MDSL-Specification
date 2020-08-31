package io.mdsl.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.eclipse.xtext.resource.SaveOptions;
import org.junit.jupiter.api.Test;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.OpenAPIGenerator;

public class StandaloneAPITest {

	@Test
	public void canLoadMDSLResource() {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();

		// when
		MDSLResource mdsl = api.loadMDSL("./test-data/standalone/hello-world.mdsl");

		// then
		assertNotNull(mdsl);
		assertEquals("HelloWorldAPI", mdsl.getServiceSpecification().getName());
	}

	@Test
	public void canLoadMDSLResourceViaFile() {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File inputFile = new File("./test-data/standalone/hello-world.mdsl");

		// when
		MDSLResource mdsl = api.loadMDSL(inputFile);

		// then
		assertNotNull(mdsl);
		assertEquals("HelloWorldAPI", mdsl.getServiceSpecification().getName());
	}

	@Test
	public void canCreateNewMDSLModel() throws IOException {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		String filename = "./out/test-new-mdsl-model.mdsl";
		File file = new File(filename);

		// when
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = api.createMDSL(filename);
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		spec.setName("TestMDSLSpec");
		EndpointContract endpoint = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		endpoint.setName("TestEndpoint");
		spec.getContracts().add(endpoint);
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());

		// then
		assertTrue(file.exists());
		assertEquals("API description TestMDSLSpec endpoint type TestEndpoint",
				FileUtils.readFileToString(file, Charset.forName("UTF-8")));
	}

	@Test
	public void canCreateNewMDSLModelViaFile() throws IOException {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-new-mdsl-model.mdsl");

		// when
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = api.createMDSL(file);
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		spec.setName("TestMDSLSpec");
		EndpointContract endpoint = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		endpoint.setName("TestEndpoint");
		spec.getContracts().add(endpoint);
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());

		// then
		assertTrue(file.exists());
		assertEquals("API description TestMDSLSpec endpoint type TestEndpoint",
				FileUtils.readFileToString(file, Charset.forName("UTF-8")));
	}

	@Test
	public void canCallGenerator() {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		MDSLResource mdsl = api.loadMDSL("./test-data/standalone/hello-world.mdsl");
		File expectedOutput = new File("./src-gen/hello-world.yaml");
		ensureFileDoesNotExist(expectedOutput);

		// when
		api.callGenerator(mdsl, new OpenAPIGenerator());

		// then
		assertTrue(expectedOutput.exists());
	}

	@Test
	public void canGenerateIntoCustomDir() {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		MDSLResource mdsl = api.loadMDSL("./test-data/standalone/hello-world.mdsl");
		File expectedOutput = new File("./out/hello-world.yaml");
		ensureFileDoesNotExist(expectedOutput);

		// when
		api.callGenerator(mdsl, new OpenAPIGenerator(), "./out");

		// then
		assertTrue(expectedOutput.exists());
	}

	private void ensureFileDoesNotExist(File file) {
		if (file.exists())
			file.delete();
	}

}

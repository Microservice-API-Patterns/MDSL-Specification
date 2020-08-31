/*
 * Copyright 2020 The MDSL Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mdsl.generator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MDSLGeneratorModelTest {

	@Test
	public void canCreateMDSLGeneratorModelWithName() {
		// given
		MDSLGeneratorModel mdslGenModel;

		// when
		mdslGenModel = new MDSLGeneratorModel("TestAPI");

		// then
		assertEquals("TestAPI", mdslGenModel.getApiName());
	}

	@Test
	public void canAddDataType() {
		// given
		MDSLGeneratorModel mdslGenModel = new MDSLGeneratorModel("TestAPI");
		DataType testType = new DataType("TestType");

		// when
		mdslGenModel.addDataType(testType);

		// then
		assertEquals(1, mdslGenModel.getDataTypes().size());
		assertEquals("TestType", mdslGenModel.getDataTypes().get(0).getName());
	}

	@Test
	public void canAddEndpoint() {
		// given
		MDSLGeneratorModel mdslGenModel = new MDSLGeneratorModel("TestAPI");
		EndpointContract endpoint = new EndpointContract("TestEndpoint");

		// when
		mdslGenModel.addEndpoint(endpoint);

		// then
		assertEquals(1, mdslGenModel.getEndpoints().size());
		assertEquals("TestEndpoint", mdslGenModel.getEndpoints().get(0).getName());
	}

	@Test
	public void canAddProvider() {
		// given
		MDSLGeneratorModel mdslGenModel = new MDSLGeneratorModel("TestAPI");
		Provider provider = new Provider("TestProvider");

		// when
		mdslGenModel.addProvider(provider);

		// then
		assertEquals(1, mdslGenModel.getProviders().size());
		assertEquals("TestProvider", mdslGenModel.getProviders().get(0).getName());
	}

	@Test
	public void canAddClient() {
		// given
		MDSLGeneratorModel mdslGenModel = new MDSLGeneratorModel("TestAPI");
		Client client = new Client("TestClient");

		// when
		mdslGenModel.addClient(client);

		// then
		assertEquals(1, mdslGenModel.getClients().size());
		assertEquals("TestClient", mdslGenModel.getClients().get(0).getName());
	}

	@Test
	public void canAddProviderImplementation() {
		// given
		MDSLGeneratorModel mdslGenModel = new MDSLGeneratorModel("TestAPI");
		Provider provider = new Provider("TestProvider");
		ProviderImplementation providerImplementation = new ProviderImplementation("TestImpl", provider,
				StandardImplTechnology.PLAIN_JAVA);

		// when
		mdslGenModel.addProviderImplementation(providerImplementation);

		// then
		assertEquals(1, mdslGenModel.getProviderImplementations().size());
		assertEquals("TestImpl", mdslGenModel.getProviderImplementations().get(0).getName());
	}

}

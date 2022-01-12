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

public class ProviderTest {

	@Test
	public void canCreateNewProviderWithName() {
		// given
		Provider provider;

		// when
		provider = new Provider("TestProvider");

		// then
		assertEquals("TestProvider", provider.getName());
	}

	@Test
	public void canAddOfferedEndpoint() {
		// given
		Provider provider = new Provider("TestProvider");
		EndpointContract endpoint = new EndpointContract("TestEndpoint");

		// when
		provider.addEndpoint(endpoint);

		// then
		assertEquals(1, provider.offeredEndpoints().size());
		assertEquals("TestEndpoint", provider.offeredEndpoints().get(0).getName());
	}

}

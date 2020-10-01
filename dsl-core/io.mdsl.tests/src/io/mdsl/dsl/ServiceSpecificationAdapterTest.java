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
package io.mdsl.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.ServiceSpecification;

public class ServiceSpecificationAdapterTest {

	@Test
	public void canDistinguishBetweenEndpointContractsAndChannelContract() {
		// given
		ServiceSpecification spec = ApiDescriptionFactory.eINSTANCE.createServiceSpecification();
		spec.getContracts().add(ApiDescriptionFactory.eINSTANCE.createEndpointContract());
		spec.getContracts().add(ApiDescriptionFactory.eINSTANCE.createChannelContract());

		// when
		ServiceSpecificationAdapter adapter = new ServiceSpecificationAdapter(spec);

		// then
		assertEquals(2, adapter.getContracts().size());
		assertEquals(1, adapter.getEndpointContracts().size());
		assertEquals(1, adapter.getChannelContracts().size());
	}

	@Test
	public void canDistinguishBetweenProvidersAndMessageBrokers() {
		// given
		ServiceSpecification spec = ApiDescriptionFactory.eINSTANCE.createServiceSpecification();
		spec.getProviders().add(ApiDescriptionFactory.eINSTANCE.createProvider());
		spec.getProviders().add(ApiDescriptionFactory.eINSTANCE.createMessageBroker());

		// when
		ServiceSpecificationAdapter adapter = new ServiceSpecificationAdapter(spec);

		// then
		assertEquals(2, adapter.getProviders().size());
		assertEquals(1, adapter.getProviderProviders().size());
		assertEquals(1, adapter.getMessageBrokers().size());
	}

	@Test
	public void canDistinguishBetweenClientsAndMessageEndpoints() {
		// given
		ServiceSpecification spec = ApiDescriptionFactory.eINSTANCE.createServiceSpecification();
		spec.getClients().add(ApiDescriptionFactory.eINSTANCE.createClient());
		spec.getClients().add(ApiDescriptionFactory.eINSTANCE.createMessageEndpoint());

		// when
		ServiceSpecificationAdapter adapter = new ServiceSpecificationAdapter(spec);

		// then
		assertEquals(2, adapter.getClients().size());
		assertEquals(1, adapter.getClientClients().size());
		assertEquals(1, adapter.getMessageEndpoints().size());
	}

}

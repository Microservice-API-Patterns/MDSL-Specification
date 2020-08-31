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
package io.mdsl.generator.model.converter;

import java.util.List;
import java.util.Optional;

import io.mdsl.apiDescription.Consumption;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.model.Client;
import io.mdsl.generator.model.EndpointContract;
import io.mdsl.generator.model.MDSLGeneratorModel;

/**
 * Converts MDSL clients (AST model) into clients of our generator model.
 *
 */
public class ClientConverter {

	private MDSLGeneratorModel genModel;

	public ClientConverter(MDSLGeneratorModel genModel) {
		this.genModel = genModel;
	}

	/**
	 * Converts MDSL client to a generator model client.
	 * 
	 * @param mdslClient the MDSL client that shall be converted
	 * @return the generator model client
	 */
	public Client convert(io.mdsl.apiDescription.Client mdslClient) {
		Client client = new Client(mdslClient.getName());
		mapEndpoints(client, mdslClient.getCons());
		return client;
	}

	private void mapEndpoints(Client client, List<Consumption> consumptions) {
		for (Consumption consumption : consumptions) {
			Optional<EndpointContract> correspondingEndpoint = this.genModel.getEndpoints().stream()
					.filter(e -> e.getName().equals(consumption.getContract().getName())).findFirst();
			if (correspondingEndpoint.isEmpty())
				throw new MDSLException("MDSL error: a client consumes an endpoint that does not exist!");
			client.addEndpoint(correspondingEndpoint.get());
		}
	}

}

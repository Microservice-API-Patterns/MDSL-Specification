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

import io.mdsl.apiDescription.EndpointList;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.model.EndpointContract;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.Provider;

/**
 * Converts MDSL providers (AST model) into providers of our generator model.
 *
 */
public class ProviderConverter {

	private MDSLGeneratorModel genModel;

	public ProviderConverter(MDSLGeneratorModel genModel) {
		this.genModel = genModel;
	}

	/**
	 * Converts MDSL provider to a generator model provider.
	 * 
	 * @param mdslProvider the MDSL provider that shall be converted
	 * @return the generator model provider
	 */
	public Provider convert(io.mdsl.apiDescription.Provider mdslProvider) {
		Provider provider = new Provider(mdslProvider.getName());
		mapEndpoints(provider, mdslProvider.getEpl());
		return provider;
	}

	private void mapEndpoints(Provider provider, List<EndpointList> endpoints) {
		for (EndpointList endpoint : endpoints) {
			Optional<EndpointContract> correspondingEndpoint = this.genModel.getEndpoints().stream()
					.filter(e -> e.getName().equals(endpoint.getContract().getName())).findFirst();
			if (correspondingEndpoint.isEmpty())
				throw new MDSLException("MDSL error: a provider exposes an endpoint that does not exist!");
			provider.addEndpoint(correspondingEndpoint.get());
		}
	}

}

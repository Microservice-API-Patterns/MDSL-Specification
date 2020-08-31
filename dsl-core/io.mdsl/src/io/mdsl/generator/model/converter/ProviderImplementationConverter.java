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

import java.util.Optional;

import io.mdsl.exception.MDSLException;
import io.mdsl.generator.model.ImplementationTechnology;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.Provider;
import io.mdsl.generator.model.ProviderImplementation;

/**
 * Converts MDSL provider implementations (AST model) into provider
 * implementations of our generator model.
 *
 */
public class ProviderImplementationConverter {

	private MDSLGeneratorModel genModel;

	public ProviderImplementationConverter(MDSLGeneratorModel genModel) {
		this.genModel = genModel;
	}

	public ProviderImplementation convert(io.mdsl.apiDescription.ProviderImplementation mdslProviderImplementation) {
		ProviderImplementation implementation = new ProviderImplementation(mdslProviderImplementation.getName(),
				findProvider(mdslProviderImplementation.getUpstreamBinding()),
				ImplementationTechnology.getTechnology(mdslProviderImplementation.getPlatform().getName()));
		implementation.setClass(mdslProviderImplementation.getClass_());
		implementation.setSuperClass(mdslProviderImplementation.getSuperclass());
		implementation.setDownstreamBinding(findProvider(mdslProviderImplementation.getDownstreamBinding()));
		return implementation;
	}

	private Provider findProvider(io.mdsl.apiDescription.Provider provider) {
		if (provider == null)
			return null;

		Optional<Provider> optProvider = genModel.getProviders().stream()
				.filter(p -> p.getName().equals(provider.getName())).findFirst();
		if (optProvider.isEmpty())
			throw new MDSLException("MDSL error: The provider implementation '" + provider.getName()
					+ "' references non-existing upstream binding (provider)!");
		return optProvider.get();
	}

}

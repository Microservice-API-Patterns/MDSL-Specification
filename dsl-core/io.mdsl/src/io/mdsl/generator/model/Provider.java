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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Represents an MDSL provider that offers several endpoints.
 *
 */
public class Provider {

	private String name;
	private List<EndpointContract> offeredEndpoints;

	/**
	 * Creates a new provider.
	 * 
	 * @param name the name of the new provider
	 */
	public Provider(String name) {
		this.name = name;
		this.offeredEndpoints = Lists.newLinkedList();
	}

	/**
	 * Returns the name the provider.
	 * 
	 * @return the name of the provider
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a list with all endpoints offered by the represented provider;
	 * 
	 * @return a list with all endpoints offered by the provider
	 */
	public List<EndpointContract> getOfferedEndpoints() {
		return Lists.newLinkedList(offeredEndpoints);
	}

	/**
	 * Adds a new endpoint to the endpoints offered by the represented provider.
	 * 
	 * @param endpoint the new endpoint that shall be offered by the provider
	 */
	public void addEndpoint(EndpointContract endpoint) {
		this.offeredEndpoints.add(endpoint);
	}

}

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
 * Represents an MDSL client that consumes several endpoints.
 *
 */
public class Client {

	private String name;
	private List<EndpointContract> consumedEndpoints;

	/**
	 * Creates a new client.
	 * 
	 * @param name the name of the new client
	 */
	public Client(String name) {
		this.name = name;
		this.consumedEndpoints = Lists.newLinkedList();
	}

	/**
	 * Returns the name the client.
	 * 
	 * @return the name of the client
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a list with all endpoints consumed by the represented client;
	 * 
	 * @return a list with all endpoints consumed by the client
	 */
	public List<EndpointContract> getConsumedEndpoints() {
		return Lists.newLinkedList(consumedEndpoints);
	}

	/**
	 * Adds a new endpoint to the endpoints consumed by the represented client.
	 * 
	 * @param endpoint the new endpoint that shall be consumed by the client
	 */
	public void addEndpoint(EndpointContract endpoint) {
		this.consumedEndpoints.add(endpoint);
	}

}

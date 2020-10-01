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

import java.util.List;

import io.mdsl.apiDescription.ChannelContract;
import io.mdsl.apiDescription.Client;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.MessageBroker;
import io.mdsl.apiDescription.MessageEndpoint;
import io.mdsl.apiDescription.Provider;

/**
 * Operations that are useful when dealing with a ServiceSpecification object,
 * but no longer provided by the grammar.
 *
 */
public interface ServiceSpecificationExtensions {

	/**
	 * Returns only contracts of the type EndpointContract.
	 * 
	 * @return list of endpoint contracts
	 */
	List<EndpointContract> getEndpointContracts();

	/**
	 * Returns only contracts of the type ChannelContract.
	 * 
	 * @return list of channel contracts
	 */
	List<ChannelContract> getChannelContracts();

	/**
	 * Returns only providers of the type Provider.
	 * 
	 * @return list of providers
	 */
	List<Provider> getProviderProviders();

	/**
	 * Returns only providers of the type MessageBroker.
	 * 
	 * @return list of message brokers
	 */
	List<MessageBroker> getMessageBrokers();

	/**
	 * Returns only clients of the type Client.
	 * 
	 * @return list of clients
	 */
	List<Client> getClientClients();

	/**
	 * Returns only clients of the type MessageEndpoint.
	 * 
	 * @return list of message endpoints
	 */
	List<MessageEndpoint> getMessageEndpoints();

}

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

import com.google.common.collect.Lists; // TODO replace with ArrayList

import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.model.Client;
import io.mdsl.generator.model.DataType;
import io.mdsl.generator.model.EndpointContract;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.Provider;
import io.mdsl.generator.model.ProviderImplementation;
// import io.mdsl.generator.model.carving.ClusterCollection;
import io.mdsl.generator.model.composition.Flow;
import io.mdsl.generator.model.composition.converter.OrchestrationConverter;

/**
 * Converts MDSL (AST model) into a simpler generator model.
 *
 */
public class MDSL2GeneratorModelConverter {

	private ServiceSpecificationAdapter serviceSpecification;
	private DataTypeConverter dataTypeConverter;
	private EndpointConverter endpointConverter;
	private ProviderConverter providerConverter;
	private ClientConverter clientConverter;
	private ProviderImplementationConverter providerImplementationConverter;
	private OrchestrationConverter orchestrationConverter;
	private MDSLGeneratorModel genModel;

	public MDSL2GeneratorModelConverter(ServiceSpecification serviceSpecification) {
		this.serviceSpecification = new ServiceSpecificationAdapter(serviceSpecification);
		this.genModel = new MDSLGeneratorModel(serviceSpecification.getName());
		this.dataTypeConverter = new DataTypeConverter(genModel);
		this.endpointConverter = new EndpointConverter(this.serviceSpecification, genModel, dataTypeConverter);
		this.providerConverter = new ProviderConverter(genModel);
		this.providerImplementationConverter = new ProviderImplementationConverter(genModel);
		this.clientConverter = new ClientConverter(genModel);
		this.orchestrationConverter = new OrchestrationConverter(genModel);
	}

	/**
	 * Converts the service specification passed to the constructor into the
	 * generator model.
	 * 
	 * @return the generator model of the corresponding MDSL model
	 */
	public MDSLGeneratorModel convert() {
		// convert data types
		for (DataType dataType : convertDataTypes(serviceSpecification.getTypes()))
			genModel.addDataType(dataType);
  
		// convert endpoints
		for (EndpointContract endpoint : convertEndpoints(serviceSpecification.getEndpointContracts()))
			genModel.addEndpoint(endpoint);

		// convert providers
		for (Provider provider : convertProviders(serviceSpecification.getProviderProviders()))
			genModel.addProvider(provider);

		// convert clients
		for (Client client : convertClients(serviceSpecification.getClientClients()))
			genModel.addClient(client);

		// convert provider implementations
		for (ProviderImplementation providerImpl : convertProviderImplementations(serviceSpecification.getRealizations()))
			genModel.addProviderImplementation(providerImpl);
		
		// convert orchestration flows
		for (Flow oFlow : convertOrchestrationFlows(serviceSpecification.getOrchestrations())) {
			genModel.addOrchestration(oFlow);
		}
		
		// List<ClusterCollection> clusters = OrchestrationConverter.postprocessFlowConversions(); // NYI
		// genModel.addAllClustersToCuts(clusters);

		return genModel;
	}
	
	private List<Flow> convertOrchestrationFlows(List<Orchestration> orchestrations) {
		List<Flow> oFlows = Lists.newLinkedList();
		for (Orchestration oFlow : orchestrations) {
			oFlows.add(orchestrationConverter.convert(oFlow));
		}
		return oFlows;
	}

	private List<DataType> convertDataTypes(List<DataContract> contracts) {
		List<DataType> dataTypes = Lists.newLinkedList();
		for (DataContract contract : contracts) {
			dataTypes.add(dataTypeConverter.convert(contract));
		}
		return dataTypes;
	}

	private List<EndpointContract> convertEndpoints(List<io.mdsl.apiDescription.EndpointContract> mdslEndpoints) {
		List<EndpointContract> endpoints = Lists.newLinkedList();
		for (io.mdsl.apiDescription.EndpointContract endpoint : mdslEndpoints) {
			endpoints.add(endpointConverter.convert(endpoint));
		}
		return endpoints;
	}

	private List<Provider> convertProviders(List<io.mdsl.apiDescription.Provider> mdslProviders) {
		List<Provider> providers = Lists.newLinkedList();
		for (io.mdsl.apiDescription.Provider provider : mdslProviders) {
			providers.add(providerConverter.convert(provider));
		}
		return providers;
	}

	private List<Client> convertClients(List<io.mdsl.apiDescription.Client> mdslClients) {
		List<Client> clients = Lists.newLinkedList();
		for (io.mdsl.apiDescription.Client client : mdslClients) {
			clients.add(clientConverter.convert(client));
		}
		return clients;
	}

	private List<ProviderImplementation> convertProviderImplementations(List<io.mdsl.apiDescription.ProviderImplementation> mdslProviderImplemenations) {
		List<ProviderImplementation> providerImplementations = Lists.newLinkedList();
		for (io.mdsl.apiDescription.ProviderImplementation providerImpl : mdslProviderImplemenations) {
			providerImplementations.add(providerImplementationConverter.convert(providerImpl));
		}
		return providerImplementations;
	}

}

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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import io.mdsl.apiDescription.ChannelContract;
import io.mdsl.apiDescription.Client;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DirectionList;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Gateway;
import io.mdsl.apiDescription.MessageBroker;
import io.mdsl.apiDescription.MessageEndpoint;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.ProviderImplementation;
import io.mdsl.apiDescription.SLATemplate;
import io.mdsl.apiDescription.ServiceSpecification;

public class ServiceSpecificationAdapter implements ServiceSpecification, ServiceSpecificationExtensions {

	private ServiceSpecification internalSpec;

	public ServiceSpecificationAdapter(ServiceSpecification specification) {
		this.internalSpec = specification;
	}

	@Override
	public EClass eClass() {
		return internalSpec.eClass();
	}

	@Override
	public Resource eResource() {
		return internalSpec.eResource();
	}

	@Override
	public EObject eContainer() {
		return internalSpec.eContainer();
	}

	@Override
	public EStructuralFeature eContainingFeature() {
		return internalSpec.eContainingFeature();
	}

	@Override
	public EReference eContainmentFeature() {
		return internalSpec.eContainmentFeature();
	}

	@Override
	public EList<EObject> eContents() {
		return internalSpec.eContents();
	}

	@Override
	public TreeIterator<EObject> eAllContents() {
		return internalSpec.eAllContents();
	}

	@Override
	public boolean eIsProxy() {
		return internalSpec.eIsProxy();
	}

	@Override
	public EList<EObject> eCrossReferences() {
		return internalSpec.eCrossReferences();
	}

	@Override
	public Object eGet(EStructuralFeature feature) {
		return internalSpec.eGet(feature);
	}

	@Override
	public Object eGet(EStructuralFeature feature, boolean resolve) {
		return internalSpec.eGet(feature, resolve);
	}

	@Override
	public void eSet(EStructuralFeature feature, Object newValue) {
		internalSpec.eSet(feature, newValue);
	}

	@Override
	public boolean eIsSet(EStructuralFeature feature) {
		return internalSpec.eIsSet(feature);
	}

	@Override
	public void eUnset(EStructuralFeature feature) {
		internalSpec.eUnset(feature);
	}

	@Override
	public Object eInvoke(EOperation operation, EList<?> arguments) throws InvocationTargetException {
		return internalSpec.eInvoke(operation, arguments);
	}

	@Override
	public EList<Adapter> eAdapters() {
		return internalSpec.eAdapters();
	}

	@Override
	public boolean eDeliver() {
		return internalSpec.eDeliver();
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		internalSpec.eSetDeliver(deliver);
	}

	@Override
	public void eNotify(Notification notification) {
		internalSpec.eNotify(notification);
	}

	@Override
	public String getName() {
		return internalSpec.getName();
	}

	@Override
	public void setName(String value) {
		internalSpec.setName(value);
	}

	@Override
	public String getSvi() {
		return internalSpec.getSvi();
	}

	@Override
	public void setSvi(String value) {
		internalSpec.setSvi(value);
	}

	@Override
	public String getReach() {
		return internalSpec.getReach();
	}

	@Override
	public void setReach(String value) {
		internalSpec.setReach(value);
	}

	@Override
	public EList<DirectionList> getDirection() {
		return internalSpec.getDirection();
	}

	@Override
	public String getDescription() {
		return internalSpec.getDescription();
	}

	@Override
	public void setDescription(String value) {
		internalSpec.setDescription(value);
	}

	@Override
	public EList<DataContract> getTypes() {
		return internalSpec.getTypes();
	}

	@Override
	public EList<EObject> getContracts() {
		return internalSpec.getContracts();
	}

	@Override
	public EList<SLATemplate> getSlas() {
		return internalSpec.getSlas();
	}

	@Override
	public EList<EObject> getProviders() {
		return internalSpec.getProviders();
	}

	@Override
	public EList<EObject> getClients() {
		return internalSpec.getClients();
	}

	@Override
	public EList<Gateway> getGateways() {
		return internalSpec.getGateways();
	}

	@Override
	public EList<ProviderImplementation> getRealizations() {
		return internalSpec.getRealizations();
	}

	@Override
	public List<EndpointContract> getEndpointContracts() {
		return internalSpec.getContracts().stream().filter(contract -> contract instanceof EndpointContract).map(contract -> (EndpointContract) contract)
				.collect(Collectors.toList());
	}

	@Override
	public List<ChannelContract> getChannelContracts() {
		return internalSpec.getContracts().stream().filter(contract -> contract instanceof ChannelContract).map(contract -> (ChannelContract) contract)
				.collect(Collectors.toList());
	}

	@Override
	public List<MessageBroker> getMessageBrokers() {
		return internalSpec.getProviders().stream().filter(provider -> provider instanceof MessageBroker).map(provider -> (MessageBroker) provider).collect(Collectors.toList());
	}

	@Override
	public List<Provider> getProviderProviders() {
		return internalSpec.getProviders().stream().filter(provider -> provider instanceof Provider).map(provider -> (Provider) provider).collect(Collectors.toList());
	}

	@Override
	public List<MessageEndpoint> getMessageEndpoints() {
		return internalSpec.getClients().stream().filter(client -> client instanceof MessageEndpoint).map(client -> (MessageEndpoint) client).collect(Collectors.toList());
	}

	@Override
	public List<Client> getClientClients() {
		return internalSpec.getClients().stream().filter(client -> client instanceof Client).map(client -> (Client) client).collect(Collectors.toList());
	}

}

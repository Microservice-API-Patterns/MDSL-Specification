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

/**
 * Represents the implementation of a provider (also named
 * ProviderImplementation in MDSL).
 *
 */
public class ProviderImplementation {

	private String name;
	private Provider provider;
	private ImplementationTechnology implTechnology;
	private String clazz;
	private String superClass;
	private Provider downstreamBinding;

	/**
	 * Creates a new provider implementation.
	 * 
	 * @param name       the name of the provider implementation
	 * @param provider   the provider
	 * @param technology the technology that is used to implement the provider
	 */
	public ProviderImplementation(String name, Provider provider, ImplementationTechnology technology) {
		this.name = name;
		this.provider = provider;
		this.implTechnology = technology;
	}

	/**
	 * Returns the name of the provider implementation.
	 * 
	 * @return the name of the provider implementation
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the provider that is implemented.
	 * 
	 * @return the provider that is implemented
	 */
	public Provider getProvider() {
		return provider;
	}

	/**
	 * Returns the name of the provider that is implemented.
	 * 
	 * @return the name of the provider that is implemented
	 */
	public String getProviderName() {
		return provider.getName();
	}

	/**
	 * Returns the implementation technology that is used to implement the provider.
	 * 
	 * @return the implementation technology that is used to implement the provider
	 */
	public String getImplTechnology() {
		return implTechnology.toString();
	}

	/**
	 * Returns the implementation class.
	 * 
	 * @return the name of the implementation class
	 */
	public String getClazz() {
		return clazz;
	}

	/**
	 * Returns the super type of the implementation class.
	 * 
	 * @return the super type of the implementation class
	 */
	public String getSuperClass() {
		return superClass;
	}

	/**
	 * Gets the downstream binding (provider instance).
	 * 
	 * @return the downstream binding (provider instance)
	 */
	public Provider getDownstreamBinding() {
		return downstreamBinding;
	}

	/**
	 * Sets the implementation class.
	 * 
	 * @param clazz the implementation class name as string
	 */
	public void setClass(String clazz) {
		this.clazz = clazz;
	}

	/**
	 * Sets the super type of the implementation class.
	 * 
	 * @param superClass the super type name of the implementation class as string
	 */
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}

	/**
	 * Sets the downstream binding.
	 * 
	 * @param downstreamBinding the downstream binding (provider instance)
	 */
	public void setDownstreamBinding(Provider downstreamBinding) {
		this.downstreamBinding = downstreamBinding;
	}

}

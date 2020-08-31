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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ProviderImplementationTest {

	@Test
	public void canCreateProviderImplementation() {
		// given
		Provider provider = new Provider("TestProvider");
		ProviderImplementation providerImpl;

		// when
		providerImpl = new ProviderImplementation("JavaImpl", provider, StandardImplTechnology.PLAIN_JAVA);

		// then
		assertEquals("JavaImpl", providerImpl.getName());
		assertEquals("TestProvider", providerImpl.getProvider().getName());
		assertEquals("TestProvider", providerImpl.getProviderName());
		assertEquals("PlainJava", providerImpl.getImplTechnology());
	}

	@Test
	public void canSetImplClass() {
		// given
		Provider provider = new Provider("TestProvider");
		ProviderImplementation providerImpl = new ProviderImplementation("JavaImpl", provider,
				StandardImplTechnology.PLAIN_JAVA);

		// when
		providerImpl.setClass("TestClass");
		providerImpl.setSuperClass("SuperTestClass");

		// then
		assertEquals("TestClass", providerImpl.getClazz());
		assertEquals("SuperTestClass", providerImpl.getSuperClass());
	}

	@Test
	public void canSetDownstreamBinding() {
		// given
		Provider provider = new Provider("TestProvider");
		ProviderImplementation providerImpl = new ProviderImplementation("JavaImpl", provider,
				StandardImplTechnology.PLAIN_JAVA);
		Provider downstreamProvider = new Provider("DownstreamTestProvider");

		// when
		providerImpl.setDownstreamBinding(downstreamProvider);

		// then
		assertEquals("DownstreamTestProvider", providerImpl.getDownstreamBinding().getName());
	}

}

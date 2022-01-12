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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ImplementationTechnologyTest {
	
	// TODO (M) could add HTTP binding related tests here as well

	@Test
	public void canCreateStandardTechnology() {
		// given
		ImplementationTechnology tech;

		// when
		tech = StandardImplTechnology.PLAIN_JAVA;

		// then
		assertEquals(StandardImplTechnology.PLAIN_JAVA, tech);
		assertEquals("PlainJava", tech.getTechnologyName());
		assertEquals("PlainJava", tech.toString());
	}

	@Test
	public void canCreateStandardTechnologyByString() {
		// given
		String techString = "PlainJava";

		// when
		ImplementationTechnology tech = StandardImplTechnology.byName(techString);

		// then
		assertEquals(StandardImplTechnology.PLAIN_JAVA, tech);
	}

	@Test
	public void canCreateCustomTechnology() {
		// given
		String myTechnology = "Kotlin";

		// when
		ImplementationTechnology technology = ImplementationTechnology.getTechnology(myTechnology);

		// then
		assertTrue(technology instanceof CustomImplTechnology);
		assertEquals("Kotlin", technology.getTechnologyName());
		assertEquals("Kotlin", technology.toString());
	}

	@Test
	public void canHandleNullInFactoryMethod() {
		// given
		String myTechnology = null;

		// when
		ImplementationTechnology technology = ImplementationTechnology.getTechnology(myTechnology);

		// then
		assertEquals("UnknownTechnology", technology.getTechnologyName());
		assertEquals("UnknownTechnology", technology.toString());
	}

}

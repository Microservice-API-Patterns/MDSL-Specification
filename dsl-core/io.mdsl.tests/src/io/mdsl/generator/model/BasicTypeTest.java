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

public class BasicTypeTest {

	@Test
	public void canGetBasicTypeByName() {
		// given
		String name = "string";

		// when
		BasicType basicType = BasicType.byName(name);

		// then
		assertEquals(BasicType.STRING, basicType);
	}

	@Test
	public void ensureVoidIsDefaultType() {
		// given
		String name = null;

		// when
		BasicType basicType = BasicType.byName(name);

		// then
		assertEquals(BasicType.VOID, basicType);
	}

	@Test
	public void canReturnVoid4NonExistingType() {
		// given
		String name = "MySuperDuperBasicType";

		// when
		BasicType basicType = BasicType.byName(name);

		// then
		assertEquals(BasicType.VOID, basicType);
	}

	@Test
	public void canGetName() {
		// given
		BasicType string = BasicType.STRING;

		// when
		String name = string.getName();

		// then
		assertEquals("string", name);
	}

	@Test
	public void ensureToStringIsName() {
		// given
		BasicType string = BasicType.STRING;

		// when
		String name = string.toString();

		// then
		assertEquals("string", name);
	}

}

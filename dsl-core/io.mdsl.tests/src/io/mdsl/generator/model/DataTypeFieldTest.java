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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DataTypeFieldTest {

	@Test
	public void canCreateADataTypeField() {
		// given
		DataTypeField field = new DataTypeField("testField");

		// when
		field.setType(BasicType.STRING);
		field.isList(true);
		field.isNullable(false);

		// then
		assertEquals("testField", field.getName());
		assertTrue(field.isList());
		assertFalse(field.isNullable());
		assertEquals(BasicType.STRING, field.getType());
		assertEquals("string", field.getTypeAsString());
	}

}

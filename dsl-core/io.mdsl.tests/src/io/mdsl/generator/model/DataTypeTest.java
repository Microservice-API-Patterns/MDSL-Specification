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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Sets;

public class DataTypeTest {

	@Test
	public void canCreateDataTypeWithName() {
		// given
		DataType dataType;

		// when
		dataType = new DataType("TestType");

		// then
		assertEquals("TestType", dataType.getName());
	}

	@Test
	public void canAddField() {
		// given
		DataType dataType = new DataType("TestType");
		DataTypeField field = new DataTypeField("attr1");
		field.setType(BasicType.STRING);

		// when
		dataType.addField(field);

		// then
		assertEquals(1, dataType.getFields().size());
		assertEquals("attr1", dataType.getFields().get(0).getName());
	}

	@Test
	public void canDetermineEquality() {
		// given
		DataType dataType1 = new DataType("TestType");
		DataType dataType2 = new DataType("TestType");
		DataType dataType3 = new DataType("AnotherTestType");

		// when
		boolean equal = dataType1.equals(dataType2);
		boolean notEqual = dataType1.equals(dataType3);

		// then
		assertTrue(equal);
		assertFalse(notEqual);
		assertFalse(dataType1.equals(null));
		assertTrue(dataType1.equals(dataType1));
	}

	@Test
	public void canCalculateHashCode() {
		// given
		DataType dataType1 = new DataType("TestType");
		DataType dataType2 = new DataType("TestType");
		Set<DataType> dataTypeSet = Sets.newHashSet();

		// when
		dataTypeSet.add(dataType1);
		dataTypeSet.add(dataType2);

		// then
		assertEquals(1, dataTypeSet.size());
	}

}

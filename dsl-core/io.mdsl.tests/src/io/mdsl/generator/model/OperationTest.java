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

public class OperationTest {

	@Test
	public void canCreateOperationWithName() {
		// given
		Operation operation;

		// when
		operation = new Operation("callTest");

		// then
		assertEquals("callTest", operation.getName());
	}

	@Test
	public void canDefineReturnType() {
		// given
		Operation operation = new Operation("callTest");

		// when
		operation.setResponse(BasicType.BOOLEAN);

		// then
		assertEquals("bool", operation.getResponse().toString());
	}

	@Test
	public void canReturnVoidAsDefaultReturnType() {
		// given
		Operation operation;

		// when
		operation = new Operation("TestOperation");

		// then
		assertEquals(BasicType.VOID, operation.getResponse());
	}

	@Test
	public void canAddParameters() {
		// given
		Operation operation = new Operation("callTest");
		OperationParameter parameter = new OperationParameter("testParam", BasicType.DOUBLE);

		// when
		operation.addParameter(parameter);

		// then
		assertEquals(1, operation.getParameters().size());
		assertEquals("testParam", operation.getParameters().get(0).getName());
	}

	@Test
	public void canDefineResponsibility() {
		// given
		Operation operation = new Operation("callTest");

		// when
		operation.setResponsibility("COMPUTATION_FUNCTION");

		// then
		assertEquals("COMPUTATION_FUNCTION", operation.getResponsibility());
	}

}

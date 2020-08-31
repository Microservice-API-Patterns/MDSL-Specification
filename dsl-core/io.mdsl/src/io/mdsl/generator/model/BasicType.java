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
 * Represents an MDSL basic type.
 *
 */
public enum BasicType implements MDSLType {

	STRING("string"), BOOLEAN("bool"), INTEGER("int"), LONG("long"), DOUBLE("double"), RAW("raw"), VOID("void");

	private String name;

	private static final BasicType DEFAULT_BASIC_TYPE = VOID;

	BasicType(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the MDSL basic type.
	 * 
	 * @return the name of the MDSL basic type
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the basic type for the given name.
	 * 
	 * @param basicTypeName the name of the requested basic type
	 * @return the corresponding basic type
	 */
	public static BasicType byName(String basicTypeName) {
		if (basicTypeName == null)
			return DEFAULT_BASIC_TYPE;
		for (BasicType basicType : values()) {
			if (basicType.getName().equals(basicTypeName))
				return basicType;
		}
		return DEFAULT_BASIC_TYPE;
	}

	@Override
	public String toString() {
		return this.name;
	}

}

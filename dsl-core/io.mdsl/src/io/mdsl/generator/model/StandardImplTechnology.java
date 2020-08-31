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
 * Represents implementation technologies available in MDSL.
 *
 */
public enum StandardImplTechnology implements ImplementationTechnology {

	PLAIN_JAVA("PlainJava"), SPRING_MVC("SpringMVC"), VLINGO("vlingo");

	private String name;

	StandardImplTechnology(String name) {
		this.name = name;
	}

	@Override
	public String getTechnologyName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the standard implementation technology for a given string. Or null,
	 * in case the string cannot be mapped.
	 * 
	 * @param name the name of the standard implementation technology
	 * @return the standard implementation technology, or null in case it does not
	 *         match one of the given values
	 */
	public static StandardImplTechnology byName(String name) {
		for (StandardImplTechnology technology : values()) {
			if (technology.getTechnologyName().equals(name))
				return technology;
		}
		return null;
	}

}

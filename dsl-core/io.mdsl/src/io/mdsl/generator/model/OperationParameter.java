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
 * Represents an MDSL operation parameter.
 *
 */
public class OperationParameter {

	private String name;
	private MDSLType type;

	/**
	 * Creates a new operation parameter.
	 * 
	 * @param name the name of the new parameter
	 * @param type the type of the new parameter
	 */
	public OperationParameter(String name, MDSLType type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the name of the represented operation parameter.
	 * 
	 * @return the name of the operation parameter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of the represented operation parameter.
	 * 
	 * @return the type of the operation parameter
	 */
	public MDSLType getType() {
		return type;
	}
	
	// TODO v55 add isTypeReference indicator? impact on Java generator, gRPC, GraphQL?

	/**
	 * Returns the type of the represented operation parameter as a String.
	 * 
	 * @return the type of the operation parameter as a String
	 */
	/*
	public String getTypeName() {
		return type.getName();
	}
	*/

}

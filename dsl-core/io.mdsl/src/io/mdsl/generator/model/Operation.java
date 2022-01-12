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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Represents an MDSL operation.
 */
public class Operation {

	private String name;
	private MDSLType response;
	private List<OperationParameter> parameters;
	private String responsibility;

	/**
	 * Creates a new operation.
	 * 
	 * @param name the name of the operation
	 */
	public Operation(String name) {
		this.name = name;
		this.parameters = Lists.newLinkedList();
		this.responsibility = "";
	}

	/**
	 * Gets the name of the represented operation.
	 * 
	 * @return the name of the represented operation
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Wraps the parameters of the represented operation in a new MDSLType.
	 * 
	 * @return a DataType DTO with the parameters of the operation as fields
	 */
	public MDSLType getRequest() {
		if (this.parameters == null)
			return BasicType.VOID;
		DataType result = new DataType(name+"Request");
		for(OperationParameter parameter : this.parameters) {
			DataTypeField field = new DataTypeField(parameter.getName());
			field.setType(parameter.getType());
			result.addField(field);
		}
		return result;
	}

	/**
	 * Returns the return/response type of the represented operation.
	 * 
	 * @return the return/response type of the operation
	 */
	public MDSLType getResponse() {
		if (response == null)
			return BasicType.VOID;
		return response;
	}

	/**
	 * Sets the return/response type of the represented operation.
	 * 
	 * @param response the return/response type of the operation
	 */
	public void setResponse(MDSLType response) {
		this.response = response; 
	}

	/**
	 * Returns a list with all the parameters of the operation.
	 * 
	 * @return a list with all the parameters of the operation
	 */
	public List<OperationParameter> getParameters() {
		return Lists.newLinkedList(parameters);
	}

	/**
	 * Adds a new operation parameter to the represented operation.
	 * 
	 * @param parameter the parameter that shall be added to the operation
	 */
	public void addParameter(OperationParameter parameter) {
		// TODO v55 this getter causes parameters to appear twice in genmodel export
		this.parameters.add(parameter);
	}

	/**
	 * Returns the responsibility of the represented operation.
	 * 
	 * @return the responsibility of the operation as a String.
	 */
	public String getResponsibility() {
		return responsibility;
	}

	/**
	 * Sets the responsibility of the represented operation.
	 * 
	 * @param responsibility the responsibility of the operation as a String
	 */
	public void setResponsibility(String responsibility) {
		this.responsibility = responsibility;
	}

	// TODO provide a sampleJSON method here that walks through the parameters and turns them into a JSON object
}

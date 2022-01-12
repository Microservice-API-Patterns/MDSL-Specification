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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Java protocol binding for an endpoint.
 *
 */
public class JavaBinding implements ProtocolBinding {


	private String javaPackage;
	private Map<String, String> operationNameMapping;

	public JavaBinding() {
		this.operationNameMapping = new HashMap<String, String>();
	}

	@Override
	public String getProtocolName() {
		return "Java";
	}

	public String getPackage() {
		return javaPackage;
	}

	public void setPackage(String javaPackage) {
		this.javaPackage = javaPackage;
	}

	public void mapOperationName(String endpointOperationName, String javaMethodName) {
		this.operationNameMapping.put(endpointOperationName, javaMethodName);
	}

	public String getJavaMethodName4Operation(String operationName) {
		if (this.operationNameMapping.containsKey(operationName))
			return this.operationNameMapping.get(operationName);
		return operationName;
	}
}

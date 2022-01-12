package io.mdsl.generator.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.WordUtils;

import io.mdsl.utils.MDSLLogger;

public class HTTPResource {

	private String name;
	private String uriPath;
	
	private Map<String, String> operationNameMapping; // TODO no longer needed
	private Map<String, io.mdsl.generator.model.HTTPOperationBinding> operationAndParameterMapping;

	public HTTPResource(String name, String address) {
		this.name = name;
		uriPath = address;
		this.operationNameMapping = new HashMap<String, String>();
		this.operationAndParameterMapping = new HashMap<String, io.mdsl.generator.model.HTTPOperationBinding>();
	}
	
	public void mapOperationName(String endpointOperationName, String httpMethodName) {
		this.operationNameMapping.put(endpointOperationName, httpMethodName);
	}
	
	public void mapOperationAndParameters(String endpointOperationName, io.mdsl.generator.model.HTTPOperationBinding operationAndParameterBinding) {
		this.operationAndParameterMapping.put(endpointOperationName, operationAndParameterBinding);
	}

	public String httpMethodName4Operation(String operationName) {
		if(operationAndParameterMapping.containsKey(operationName)) {
			String verbMapping = this.operationAndParameterMapping.get(operationName).getVerb().toLowerCase();
			// verbMapping.charAt(0);
			return WordUtils.capitalize(verbMapping);
		}
		MDSLLogger.reportWarning("No explicit operation binding found for " + operationName + ", using default");
		return "Post"; // default
	}
	
	public String getName() {
		return name;
	}

	public String getUriPath() {
		return uriPath;
	}
	
	/*
	public Map<String, String> getOperationBindings() {
		return this.operationNameMapping;
	}
	*/
	
	public Map<String, io.mdsl.generator.model.HTTPOperationBinding> getOperationBindings() {
		return this.operationAndParameterMapping;
	}
}

package io.mdsl.generator.model;

import java.util.Map;
import java.util.Map.Entry;

public class HTTPOperationBinding {
	
	private String name;
	private String verb;
	private Map<String, String> parameterBinding;

	public HTTPOperationBinding(String name, String verb, Map<String, String> parameterBinding) {
		this.name = name;
		this.verb = verb;
		this.parameterBinding = parameterBinding; // TODO resolve global binding during construction
	}
	
	public String getName() {
		return name;
	}
	
	public String getVerb() {
		return verb;
	}
	
	public Map<String, String> getParameterBindings() {
		return parameterBinding;
	}
	
	public String pathParameterSamples() {
		StringBuffer result = new StringBuffer();
		for(Entry<String, String> binding : this.parameterBinding.entrySet() ) {
			if("PATH".equals(binding.getValue())) {
				result.append("/{" + binding.getKey() + '}');
			}
		}
		return result.toString(); 
	}
	
	public String queryParameterSamples() {
		StringBuffer result = new StringBuffer();
		boolean first=true;
		for(Entry<String, String> binding : this.parameterBinding.entrySet() ) {
			if("QUERY".equals(binding.getValue())) {
				if(!first) {
					result.append('&');
				}
				else {
					first=false;
				}
				result.append(binding.getKey() + "=tbc"); // could be APL or PT (ept/dt gm!)
			}
		}
		return result.toString(); 
		// return "name=value"; 
	}
	
	public String headerParameterSamples() {
		StringBuffer result = new StringBuffer();
		for(Entry<String, String> binding : this.parameterBinding.entrySet() ) {
			if("HEADER".equals(binding.getValue())) {
				result.append(" -H \"" + binding.getKey() + ": tbc\""); // could be APL or PT
			}
		}
		return result.toString();
		// return "name: value"; // -H; 
	}
	
	public String cookieParameterSamples() {
		StringBuffer result = new StringBuffer();
		for(Entry<String, String> binding : this.parameterBinding.entrySet() ) {
			if("COOKIE".equals(binding.getValue())) {
				result.append(" -b \"" + binding.getKey() + "=tbc\""); // could be APL or PT
			}
		}
		return result.toString();
		// return "Name=Value"; // -b (could also use --cookie); 
	}
	
	public String bodyParameterSamples() {
		StringBuffer result = new StringBuffer("{");
		boolean first=true;
		for(Entry<String, String> binding : this.parameterBinding.entrySet() ) {
			if("BODY".equals(binding.getValue())) {
				if(!first) {
					result.append(", ");
				}
				else {
					first=false;
				}
				result.append("\"" + binding.getKey() + "\": \"tbc\""); // could be APL or PT
			}
		}
		result.append("}");
		return result.toString();
		// return "{\"TODO\": \"TODO\"}"; // TODO create JSON from new PT
	}
}

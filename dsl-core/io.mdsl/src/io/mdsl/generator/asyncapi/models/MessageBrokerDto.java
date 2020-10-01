package io.mdsl.generator.asyncapi.models;

import io.mdsl.apiDescription.BindingParams;

public class MessageBrokerDto {
	
	private String name;
	private String url;
	private String protocol;
	private String description;
	private BindingParams bindings;
	
	public MessageBrokerDto(String name, String url, String protocol, String description, BindingParams bindings) {
		super();
		this.name = name;
		this.url = url;
		this.protocol = protocol;
		this.description = description;
		this.bindings = bindings;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public BindingParams getBindings() {
		return bindings;
	}

	public void setBindings(BindingParams bindings) {
		this.bindings = bindings;
	}
	
	
	

}

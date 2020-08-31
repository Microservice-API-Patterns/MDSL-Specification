package io.mdsl.generator.jolie.converter;

public class TypeModel {
	private String name;
	private String definition;
	
	public TypeModel(String name, String definition) {
		super();
		this.name = name;
		this.definition = definition;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	
	
}

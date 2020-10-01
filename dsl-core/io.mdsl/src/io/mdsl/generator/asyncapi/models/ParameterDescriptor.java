package io.mdsl.generator.asyncapi.models;

import io.mdsl.apiDescription.Cardinality;

public class ParameterDescriptor {
	
	private String name;
	private Cardinality card;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Cardinality getCard() {
		return card;
	}
	public void setCard(Cardinality card) {
		this.card = card;
	}
	public ParameterDescriptor(String name, Cardinality card) {
		super();
		this.name = name;
		this.card = card;
	}
	
	

}

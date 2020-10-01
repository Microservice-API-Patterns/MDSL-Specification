package io.mdsl.generator.asyncapi.models;

public class CardinalityDescription {
	
	private boolean isArray;
	private boolean isAtLeastOne;
	public boolean isArray() {
		return isArray;
	}
	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}
	public boolean isAtLeastOne() {
		return isAtLeastOne;
	}
	public void setAtLeastOne(boolean isAtLeastOne) {
		this.isAtLeastOne = isAtLeastOne;
	}
	
	public CardinalityDescription(boolean isArray, boolean isAtLeastOne) {
		super();
		this.isArray = isArray;
		this.isAtLeastOne = isAtLeastOne;
	}	

}

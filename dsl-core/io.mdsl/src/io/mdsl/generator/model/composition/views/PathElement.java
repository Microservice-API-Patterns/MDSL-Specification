package io.mdsl.generator.model.composition.views;

public class PathElement {
	private static final String INVOCATION_ARROW = "->";
	private String from;
	private String to;
	
	public PathElement(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public String getName() {
		return from + INVOCATION_ARROW + to;
	}
	
	public String getSource() {
		return from;
	}
	
	public String getSink() {
		return to;
	}
		
	public String toString() {
		return "(" + from + ")\n" + to + "\n";  
	}
	
	public String dump() {
		return from + "->" + to + ";";  
	}
}

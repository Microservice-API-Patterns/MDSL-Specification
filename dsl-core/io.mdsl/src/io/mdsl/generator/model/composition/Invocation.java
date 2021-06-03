package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

public class Invocation {
	private String event;
	private String command;
	
	public Invocation(String event, String command) {
		this.event = event;
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}
	
	public boolean getAnd() {
		// System.out.println("not an AND");
		return false;
	}
	
	public List<String> getParallelActions() {
		return new ArrayList<String>(); // could also return null
	}
}
package io.mdsl.generator.model.composition.views;

import java.util.ArrayList;
import java.util.List;

public class Invocation {
	protected String event;
	protected String command;
	
	public Invocation(String command, String event) {
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
	
	/*
	public boolean getAnd() {
		return false;
	}
	*/
}
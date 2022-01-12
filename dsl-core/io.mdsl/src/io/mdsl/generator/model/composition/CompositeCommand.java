package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand extends Command {
	
	List<Command> composedCommands;
	private String type;
	
	public CompositeCommand(String name, List<Command> commands, String type) {
		super(name);
		this.composedCommands = commands;
		this.type = type;
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	
	public void addCommand(Command command) {
		this.composedCommands.add(command);
	}
	
	@Override
	public List<Command> containedCommands() {
		return composedCommands;
	}
	
	@Override
	public List<String> getComposedEvents() {
		List<String> result = new ArrayList<String>();
		this.composedCommands.forEach(event->result.add(event.getName()));
		return result;
	}
	
	@Override
	public String containedCommandsAsCommaSeparatedList(String prefix) {
		String result = ""; 
		boolean isFirst=true;
		for(Command cmd : composedCommands) {
			if(isFirst) {
				isFirst=false;
			}
			else {
				result += ", ";
			}
			result += prefix + cmd.getName();
		}
		return result;
	}
}

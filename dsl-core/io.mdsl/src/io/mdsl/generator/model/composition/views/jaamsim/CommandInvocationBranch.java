package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.ArrayList;
import java.util.List;

import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.Flow;

public class CommandInvocationBranch extends Branch {

	public static final String CHOICE_QUEUE_SUFFIX = "ChoiceQueue";
	 
	private Event event;
	
	public CommandInvocationBranch(String name, Event event) {
		super(name);
		this.event = event;
	}
	
	public String getName() {
		return name;
	}

	public List<String> getNextComponentList() {
		List<Command> orCommands = event.triggeredCommands();
		ArrayList<String> result = new ArrayList<String>();
		orCommands.forEach(command->result.add(command.getName() + CHOICE_QUEUE_SUFFIX));
		return result;
	}
}

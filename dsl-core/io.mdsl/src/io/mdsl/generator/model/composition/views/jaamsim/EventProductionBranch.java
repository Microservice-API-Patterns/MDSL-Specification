package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.ArrayList;
import java.util.List;

import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.Event;

public class EventProductionBranch extends Branch {

	private Command command;

	public EventProductionBranch(String name, Command command) {
		super(name);
		this.command = command;
	}
	
	public String getName() {
		return name;
	}

	public List<String> getNextComponentList() {
		List<Event> orEvents = command.emits();
		ArrayList<String> result = new ArrayList<String>();
		orEvents.forEach(command->result.add(command.getName()));
		return result;
	}
}

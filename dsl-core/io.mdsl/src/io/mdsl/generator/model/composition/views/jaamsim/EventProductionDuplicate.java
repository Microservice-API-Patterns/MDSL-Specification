package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.ArrayList;
import java.util.List;

import io.mdsl.generator.model.composition.Command;
import io.mdsl.utils.MDSLLogger;

public class EventProductionDuplicate extends Duplicate {
	
	private Command command;
	
	public EventProductionDuplicate(String name, Command command) {
		super(name);
		this.command = command;
	}
	
	public Command getCommand() {
		return command;
	}

	public String getNextComponent() {
		List<Command> andedCommands = command.containedCommands();
		// MDSLLogger.reportDetailedInformation("Processing CIS_AND: " + this.name + ": " + andedCommands.get(0).getName() + "SplitQueue");
		return andedCommands.get(0).getName() + "SplitQueue";
	}

	public List<String> getTargetComponents() {
		List<Command> andedCommands = command.containedCommands();
		// MDSLLogger.reportDetailedInformation("Processing CIS_AND: " + this.name + ": " + andedCommands.get(1).getName());
		return namesOfSplitQueues(andedCommands.subList(1, andedCommands.size()));
	}

	private List<String> namesOfSplitQueues(List<Command> cmdList) {
		ArrayList<String> result = new ArrayList<String>();
		cmdList.forEach(nextCommand->result.add(nextCommand.getName() + "SplitQueue"));
		return result;
	}
}

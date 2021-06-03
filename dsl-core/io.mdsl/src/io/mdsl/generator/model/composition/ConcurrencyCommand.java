package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

public class ConcurrencyCommand extends Command {

	List<Command> parallelActions = new ArrayList<Command>();
	
	public ConcurrencyCommand(String name) {
		super(name);
	}
	
	public ConcurrencyCommand(String name, List<Command> parallelActions) {
		super(name);
		this.parallelActions = parallelActions;
	}
	
	public List<Command> getParallelActions() {
		return parallelActions;
	}

	public void addParallelAction(Command parallelAction) {
		this.parallelActions.add(parallelAction);
	}

	public boolean isAnd() {
		return true;
	}
	
	public List<String> getConcurrentCommands() {
		// TODO there are smarter ways of doing this:
		List<String> result = new ArrayList<String>(); 
		for(Command cmd : parallelActions)
			result.add(cmd.getName());
		return result;
	}
}

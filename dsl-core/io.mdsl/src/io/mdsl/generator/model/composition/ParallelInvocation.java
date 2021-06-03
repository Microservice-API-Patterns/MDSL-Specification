package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

public class ParallelInvocation extends Invocation {
	
	List<String> actionList = new ArrayList<String>();

	public ParallelInvocation(String event, String commandName, List<String> aggregatedCommands) {
		super(event, commandName);
		actionList = aggregatedCommands;
	}
	
	public List<String> getParallelActions() {
		return actionList;
	}
	
	public boolean getAnd() {
		return true;
	}
}

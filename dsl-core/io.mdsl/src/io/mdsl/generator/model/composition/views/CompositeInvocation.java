package io.mdsl.generator.model.composition.views;

import java.util.ArrayList;
import java.util.List;

import io.mdsl.generator.model.composition.Flow;

public class CompositeInvocation extends Invocation {
	List<String> actionList = new ArrayList<String>();

	public CompositeInvocation(String commandName, String eventName, List<String> composedEvents) {
		super(commandName, eventName);
		actionList = composedEvents;
	}
	
	public List<String> getParallelActions() {
		return actionList;
	}
	
	public boolean isAnd() {
		return this.event.startsWith(Flow.AND_OPERATOR);
	}
}

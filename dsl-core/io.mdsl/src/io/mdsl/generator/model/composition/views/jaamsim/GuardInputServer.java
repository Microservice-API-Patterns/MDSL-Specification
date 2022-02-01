package io.mdsl.generator.model.composition.views.jaamsim;

import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.Flow;
import io.mdsl.utils.MDSLLogger;

public class GuardInputServer extends Server {
	
	public static final String GUARD_QUEUE_SUFFIX = "GuardQueue";
	
	private Event triggerEvent;

	public GuardInputServer(String name, Event trigger, Command command, Flow flow) {
		super(name, command, flow);
		this.triggerEvent = trigger;
	}

	public String getWaitQueue() {
		MDSLLogger.reportDetailedInformation("In getWaitQueue of " + name);
		
		if(triggerEvent.isJoin()) {
			MDSLLogger.reportDetailedInformation(triggerEvent.getName() + Combine.AGGREGATION_QUEUE_SUFFIX);
			return triggerEvent.getName() + Combine.AGGREGATION_QUEUE_SUFFIX;
		}
		else if(triggerEvent.triggersOrCommandComposition()) {
			MDSLLogger.reportDetailedInformation(triggerEvent.getName());
			return command.getName() + Branch.CHOICE_QUEUE_SUFFIX;
		}
		else {
			MDSLLogger.reportDetailedInformation(triggerEvent.getName());
			return triggerEvent.getName();
		}
	}
	
	public String getNextComponent() {
		return command.getName() + GUARD_QUEUE_SUFFIX; // Jan 22 fix
	}
}

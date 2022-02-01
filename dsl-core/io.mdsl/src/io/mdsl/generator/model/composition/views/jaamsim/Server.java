package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.List;

import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.Flow;
import io.mdsl.utils.MDSLLogger;

public class Server extends Component {
	protected static final String DEAD_LETTER_QUEUE = "DeadLetterQueue";
	protected Command command;
	private Flow flow;
	
	public Server(String name, Command command, Flow flow) {
		super(name);
		this.command = command;
		this.flow = flow;
	}
	
	public Command getCommand() {
		return command;
	}
	
	public String getWaitQueue() {
		List<Event> events = this.flow.processView().getEventsThatTrigger(command);
		
		if(events.size()==1) {
			Event trigger = events.get(0);
			// check whether event is a composite/join, add "AggregationQueue" to name if so
			if(trigger.isJoin()) {
				return trigger.getName() + Combine.AGGREGATION_QUEUE_SUFFIX;
			}
			else if (trigger.triggersOrCommandComposition()) {
				return command.getName() + Branch.CHOICE_QUEUE_SUFFIX;
			}
			else {
				return trigger.getName();
			}
		}
		else if(events.size()==0) {
			if(this.flow.processView().participatesInAnd(command)) {
				List<Command> andCommands = this.flow.processView().getCompositeCommandsWith(command);
				if(andCommands.size()==1) {
					// Command andComposite = andCommands.get(0);
					return this.name + "SplitQueue";
				}
				else {
					MDSLLogger.reportWarning(command.getName() + ": unexpected/unsupported number of and commands: " + andCommands.size());
					return DEAD_LETTER_QUEUE;
				}
			}
			else {
				MDSLLogger.reportDetailedInformation(command.getName() + "is a flow-initiating command");
				return this.flow.getName() + "InitQueue";
			}
		}
		else if(events.size()>=2) {
			if(this.name.endsWith(JaamSimView.SERVER_SUFFIX_FOR_GUARDED_SERVER)) {
				int end = this.name.length() - 6; // "Server" has to go
				return this.name.substring(0, end) + GuardInputServer.GUARD_QUEUE_SUFFIX;
			}
			else {
				MDSLLogger.reportError(command.getName() + ": expected server name to end with " + JaamSimView.SERVER_SUFFIX_FOR_GUARDED_SERVER);
				return DEAD_LETTER_QUEUE;
			}
			
		}
		else {
			MDSLLogger.reportWarning("Unsupported case in " + this.name + ", number of events is: " + events.size());
			return DEAD_LETTER_QUEUE;
		}
	}
	
	public String getNextComponent() {
		if(this.command.emitsSingleSimpleEvent()) {
			return this.command.singleSimpleEvent().getName();
		}
		else if(this.command.emitsMultipleAlternativeEvents()) {
			return this.command.getName(); // must be a Branch
		}
		else if(this.command.emitsSingleCompositeEvent()) {
			return this.command.emits().get(0).getName(); // must be a Duplicate
		}
		else if(flow.processView().terminatesFlow(command)) {
			return this.command.getName() + "Statistics";
		}
		else {
			MDSLLogger.reportWarning("Unsupported case in " + this.name);
		}
			
		return DEAD_LETTER_QUEUE;
	}

	public String dump() {
		return name + ", waitQueue: " + this.getWaitQueue() + ", nextComponent: " + this.getNextComponent();
	}
}

package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.mdsl.utils.MDSLLogger;
// import io.mdsl.generator.model.carving.CouplingCriterion;

// Jackson changes order within YAML/JSON exports randomly if @JsonPropertyOrder annotation is not present)
@JsonPropertyOrder({"name", "type", "triggeredCommands", "composite", "composedEvents", "join", "andComposedCommands"})
public class Event {
	
	private static final String CHOICE = "choiceOf";
	
	protected String name;
	protected List<Command> triggeredAlternativeCommands;
	
	// could move this to a superclass "scoreable element":
	// private HashMap<String, CouplingCriterion> couplingScore;
	
	public Event(String name) {
		this.name = name;
		this.triggeredAlternativeCommands = new ArrayList<Command>();
		// this.couplingScore = new HashMap<String, CouplingCriterion>();
		
		// this.addCouplingScore(name + "-EventCount", new CouplingCriterion(1.0f));
		// this.addCouplingScore(name + "-EvCCrit2", new CouplingCriterion(2, 0.5f));
	}
	
	@Override
    public boolean equals(Object object) {
        if(object == this) {
            return true;
        }
        if(!(object instanceof Event)) {
            return false;
        }
        Event ev = (Event) object;
        return this.name.equals(ev.getName());
	}
	
	public String getType() {
		return Flow.NO_OPERATOR;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isJoin() {
		return false;
	}
	
	/*
	public int getConditionCount() {
		return 0;
	}
	*/
	
	public List<Event> joinedEvents() {
		return null;
	}
		
	public Command addTriggeredCommands(List<Command> triggeredCommands, String operator) {
		if(operator.equals(Flow.NO_OPERATOR)) {
			triggeredAlternativeCommands.addAll(triggeredCommands);
			return null;
		}
		else if(operator.equals(Flow.OR_OPERATOR)||operator.equals(Flow.XOR_OPERATOR)) {
			// no need to do anything special
			triggeredAlternativeCommands.addAll(triggeredCommands);
			return null;
		}
		else if(operator.equals(Flow.AND_OPERATOR)) {
			Command andCommand = findOrCreateCompositeCommand(triggeredCommands, operator);
			if(!triggeredAlternativeCommands.contains(andCommand)) {
				// new check, added Dec 18, TODO v55 also add for other add/addAll calls here (and in Command) 
				triggeredAlternativeCommands.add(andCommand);
			}
				
			return andCommand;
		}
		else {
			MDSLLogger.reportError("Unknown composite event operator: " + operator);
			return null; // not getting here
		}
	}
	
	private Command findOrCreateCompositeCommand(List<Command> commands, String operator) {
		String composedName = createNameOfCompositeCommand(commands, operator);
		for(Command command : triggeredAlternativeCommands) {
			if(command.getName().equals(composedName)) {
				return command;
			}
		}
		CompositeCommand result = new CompositeCommand(composedName, commands, operator);
		return result;
	}

	private String createNameOfCompositeCommand(List<Command> commands, String operator) {
		StringBuffer result = new StringBuffer(operator);
		commands.forEach(event->result.append("_" + event.getName()));
		return result.toString();
	}

	public List<Command> triggeredCommands() {
		return triggeredAlternativeCommands;
	}
	
	// this is a bean property, used by JSON/YAML export:
	public List<String> getTriggeredCommands() {
		List<String> result = new ArrayList<String>();
		this.triggeredAlternativeCommands.forEach(command->result.add(command.getName()));
		return result;
	}
	
	public String optionValue() {
		return CHOICE + this.name;
	}
	
	public String optionBranch(String prefix) {
		return prefix + this.name;
	}
	
	public List<Event> composedEvents() {
		return null;
	}
	
	public Command singleCommand() {
		if(this.triggersSingleCommand()) {
			// access to index 0 is ok now:
			return this.triggeredAlternativeCommands.get(0);
		}
		else {
			MDSLLogger.reportWarning("Expected event to be triggering a single simple (non-composite) command.");
			return null;
			// throw new IllegalStateException("Expected event to be triggering a single simple (non-composite) command.");
		}
	}
	
	public boolean isComposite() {
		return this instanceof CompositeEvent;
	}

	public boolean triggersSingleCommand() {
		return this.triggeredAlternativeCommands.size()==1;
	}
	
	public boolean triggersSingleSimpleCommand() {
		return this.triggeredAlternativeCommands.size()==1 && !this.triggeredAlternativeCommands.get(0).isComposite();
	}
	
	public boolean triggersAndCommandComposition() {
		return this.triggeredAlternativeCommands.size()==1 
			&& this.triggeredAlternativeCommands.get(0).isComposite(); 
			// && this.triggeredAlternativeCommands.get(0).getType().equals(Flow.AND_OPERATOR);
	}
	
	public boolean triggersOrCommandComposition() {
		return this.triggeredAlternativeCommands.size()>1; 
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(" Event: " + name + " (" + this.getClass().getSimpleName() +")");
		// TODO v55 show composed events (if any) by also implementing this method in subclass; same for join events 
		result.append(" triggers " + triggeredAlternativeCommands.size() + " command(s): ");
		triggeredAlternativeCommands.forEach(command->result.append(command.getName() + " (" + command.getClass().getSimpleName() +") "));
		return result.toString() + "\n";
	}

	// note that gen model JSON/YAML serializer (Jackson) calls this method because it is a getter; unlike custom FTLs, preconditions not checked
	public List<Command> getAndComposedCommands() {
		if(this.triggersAndCommandComposition()) {
			// access to first element in array is ok now:
			return triggeredAlternativeCommands.get(0).containedCommands();
		}
		else {
			MDSLLogger.reportWarning("Expected event to be triggering a single composite AND command.");
			// throw new IllegalStateException("Expected event to be triggering a single composite AND command.");
			return null;
		}
	}

	public List<String> getComposedEvents() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	// ** experimental (future work, tech. preview)
	
	public HashMap<String, CouplingCriterion> getCouplingCriteria() {
		return this.couplingScore;
	}

	public float getCouplingScore() {
		return this.scoreCoupling();
	}

	private float scoreCoupling() {
		float score=0f;
		for(CouplingCriterion cc: this.couplingScore.values()) {
			score += cc.getWeight() * cc.getScore(); 
		}
		return score;
	}
	
	public void addCouplingScore(String name, CouplingCriterion couplingCriterion) {
		this.couplingScore.put(name, couplingCriterion);
	}
	*/
}

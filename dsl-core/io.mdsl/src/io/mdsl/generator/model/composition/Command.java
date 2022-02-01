package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.mdsl.utils.MDSLLogger;

// import io.mdsl.generator.model.carving.CohesionCriterion;

//Jackson changes order within YAML/JSON exports randomly if @JsonPropertyOrder annotation is not present)
@JsonPropertyOrder({"name", "type", "emittedEvents", "composite", "composedEvents"})
public class Command {

	private static final String COMMA_SEPARATOR = ", ";
	private static final String CHOICE = "choiceOf";

	/*
	private static final String COMMAND_COUNT_SUFFIX = "CommandCount";
	private static final String COUPLING_CRITERION_2 = "CmdCCrit2";
	private static final String SEPARATOR = "-";
	private static final int SAMPLE_WEIGHT = 2;
	private static final float SAMPLE_COHESION_VALUE = 1.0f;
	*/
	
	protected String name;
	private List<Event> emittedAlternativeEvents = new ArrayList<Event>();
	
	// private HashMap<String, CohesionCriterion> cohesionScore;
	
	public Command(String name) {
		if(name==null||name.equals("")) {
			throw new IllegalArgumentException("Command name must not be null or empty.");
		}
		this.name = name; 
		// this.cohesionScore = new HashMap<String, CohesionCriterion>();
		// this.addCohesionScore(name + SEPARATOR + COMMAND_COUNT_SUFFIX, new CohesionCriterion(SAMPLE_COHESION_VALUE));
		// this.addCohesionScore(name + SEPARATOR + COUPLING_CRITERION_2, new CohesionCriterion(SAMPLE_WEIGHT, SAMPLE_COHESION_VALUE));
	}
	
	@Override
    public boolean equals(Object object) {
        if(object == this) {
            return true;
        }
        if(!(object instanceof Command)) {
            return false;
        }
        Command cmd = (Command) object;
        return this.name.equals(cmd.getName());
	}
	
	public String getType() {
		return Flow.NO_OPERATOR;
	}

	public String getName() {
		return name;
	}
	
	public void addEmittedEvents(List<Event> sinks, String operator) {
		emittedAlternativeEvents.addAll(sinks);
	}

	public String firstEmittedEventName() {
		if(this.emittedAlternativeEvents.size()>0)
			return this.emittedAlternativeEvents.get(0).getName();
		else {
			MDSLLogger.reportWarning("No emitted events present");
			return null;
			// throw new IllegalStateException("No emitted events present");
		}
	}
	
	public boolean emitsSingleEvent() {
		return this.emittedAlternativeEvents.size()==1;
	}
	
	public List<Event> emits() {
		return emittedAlternativeEvents;
	}
	
	public List<Event> emits(int index) {
		return emittedAlternativeEvents.subList(index, emittedAlternativeEvents.size());
	}
	
	// this is a bean property, used by JSON/YAML export:
	public List<String> getEmittedEvents() {
		List<String> result = new ArrayList<String>();
		this.emittedAlternativeEvents.forEach(command->result.add(command.getName()));
		return result;
	}

	public boolean emitsMultipleAlternativeEvents() {
		// could check name, should start with 'OR'? could also check instance type
		return this.emittedAlternativeEvents.size()>1; 
	}
	
	public boolean isComposite() {
		return this instanceof CompositeCommand;
	}
	
	// note that gen model JSON/YAML serializer (Jackson) calls this method because it is a getter
	public List<Command> containedCommands() {
		return new ArrayList<Command>();
	}
	
	public String containedCommandsAsCommaSeparatedList(String prefix) {
		return "";
	}
	
	public boolean emitsSingleCompositeEvent() {
		return this.emittedAlternativeEvents.size()==1 
			&& this.emittedAlternativeEvents.get(0) instanceof CompositeEvent;
	}
	
	public boolean emitsSingleSimpleEvent() {
		return this.emittedAlternativeEvents.size()==1 
			&& !(this.emittedAlternativeEvents.get(0) instanceof CompositeEvent);
	}
	
	public Event singleSimpleEvent() {
		if(emitsSingleSimpleEvent()) {
			return this.emittedAlternativeEvents.get(0);
		}
		else {
			MDSLLogger.reportWarning("Expected command to emit single simple event.");
			return null;
			// throw new IllegalStateException("Expected command to emit single simple event.");
		}
	}
	
	public Event singleCompositeEvent() {
		if(emitsSingleCompositeEvent()) {
			CompositeEvent ce = (CompositeEvent) this.emittedAlternativeEvents.get(0);
			return ce; 
		}
		else {
			MDSLLogger.reportWarning("Emitted event is not a composite.");
			return null;
		}
	}
	
	public String multipleAlternativeEventsAsCommaSeparatedList(String prefix) {
		List<Event> eventList = this.emittedAlternativeEvents;		
		String result = prepareEventList(prefix, eventList);
		return result;
	}

	private String prepareEventList(String prefix, List<Event> eventList) {
		if(eventList==null) {
			MDSLLogger.reportWarning("Processing empty event list");
			return null;
		}
		
		boolean isFirst=true;
		String result = "";
		for(Event event: eventList) {
			if(isFirst) {
				isFirst=false;
				
			}
			else {
				result += COMMA_SEPARATOR;
			}
			result += prefix + event.getName();
		}
		return result;
	}
	
	public String multipleConcurrentEventsAsCommaSeparatedList(String prefix) {	
		Event andEvent = this.singleCompositeEvent();
		return prepareEventList(prefix, andEvent.composedEvents());
	}
	
	public String optionValue() {
		return CHOICE + this.name;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(" Command " + name);
		
		// TODO v55 show composed commands (if any) by overwriting this methods in subclass
		
		result.append(" emits " + emittedAlternativeEvents.size() + " event(s): ");
		emittedAlternativeEvents.forEach(event->result.append(event.getName() + " (" + event.getClass().getSimpleName() +") "));
		
		return result.toString() + "\n";
	}

	public List<String> getComposedEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	// ** cohesion and carving related (future work)

	/*
	public HashMap<String, CohesionCriterion> getCohesionCriteria() {
		return this.cohesionScore;
	}
	
	public float getCohesionScore() {
		return this.scoreCohesion();
	}

	public void addCohesionScore(String name, CohesionCriterion cohesionCriterion) {
		this.cohesionScore.put(name, cohesionCriterion);
	}
	
	private float scoreCohesion() {
		float score=0f;
		for(CohesionCriterion cc: this.cohesionScore.values()) {
			score += cc.getWeight() * cc.getScore(); 
		}
		return score;
	}
	*/
}
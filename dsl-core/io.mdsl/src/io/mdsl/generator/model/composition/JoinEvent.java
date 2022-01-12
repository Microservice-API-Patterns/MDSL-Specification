package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.EventType;

public class JoinEvent extends Event {
	private static final String JOIN_CONDITION = "_JOIN_";
	
	private List<Event> joinedEvents = new ArrayList<Event>();
	int conditionCount = 0;
	
	public JoinEvent(List<Event> joinedEvents, int conditionCount) {
		super(joinEventNameFor(joinedEvents));
		this.joinedEvents.addAll(joinedEvents);
		this.conditionCount=conditionCount;
	}
	
	public JoinEvent(String name, List<Event> joinedEvents, String operator) {
		super(name);
		this.joinedEvents.addAll(joinedEvents);
		if(operator.equals("AND"))
			this.conditionCount = joinedEvents.size();
		else
			this.conditionCount = 1;
	}

	@Override
	public boolean isJoin() {
		return true;
	}

	// used in Apache Camel FTL:
	public int getConditionCount() {
		return conditionCount;
	}
	
	@Override
	public List<Event> joinedEvents() {
		return joinedEvents;
	}

	@Override
	public List<String> getComposedEvents() {
		List<String> result = new ArrayList<String>();
		this.joinedEvents.forEach(event->result.add(event.getName()));
		return result;
	}
	
	public static String joinEventNameFor(List<Event> joinedEvents) {
		boolean isFirst=true;
		String result = "";
		for(Event event : joinedEvents) {
			if(isFirst) {
				isFirst=false;
			}
			else {
				result += JOIN_CONDITION;
			}
			result += event.getName();
		}
		return result;
	}

	public static String eventNameFor(EList<EventType> mdslEvents, String operator) {
		StringBuffer result = new StringBuffer(operator);
		mdslEvents.forEach(event->result.append("_" + event.getName()));
		return result.toString();
	}
}

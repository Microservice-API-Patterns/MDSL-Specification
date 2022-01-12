package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.List;

public class CompositeEvent extends Event {	
	private List<Event> composedEvents = new ArrayList<Event>();
	private String operator = "";
	
	public CompositeEvent(String name, List<Event> composedEvents, String operator) {
		super(name);
		this.composedEvents.addAll(composedEvents);
		this.operator = operator;
	}
	
	@Override
	public String getType() {
		return this.operator;
	}
	
	public List<Event> composedEvents() {
		return composedEvents;
	}
	
	@Override
	public List<String> getComposedEvents() {
		List<String> result = new ArrayList<String>();
		this.composedEvents.forEach(event->result.add(event.getName()));
		return result;
	}

	public static String eventNameFor(List<Event> combinedEvents, String operator) {
		boolean isFirst=true;
		String result = "";
		for(Event event : combinedEvents) {
			if(isFirst) {
				isFirst=false;
			}
			else {
				result += operator;
			}
			result += event.getName();
		}
		return result;
	}
}

package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.ArrayList;
import java.util.List;

import io.mdsl.generator.model.composition.Event;
import io.mdsl.utils.MDSLLogger;

public class CommandInvocationDuplicate extends Duplicate {
	private Event event;
	
	public CommandInvocationDuplicate(String name, Event event) {
		super(name);
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}

	public String getNextComponent() {
		List<Event> andedEvents = event.composedEvents();
		return andedEvents.get(0).getName();
	}
		
	public List<String> getTargetComponents() {
		List<Event> andedEvents = event.composedEvents();
		return namesOfQueuesForEvents(andedEvents.subList(1, andedEvents.size()));
	}

	private List<String> namesOfQueuesForEvents(List<Event> evList) {
		ArrayList<String> result = new ArrayList<String>();
		evList.forEach(nextEvent->result.add(nextEvent.getName()));
		return result;
	}
}

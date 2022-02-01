package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.ArrayList;
import java.util.List;

import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.Flow;

public class Combine extends Component {
	public static final String AGGREGATION_QUEUE_SUFFIX = "AggregationQueue";
	
	private String operator;
	private Event joinEvent;
	protected Flow flow;
		
	public Combine(String name, Event event, String operator, Flow flow) {
		super(name);
		this.joinEvent = event;
		this.operator = operator;
		this.flow = flow;
	}

	public String getName() {
		return name;
	}

	public Event getJoinEvent() {
		return joinEvent;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public List<String> getWaitQueueList() {
		List<String> result = new ArrayList<>();
		for(Event joinedEvent : this.joinEvent.joinedEvents()) {
			result.add(joinedEvent.getName());
		}
		return result;
	}

	public String getNextComponent() {
		// TODO other cases, for instance 'e1 and e1 trigger c1 or c2'
		return this.name + AGGREGATION_QUEUE_SUFFIX; 
	}

	public String dump() {
		String result = this.name;
		result +=  ", waitQueueList: " + getWaitQueueList().toString(); // JaamSimView.getNamesAsString(waitQueueList);
		result +=  ", nextComponent: " + getNextComponent();
		
		return result;
	}
}

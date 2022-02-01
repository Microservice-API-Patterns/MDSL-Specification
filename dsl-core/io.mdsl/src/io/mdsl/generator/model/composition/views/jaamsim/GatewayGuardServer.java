package io.mdsl.generator.model.composition.views.jaamsim;

import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.Flow;

// note: not to be confused with GuardInput server, two different cases/scenarios
public class GatewayGuardServer extends Server {

	private Event event;
		
	public GatewayGuardServer(String name, Event event, Command command, Flow flow) {
		super(name, command, flow);
		this.event = event;
	}

	public String getNextComponent() {
		return command.getName();
	}

	public Event getEvent() {
		return event;
	}
}

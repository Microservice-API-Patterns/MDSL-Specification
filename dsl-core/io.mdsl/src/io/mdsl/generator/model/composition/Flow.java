/*
 * Copyright 2021 The MDSL Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import io.mdsl.exception.MDSLException;
import io.mdsl.generator.model.composition.camel.CamelUtils;
import io.mdsl.generator.model.composition.views.Process;
import io.mdsl.transformations.TransformationHelpers;

// import io.mdsl.generator.model.carving.ClusterCollection;

public class Flow {
	
	public static final String XOR_OPERATOR = "XOR";
	public static final String OR_OPERATOR = "OR";
	public static final String AND_OPERATOR = "AND";
	public static final String NO_OPERATOR = "SIMPLE";

	private String name;
	private ArrayList<Command> commands; // TODO change to LinkedHashMap and hide in interface
	private HashMap<String, Event> events;
	
	// private float cohesion = 0.0f;
	// private float coupling = 0.0f;
	// private ClusterCollection flowCluster = new ClusterCollection();
	
	private Process processView = null;
	
	/**
	 * Creates a new flow.
	 * 
	 * @param name the name of the new flow
	 */
	public Flow(String name) {
		this.name = name;
		this.commands = new ArrayList<Command>();
		this.events = new LinkedHashMap<String, Event>(); // order preserving
	}

	public String getName() {
		return name;
	}
	
	public List<Command> getCommands() {
		return this.commands;
	}
	
	public Process processView() {
		if(this.processView==null) {
			this.processView = new Process(this);
		}
		return this.processView;
	}
	
	public HashMap<String, Event> getEvents() {
		return this.events;
	}
	
	public Collection<Event> eventsAsSet() {
		return this.events.values();
	}
	
	public List<Command> initCommands() {
		// commands triggered by events do not start flows
		List<Command> emittedCommands = new ArrayList<Command>();
		for(Entry<String, Event> eventEntry : this.events.entrySet()) {
			Event event = eventEntry.getValue();
			event.triggeredCommands().forEach(command->emittedCommands.add(command));
		}

		// composite commands and their parts do not start flows either
		for(Command command : this.commands) {
			if(command.isComposite()) {
				emittedCommands.add(command);
				command.containedCommands().forEach(containedCommand->emittedCommands.add(containedCommand));
			}
		}
		
		List<Command>result = (List<Command>) this.commands.clone();
		emittedCommands.forEach(command->result.remove(command));
	
		return result;
	}
	
	public HashMap<String, Event> initEvents() {
		// events emitted by commands do not start flows
		HashMap<String, Event> eventsComingFromCommands = new HashMap<String, Event>(); // TODO simplify, list of string key will do
		for(Command command : this.commands) {
			for(Event emittedEvent : command.emits()) {
				eventsComingFromCommands.put(emittedEvent.getName(), emittedEvent);
				if(emittedEvent instanceof CompositeEvent) {
					CompositeEvent andEvent = (CompositeEvent) emittedEvent;
					andEvent.composedEvents().forEach(event->eventsComingFromCommands.put(event.getName(), event));
				}
			}
		}

		// join events do not start flows
		for(Entry<String, Event> event : this.events.entrySet()) {
			if(event.getValue() instanceof JoinEvent) {
				eventsComingFromCommands.put(event.getKey(), event.getValue());
			}
		}
		
		HashMap<String, Event> result = (HashMap<String, Event>) this.events.clone();
		eventsComingFromCommands.entrySet().forEach(event->result.remove(event.getKey()));
		
		return result;
	}
	
	public HashMap<String, Event> terminationEvents() {
		// start with all events that do not have outgoing triggeredCommands
		HashMap<String, Event> result = new HashMap<String, Event>(); 
		for(Entry<String, Event> eventEntry : this.events.entrySet()) {
			if(eventEntry.getValue().triggeredCommands().size()==0
				&& !(eventEntry.getValue() instanceof CompositeEvent)
				&& !(eventEntry.getValue() instanceof JoinEvent)) {
				result.put(eventEntry.getKey(), eventEntry.getValue());
			}
		}
		
		// check join events, get ANDed in events, remove them from result
		for(Entry<String, Event> eventEntry : this.events.entrySet()) {
			if(eventEntry.getValue() instanceof JoinEvent) {
				JoinEvent je = (JoinEvent) eventEntry.getValue();
				List<Event> jes = je.joinedEvents();
				for(Event event : jes) {
					result.remove(event.getName());
				}
			}
		}

		return result;
	}
	
	public void addCommand(Command cmd) {
		if(cmd==null) {
			TransformationHelpers.reportWarning("Trying to add a null command to flow");
			return;
		}
		
		if(!commandAlreadyPresent(cmd.getName())) {
			this.commands.add(cmd);
		}
		else {
			throw new IllegalArgumentException("Command has already been added");
		}
	}
	
	public boolean commandAlreadyPresent(String commandName) {
		for(Command existingCommand : this.commands) {
			if(existingCommand.getName().equals(commandName))
				return true;
		}
		return false;
	}
	
	public Command getCommand(String commandName) {
		for(Command existingCommand : this.commands) {
			if(existingCommand.getName().equals(commandName))
				return existingCommand;
		}
		return null;
	}
	
	public void addEvent(Event ev) {
		if(!this.events.containsKey(ev.getName())) {
			this.events.put(ev.getName(), ev);
		}
		else {
			this.events.put(ev.getName(), ev);
			TransformationHelpers.reportWarning("Adding an event that has already been added: " + ev.name);
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer("Flow " + name + "\n");
		
		result.append("Number of events: " + events.size() + "\n");
		result.append("Number of commands: " + commands.size() + "\n");
		
		events.entrySet().forEach(event->result.append(event.getValue().toString()));
		commands.forEach(command->result.append(command.toString()));
		
		return result.toString();
	}
	
	public CamelUtils camelUtils() {
		// easier to do in here because $ and { are used both by Camel and by Freemarker:
		return new CamelUtils();
	}

	// ** coupling and carving related (future work)

	/*
	public void setCohesion(float cohesionSum) {
		cohesion=cohesionSum;
	}
	
	public void setCoupling(float couplingSum) {
		coupling=couplingSum;
	}
	
	public float getCohesion() {
		return cohesion;
	}
	
	public float getCoupling() {
		return coupling;
	}

	public void setFlowCluster(ClusterCollection clusterCollection) {
		flowCluster = clusterCollection;
	}

	public ClusterCollection getFlowCluster() {
		return flowCluster;
	}
	*/
}

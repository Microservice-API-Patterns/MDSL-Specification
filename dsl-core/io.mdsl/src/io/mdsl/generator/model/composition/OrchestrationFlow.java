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

import java.util.HashMap;
import java.util.TreeMap;

import io.mdsl.generator.model.carving.ClusterCollection;

/**
 * Represents an MDSL flow.
 *
 */
public class OrchestrationFlow {

	private String name;
	private TreeMap<String, Command> commands;
	private HashMap<String, Event> events;
	
	private float cohesion = 0.0f;
	private float coupling = 0.0f;
	private ClusterCollection flowCluster = new ClusterCollection();
	
	/**
	 * Creates a new flow.
	 * 
	 * @param name the name of the new flow
	 */
	public OrchestrationFlow(String name) {
		this.name = name;
		this.commands = new TreeMap<String, Command>();
		this.events = new HashMap<String, Event>();
	}

	/**
	 * Returns the name the flow.
	 * 
	 * @return the name of the flow
	 */
	public String getName() {
		return name;
	}
	
	public TreeMap<String, Command> getCommands() {
		return this.commands;
	}
	
	public HashMap<String, Event> getEvents() {
		return this.events;
	}
	
	public void addCommand(Command cmd) {
		// only add if not present already; merge otherwise
		if(!this.commands.containsKey(cmd.getName()))
			this.commands.put(cmd.getName(), cmd);
		else 
			this.commands.get(cmd.getName()).merge(cmd);
	}
	
	public void addEvent(Event ev) {
		// only add if not present already; merge otherwise
		if(!this.events.containsKey(ev.getName()))
			this.events.put(ev.getName(), ev);
		else 
			this.events.get(ev.getName()).merge(ev);
	}

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
}

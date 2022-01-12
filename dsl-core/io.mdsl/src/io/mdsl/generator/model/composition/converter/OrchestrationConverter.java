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

package io.mdsl.generator.model.composition.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.CombinedInvocationStep;
import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EventProduction;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.CompositeEvent;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.JoinEvent;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.generator.model.composition.Flow;

public class OrchestrationConverter {	
	// private static final String CLUSTER_FOR_FLOW_NAME_PREFIX = "cluster-for-flow";
	// private static ClusterCollection clustersForFlows = new ClusterCollection();
	
	public OrchestrationConverter(MDSLGeneratorModel genModel) {
	}

	/**
	 * Converts MDSL orchestration flows to a generator model orchestration.
	 * 
	 * @param mdslOrchestrationFlow the MDSL orchestration that shall be converted
	 * @return the generator model orchestration
	 */
	public Flow convert(io.mdsl.apiDescription.Orchestration mdslOrchestrationFlow) {
		Flow oflow = mapSteps(mdslOrchestrationFlow);
		// CommandCluster c4f = new CommandCluster(CLUSTER_FOR_FLOW_NAME_PREFIX + mdslOrchestration.getName());
		// applyCommandHeuristics(oflow, c4f);
		// OrchestrationConverter.clustersForFlows.addCluster(c4f);
		return oflow;
	}

	private Flow mapSteps(Orchestration mdslOrchestrationFlow) {
		EList<FlowStep> steps = mdslOrchestrationFlow.getSteps();
		Flow oflow = new Flow(mdslOrchestrationFlow.getName());
		for (FlowStep flowStep : steps) {
			CommandInvokationStep cis = flowStep.getCisStep();
			if(cis!=null) {
				mapCommandInvocationStep(oflow, cis);
			}
			DomainEventProductionStep dep = flowStep.getDepStep();
			if(dep!=null) {
				mapDomainEventProductionStep(oflow, dep);
			}
			CombinedInvocationStep ece = flowStep.getEceStep();
			if(ece!=null) {
				cis = ece.getCisStep();
				mapCommandInvocationStep(oflow, cis); // cannot be null according to grammar
				EventProduction ep = ece.getEventProduction(); // cannot be null according to grammar
				ArrayList<CommandType> cmdTypes = new ArrayList<CommandType>();
				CommandInvokation ci = cis.getAction().getCi();
				if(ci!=null) { 
					EList<CommandType> commandList = null;
					if(ci.getSci()!=null) {
						commandList = ci.getSci().getCommands();
						for(CommandType command : commandList) {
							mapEventProductionStep(oflow, command, ep);
						}
					}
					else {
						// TODO v55 implement other options: MEP, EEAP, IEAP
						TransformationHelpers.reportWarning("Combined invocation steps must have one and only one command.");
					}
				}
				else {
					TransformationHelpers.reportWarning("Combined invocation step: subprocess not supported."); 
				}
			}
		}
		return oflow;
	}

	private void mapDomainEventProductionStep(Flow oflow, DomainEventProductionStep dep) {
		// only one, no c1 + c2 emit(s) e1 in grammar
		mapEventProductionStep(oflow, dep.getAction().getCommand(), dep.getEventProduction());
	}
	
	private void mapEventProductionStep(Flow oflow, CommandType mdslCommand, EventProduction ep) {
		if(ep==null) {
			return; // not possible 
		}

		EList<EventType> events = null;
		String operator = null;

		if(ep.getSep()!=null) {
			events = ep.getSep().getEvents();
			operator = Flow.NO_OPERATOR;
		}
		else if(ep.getMep()!=null) {
			events = ep.getMep().getEvents();
			operator = Flow.AND_OPERATOR;
		}
		else if(ep.getEaep()!=null) {
			events = ep.getEaep().getEvents();
			operator = Flow.XOR_OPERATOR;
		}
		else if(ep.getIaep()!=null) {
			events = ep.getIaep().getEvents();
			operator = Flow.OR_OPERATOR;
		}
		else
			throw new IllegalArgumentException("mapCombinedEventProduction: Unsupported event production option");

		List<Event> sinks = new ArrayList<Event>();

		if(events.size()>1) {
			if(operator.equals(Flow.AND_OPERATOR)) {
				String compositeEventName = JoinEvent.eventNameFor(events, operator);
				Event andEvent = findEvent(oflow, compositeEventName);
				if(andEvent==null) {	
					List<Event> composedEvents = new ArrayList<Event>();
					events.forEach(mdslEvent->composedEvents.add(findOrCreateSingleEvent(oflow, mdslEvent)));
					andEvent = new CompositeEvent(compositeEventName, composedEvents, operator);
					oflow.addEvent(andEvent);
				}
				sinks.add(andEvent);
			}
			else if(operator.equals(Flow.OR_OPERATOR) || operator.equals(Flow.XOR_OPERATOR)) {
				for(EventType event : events) {
					Event genModelEvent = findOrCreateSingleEvent(oflow, event);
					sinks.add(genModelEvent);
				}
			}
		}
		else if (events.size()==1) {
			// could also be handled in OR/XOR loop:
			Event genModelEvent = findOrCreateSingleEvent(oflow, events.get(0));
			sinks.add(genModelEvent);
		}
		else {
			throw new MDSLException("Size of events is " + events.size());
		}

		Command cmd = findOrCreateCommand(oflow, mdslCommand);
		cmd.addEmittedEvents(sinks, operator);
	}

	private Event findOrCreateSingleEvent(Flow oflow, EventType event) {
		Event genModelEvent = findEvent(oflow, event.getName());
		if(genModelEvent==null) {
			genModelEvent = new Event(event.getName());
			oflow.addEvent(genModelEvent);
		}
		return genModelEvent;
	}

	private List<Command> mapCommandInvocationStep(Flow oflow, CommandInvokationStep cis) {
		String commandOperator = "tbd";
		String eventOperator = Flow.NO_OPERATOR; // not used
		List<Command> genModelCommands = new ArrayList<Command>();
		EList<EventType> triggeringEvents = cis.getEvents();
		EList<CommandType> invokedCommands = null;
		
		if(cis.getAction().getCi().getSci()!=null) {
			invokedCommands = cis.getAction().getCi().getSci().getCommands();
			commandOperator = Flow.NO_OPERATOR;
		}
		else if(cis.getAction().getCi().getCci()!=null) {
			invokedCommands = cis.getAction().getCi().getCci().getCommands();
			commandOperator = Flow.AND_OPERATOR;
		}
		else if(cis.getAction().getCi().getEaci()!=null) {
			invokedCommands = cis.getAction().getCi().getEaci().getCommands();
			commandOperator = Flow.XOR_OPERATOR;
		}
		else if(cis.getAction().getCi().getIaci()!=null) {
			invokedCommands = cis.getAction().getCi().getIaci().getCommands();
			commandOperator = Flow.OR_OPERATOR;
		}
		else if(cis.getAction().getSpi()!=null) {
			MDSLLogger.reportWarning("Subprocesses are not supported in the generator model, but direct grammar access is available.");
		}
		else 
			throw new MDSLException("Unknown/unsupported type of command invocation step");
		
		for(CommandType mdslCommand : invokedCommands) {
			Command command = findOrCreateCommand(oflow, mdslCommand);
			genModelCommands.add(command);
		}

		Event joinEvent;
		if(triggeringEvents.size()==1) {
			joinEvent = findEvent(oflow, triggeringEvents.get(0).getName());
			Command command = null;
			if(joinEvent==null) {
				joinEvent = new Event(triggeringEvents.get(0).getName());
				command  = joinEvent.addTriggeredCommands(genModelCommands, commandOperator);
				oflow.addEvent(joinEvent);
				// oflow.addCommand(command); // has already been added at this point
			}
			else {
				command = joinEvent.addTriggeredCommands(genModelCommands, commandOperator);
			}
			if(command!=null) {
				// add CompositeCommand created in addTriggeredCommands to flow
				if(!oflow.commandAlreadyPresent(command.getName())) {
					oflow.addCommand(command); // this CompositeCommand does not emit events itself (?)
				}
			}
		}
		else if (triggeringEvents.size()>=1) {
			eventOperator = Flow.AND_OPERATOR;
			String eventName = JoinEvent.eventNameFor(triggeringEvents, eventOperator);
			joinEvent = findEvent(oflow, eventName);
			if(joinEvent==null) {
				List<Event> genModelEvents = new ArrayList<Event>();
				for(EventType trigger : triggeringEvents) {
					// TODO v55 check whether Event exists already, do not add if so (use HashTable rather than list)
					genModelEvents.add(new Event(trigger.getName()));
					// not adding joined events to flow, done elsewhere
				}
				joinEvent = new JoinEvent(eventName, genModelEvents, eventOperator);
				Command andCommand = joinEvent.addTriggeredCommands(genModelCommands, commandOperator);
				oflow.addCommand(andCommand); 
				oflow.addEvent(joinEvent);
			}
			else {
				joinEvent.addTriggeredCommands(genModelCommands, commandOperator);
			}
		}
		else {
			throw new MDSLException("Unexpected number of triggering events: " + triggeringEvents.size());
		}
		return genModelCommands;
	}

	private Command findOrCreateCommand(Flow oflow, CommandType command) {
		Command result;
		if(oflow.commandAlreadyPresent(command.getName())) {
			result = oflow.getCommand(command.getName());
		}
		else {
			Command genModelCommand = new Command(command.getName());
			oflow.addCommand(genModelCommand);
			result = genModelCommand;
		}
		return result;
	}
	
	private Event findEvent(Flow oflow, String eventName) {
		if(oflow.getEvents().containsKey(eventName)) {
			return oflow.getEvents().get(eventName);
		}
		else {
			return null;
		}
	}

	/*
	// ** coupling and carving related
	
	private void applyCommandHeuristics(Flow oflow, CommandCluster c4f) {
		// go through all command and events in flow and calculate two scores for it
		float cohesionSum = 0f;
		float couplingSum = 0f;
		
		List<Command> cmds = oflow.getCommands();
		for(Command cmd : cmds) {
			cohesionSum+=cmd.getCohesionScore();
			c4f.addConnectedCommand(cmd.getName(), cmd);
		}
		oflow.setCohesion(cohesionSum);

		for(Event ev : oflow.getEvents().values()) {
			couplingSum+=ev.getCouplingScore();
		}
		oflow.setCoupling(couplingSum);
	}
	
	public static List<ClusterCollection> postprocessFlowConversions() {	
		ArrayList<ClusterCollection> result = new ArrayList<ClusterCollection>();
		// ClusterCollection clusterCollectionAZ = new ClusterCollection();
		
		// clusterCollectionAZ.addCluster(clusterAM);
		// clusterCollectionAZ.addCluster(clusterNZ);
		
		// result.add(clusterCollectionAZ);
		// result.add(clustersForFlows);
		
		return result;
	}

	// helper (only required for ABC heuristic):
	boolean inAtoM(String input) {
	    return (input.charAt(0) >= 'A' && input.charAt(0) <= 'M')
	    	|| (input.charAt(0) >= 'a' && input.charAt(0) <= 'm');
	}
	*/
}

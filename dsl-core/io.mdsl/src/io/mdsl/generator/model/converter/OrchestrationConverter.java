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

package io.mdsl.generator.model.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.CombinedInvocationStep;
import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.ConcurrentCommandInvokation;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EitherCommandOrOperation;
import io.mdsl.apiDescription.EitherCommandOrOperationInvokation;
import io.mdsl.apiDescription.EventProduction;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.MultipleEventProduction;
// import io.mdsl.apiDescription.OperationInvokation;
// import io.mdsl.apiDescription.ServiceOperation;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.carving.ClusterCollection;
import io.mdsl.generator.model.carving.CommandCluster;
import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.ConcurrencyCommand;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.OrchestrationFlow;

public class OrchestrationConverter {
	// private MDSLGeneratorModel genModel;
	
	// TODO taken out (PoC)
	// private static CommandCluster clusterAM = new CommandCluster("commandsAtoM");
	// private static CommandCluster clusterNZ = new CommandCluster("commandsNtoZ");
	
	private static ClusterCollection clustersForFlows = new ClusterCollection();
	
	public OrchestrationConverter(MDSLGeneratorModel genModel) {
		// this.genModel = genModel;
	}

	/**
	 * Converts MDSL orchestration flows to a generator model orchestration.
	 * 
	 * @param mdslOrchestration the MDSL orchestration that shall be converted
	 * @return the generator model orchestration
	 */
	public OrchestrationFlow convert(io.mdsl.apiDescription.Orchestration mdslOrchestration) {
		System.out.println("+++ Converting next flow: " + mdslOrchestration.getName());
		OrchestrationFlow oflow = new OrchestrationFlow(mdslOrchestration.getName());
		mapSteps(oflow, mdslOrchestration.getSteps());
		CommandCluster c4f = new CommandCluster("cluster-for-flow " + mdslOrchestration.getName());
		applyCommandHeuristics(oflow, c4f);
		OrchestrationConverter.clustersForFlows.addCluster(c4f);
		return oflow;
	}
	
	public static List<ClusterCollection> postprocessFlowConversions() {
		// need multiple when working with multiple heuristics
		
		ArrayList<ClusterCollection> result = new ArrayList<ClusterCollection>();
		ClusterCollection clusterCollectionAZ = new ClusterCollection();
		
		// clusterCollectionAZ.addCluster(clusterAM);
		// clusterCollectionAZ.addCluster(clusterNZ);
		
		// result.add(clusterCollectionAZ);
		// result.add(clustersForFlows);
		
		return result;
	}

	private void applyCommandHeuristics(OrchestrationFlow oflow, CommandCluster c4f) {
		// go through all command and events in flow and calculate two scores for it
		float cohesionSum = 0f;
		float couplingSum = 0f;
		
		TreeMap<String, Command> cmds = oflow.getCommands();
		for(Command cmd : cmds.values()) {
			cohesionSum+=cmd.getCohesionScore();
			// System.out.print("+++ Checking next cmd name: " + cmd.getName());
			
			// H1
			c4f.addConnectedCommand(cmd.getName(), cmd);
			
			// H2
			if(inAtoM(cmd.getName())) {
				// clusterAM.addConnectedCommand(cmd.getName(), cmd);
			}
			else {
				// clusterNZ.addConnectedCommand(cmd.getName(), cmd);
			}
		}
		oflow.setCohesion(cohesionSum);

		for(Event ev : oflow.getEvents().values()) {
			couplingSum+=ev.getCouplingScore();
		}
		oflow.setCoupling(couplingSum);
		
		// TODO improve score algorithmically: split flow into multiple ones (?)		
		// oflow.setFlowCluster(clusterCollectionAZ); // no longer needed (moved)
	}

	private void mapSteps(OrchestrationFlow oflow, EList<FlowStep> steps) {
		CommandInvokationStep cis = null;
		
		for (FlowStep flowStep : steps) {
			cis = flowStep.getCisStep();
			if(cis!=null) {
				mapCommandInvocationStep(oflow, cis);
			}
			
			DomainEventProductionStep dep = flowStep.getDepStep();
			if(dep!=null) {
				mapEventProductionStep(oflow, dep);
			}
			
			CombinedInvocationStep ece = flowStep.getEceStep();
			if(ece!=null) {
				cis = ece.getCisStep();
				List<Command> cmds = null;
				if(cis!=null) {
					cmds = mapCommandInvocationStep(oflow, cis);
				}
				EventProduction ep = ece.getEventProduction();
				mapCombinedEventProduction(oflow, cmds, ep);
			}
		}
	}

	private void mapCombinedEventProduction(OrchestrationFlow oflow, List<Command> cmds, EventProduction ep) {
		List<Event> sinks = new ArrayList<Event>();
		for(Command cmd : cmds) {

			if(ep.getSep()!=null) {
				EventType ev0 = ep.getSep().getEvents().get(0);
				sinks.add(createEvent(oflow, cmd, ev0));
			}
			else if (ep.getMep()!=null) {
				MultipleEventProduction mep = ep.getMep();
				for(EventType de : mep.getEvents()) {
					sinks.add(createEvent(oflow, cmd, de));
				}
			}
			else // TODO
				throw new IllegalArgumentException("Not yet supported: eae, iae options");

			cmd.addSinks(sinks);
		}
	}

	private void mapEventProductionStep(OrchestrationFlow oflow, DomainEventProductionStep dep) {
		Command cmd = mapCommand(dep.getAction());
		List<Event> sinks = this.addEvents(oflow, dep);
		cmd.addSinks(sinks);
		oflow.addCommand(cmd);
	}

	private List<Command> mapCommandInvocationStep(OrchestrationFlow oflow, CommandInvokationStep cis) {
		List<Command> cmds = mapCommandInvocation(cis.getAction());				
		List<Event> triggerList = this.addEvents(oflow, cis);
		
		for(Command nextCommand: cmds) {
			for(Event trigger : triggerList) {
				nextCommand.addTrigger(trigger);
			}
			oflow.addCommand(nextCommand);
		}
		
		return cmds;
	}
	
	private List<Event> addEvents(OrchestrationFlow oflow, DomainEventProductionStep dep) {
		List<Event> result = new ArrayList<Event>();
		if(dep.getEventProduction().getSep()!=null) {
			EventType ev0 = dep.getEventProduction().getSep().getEvents().get(0);
			result.add(createEvent(oflow, mapCommand(dep.getAction()), ev0));
		}
		else if (dep.getEventProduction().getMep()!=null) {
			MultipleEventProduction mep = dep.getEventProduction().getMep();
			for(EventType de : mep.getEvents()) {
				result.add(createEvent(oflow, mapCommand(dep.getAction()), de));
			}
		}
		else // TODO
			throw new IllegalArgumentException("Not yet supported: eae, iae options");
		
		return result;
	}

	private Event createEvent(OrchestrationFlow oflow, Command cmd, EventType de) {
		Event cev = new Event(de.getName());
		cev.addSource(cmd);
		oflow.addEvent(cev);
		return cev;
	}

	private List<Event> addEvents(OrchestrationFlow oflow, CommandInvokationStep cis) {
		List<Event> result = new ArrayList<Event>();
		for( EventType nextDomainModelElement : cis.getEvents()) {
			Event nextGenModelEvent = new Event(nextDomainModelElement.getName());
			List<Command> cmds = mapCommandInvocation(cis.getAction());
			nextGenModelEvent.addTriggeredCommands(cmds);
			oflow.addEvent(nextGenModelEvent);
			result.add(nextGenModelEvent);
		}
		return result;
	}

	private Command mapCommand(EitherCommandOrOperation eco) {
		CommandType cmd = eco.getCommand();
		// ServiceOperation op = eco.getOperation();
		if(cmd!=null)
			return new Command(cmd.getName());
		// else if(op!=null) {
		// 	return new Command(op.getTyperef() + "." +  op.getOpref());
		// }
		else
			throw new IllegalArgumentException("Internal error: neither cmd nor op"); 
	}
	
	private List<Command> mapCommandInvocation(EitherCommandOrOperationInvokation eco) {
		List<Command> result = new ArrayList<Command>();
		CommandInvokation cmd = eco.getCi();
		// OperationInvokation op = eco.getOi();
		if(cmd!=null) {
			if(cmd.getSci()!=null)
				result.add(new Command(cmd.getSci().getCommands().get(0).getName()));
			else if(cmd.getCci()!=null) {
				ConcurrentCommandInvokation cci = cmd.getCci();
				List<Command> parallelActions = new ArrayList<Command>();
				String andCommandName = "";
				for(CommandType nextCommand : cci.getCommands()) {
					parallelActions.add(new Command(nextCommand.getName()));
					andCommandName += nextCommand.getName() + "_";
				}
				andCommandName += "AND";
				result.add(new ConcurrencyCommand(andCommandName, parallelActions));
			}
			else if(cmd.getEaci()!=null)
				// TODO
				throw new IllegalArgumentException("Not yet supported: eaci");
			else if(cmd.getIaci()!=null)
				// TODO
				throw new IllegalArgumentException("Not yet supported: iaci");
			else 
				throw new IllegalArgumentException("Unsupported command invocation option");
		}
		/*
		else if(op!=null) {
			if(op.getSoi()!=null) {
				// TODO check whether typeref is null?
				result.add(new Command(op.getSoi().getOperations().get(0).getTyperef()
						+ "." + op.getSoi().getOperations().get(0).getOpref()));
			}
			else if(op.getCoi()!=null) {
				// TODO
				throw new IllegalArgumentException("Not yet supported: coi");
			}
			else if(op.getEaoi()!=null) // TODO
				throw new IllegalArgumentException("Not yet supported: eaoi");
			else if(op.getIaoi()!=null) // TODO
				throw new IllegalArgumentException("Not yet supported: iaoi");
			else 
				throw new IllegalArgumentException("Unsupported command invocation option");
		}
		*/
		else
			throw new IllegalArgumentException("Internal error: neither cmd nor op"); 
		return result;
	}
	
	// TODO move helper (only required for ABC heuristic):
	boolean inAtoM(String input) {
	    return (input.charAt(0) >= 'A' && input.charAt(0) <= 'M')
	    	|| (input.charAt(0) >= 'a' && input.charAt(0) <= 'm');
	}
}

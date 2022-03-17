package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import io.mdsl.generator.model.composition.Command;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.Flow;
import io.mdsl.generator.model.composition.views.Process;
import io.mdsl.utils.MDSLLogger;

public class JaamSimView {
	public static final String SERVER_SUFFIX_FOR_GUARDED_SERVER = "Server";
	private static final String AGGREGATION_INPUT_SUFFIX = "AggregationInput";
	private static final String SPLIT_QUEUE_SUFFIX = "SplitQueue";
	private static final String GUARD_SERVER_SUFFIX = "GuardServer";
	
	Process processView;
	Flow flow;
	
	// a single meta-map with all event/command names and Command as value 
	HashMap<String, Component> alreadyMappedFlowElements = new HashMap<String, Component>();
	
	// direct mappings:
	List<Queue> queues = new ArrayList<>();
	List<Server> servers = new ArrayList<>();
	List<Branch> branches = new ArrayList<>();
	List<Duplicate> duplicates = new ArrayList<>();
	List<Combine> combines = new ArrayList<>();
	List<Statistics> statistics = new ArrayList<>();
	// could also collect event simulation entities, entity generators, entity sinks
	// could also support Assign, Entity Logger, Entity Conveyor 
	
	// extra elements to overcome model mismatches (special mapping cases A to D): 
	List<Server> inputGuardServers = new ArrayList<>();
	List<Queue> guardInputQueues = new ArrayList<>();
	
	List<Queue> gateQueues = new ArrayList<>();
	List<Server> gatewayGuardServers = new ArrayList<>();
	List<Queue> aggregationQueues = new ArrayList<>();
	
	public JaamSimView(Flow flow) {
		this.flow = flow;
		this.processView = flow.processView();
		convertAllEventsAndCommands();
	}
	
	private void convertAllEventsAndCommands() {
		convertAllEvents();
		convertAllCommands();
	}

	private void convertAllCommands() {
		this.flow.getCommands().forEach(command->this.convertCommandToComponent(command));
		
	}

	private void convertAllEvents() {
		for(Entry<String, Event> eventEntry : this.flow.getEvents().entrySet()) {
			this.convertEventToComponent(eventEntry.getValue());
		}
	}
	
	// ** full view (getters)
	
	public List<Branch> getBranches() {		
		return this.branches;
	}
	
	public List<Queue> getQueues() {
		return this.queues;
	}
		
	public List<Server> getServers() {
		return this.servers;
	}
	
	public List<Duplicate> getDuplicates() {
		return this.duplicates;
	}
	
	public List<Combine> getCombines() {
		return this.combines;
	}
	
	public List<Statistics> getStatistics() {
		return this.statistics;
	}
	
	public List<Queue> getGateQueues() {
		return this.gateQueues;
	}
		
	public List<Queue> getAggregationQueues() {
		return this.aggregationQueues;
	}
	
	public List<Queue> getGuardInputQueues() {
		return this.guardInputQueues;
	}
	
	public List<Server> getInputGuardServers() {
		return this.inputGuardServers;
	}
		
	public List<Server> getGatewayGuardServers() {
		return this.gatewayGuardServers;
	}
		
	// ** creators (links between them calculated later in getters)
	
	private void convertCommandToComponent(Command command) {		
		// case 1: simple command (DEP step)
		if(command.emitsSingleSimpleEvent()) {
			MDSLLogger.reportDetailedInformation("Processing command " + command.getName() + ": case 1.");
			handleCase1AndCase4(command);
			// new March 7, 2022:
			return;
		}
		
		// case 2a: single command emitting multiple composed events (DEP step AND)
		if(command.emitsSingleCompositeEvent()) {
			MDSLLogger.reportDetailedInformation("Processing command " + command.getName() + ": case 2a (DEP STEP AND).");
			createAndRegisterServer(command);
			return;
		}
		
		// case 2b: single command emitting multiple events (DEP step OR/XOR)
		if(command.emitsMultipleAlternativeEvents()) {
			MDSLLogger.reportDetailedInformation("Processing command " + command.getName() + ": case 2 (DEP STEP OR or XOR).");
			List<Event> triggerList = processView.getEventsThatTrigger(command);
			if(triggerList.size()>1) {
				MDSLLogger.reportWarning(command.getName() + " is triggered by more than one event, picking first");
			}
			else if(triggerList.size()==0){
				MDSLLogger.reportError(command.getName() + " is not triggered by any event");
			}
			Event trigger = processView.getEventsThatTrigger(command).get(0); 
			// special mapping case A: DEP step (X)OR branch has to be guarded 
			createAndRegisterGatewayGuardServer(trigger.getName() + GUARD_SERVER_SUFFIX, trigger, command);
			Branch branchForCompositeCommand = new EventProductionBranch(command.getName(), command);
			if(notAlreadyPresent(branchForCompositeCommand)) {
				this.branches.add(branchForCompositeCommand);
			}
			return;
		}
		
		// case 3: composite command (CIS step AND, part 1; part 2 is in convertEventToComponent)
		if(command.isComposite()) { // must be AND, (X)OR represented differently
			MDSLLogger.reportDetailedInformation("Processing command " + command.getName() + ": case 3 (CIS step AND).");
			Duplicate duplicateForCompositeCommand = new EventProductionDuplicate(command.getName(), command);
			if(notAlreadyPresent(duplicateForCompositeCommand)) {
				this.duplicates.add(duplicateForCompositeCommand);
			}
			return;
		}
		
		// case 4:
		if(this.processView.terminatesFlow(command)) {
			MDSLLogger.reportDetailedInformation("Processing command " + command.getName() + ": case 4 (terminating command).");
			handleCase1AndCase4(command);
			
			Statistics statisticsForTerminationCommand = new Statistics(command.getName() + "Statistics", flow);
			if(notAlreadyPresent(statisticsForTerminationCommand)) {
				this.statistics.add(statisticsForTerminationCommand);
			}
			
			return;
		}
		
		// default case:
		MDSLLogger.reportWarning("Skipping command (unknown case): " + command.getName());
	}

	private void handleCase1AndCase4(Command command) {
		List<Event> triggers = processView.getEventsThatTrigger(command);
		if(triggers.size()>=2) {
			// special mapping case B: command servers can only wait on one queue, so guard servers and queue needed in front of it
			createAndRegisterCommandGuards(command);
			Server serverForCommand = new Server(command.getName() + SERVER_SUFFIX_FOR_GUARDED_SERVER, command, flow);
			if(notAlreadyPresent(serverForCommand)) {
				this.servers.add(serverForCommand);
			}
			return;
		}
		else {
			// normal case
			createAndRegisterServer(command);
			return;
		}
	}
		
	private void convertEventToComponent(Event event) {
		
		// case 0: join event 
		if(event.isJoin()) {
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 0.");
			Combine combinesForJoinEvent = new Combine(event.getName(), event, "AND", flow);
			if(notAlreadyPresent(combinesForJoinEvent)) {
				this.combines.add(combinesForJoinEvent);
			}
			// special mapping case C: single queue needed after Combines so that following server(s) have something to wait on 
			Queue agregationQueue = new Queue(event.getName() + Combine.AGGREGATION_QUEUE_SUFFIX);
			if(notAlreadyPresent(agregationQueue)) {
				this.aggregationQueues.add(agregationQueue);
			}
			return;
		}

		// case 1: simple event (CIS step)
		if(event.triggersSingleSimpleCommand()) {
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 1.");
			createAndRegisterRegularQueue(event.getName());
			return;
		}
				
		// case 2: single event emitting multiple commands (CIS step OR/XOR)
		if(event.triggersOrCommandComposition()) {
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 2.");
			Branch branchForOrCommandComposition = new CommandInvocationBranch(event.getName(), event);
			if(notAlreadyPresent(branchForOrCommandComposition)) {
				this.branches.add(branchForOrCommandComposition);
			}
			List<Command> oredCommands = event.triggeredCommands();
			for(Command nextAlternative : oredCommands) {
				createAndRegisterGateQueue(nextAlternative.getName() + Branch.CHOICE_QUEUE_SUFFIX);
			}
			return;
		}
				
		// case 3: composite event
		if(event.isComposite()) {
			// DEP step AND
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 3.");
			Duplicate duplicateForAndCommandComposition = new CommandInvocationDuplicate(event.getName(), event); 
			if(notAlreadyPresent(duplicateForAndCommandComposition)) {
				this.duplicates.add(duplicateForAndCommandComposition);
			}
			return;
		}
		
		// case 4: composite command (CIS step AND, part 2; part 1 is in convertCommandToComponent)
		if(event.triggersAndCommandComposition()) {
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 4.");
			createAndRegisterRegularQueue(event.getName());
			Command andCompositeCommand = event.singleCommand(); // must be composite
			// special mapping case D: CIS step AND needs guard server and split queue
			createAndRegisterGatewayGuardServer(event.getName() + GUARD_SERVER_SUFFIX, event, andCompositeCommand);
			List<Command> andedCommands = event.getAndComposedCommands();
			for(Command andedCommand : andedCommands) {
				createAndRegisterGateQueue(andedCommand.getName() + SPLIT_QUEUE_SUFFIX);
			}
			return;
		}
		
		// case 5a: 
		if(this.processView.participatesInJoin(event)) {
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 5a.");
			createAndRegisterRegularQueue(event.getName()); 
			return;
		}
		
		// note: join has to be checked before termination
		
		// case 5b: 
		if(this.processView.participatesInAnd(event)) {
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 5b (noop).");
			return;
		}
					
		// case 6:
		if(this.processView.terminatesFlow(event)) {
			MDSLLogger.reportDetailedInformation("Processing event " + event.getName() + ": case 6.");
			Statistics statisticsForTerminationEvent = new Statistics(event.getName(), flow);
			if(notAlreadyPresent(statisticsForTerminationEvent)) {
				this.statistics.add(statisticsForTerminationEvent);
			}
			return;
		}
		
		// default case:
		MDSLLogger.reportWarning("Skipping event (unknown case): " + event.getName());
	}

	private void createAndRegisterGateQueue(String name) {
		GateQueue newQueue = new GateQueue(name);
		
		if(notAlreadyPresent(newQueue)) {
			MDSLLogger.reportDetailedInformation("Gate/Split/ChoiceQueue newly registered: " + newQueue.getName());
			this.gateQueues.add(newQueue);
		}
		else {
			MDSLLogger.reportWarning("Gate/Split/ChoiceQueue already registered: " + newQueue.getName());
		}
	}

	private void createAndRegisterServer(Command command) {
		Server serverForCommand = new Server(command.getName(), command, flow);
		if(notAlreadyPresent(serverForCommand)) {
			this.servers.add(serverForCommand);
		}
	}

	private void createAndRegisterGatewayGuardServer(String name, Event event, Command command) {
		Server newServer;
		newServer = new GatewayGuardServer(name, event, command, flow); 

		if(notAlreadyPresent(newServer)) {
			this.gatewayGuardServers.add(newServer);
		}
	}
	
	private void createAndRegisterCommandGuards(Command command) {
		// needed: custom aggregator: one server per triggering event, aggregation queue, command server
		// so n+1 extra elements (for n triggers) and normal command server
		List<Event> triggers = processView.getEventsThatTrigger(command);
		/*
		// generated .cfg validates, but model object does not have required aggregation/pass through behavior:
		CommandCombine guard = new CommandCombine(name, command, flow);
		if(notAlreadyPresent(guard)) {
			this.combines.add(guard);
		}
		*/
		MDSLLogger.reportInformation("createAndRegisterCommandGuards: " + command.getName());
		for(Event trigger : triggers) {
			MDSLLogger.reportInformation("Processing: " + trigger.getName());
			createAndRegisterInputGuardServer(trigger, command);
		}
		
		createAndRegisterGuardQueue(command.getName());
	}
	
	private void createAndRegisterGuardQueue(String name) {
		// could create a GuardQueue class that knows its guarded server 
		Queue newQueue = new Queue(name + GuardInputServer.GUARD_QUEUE_SUFFIX);
		
		if(notAlreadyPresent(newQueue)) {
			MDSLLogger.reportDetailedInformation("GuardQueue newly registered: " + newQueue.getName());
			this.guardInputQueues.add(newQueue);
		}
		else {
			MDSLLogger.reportWarning("GuardQueue already registered: " + newQueue.getName());
		}
	}
	
	private void createAndRegisterInputGuardServer(Event trigger, Command command) {
		Server newServer = new GuardInputServer(trigger.getName() + AGGREGATION_INPUT_SUFFIX, trigger, command, flow); 

		if(notAlreadyPresent(newServer)) {
			MDSLLogger.reportDetailedInformation("Creating guard server for commmand " + command.getName() + " and event trigger " + trigger.getName());
			this.inputGuardServers.add(newServer);
		}
	}

	public void createAndRegisterRegularQueue(String name) {
		Queue newQueue = new Queue(name);	
		if(notAlreadyPresent(newQueue)) {
			this.queues.add(newQueue);
		}
	}

	
	// ** helpers
	
	private boolean notAlreadyPresent(Component component) {
		if(this.alreadyMappedFlowElements.get(component.getName())!=null) {
			MDSLLogger.reportWarning("Component already mapped (not added again): " + component.getName());
			return false;
		}
		else {
			MDSLLogger.reportDetailedInformation("Registering mapped component: " + component.getName() + " (" + component.getClass().getSimpleName() + ")");
			this.alreadyMappedFlowElements.put(component.getName(), component);
			return true;
		}
	}
	
	public static String getNamesAsString(List<Component> components) {
		String result = "";
		for(Component component : components) {
			result += " " + component.getName();
		}
		return result;
	}
	
	public String getQueueNamesAsString() {
		String result = "";
		for(Queue queue : this.getQueues()) {
			result += " " + queue.getName();
		}
		return result;
	}

	public String getServerNamesAsString() {
		String result = "";
		for(Server server : this.getServers()) {
			result += " " + server.getName();
		}
		return result;
	}
	
	public String dump() {
		StringBuffer result = new StringBuffer();
		result.append("JaamSim view of " + this.flow.getName() + "\n");
		result.append("Servers (of types regular, input guard, server guard): " + this.servers.size() + "\n");
		for(Server server : servers) {
			String nextDumpElement = server.dump() + "\n";
			result.append(nextDumpElement);
		}
		for(Server igserver : inputGuardServers) {
			String nextDumpElement = igserver.dump() + "\n";
			result.append(nextDumpElement);
		}
		for(Server gserver : gatewayGuardServers) {
			String nextDumpElement = gserver.dump() + "\n";
			result.append(nextDumpElement);
		}
		result.append("Queues (of types regular, guard, guard input, split/choice, aggregation): " + this.queues.size() + "\n");
		for(Queue queue : queues ) {
			String nextDumpElement = queue.dump() + "\n";
			result.append(nextDumpElement);
		}
		for(Queue gqueue : guardInputQueues) {
			String nextDumpElement = gqueue.dump() + "\n";
			result.append(nextDumpElement);
		}
		// split and choice queues: same mapping concept used for Duplicates and Branches 
		for(Queue scqueue : gateQueues) {
			String nextDumpElement = scqueue.dump() + "\n";
			result.append(nextDumpElement);
		}
		for(Queue aqueue : aggregationQueues) {
			String nextDumpElement = aqueue.dump() + "\n";
			result.append(nextDumpElement);
		}
		result.append("Branches: " + this.branches.size() + "\n");
		for(Branch branch : branches) {
			String nextDumpElement = branch.dump() + "\n";
			result.append(nextDumpElement);
		}
		result.append("Duplicates: " + this.duplicates.size() + "\n");
		for(Duplicate duplicate : duplicates) {
			String nextDumpElement = duplicate.dump() + "\n";
			result.append(nextDumpElement);
		}
		result.append("Combines: " + this.combines.size() + "\n");
		for(Combine combine : combines) {
			String nextDumpElement = combine.dump() + "\n";
			result.append(nextDumpElement);
		}
		result.append("Statistics: " + this.statistics.size() + "\n");
		for(Statistics statisticsElement : statistics) {
			String nextDumpElement = statisticsElement.dump() + "\n";
			result.append(nextDumpElement);
		}
		
		return result.toString();
	}
}

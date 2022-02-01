package io.mdsl.generator.model.composition.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import io.mdsl.generator.model.composition.Flow;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.Command;

public class Process {

	private static final String TERMINATION_STEP = "done";

	private Flow flow;
	
	HashMap<String, PathCollection> partialPathRepositoryForEvents = new HashMap<String, PathCollection>();
	// HashMap<String, PathCollection> partialPathRepositoryForCommands = new HashMap<String, PathCollection>();

	public Process(Flow flow) {
		this.flow = flow;
	}

	// ** basic metrics 

	public int numberOfNodes() {
		return flow.getCommands().size() + flow.getEvents().size();
	}

	public int numberOfEdges() {
		return this.getAllPaths().numberOfInvocations();
	}

	public Path deepestPath() {
		PathCollection paths = new PathCollection();
		Path deepestPath = null;
		for (int i=0; i<paths.size();i++) {
			Path path = paths.getPath(i);
			if (path.length()>deepestPath.length()) {
				deepestPath =  path;		
			}
		}
		return deepestPath;
	}

	public int deepestPathLength() {
		PathCollection paths = new PathCollection();
		int maxPathLengthSoFar = 0;
		for (int i=0; i<paths.size();i++) {
			Path path = paths.getPath(i);
			if (path.length()>maxPathLengthSoFar) {
				maxPathLengthSoFar =  path.length();	
			}
		}
		return maxPathLengthSoFar;
	}

	
	// ** string-oriented access API
	
	// part of PoC, taken out

	
	// ** access to flow gen model 
	
	// start/end events and commands

	private Collection<Event> getInitEvents() {
		return flow.initEvents().values();
	}

	public boolean initiatesFlow(Event event) {
		return flow.initEvents().get(event.getName()) != null;
	}
	
	public boolean terminatesFlow(Event event) {
		HashMap<String, Event> terminationEvents = flow.terminationEvents();
		if(terminationEvents.get(event.getName())!=null) {
			return true;
		}
		else {
			return false;
		}	
	}
	
	public List<Command> getInitiationCommands() {
		ArrayList<Command> initiators = new ArrayList<>();
		for(Command nextCommand : flow.getCommands()) {
			if(initiatesFlow(nextCommand)) {
				initiators.add(nextCommand);
			}
		}
		return initiators;
	}
	
	public boolean initiatesFlow(Command command) {
		return flow.initCommands().contains(command); 
	}

	public List<Command> getTerminationCommands() {
		ArrayList<Command> terminators = new ArrayList<>();
		for(Command nextCommand : flow.getCommands()) {
			if(terminatesFlow(nextCommand)) {
				terminators.add(nextCommand);
			}
		}
		return terminators;
	}
	
	public boolean terminatesFlow(Command command) {
		// unlike events, commands cannot appear in join clauses on left side of DEP steps
		return command.emits().size()==0 && !command.isComposite(); 
	}

	public boolean isNeitherInitiatingNorTerminatingFlow(Event event) {
		HashMap<String, Event> initEvents = flow.initEvents();
		if(initEvents==null||event==null||event.getName()==null) {
			MDSLLogger.reportError("Cannot check event status, invalid event input");
		}
		if(initEvents.get(event.getName())!=null) {
			return false;
		}

		HashMap<String, Event> terminationEvents = flow.terminationEvents();
		if(terminationEvents.get(event.getName())!=null) {
			return false;
		}
		return true;
	}
	
	// backward navigation

	public Collection<Event> triggeredBy(Command command) {
		List<Event> result = new ArrayList<Event>();
		for(Event event : flow.eventsAsSet()) {
			if(event.isJoin()) {
				// any effect? check genmodel
				List<Event> joinedEvents = event.joinedEvents();
				for(Event nextJoinedEvent : joinedEvents) {
					if(nextJoinedEvent.triggeredCommands().contains(command)) {
						result.add(event);
					}
				}
			}
			if(event.isComposite()) {
				List<Event> composedEvents = event.composedEvents();
				for(Event nextComposedEvent : composedEvents) {
					if(nextComposedEvent.triggeredCommands().contains(command)) {
						result.add(event);
					}
				}
			}
			if(event.triggeredCommands().contains(command)) {
				result.add(event);
			}
		}
		return result;
	}
	
	public List<Event> getEventsThatTrigger(Command command) {
		List<Event> result = new ArrayList<Event>();
		for(Event event : flow.eventsAsSet()) {
			List<Command> triggeredCommands = event.triggeredCommands();
			for(Command nextOrCandidate : triggeredCommands ) {
				if(nextOrCandidate.getName().equals(command.getName())) // check event id instead?
					result.add(event);
			}
		}
		return result;
	}
	
	// control flow peculiarities

	public boolean participatesInOrOrXor(Command command) {
		for(Event event : flow.eventsAsSet()) {
			List<Command> triggeredCommands = event.triggeredCommands();
			for(Command nextOrCandidate : triggeredCommands ) {
				if(event.triggersOrCommandComposition()&&nextOrCandidate.equals(command)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<Command> getCompositeCommandsWith(Command command) {
		List<Command> result = new ArrayList<Command>();
		for(Command nextAndCandidate : this.flow.getCommands()) {
			if(nextAndCandidate.isComposite()) {
				if(nextAndCandidate.containedCommands().contains(command)) {
					result.add(nextAndCandidate);
				}
			}
		}
		return result;
	}
	
	public boolean participatesInAnd(Command command) {
		for(Command nextCommand : this.flow.getCommands()) {
			if(nextCommand.isComposite()) {
				if(nextCommand.containedCommands().contains(command)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean participatesInAnd(Event event) {
		// TODO use getCompositeEventsWith here and check size of returned set
		for(Event eventSetItem : flow.eventsAsSet()) {
			if(eventSetItem.isComposite()) {
				List<Event> composedEvents = eventSetItem.composedEvents();
				for(Event composedEvent : composedEvents) {
					if(composedEvent.getName().equals(event.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public List<Event> getCompositeEventsWith(Event event) {
		List<Event> result = new ArrayList<Event>();
		for(Event eventSetItem : flow.eventsAsSet()) {
			if(eventSetItem.isComposite()) {
				List<Event> composedEvents = eventSetItem.composedEvents();
				for(Event composedEvent : composedEvents) {
					if(composedEvent.getName().equals(event.getName())) {
						result.add(eventSetItem);
					}
				}
			}
		}
		return result;
	}
	
	public List<Event> getCompositeCommandTriggerFor(Command command) {
		List<Event> result = new ArrayList<Event>();
		for(Event event : flow.eventsAsSet()) {
			List<Command> triggeredCommands = event.triggeredCommands();
			for(Command nextAndCandidate : triggeredCommands ) {
				if(nextAndCandidate.isComposite()) {
					if(nextAndCandidate.containedCommands().contains(command)) {
						result.add(event);
					}
				}
			}
		}
		return result;
	}
	
	public boolean participatesInJoin(Event event) {
		return getAllJoinEventsWith(event).size()>0;
	}
	
	public boolean participatesInJoin(Command command) {
		return getJoinEventsThatTrigger(command).size()>0;
	}
	
	public List<Event> getJoinEventsThatTrigger(Command command) {
		List<Event> result = new ArrayList<Event>();
		for(Event event : flow.eventsAsSet()) {
			List<Command> triggeredCommands = event.triggeredCommands();
			for(Command nextOrCandidate : triggeredCommands ) {
				if(event.isJoin()&&nextOrCandidate.equals(command)) {
					result.add(event);
				}
			}
		}
		return result;
	}

	private List<Event> getAllJoinEventsWith(Event event) {
		List<Event> result = new ArrayList<Event>();
		for(Entry<String, Event> joinEventCandidate : this.flow.getEvents().entrySet()) {
			if(joinEventCandidate.getValue().isJoin()) {
				if(joinEventCandidate.getValue().joinedEvents().contains(event)) {
					result.add(joinEventCandidate.getValue());
				}
			}
		}
		return result;
	}

	// ** process flow traversal and path dumpers

	public PathCollection getAllPaths() {
		PathCollection result = new PathCollection();
		Collection<Event> initEvents = this.getInitEvents();
		for(Event initEvent : initEvents) {
			PathCollection nextSetOfEndToEndPaths = getDownstreamPathsOf(initEvent);
			result.mergeWithPathCollection(nextSetOfEndToEndPaths);
		}
		
		for(Command initCommand : this.getInitiationCommands()) {
			PathCollection nextSetOfEndToEndPaths = getDownstreamPathsOf(initCommand);
			PathElement launchStep = new PathElement(this.flow.getName()+"Initiated", initCommand.getName());
			nextSetOfEndToEndPaths.addEmissionAtStartOfAllPaths(launchStep);
			result.mergeWithPathCollection(nextSetOfEndToEndPaths);
		}
		
		if(result.size()==0) {
			MDSLLogger.reportWarning("Flow " + this.flow.getName() + " does not seem to have any init events or commands, cannot find paths.");
		}
			
		return result;
	}

	public PathCollection getDownstreamPathsOf(Event event) {
		MDSLLogger.reportInformation("(gDPoE) processing: " + event.getName());
		
		boolean alreadyVisited = partialPathRepositoryForEvents.containsKey(event.getName());
		PathCollection pathsForEvent = partialPathRepositoryForEvents.get(event.getName());
		if(alreadyVisited && pathsForEvent!=null) {
			MDSLLogger.reportDetailedInformation("(gDPoE) path for: " + event.getName() + " already followed:" + pathsForEvent.dump(true));
			return pathsForEvent.cloneDeeply(); // cloning needed (?)
		} 
		else {
			MDSLLogger.reportDetailedInformation("(gDPoE) path for: " + event.getName() + " creating empty alreadyVisited entry (null)");
			partialPathRepositoryForEvents.put(event.getName(), null); // will be updated when result is there
		}
		MDSLLogger.reportInformation("(gDPoE) continue processing: " + event.getName());
		PathCollection result = null;
		
		if(this.terminatesFlow(event)) {
			result = getSingleOneStepPathForTerminationEvent(event);
			// partialPathRepositoryForEvents.put(event.getName(), result.cloneDeeply());
			MDSLLogger.reportDetailedInformation("(gDPoE) path for: " + event.getName() + " added to partialPathRepositoryForEvents" + result.dumpSizeInfo());
		}
		else if(event.triggersSingleSimpleCommand()) {
			MDSLLogger.reportDetailedInformation("(gDPoE) single simple command trigger found: " + event.getName());
			Command triggeredCommand = event.singleCommand();
			PathElement emission = new PathElement(event.getName(), triggeredCommand.getName());
			result = getDownstreamPathsOf(triggeredCommand);
			// MDSLLogger.reportDetailedInformation("(gDPoE) Adding emission path element: " + event.getName() + " -> " + triggeredCommand.getName());
			result.insertAtStartOfAllPaths(emission);
			// MDSLLogger.reportDetailedInformation("(gDPoE) Added emission path element: " + result.dump(true));
		}
		else if(event.triggersAndCommandComposition()) {
			MDSLLogger.reportInformation("(gDPoE) composed command trigger found (and): " + event.getName());
			List<Command> compositeCommands = event.getAndComposedCommands(); 
			result = createFreshPathCollection();
			for(Command nextComposedCommand : compositeCommands) { 
				PathCollection pathsFromCommand = getDownstreamPathsOf(nextComposedCommand);
				PathElement andTransition = new PathElement(event.getName(), nextComposedCommand.getName());
				pathsFromCommand.addEmissionAtStartOfAllPaths(andTransition);
				result.mergeWithPathCollection(pathsFromCommand);
			}
		}
		else if(event.triggersOrCommandComposition()) {
			MDSLLogger.reportInformation("(gDPoE) alternative command trigger found (or, xor): " + event.getName());
			List<Command> triggeredCommands = event.triggeredCommands();
			result = createFreshPathCollection();
			for(Command nextTriggeredCommand : triggeredCommands) {
				PathCollection pathsFromCommand = getDownstreamPathsOf(nextTriggeredCommand);
				PathElement andTransition = new PathElement(event.getName(), nextTriggeredCommand.getName());
				pathsFromCommand.addEmissionAtStartOfAllPaths(andTransition);
				result.mergeWithPathCollection(pathsFromCommand);
			}
		}
		else if(event.triggeredCommands().size()==0) {
			MDSLLogger.reportInformation(event.getName() + "(gDPoE) does not trigger any commands.");
			result = createFreshPathCollection();
		}
		else {
			MDSLLogger.reportError("Unknown or unsupported event type: " + event.getName());
		}
		
		if(this.participatesInJoin(event)) {
			MDSLLogger.reportInformation(event.getName() + " participates in one or more join events.");
			List<Event> joinEvents = this.getAllJoinEventsWith(event);
			PathCollection resultSoFar = result.cloneDeeply();
			// result.clear(); // this caused current result to be overwritten/left out in case of overlaps in flow (test case 4c)
			for(Event nextJoinEvent : joinEvents) {
				// MDSLLogger.reportDetailedInformation("(gDPoE) joinpart: result for " + event.getName() + " before join processing" + result.dump(true));
				PathCollection nextPartialResult = combineAndClonePathsForJoinEventAggregation(resultSoFar, event, nextJoinEvent);
				result.mergeWithPathCollection(nextPartialResult);
				MDSLLogger.reportDetailedInformation("(gDPoE) joinpart: result for" + event.getName() + " after join processing" + result.dump(true));
			}
		}

		partialPathRepositoryForEvents.put(event.getName(), result.cloneDeeply()); // cloning needed here!
		MDSLLogger.reportInformation("(gDPoE) path for: " + event.getName() + " updated in partialPathRepositoryForEvents" + result.dump(false));
		return result;
	}

	private PathCollection combineAndClonePathsForJoinEventAggregation(PathCollection pathsUpToHere, Event launchingEvent, Event joinEvent) {
		PathCollection nextPaths;
		if(!alreadyVisitedBefore(joinEvent)) {
			MDSLLogger.reportInformation("(gDPoC) First time join event is visited: " + joinEvent.getName());
			nextPaths = getDownstreamPathsOf(joinEvent);
		}
		else {
			MDSLLogger.reportInformation("(gDPoC) Revisiting join event: " + joinEvent.getName());
			MDSLLogger.reportInformation(pathsUpToHere.dump(true));
			nextPaths = alreadyFoundsPathFor(joinEvent).cloneDeeply();
			MDSLLogger.reportDetailedInformation("(gDPoC) Found: " + nextPaths.dump(true));
		}
		PathElement aggregation = new PathElement(launchingEvent.getName(), joinEvent.getName());
		nextPaths.addEmissionAtStartOfAllPaths(aggregation);
		return nextPaths;
		
	}

	public PathCollection getDownstreamPathsOf(Command command) {
		
		// could check whether repo has this command already (see event visit)
		if(command.isComposite()) {
			MDSLLogger.reportInformation("(gDPoC) processing composite command " + command.getName());
			List<Command> composedCommands = command.containedCommands();
			PathCollection pathStartingHere = createFreshPathCollection();
			for(Command nextComposedCommand : composedCommands) { 
				PathCollection pathsFromCommand = getDownstreamPathsOf(nextComposedCommand);
				pathStartingHere.mergeWithPathCollection(pathsFromCommand);
			}
			return pathStartingHere;
		}

		if(command.emitsSingleSimpleEvent()) {
			MDSLLogger.reportInformation("(gDPoC) processing single simple even emission in command " + command.getName());
			Event emittedEvent = command.singleSimpleEvent();
			PathCollection pathStartingAtEmittedEvent = visitIfNotAlreadyDoneOrGoingOn(emittedEvent, command.getName());
			return pathStartingAtEmittedEvent;
		}
		else if(command.emitsSingleCompositeEvent()) {
			MDSLLogger.reportInformation("(gDPoC) processing single composite event emission (and) in command " + command.getName());
			List<Event> emittedEvents = command.singleCompositeEvent().composedEvents();
			PathCollection pathStartingHere = createFreshPathCollection();
			for(Event emittedEvent : emittedEvents) {
				PathCollection pathStartingAtEmittedEvent = visitIfNotAlreadyDoneOrGoingOn(emittedEvent, command.getName());
				pathStartingHere.mergeWithPathCollection(pathStartingAtEmittedEvent);
			}
			return pathStartingHere;
		}
		else if(command.emitsMultipleAlternativeEvents()) {
			MDSLLogger.reportInformation("(gDPoC) processing multiple event emission (or, xor) in command " + command.getName());
			List<Event> emittedEvents = command.emits();
			PathCollection pathsStartingHere = createFreshPathCollection();
			for(Event emittedEvent : emittedEvents) {
				PathCollection pathStartingAtEmittedEvent = visitIfNotAlreadyDoneOrGoingOn(emittedEvent, command.getName());
				PathCollection copy = pathStartingAtEmittedEvent.cloneDeeply();
				pathsStartingHere.mergeWithPathCollection(copy);			
				pathStartingAtEmittedEvent.clear();
			}
			return pathsStartingHere;
		}
		else if(this.terminatesFlow(command)) {
			MDSLLogger.reportInformation("(gDPoC) command that terminates flow reached, noop: " + command.getName());
			return createFreshPathCollection(); 
		}
		else {
			MDSLLogger.reportError("Unknown or unsupported command type: " + command.getName());
			return null; // not reached
		}
	}
	
	// ** helpers
	
	public static PathCollection createFreshPathCollection() {
		PathCollection newPathCollection = new PathCollection();
		Path initialPath = new Path();
		newPathCollection.addPath(initialPath);
		return newPathCollection;
	}

	private PathCollection getSingleOneStepPathForTerminationEvent(Event event) {
		PathCollection result = createFreshPathCollection();
		PathElement flowEnds = new PathElement(event.getName(), TERMINATION_STEP);
		result.addEmissionToAllPaths(flowEnds);
		PathCollection flowEndPC = new PathCollection();
		Path flowEndPath = new Path();
		flowEndPath.addEmission(flowEnds);
		flowEndPC.addPath(flowEndPath);

		return result;
	}

	private PathCollection visitIfNotAlreadyDoneOrGoingOn(Event emittedEvent, String originatingNode) {
		PathCollection pathStartingAtEmittedEvent;
		if(!alreadyBeingVisited(emittedEvent)) {
			MDSLLogger.reportInformation("(gDPoC) processing event is ongoing already: " + emittedEvent.getName());
			pathStartingAtEmittedEvent = getDownstreamPathsOf(emittedEvent);
		}
		else if(!alreadyVisitedBefore(emittedEvent)) {
			MDSLLogger.reportInformation("(gDPoC) not processing event again, has been done already: " + emittedEvent.getName());
			pathStartingAtEmittedEvent = alreadyFoundsPathFor(emittedEvent);
			if(pathStartingAtEmittedEvent==null) {
				pathStartingAtEmittedEvent = createFreshPathCollection();
			}
			else {
				pathStartingAtEmittedEvent = pathStartingAtEmittedEvent.cloneDeeply(); // clone deeply is new!
			}
			PathElement continuationMarker = new PathElement(originatingNode, "GOTO-" + emittedEvent.getName());
			pathStartingAtEmittedEvent.addEmissionAtStartOfAllPaths(continuationMarker);
		}
		else {
			MDSLLogger.reportDetailedInformation("(gDPoC) not been there before, so will process event now: " + emittedEvent.getName());
			pathStartingAtEmittedEvent = getDownstreamPathsOf(emittedEvent);
		}
		return pathStartingAtEmittedEvent;
	}
	
	public boolean alreadyBeingVisited(Event event) {
		return partialPathRepositoryForEvents.containsKey(event.getName())&&(partialPathRepositoryForEvents.get(event.getName())==null);
	}
	
	public boolean alreadyVisitedBefore(Event event) {
		return partialPathRepositoryForEvents.containsKey(event.getName())&&(partialPathRepositoryForEvents.get(event.getName())!=null);
	}

	private PathCollection alreadyFoundsPathFor(Event event) {
		return partialPathRepositoryForEvents.get(event.getName());
	}
	
	public String dumpAllPaths() {
		MDSLLogger.reportInformation("(dAP) Finding all paths.");
		PathCollection paths = this.getAllPaths();
		MDSLLogger.reportDetailedInformation("(dAP) Dump found paths.\n");
		if(paths!=null) {
			return "Dumping flow \"" + this.flow.getName() +  "\": \n\n" + paths.toString();
		}
		else {
			TransformationHelpers.reportWarning("Path collection is null/empty.");
			return null;
		}
	}
	
	public String dumpAllPathsDirectly() {
		PathCollection paths = this.getAllPaths();
		return paths.dump(false);
	}
	
	public String exportAsSketchMinerStories() {
		// note: AND would have to be treated differently (fragment)?
		// see https://www.bpmn-sketch-miner.ai/doc/10-ref.html
		PathCollection paths = this.getAllPaths();
		return this.getAllPaths().toString();
	}
}

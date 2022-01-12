/*
 * Copyright 2022 The Context Mapper and MDSL Project Teams
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.CombinedInvocationStep;

// import org.apache.commons.lang3.StringUtils;

import io.mdsl.apiDescription.CommandInvokation;
import io.mdsl.apiDescription.CommandInvokationStep;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.ConcurrentCommandInvokation;
import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.apiDescription.EitherCommandOrOperation;
import io.mdsl.apiDescription.EventProduction;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.FlowStep;
import io.mdsl.apiDescription.InclusiveAlternativeCommandInvokation;
import io.mdsl.apiDescription.InclusiveAlternativeEventProduction;
import io.mdsl.apiDescription.MultipleEventProduction;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.generator.model.composition.sketchminer.SimplifiedFlowStep;
import io.mdsl.generator.model.composition.sketchminer.SketchMinerModel;
import io.mdsl.generator.model.composition.sketchminer.Task;
import io.mdsl.generator.model.composition.sketchminer.TaskSequence;
import io.mdsl.generator.model.composition.sketchminer.TaskType;
import io.mdsl.generator.model.composition.sketchminer.SimplifiedFlowStep.ToType;
import io.mdsl.utils.MDSLLogger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Flow2SketchMinerConverter {

	private Orchestration flow;
	private List<SimplifiedFlowStep> simplifiedSteps;
	private Map<String, Task> taskMap;
	private SketchMinerModel model;

	public Flow2SketchMinerConverter(Orchestration flow) {
		this.flow = flow;
		this.model = new SketchMinerModel(flow.getName());
		initIntermediateTypes();
	}

	public SketchMinerModel convert() {
		for (Task initialTask : getInitialTasks()) {
			TaskSequence seq = new TaskSequence(initialTask);
			model.addSequence(seq);
			finishSequence(seq);
		}
		model.cleanupDuplicateSequences();
		return model;
	}

	private void finishSequence(TaskSequence seq) {
		Task lastTask = seq.getLastTaskInSequence();
		List<SimplifiedFlowStep> nextSteps = getNextSteps(lastTask);
		if (!nextSteps.isEmpty()) {
			for (SimplifiedFlowStep nextStep : nextSteps) {
				Task nextStepsParallelTask = createParallelTask(nextStep.getFroms());
				if (nextStep.getFroms().size() > 1 && !nextStepsParallelTask.equals(lastTask) && !seq.getTasks().contains(nextStepsParallelTask)) {
					seq.isSplittingFragment(true);
					Task mergingTask = createParallelTask(nextStep.getFroms());
					TaskSequence newSeq = new TaskSequence(mergingTask);
					model.addSequence(newSeq);
					newSeq.isMergingFragment(true);
					finishSequence(newSeq);
				} else if (nextStep.getTos().size() == 1) {
					if (seq.addTask(nextStep.getTos().iterator().next()))
						finishSequence(seq);
				} else if (nextStep.getToType().equals(ToType.AND)) {
					endSequenceAsFragment(seq, nextStep.getTos());
				} else {
					forkSequence(seq, nextStep.getTos());
				}
			}
		}
	}

	private void forkSequence(TaskSequence seq, Collection<Task> nextTasks) {
		Iterator<Task> it = nextTasks.iterator();
		Task firstTask = it.next();
		while (it.hasNext()) {
			Task nextTask = it.next();
			createNewSequenceWithTask(seq, nextTask);
		}
		if (seq.addTask(firstTask))
			finishSequence(seq);
	}

	private void endSequenceAsFragment(TaskSequence seq, Collection<Task> nextTasks) {
		seq.isSplittingFragment(true);
		seq.addTask(createParallelTask(nextTasks));
		for (Task task : nextTasks) {
			if (seq.getTasks().contains(task))
				continue;

			TaskSequence newSeq = new TaskSequence(task);
			newSeq.isMergingFragment(true);
			model.addSequence(newSeq);
			finishSequence(newSeq);
		}
	}

	private void createNewSequenceWithTask(TaskSequence seq, Task nextTask) {
		TaskSequence newSeq = seq.copy();
		if (newSeq.addTask(nextTask))
			finishSequence(newSeq);
		model.addSequence(newSeq);
	}

	private Task createParallelTask(Collection<Task> allTasks) {
		Iterator<Task> it = allTasks.iterator();
		Task firstTask = it.next();
		List<Task> parallelTasks = Lists.newLinkedList();
		while (it.hasNext())
			parallelTasks.add(it.next());
		return new Task(firstTask.getName(), firstTask.getType(), parallelTasks);
	}

	private List<SimplifiedFlowStep> getNextSteps(Task lastTask) {
		List<SimplifiedFlowStep> nextSteps = Lists.newLinkedList();
		for (SimplifiedFlowStep step : simplifiedSteps) {
			for (Task task : step.getFroms()) {
				if (task.equalsOrContainsTask(lastTask))
					nextSteps.add(step);
			}
		}
		return nextSteps;
	}

	private void initIntermediateTypes() {
		this.simplifiedSteps = Lists.newLinkedList();
		this.taskMap = Maps.newLinkedHashMap();

		for (FlowStep step : flow.getSteps()) {
			convert(step);
		}
	}
	
	// (!) code must be different because grammars seem to differ: instanceof vs. getNN()!=null (prefixes in grammar?)

	private void convert(FlowStep step) {
		Set<Task> froms = Sets.newLinkedHashSet();
		Set<Task> tos = Sets.newLinkedHashSet();
		if (step.getCisStep()!=null) {
			CommandInvokationStep cisStep = step.getCisStep();
			convertCisStep(froms, tos, cisStep);
		} 
		else if (step.getDepStep()!=null) {
			DomainEventProductionStep eventStep = (DomainEventProductionStep) step.getDepStep();
			convertDepStep(froms, tos, eventStep);
		}
		else if (step.getEceStep()!=null) {
			// TODO not working yet (strange output)
			CombinedInvocationStep eceStep = step.getEceStep();
			CommandInvokationStep cisStep = eceStep.getCisStep();
			convertCisStep(froms, tos, cisStep);
			
			EventProduction ep = eceStep.getEventProduction();
			// TODO check and document constraint: command in ece step must be simple
			// copied from FlowTransformations (QF):
			CommandType commandInEceStep = cisStep.getAction().getCi().getSci().getCommands().get(0);
			EitherCommandOrOperation simpleCommand = ApiDescriptionFactory.eINSTANCE.createEitherCommandOrOperation();
			simpleCommand.setCommand(commandInEceStep);
			DomainEventProductionStep surrogateDepStep = ApiDescriptionFactory.eINSTANCE.createDomainEventProductionStep();
			surrogateDepStep.setEventProduction(EcoreUtil.copy(ep));
			surrogateDepStep.setAction(simpleCommand);		
			
			Set<Task> froms2 = Sets.newLinkedHashSet();
			Set<Task> tos2 = Sets.newLinkedHashSet();
			convertDepStep(froms2, tos2, surrogateDepStep);
		}
		else {
			MDSLLogger.reportWarning("Unknown or unsupported flow step type: " + step.getClass());
		}
	}

	private void convertDepStep(Set<Task> froms, Set<Task> tos, DomainEventProductionStep eventStep) {
		ToType toType = ToType.XOR; // default (?)
		froms.add(createTask4EventProduction(eventStep));
		EList<EventType> events = getEvents(eventStep.getEventProduction());
		tos.addAll(events.stream().map(e -> getOrCreateTask(e.getName(), TaskType.EVENT)).collect(Collectors.toList()));
		if (eventStep.getEventProduction() instanceof MultipleEventProduction)
			toType = ToType.AND;
		if (eventStep.getEventProduction() instanceof InclusiveAlternativeEventProduction)
			toType = ToType.OR;
		this.simplifiedSteps.add(new SimplifiedFlowStep(froms, tos, toType));
	}

	private void convertCisStep(Set<Task> froms, Set<Task> tos, CommandInvokationStep cisStep) {
		ToType toType = ToType.XOR; // default (?)
		froms.addAll((cisStep.getEvents().stream().map(e -> getOrCreateTask(e.getName(), TaskType.EVENT))).collect(Collectors.toList()));
		if (cisStep.getAction().getCi()!=null) {
			CommandInvokation commandInvocation = cisStep.getAction().getCi(); 
			EList<CommandType> commands = getCommands(commandInvocation);
			tos.addAll(commands.stream().map(c -> getOrCreateTask(c.getName(), TaskType.COMMAND)).collect(Collectors.toList()));
			if (commandInvocation instanceof ConcurrentCommandInvokation)
				toType = ToType.AND;
			if (commandInvocation instanceof InclusiveAlternativeCommandInvokation)
				toType = ToType.OR;
			this.simplifiedSteps.add(new SimplifiedFlowStep(froms, tos, toType));
		}
		else {
			// TODO (future work, known limitation) SubProcessInvocation is not supported yet
			MDSLLogger.reportError("Unkonwn/unsupported type of CIS action: " + cisStep.getAction().getClass());
		}
	}

	private EList<CommandType> getCommands(CommandInvokation commandInvokation) {
		if(commandInvokation.getSci()!=null) {
			return commandInvokation.getSci().getCommands();
		}
		if(commandInvokation.getEaci()!=null) {
			return commandInvokation.getEaci().getCommands();
		}
		if(commandInvokation.getIaci()!=null) {
				return commandInvokation.getIaci().getCommands();
		}
		if(commandInvokation.getCci()!=null) {
			return commandInvokation.getCci().getCommands();
	}
		return null;
	}
	
	private EList<EventType> getEvents(EventProduction eventProduction) {
		if(eventProduction.getSep()!=null) {
			return eventProduction.getSep().getEvents();
		}
		if(eventProduction.getMep()!=null) {
			return eventProduction.getMep().getEvents();
		}
		if(eventProduction.getEaep()!=null) {
			return eventProduction.getEaep().getEvents();
		}
		if(eventProduction.getIaep()!=null) {
			return eventProduction.getIaep().getEvents();
		}
		return null;
	}

	private Task getOrCreateTask(String name, TaskType type) {
		if (taskMap.containsKey(name))
			return taskMap.get(name);
		Task task = new Task(name, type);
		taskMap.put(name, task);
		return task;
	}

	private Task createTask4EventProduction(DomainEventProductionStep eventStep) {
		String name = "UndefinedTask";
		if (eventStep.getAction().getCommand() != null) {
			name = eventStep.getAction().getCommand().getName();
		} 
		Task task = getOrCreateTask(name, TaskType.COMMAND);
		return task;
	}

	private List<Task> getInitialTasks() {
		List<Task> initialTasks = Lists.newLinkedList();
		for (Task task : this.taskMap.values()) {
			if (isInitialTask(task))
				initialTasks.add(task);
		}
		if (initialTasks.isEmpty()) { // just take the first mentioned task if we cannot find clear entry point
			SimplifiedFlowStep firstStep = this.simplifiedSteps.get(0);
			Task generatedStartTask = new Task(getGeneratedStartName(), TaskType.EVENT);
			Set<Task> generatedFroms = Sets.newLinkedHashSet();
			Set<Task> generatedTos = Sets.newLinkedHashSet();
			// new (InitialEvent still does not have an actor name in output):
			MDSLLogger.reportInformation("Setting initial event: " + this.model.getDefaultActorName());
			generatedStartTask.setActor(this.model.getDefaultActorName());
			generatedFroms.add(generatedStartTask);
			generatedTos.add(firstStep.getFroms().iterator().next());
			SimplifiedFlowStep generatedStep = new SimplifiedFlowStep(generatedFroms, generatedTos, ToType.OR);
			this.simplifiedSteps.add(generatedStep);
			initialTasks.add(generatedStartTask);
		}
		return initialTasks;
	}

	private String getGeneratedStartName() {
		String initName = "Initialize" + this.model.getDefaultActorName();
		String name = initName;
		int counter = 0;
		while (this.taskMap.keySet().contains(name)) {
			name = initName + counter;
			counter++;
		}
		return name;
	}

	private boolean isInitialTask(Task potentialInitTask) {
		for (SimplifiedFlowStep step : this.simplifiedSteps) {
			if (step.getTos().contains(potentialInitTask))
				return false;
		}
		return true;
	}
}

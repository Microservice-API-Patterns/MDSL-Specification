package io.mdsl.generator.model.composition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

import io.mdsl.generator.model.carving.CohesionCriterion;
import io.mdsl.generator.model.carving.CouplingCriterion;

public class Command {
	protected String name;
	private List<Event> emits;
	private List<Event> triggeredBy;
	// could also list all related commands (upstream)
	
	private HashMap<String, CohesionCriterion> cohesionScore;
	
	public Command(String name) {
		this.name = name;
		this.emits = Lists.newLinkedList();
		this.triggeredBy = Lists.newLinkedList();
		this.cohesionScore = new HashMap<String, CohesionCriterion>();
		this.addCohesionScore(name + "-CommandCount", new CohesionCriterion(1.0f));
		this.addCohesionScore(name + "-CmdCCrit2", new CohesionCriterion(2, 1.0f));
	}

	public String getName() {
		return name;
	}
	
	public void addTrigger(Event ev) {
		triggeredBy.add(ev);
	}
	
	public void addSink(Event ev) {
		emits.add(ev);
	}
	
	public void addSinks(List<Event> sinks) {
		emits.addAll(sinks);
	}

	public List<Event> getTriggeredBy() {
		return triggeredBy;
	}

	public List<Event> getEmits() {
		return emits;
	}

	public void merge(Command cmd) {
		List<Event> ees = cmd.getEmits();
		this.emits.addAll(ees);
		
		// TODO merge cohesion criteria hash tables too
		
		this.triggeredBy.addAll(cmd.triggeredBy);
	}
	
	public HashMap<String, CohesionCriterion> getCohesionCriteria() {
		return this.cohesionScore;
	}

	public float getCohesionScore() {
		return this.scoreCohesion();
	}

	private float scoreCohesion() {
		float score=0f;
		for(CohesionCriterion cc: this.cohesionScore.values()) {
			score += cc.getWeight() * cc.getScore(); 
			// System.out.println("[CMD] Scoring " + cc.getWeight() + " and " + cc.getScore()); 
		}
		return score;
	}

	public void addCohesionScore(String name, CohesionCriterion cohesionCriterion) {
		this.cohesionScore.put(name, cohesionCriterion);
	}
	
	public List<Invocation> getInvocations() {
		List<Invocation> directInvocations = new ArrayList<Invocation>();
		for(Event ev : this.emits) {
			for(Command emittedAndTriggered : ev.getTriggers()) {
				if(emittedAndTriggered.isAnd())
					directInvocations.add(new ParallelInvocation(ev.getName(), emittedAndTriggered.getName(), emittedAndTriggered.getConcurrentCommands()));
				else
					directInvocations.add(new Invocation(ev.getName(), emittedAndTriggered.getName()));
			}
		}
		return directInvocations;	
	}

	public List<String> getConcurrentCommands() {
		return new ArrayList<String>(); // could also return null
	}

	public boolean isAnd() {
		System.out.println(name + " is not an AND");
		return false;
	}
}
package io.mdsl.generator.model.composition;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

import io.mdsl.generator.model.carving.CouplingCriterion;

public class Event {
	private String name;
	private List<Command> triggers;
	private List<Command> emittedBy;
	
	// could move this to a superclass "scoreable element"
	private HashMap<String, CouplingCriterion> couplingScore;
	
	public Event(String name) {
		this.name = name;
		this.triggers = Lists.newLinkedList();
		this.emittedBy = Lists.newLinkedList();
		this.couplingScore = new HashMap<String, CouplingCriterion>();
		
		this.addCouplingScore(name + "-EventCount", new CouplingCriterion(1.0f));
		this.addCouplingScore(name + "-EvCCrit2", new CouplingCriterion(2, 0.5f));
	}
	
	public void addTriggeredCommand(Command cmd) {
		triggers.add(cmd);
	}
	
	public void addTriggeredCommands(List<Command> cmds) {
		triggers.addAll(cmds);
	}
	
	public void addSource(Command cmd) {
		emittedBy.add(cmd);
	}
	
	public String getName() {
		return name;
	}

	public List<Command> getTriggers() {
		return triggers;
	}

	public List<Command> getEmittedBy() {
		return emittedBy;
	}

	public void merge(Event ev) {
		this.emittedBy.addAll(ev.emittedBy);
		this.triggers.addAll(ev.triggers);
		
		// TODO merge coupling criteria hash tables too
	}
	
	public HashMap<String, CouplingCriterion> getCouplingCriteria() {
		return this.couplingScore;
	}

	public float getCouplingScore() {
		return this.scoreCoupling();
	}

	private float scoreCoupling() {
		float score=0f;
		for(CouplingCriterion cc: this.couplingScore.values()) {
			score += cc.getWeight() * cc.getScore(); 
			// System.out.println("[EV] Scoring " + cc.getWeight() + " and " + cc.getScore()); 
		}
		return score;
	}
	
	public void addCouplingScore(String name, CouplingCriterion couplingCriterion) {
		this.couplingScore.put(name, couplingCriterion);
	}

}

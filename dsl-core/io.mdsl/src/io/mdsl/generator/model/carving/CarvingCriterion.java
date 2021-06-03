package io.mdsl.generator.model.carving;

import java.util.List;

public class CarvingCriterion {
	enum Type {Operational, Developmental, LongTermEvolutionAndMaintenance}
	
	public final static String cc1="IDENTITY_COMMONALITY"; // from Service Cutter
	// TODO tbc
	
	// TODO decide on most suited types
	protected float weight = 1;
	protected float score;
	
	public CarvingCriterion(int weight, float score) {
		this.weight = weight;
		this.score = score;
	}
	
	public float getWeight() {
		return weight;
	}
	
	public void setWeight(float weight) {
		this.weight = weight;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public static void setDefaultSWeightForType(CarvingCriterion.Type type, List<CarvingCriterion> criteria) {
		// TODO find all instances in a collection (command or event)
		// check their type and set to 0, 0.5, 1 depending on it
	}
}

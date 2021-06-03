package io.mdsl.generator.model.carving;

public class CouplingCriterion extends CarvingCriterion {
	
	public CouplingCriterion(float score) {
		super(1, score);
	}
	
	public CouplingCriterion(int weight, float score) {
		super(weight, score);
	}
}

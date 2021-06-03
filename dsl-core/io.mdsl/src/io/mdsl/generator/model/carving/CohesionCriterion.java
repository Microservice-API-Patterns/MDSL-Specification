package io.mdsl.generator.model.carving;

public class CohesionCriterion extends CarvingCriterion {
	
	public CohesionCriterion(float score) {
		super(1, score);
	}
	
	public CohesionCriterion(int weight, float score) {
		super(weight, score);
	}
}

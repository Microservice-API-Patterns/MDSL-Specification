package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.generator.OpenAPIGenerator;
import io.mdsl.transformations.ScenarioTransformationHelpers;
import io.mdsl.transformations.ScenarioTransformations;
import io.mdsl.transformations.TransformationChains;
import io.mdsl.transformations.TransformationHelpers;

public class StoryToOpenAPIGenerator extends AbstractMDSLGenerator {

	public StoryToOpenAPIGenerator(String desiredQuality) {
		super();
		this.desiredQuality = desiredQuality;
	}

	private String desiredQuality = "all";

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		ScenarioTransformations st = new ScenarioTransformations();
		// TODO just a PoC, working with first scenario and story
		ServiceSpecificationAdapter ssa = new ServiceSpecificationAdapter(mdslSpecification);
		IntegrationScenario firstScenario = ssa.getScenarios().get(0);
		// IntegrationStory firstStory = firstScenario.getStories().get(0);
		
		ScenarioTransformations sts = new ScenarioTransformations();
		sts.addEndpointForScenario(firstScenario, true);
		
		if(desiredQuality.equals("all") || desiredQuality.equals("performance")) {
			TransformationChains tc = new TransformationChains();
			tc.applyEntireChainToAllScenariosAndStories(mdslSpecification); 
		}
		
		OpenAPIGenerator oag = new OpenAPIGenerator();
		oag.doGenerate(mdslSpecification.eResource(), fsa, null);
		
		// CharSequence result = "// SOAD transformation 'StoryToOpenAPI' applied (quality goal: " + desiredQuality + ").\n";
		// RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, mdslSpecification, result);		
	}
}

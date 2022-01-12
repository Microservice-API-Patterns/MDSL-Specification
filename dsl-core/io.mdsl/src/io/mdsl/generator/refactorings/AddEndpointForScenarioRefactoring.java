package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

// import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.ScenarioTransformations;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.utils.MDSLLogger;

public class AddEndpointForScenarioRefactoring extends AbstractMDSLGenerator {
	
	private String scenarioName;
	private String storyName;
	
	public AddEndpointForScenarioRefactoring(String scenarioName, String storyName) {
		super();
		this.scenarioName = scenarioName; 
		this.storyName = storyName; 
	}

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		for (IntegrationScenario scenario : new ServiceSpecificationAdapter(mdslSpecification).getScenarios()) {
			if(scenario.getName().equals(scenarioName)) {
				for(IntegrationStory story : scenario.getStories()) {
					if(story.getName().equals(storyName)) {					
						ScenarioTransformations s2et = new ScenarioTransformations();
						s2et.addEndpointForScenario(scenario, true);
						CharSequence result = "// Interface refactoring 'AddEndpointForScenario' applied.\n";
						RefactoringHelpers.generateRefactoringOutput(mdslSpecification, fsa, inputFileURI, story, result);
						return;
					}
				}
			}
		}
		MDSLLogger.reportWarning("Scenario " + scenarioName + " with story " + storyName + " not found");
	}
}

package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.transformations.ScenarioTransformations;

public class AddOperationForScenarioStory extends QuickfixSemanticModification {

	AddOperationForScenarioStory() {
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		IntegrationStory story = (IntegrationStory) element;
		ScenarioTransformations et = new ScenarioTransformations();
		et.addOperationForScenarioStory(story);
	}
}

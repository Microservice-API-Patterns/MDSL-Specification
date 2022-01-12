package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.DomainEventProductionStep;
import io.mdsl.transformations.FlowTransformations;
import io.mdsl.transformations.TransformationHelpers;

class ConsolidateFlowSteps extends QuickfixSemanticModification {
	private String branchingType;

	public ConsolidateFlowSteps(String branchingType) {
		this.branchingType = branchingType;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		FlowTransformations ft = new FlowTransformations();
		if (element instanceof DomainEventProductionStep) {
			ft.consolidateFlowSteps((DomainEventProductionStep) element, branchingType);
		} else {
			TransformationHelpers.reportError("This transformation operates on Single Event Productions only.");
		}
	}
}
package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.transformations.FlowTransformations;

class AddBranchWithMerge extends QuickfixSemanticModification {
	private String branchingType;

	public AddBranchWithMerge(String branchingType) {
		this.branchingType = branchingType;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		FlowTransformations ft = new FlowTransformations();
		ft.addBranchesWithMerge(element, branchingType);
	}
}
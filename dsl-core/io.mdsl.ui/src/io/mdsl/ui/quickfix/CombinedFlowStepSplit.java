package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.CombinedInvocationStep;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.FlowTransformations;

class CombinedFlowStepSplit implements ISemanticModification {
	public CombinedFlowStepSplit() {
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		FlowTransformations ft = new FlowTransformations();
		if(element instanceof CombinedInvocationStep) {
			ft.splitCombinedFlowStep((CombinedInvocationStep) element);
		}
		else {
			throw new MDSLException("This transformation operates on Combined Cis/Dep steps only.");
		}
	}
}
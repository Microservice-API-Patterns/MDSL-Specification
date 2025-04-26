package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.RoleTypeAndDesc;
import io.mdsl.transformations.DataTypeTransformations;

class CompleteDataType extends QuickfixSemanticModification {
	private String type;

	public CompleteDataType(String type) {
		this.type = type;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		RoleTypeAndDesc rat = (RoleTypeAndDesc) element;
		DataTypeTransformations.completeDataType(rat, type);
	}
}
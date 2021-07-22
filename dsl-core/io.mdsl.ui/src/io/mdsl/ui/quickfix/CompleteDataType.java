package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.RoleAndType;

class CompleteDataType implements ISemanticModification {
	private String type;

	public CompleteDataType(String string) {
		type = string;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		RoleAndType rat = (RoleAndType) element;
		rat.setBtype(type);
	}
}
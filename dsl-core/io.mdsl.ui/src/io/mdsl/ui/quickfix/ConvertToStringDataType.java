package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.transformations.DataTypeTransformations;

class ConvertToStringDataType extends QuickfixSemanticModification {
	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		GenericParameter gp = (GenericParameter) element;
		DataTypeTransformations.convertToStringType(gp);
	}
}
package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

// import io.mdsl.transformations.TransformationHelper;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.transformations.MAPDecoratorTransformationHelpers;
public class TuneAsMAPProcessingResource implements ISemanticModification  {

	public void apply(EObject element, IModificationContext context) throws Exception {
		EndpointContract etype = (EndpointContract) element;
		MAPDecoratorTransformationHelpers.setRoleToProcessingResource(etype);
	}
}

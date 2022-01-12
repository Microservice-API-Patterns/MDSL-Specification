package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.transformations.EndpointTransformations;
import io.mdsl.transformations.TransformationHelpers;

public class SegregateCommandsFromQueries extends QuickfixSemanticModification {
	
	private boolean generateOperations;
	
	SegregateCommandsFromQueries() {
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if (!(element instanceof EndpointContract)) { 
			TransformationHelpers.reportError("SegregateCommandsFromQueries expects an EndpointContract as input");
		}
		
		EndpointTransformations ets = new EndpointTransformations();
		ets.separateCommandsFromQueries((EndpointContract)element);
	}	
}

package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.impl.ElementStructureImpl;
import io.mdsl.apiDescription.impl.TreeNodeImpl;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.transformations.MessageTransformations;

class IntroduceKeyValueMap extends QuickfixSemanticModification {
	@Override
	public void performQuickfix(EObject element, IModificationContext context) {	
		AtomicParameter ap = (AtomicParameter) element;
		// TODO the same for TR, PT
		
		MessageTransformations.addKeyValueMapWrapper(ap);
	}
}
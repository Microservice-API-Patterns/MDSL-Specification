package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.impl.AtomicParameterImpl;
import io.mdsl.apiDescription.impl.AtomicParameterListImpl;
import io.mdsl.apiDescription.impl.ElementStructureImpl;
import io.mdsl.apiDescription.impl.TreeNodeImpl;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

class IntroduceParameterTreeDTO implements ISemanticModification {
	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {	
		TreeNode tn = null;
		EObject containingElement = null;
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		
		if(element.getClass() == AtomicParameterListImpl.class) {
			AtomicParameterList apl = (AtomicParameterList) element;	
			pt.setClassifier(EcoreUtil.copy(apl.getClassifier()));
			pt.setName(apl.getName());
			tn = DataTypeTransformationHelpers.turnAtomicParameterIntoTreeNode(apl.getFirst());
			pt.setFirst(tn);
			for(AtomicParameter nextAp : apl.getNextap()) {
				tn = DataTypeTransformationHelpers.turnAtomicParameterIntoTreeNode(nextAp);
				pt.getNexttn().add(tn);
			}
			pt.setCard(EcoreUtil.copy(apl.getCard()));
			
			// TODO TN is not the only place where APL can appear, ElementStructure, ParameterTreeList (in PF)? 
			containingElement = apl.eContainer();
		}
		else if(element.getClass() == AtomicParameterImpl.class) {
			AtomicParameter ap = (AtomicParameter) element;
			tn = DataTypeTransformationHelpers.turnAtomicParameterIntoTreeNode(ap);
			pt.setClassifier(EcoreUtil.copy(ap.getClassifier()));
			
			pt.setName(ap.getRat().getName() + "Wrapper"); // still needed (now)
		
			pt.setFirst(tn);
			pt.setCard(EcoreUtil.copy(ap.getCard()));
			containingElement = ap.eContainer().eContainer(); // SPN -> TN or ES 
		}
		// TODO (M) could add PT in PT here
		else {
			System.err.println("This Quick Fix can only be applied to Atomic Parameter Lists and Atomic Parameters." + element.getClass().toString());
			throw new MDSLException("This Quick Fix can only be applied to Atomic Parameter Lists and Atomic Parameters.");
		}
		
		// TODO move to shared helper (add KVP has the same code):
		wrapParameterTreeInTreeNodeOrElementStructure(containingElement, pt);
	}

	private void wrapParameterTreeInTreeNodeOrElementStructure(EObject containingElement, ParameterTree pt) {
		TreeNode tn;
		if(containingElement.getClass().equals(TreeNodeImpl.class)) {
			tn = (TreeNode) containingElement;
			tn.setApl(null);
			tn.setPn(null);
			tn.setChildren(pt);
		}
		else if(containingElement.getClass().equals(ElementStructureImpl.class)) {
			ElementStructure es = (ElementStructure) containingElement;
			es.setApl(null);
			es.setNp(null);
			es.setPt(pt);
			es.setPf(null); // not needed
		}
		else {		
			System.err.println("Expected Tree Node or Element Structure." + containingElement.getClass().toString());
			throw new MDSLException("Expected Tree Node or Element Structure.");
		}
	}
}
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
import io.mdsl.transformations.DataTypeTransformationHelpers;

class IntroduceKeyValueMap implements ISemanticModification {
	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {	
		AtomicParameter ap = (AtomicParameter) element;
		// TODO the same for TR, PT
		
		// wrap it similar to what IntroduceDTO does 
		// "id":MD<raw> -> {"idMapKey":ID<string>, "idMapValue":MD<raw>}* 
		// could also do multi-map: {"idMapKey":ID<string>, "idMapValue":MD<raw>*}* 
		
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		AtomicParameter key = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		RoleAndType rat = DataTypeTransformationHelpers.createRoleAndType("key", "ID", "string");
		key.setRat(rat);
		
		TreeNode tn = DataTypeTransformationHelpers.turnAtomicParameterIntoTreeNode(key);
		pt.setName(ap.getRat().getName() + "KeyValueMap"); // TODO assign name if null
		pt.setFirst(tn);
		
		TreeNode tn2 = DataTypeTransformationHelpers.turnAtomicParameterIntoTreeNode(ap);
		pt.setClassifier(EcoreUtil.copy(ap.getClassifier()));
		pt.setCard(EcoreUtil.copy(ap.getCard()));
		pt.getNexttn().add(tn2);
		
		EObject containingElement = ap.eContainer().eContainer(); // SPN -> TN or ES 
		wrapParameterTree(containingElement, pt);
	}

	// ** utilities
	
	// also in IntroDTO, could go to shared helper
	private void wrapParameterTree(EObject containingElement, ParameterTree pt) {
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
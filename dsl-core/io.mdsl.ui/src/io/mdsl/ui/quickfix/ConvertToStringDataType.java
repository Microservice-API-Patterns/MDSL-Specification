package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.impl.SingleParameterNodeImpl;

class ConvertToStringDataType implements ISemanticModification {
	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		GenericParameter gp = (GenericParameter) element;
		String gpn = gp.getName();
		if(gpn==null || gpn.equals("")) {
			gpn="anonymous";
		}

		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(gpn);
		newRaT.setRole("D");
		newRaT.setBtype("string");
		AtomicParameter newAP = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		newAP.setRat(newRaT);
		
		// note that casting exceptions are not shown on console (but error log in RT has an entry):
		SingleParameterNode spn = (SingleParameterNodeImpl) element.eContainer();
		spn.setGenP(null); // needed due to '|' in grammar
		spn.setAtomP(newAP);
	}
}
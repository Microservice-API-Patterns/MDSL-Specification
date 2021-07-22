package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.SecurityPolicies;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.transformations.DataTypeTransformationHelpers;

public class CompleteOperationWithSecurityPolicy implements ISemanticModification  {

	public void apply(EObject element, IModificationContext context) throws Exception {
		// this is a very basic transformation, merely demonstrating the policy syntax 
		Operation operation = (Operation) element;
		SecurityPolicies sps = ApiDescriptionFactory.eINSTANCE.createSecurityPolicies();
		SecurityPolicy sp = ApiDescriptionFactory.eINSTANCE.createSecurityPolicy();
		sp.setName("accessControlRule");
		AtomicParameter ap = DataTypeTransformationHelpers.createAtomicDataParameter("policyData", "string");
		ElementStructure es = DataTypeTransformationHelpers.wrapAtomicParameterAsElementStructure(ap);
		sp.setSecurityObject(es);
		sps.getPolicyList().add(sp);
		operation.setPolicies(sps);
	}
}

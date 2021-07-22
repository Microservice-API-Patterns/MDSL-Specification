package io.mdsl.ui.quickfix;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.impl.HTTPResourceBindingImpl;
import io.mdsl.apiDescription.HTTPVerb;

import io.mdsl.transformations.TransformationHelpers;

class AddHttpResourceBinding implements ISemanticModification {
	private String resourceName; // TODO get name from UI or have a global counter
	private static int resourceCounter = 1;

	public AddHttpResourceBinding(String resourceName) {
		this.resourceName = resourceName;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		
		if(element.getClass()!=HTTPResourceBindingImpl.class) {
			TransformationHelpers.reportError("AddHttpResourceBinding expects an HTTP Resource Binding.");
			return;
		}
		
		HTTPResourceBinding hrb = (HTTPResourceBinding) element;
		HTTPBinding hp = (HTTPBinding) hrb.eContainer();
		
		HTTPResourceBinding newHrb = ApiDescriptionFactory.eINSTANCE.createHTTPResourceBinding();
		newHrb.setName(resourceName + resourceCounter);
		newHrb.setUri("/" + resourceName + resourceCounter);
		resourceCounter++;
				
		// find index of operations that map to verbs that already are in use and move them to new resource (might again have to be split)
		HashMap<HTTPVerb, Boolean> verbUsage = new HashMap<HTTPVerb, Boolean>();
		ArrayList<HTTPOperationBinding> hobsToBeMoved = new ArrayList<HTTPOperationBinding>();

		for(HTTPOperationBinding hob : hrb.getOpsB()) {
			HTTPVerb verb = hob.getMethod();
			if(verbUsage.get(verb)!=null && verbUsage.get(verb).booleanValue()==true) {
				hobsToBeMoved.add(hob);
			}
			else {
				verbUsage.put(verb,true);
			}
		}
		
		for(HTTPOperationBinding hob : hobsToBeMoved) {
			hrb.getOpsB().remove(hob);
			newHrb.getOpsB().add(hob);
		}
		
		// TODO could check that no resource of same name exists already (but also ok to have temporary name clash) 
		hp.getEb().add(newHrb);
	}	
}
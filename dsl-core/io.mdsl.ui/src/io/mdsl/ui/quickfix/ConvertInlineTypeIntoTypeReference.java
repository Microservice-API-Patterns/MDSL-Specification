package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

class ConvertInlineTypeIntoTypeReference implements ISemanticModification {
	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {	
		DataTransferRepresentation dtr = (DataTransferRepresentation) element; // or DTR?
		 
		// navigate to service specification: dtr->op-ec->ss
		ServiceSpecification ss = (ServiceSpecification) dtr.eContainer().eContainer().eContainer();
		
		// offer UI to select name 
		String typeName = "AnonymousType";
		Shell shell = Display.getCurrent().getActiveShell();
		NamePromptDialog dialog = new NamePromptDialog(shell, "Type name", "Please enter a data type name that is not used yet.", false);
		dialog.create();
		if (dialog.open() == Window.OK) {
			if(dialog.getFirstName()==null || dialog.getFirstName().equals(""))
				typeName = getParameterName(dtr.getPayload());
			else
				typeName = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(dialog.getFirstName());
		 } else {
			// user aborted
			System.err.println("No name for data type received from user interface dialog.");
		}
		
		// move es to new data type, add new data type to ss
		DataContract dt = ApiDescriptionFactory.eINSTANCE.createDataContract();
		dt.setName(DataTypeTransformationHelpers.capitalizeName(typeName));
		dt.setStructure(EcoreUtil.copy(dtr.getPayload())); // copy might not be needed
		
		// check all data types in ss that suggested data type name is not taken
		boolean typeCouldBeAdded = addIfNotPresent(ss, dt);
		
		if(typeCouldBeAdded) {
			// create type reference to replace es in DTR that came in
			TypeReference tr = ApiDescriptionFactory.eINSTANCE.createTypeReference();
			tr.setName(typeName);
			tr.setDcref(dt);
			SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
			spn.setTr(tr);
			ElementStructure es2 = ApiDescriptionFactory.eINSTANCE.createElementStructure();
			es2.setNp(spn);
			dtr.setPayload(es2);
			// TODO improve formatter to format TR: delivering payload "abc": abc
		}
		else 
			TransformationHelpers.reportError("A data type with the name " + typeName +  " already exists. Cannot perform the refactoring.");
	}

	private boolean addIfNotPresent(ServiceSpecification ss, DataContract dt) {
		for(DataContract type: ss.getTypes()) {
			if(type.getName().equals(dt.getName())) 
				return false;
		}
		// not found, so can be added:
		ss.getTypes().add(dt);
		return true;
	}

	private String getParameterName(ElementStructure es) {
		// similar helper exists in generator utilities		
		if(es.getNp()!=null) 
			// TODO (M) APL, TR not covered yet
			if(es.getNp().getAtomP()!=null) {
				if(es.getNp().getAtomP().getRat().getName()!=null)
					return "AnonymousTR"; 
				else 
					return es.getNp().getAtomP().getRat().getName();
			}
			else if(es.getNp().getGenP()!=null) {
				es.getNp().getGenP().getName(); // TODO not tested yet
			}
			else if(es.getNp().getTr()!=null) {
				es.getNp().getTr().getName(); // TODO not tested yet
			}
			else 
				TransformationHelpers.reportError("Can't find name of this parameter type.");
		else if(es.getPt()!=null)
			return es.getPt().getName();
		else {
			// could be PF or APL
			TransformationHelpers.reportError("Unsupported type of element structure.");
		}
		return "AnonymousTR";
	}
}
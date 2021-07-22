package io.mdsl.transformations;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;

public class DataTypeTransformationHelpers {
	public static EventType addEventTypeIfNotPresent(ServiceSpecification ss, String suggestedName) {
	
		for(EventType exEv : EcoreUtil2.eAllOfType(ss, EventType.class)) {
			if(exEv.getName().equals(suggestedName))
				TransformationHelpers.reportError(suggestedName + " already exists as a domain event. Please rename the triggering flow.");
		}
		
		EventType de = ApiDescriptionFactory.eINSTANCE.createEventType();
		de.setName(suggestedName);
		AtomicParameter flag = DataTypeTransformationHelpers.createMetadataParameter("eventDetails", "string");
		// could also add other event data
		de.setContent(DataTypeTransformationHelpers.wrapAtomicParameterAsElementStructure(flag));
		
		return de;
	}

	public static ElementStructure wrapGenericParameterNodeAsElementStructure(GenericParameter genP) {
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setGenP(genP);
		ElementStructure es =  ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setNp(spn);
		return es;
	}

	public static ElementStructure wrapAtomicParameterAsElementStructure(AtomicParameter ap) {
		// TODO refactor?
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setAtomP(ap);
		ElementStructure es =  ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setNp(spn);
		return es;
	}

	public static TreeNode wrapAtomicParameterAsTreeNode(AtomicParameter ap) {
		TreeNode result = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setAtomP(ap);
		result.setPn(spn);
		return result;
	}

	public static GenericParameter createGenericParameter(String name) {
		GenericParameter genP = ApiDescriptionFactory.eINSTANCE.createGenericParameter();
		genP.setName(name);
		return genP;
	}

	public static AtomicParameter createAtomicDataParameter(String name, String dataType) {
		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(name);
		newRaT.setRole("D");
		if(dataType!=null) {
			if(!isValid(dataType)) 
				TransformationHelpers.reportError("Can't use " + dataType + " as type of " + name + ": not a valid MDSL base type.");
			else 
				newRaT.setBtype(dataType);
		}
		AtomicParameter ap = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		ap.setRat(newRaT);
		return ap;
	}

	public static AtomicParameter createMetadataParameter(String name, String dataType) {
		// TODO see createDataParameter
		if(!isValid(dataType)) {
			TransformationHelpers.reportError("Can't use " + dataType + " as type of " + name + ": not a valid MDSL base type.");
		}
		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(name);
		newRaT.setRole("MD");
		newRaT.setBtype(dataType); 
		AtomicParameter ap = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		ap.setRat(newRaT);
		return ap;
	}

	public static AtomicParameter createLinkParameter(String name) {
		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(name);
		newRaT.setRole("L");
		newRaT.setBtype("string"); // we do not have string ranges yet ([O] Spring SPeL?)
		AtomicParameter ap = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		ap.setRat(newRaT);
		return ap;
	}

	public static TreeNode turnAtomicParameterIntoTreeNode(AtomicParameter ap) {
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setAtomP(EcoreUtil.copy(ap));
		TreeNode tn = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		tn.setPn(spn);
		return tn;
	}

	public static boolean isValid(String dataType) {
		if(dataType==null)
			return false;
		if(dataType.equals("int"))
			return true;
		if(dataType.equals("string"))
			return true;
		if(dataType.equals("bool"))
			return true;
		if(dataType.equals("raw"))
			return true;
		if(dataType.equals("long"))
			return true;
		if(dataType.equals("double"))
			return true;
		if(dataType.equals("void"))
			return true;
		return false;
	}
	
	public static void setCardinality(ElementStructure pl, Cardinality card) {
		if(pl.getPt()!=null) {
			pl.getPt().setCard(card);
		}
		if(pl.getApl()!=null) {
			pl.getApl().setCard(card);
		}
		if(pl.getNp()!=null) {
			SingleParameterNode spn = pl.getNp();
			if(spn.getAtomP()!=null) {
				spn.getAtomP().setCard(card);
			}
			if(spn.getTr()!=null) {
				spn.getTr().setCard(card);
			}
			if(spn.getGenP()!=null)
				; // no action required
		}
		// does PF have a cardinality? all cases caught?
	}

	public static RoleAndType createRoleAndType(String name, String role, String type) {
		RoleAndType rat = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		rat.setName(name);
		rat.setRole(role);
		rat.setBtype(type);
		return rat;
	}

	// ** misc helpers
	
	public static String replaceSpacesWithUnderscores(String name) {
		return name.replace(" ", "_");
	}
	
	public static String decapitalizeName(String name) {
		// not the most convenient way to decapitalize the start of the name string:
		String c1 = name.substring(0, 1).toLowerCase();
		c1 += name.substring(1,name.length());
		return c1;
	}	

	public static String capitalizeName(String name) {
		String c1 = name.substring(0, 1).toUpperCase();
		c1 += name.substring(1,name.length());
		return c1;
	}
}

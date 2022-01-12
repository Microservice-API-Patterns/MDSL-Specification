package io.mdsl.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.ParameterForest;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TypeReference;


/**
 * This class contains custom validation rules.
 *
 * See
 * https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class DataTypeValidator extends AbstractMDSLValidator {
	
	public final static String LOWER_CASE_NAME = "LOWER_CASE_NAME";
	public final static String TYPE_MISSING = "TYPE_MISSING";
	public final static String TYPE_STUCTURED = "TYPE_STUCTURED";
	public final static String APL_FOUND = "APL_FOUND";
	public final static String AP_FOUND = "AP_FOUND";
	public final static String TR_FOUND = "TR_FOUND";
	public final static String INLINED_TYPE_FOUND = "INLINED_TYPE_FOUND";
	public final static String TYPE_INCOMPLETE = "TYPE_INCOMPLETE";
	public final static String DECORATION_MISSING = "DECORATION_MISSING";
	
	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}

	@Check
	public void checkRoleAndType(final RoleAndType rat) {
		String role = rat.getRole();
		String basicType = rat.getBtype();
		
		if(basicType==null || basicType.equals("")) {
			info("Incomplete specification: No type such as <string> or <int> specified yet.", rat, ApiDescriptionPackage.eINSTANCE.getRoleAndType_Btype(), TYPE_INCOMPLETE); // ()Literals.ROLE_AND_TYPE__BTYPE);
	    }
		else if(basicType.equals("void")) {
			info("Imprecise specification: <void> only makes sense as sole element of a response message; you might want to remove the empty message from the specification.", rat, ApiDescriptionPackage.eINSTANCE.getRoleAndType_Btype()); // Literals.ROLE_AND_TYPE__BTYPE);
	    }
		else if(role.equals("ID") && !(basicType.equals("int")|| basicType.equals("string"))) {
			warning("The role-type combination ID<" + basicType + "> is somewhat unusual. Use ID<string> or ID<int> instead?", rat, ApiDescriptionPackage.eINSTANCE.getRoleAndType_Btype()); // Literals.ROLE_AND_TYPE__BTYPE);
	    }
		else if(role.equals("L") && !(basicType.equals("int") || basicType.equals("string"))) {
			warning("The role-type combination L<" + basicType + "> is somewhat unusual. Use L<string> or L<int> instead?", rat, ApiDescriptionPackage.eINSTANCE.getRoleAndType_Btype()); // Literals.ROLE_AND_TYPE__BTYPE);
	    }
		else if(role.equals("MD") && basicType.equals("raw")) {
			warning("The role-type combination MD<raw> is somewhat unusual. Use MD<string> instead?", rat, ApiDescriptionPackage.eINSTANCE.getRoleAndType_Btype()); // Literals.ROLE_AND_TYPE__BTYPE);
	    }
	}
	
	@Check
	public void reportAtomicParameterList(final AtomicParameterList apl) {
		if(apl.getName() != null) {
			info("\"" + apl.getName() + "\" is an atomic parameter list. You might want to introduce a DTO parameter tree instead.", apl, ApiDescriptionPackage.eINSTANCE.getAtomicParameterList_Name(), APL_FOUND);
		}
		else {
			warning("This is an atomic parameter list. You might want to introduce a DTO parameter tree instead.", apl, ApiDescriptionPackage.eINSTANCE.getAtomicParameterList_First(), APL_FOUND);
		}
	}
	
	@Check
	public void reportAtomicParameter(final AtomicParameter ap) {
		// RaT can not be be null according to grammar
		if(ap.getRat().getName() != null) {
			info("\"" + ap.getRat().getName() + "\" is an atomic parameter. Do you want to wrap it?", ap, ApiDescriptionPackage.eINSTANCE.getAtomicParameter_Rat(), AP_FOUND);
		}
		else {
			info("This is an atomic parameter. Do you want to wrap it?", ap, ApiDescriptionPackage.eINSTANCE.getAtomicParameter_Rat(), AP_FOUND);
		}
	}
	
	@Check
	public void reportTypeReference(final TypeReference tr) {
		if(tr.getName() != null) {
			info("\"" + tr.getName() + "\" is a data type reference. Do you want to wrap it in a DTO parameter tree?", tr, ApiDescriptionPackage.eINSTANCE.getTypeReference_Dcref(), TR_FOUND);
		}
		else {
			info("This is a data type reference. Do you want to wrap it in a DTO parameter tree?", tr, ApiDescriptionPackage.eINSTANCE.getTypeReference_Dcref(), TR_FOUND);
		}
	}

	@Check
	public void checkIncompleteTypeInformation(final GenericParameter gp) {
		if(gp.getName() != null) {
			warning("\"" + gp.getName() + "\" is a generic parameter. You might want to provide a full identfier-role-type triple before invoking any generator.", gp, ApiDescriptionPackage.eINSTANCE.getGenericParameter_P(), TYPE_MISSING); // Literals.GENERIC_PARAMETER__P);
		}
		else {
			warning("This is a generic parameter. You might want to provide a full identfier-role-type triple. See MDSL documentation at https://microservice-api-patterns.github.io/MDSL-Specification/datacontract.", gp, ApiDescriptionPackage.eINSTANCE.getGenericParameter_P(), TYPE_MISSING); // Literals.GENERIC_PARAMETER__P);
		}
	}
	
	@Check
	public void checkInappropriateTypeName(final DataContract dc) {
        if (!Character.isUpperCase(dc.getName().charAt(0))) {
            warning("Data type name should start with a capital", dc, ApiDescriptionPackage.eINSTANCE.getDataContract_Name(), LOWER_CASE_NAME);
        } 
	}
	
	@Check
	public void reportTypeOfPayload(final DataTransferRepresentation dtr) {
		// TODO do the same for headers (which are not supported in MDSL tools yet)
		if(dtr.getPayload()!=null) {
			ElementStructure es = dtr.getPayload();
			if(es.getNp()!=null) {
				SingleParameterNode spn = es.getNp();
				if(spn.getAtomP()!=null||spn.getGenP()!=null||spn.getTr()!=null) {
					info("Inlined/embedded data type definition", dtr, ApiDescriptionPackage.eINSTANCE.getDataTransferRepresentation_Payload(), INLINED_TYPE_FOUND);
				}
				// enP and APL should/must be completed first and then externalized
			}
			else if(es.getPt()!=null) {
				ParameterTree pt = es.getPt();
				info("Inlined/embedded data type definition", dtr, ApiDescriptionPackage.eINSTANCE.getDataTransferRepresentation_Payload(), INLINED_TYPE_FOUND);
			}
		}
	}

	@Check
	public void checkParameterForestAsTuple(final ParameterForest pf) {
		List<ParameterTree> trees = new ArrayList<ParameterTree>();
		trees.add(pf.getPtl().getFirst());
		trees.addAll(pf.getPtl().getNext());

		for (ParameterTree pt : trees) {
			if (pt.getName() != null) {
				// parameterForest is considered a tuple (of PTs)
				info("ParameterForest are like tuples. Tuple item names are ignored.",
						pt, ApiDescriptionPackage.eINSTANCE.getParameterTree_Name()); //  Literals.PARAMETER_TREE__NAME);
			}
		}
	}
}
package io.mdsl.transformations;

import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.utils.MDSLLogger;

public class DataTypeTransformations {

	public static void setCardinality(ElementStructure pl, Cardinality card) {
		if (pl.getPt() != null) {
			pl.getPt().setCard(card);
		} else if (pl.getApl() != null) {
			pl.getApl().setCard(card);
		} else if (pl.getNp() != null) {
			SingleParameterNode spn = pl.getNp();
			if (spn.getAtomP() != null) {
				spn.getAtomP().setCard(card);
			}
			if (spn.getTr() != null) {
				spn.getTr().setCard(card);
			}
			if (spn.getGenP() != null) {
				// no action required
			}
		} else {
			// PF does not have a cardinality
			MDSLLogger.reportWarning("Cannot set cardinality of " + pl.getClass());
		}
	}

	public static DataTransferRepresentation wrapElementStructureInDataTransferRepresentation(ElementStructure es) {
		DataTransferRepresentation result = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		result.setPayload(es);
		return result;
	}

	public static ElementStructure wrapParameterTreeAsElementStructure(ParameterTree pt) {
		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setPt(pt);
		return es;
	}

	public static ElementStructure wrapGenericParameterNodeAsElementStructure(GenericParameter genP) {
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setGenP(genP);
		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setNp(spn);
		return es;
	}

	public static ElementStructure wrapTypeReferenceAsElementStructure(TypeReference tref) {
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setTr(tref);
		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setNp(spn);
		return es;
	}

	public static TreeNode wrapTypeReferenceAsTreeNode(TypeReference tref) {
		TreeNode result = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setTr(tref);
		result.setPn(spn);
		return result;
	}

	public static ElementStructure wrapAtomicParameterAsElementStructure(AtomicParameter ap) {
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setAtomP(ap);
		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
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
		return createAtomicDataParameter(name, DataTypeTransformationHelpers.DATA_ROLE, dataType);
	}

	public static AtomicParameter createAtomicDataParameter(String name, String role, String dataType) {
		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(name);
		newRaT.setRole(role); 
		if(!DataTypeTransformationHelpers.isValidTypeRole(role)) {
			MDSLLogger.reportError("Can't use " + dataType + " as type of " + name + ": not a valid base type role.");
		}
		else {
			newRaT.setRole(role);
		}
		
		if(!DataTypeTransformationHelpers.isValidBaseType(dataType)) {
			MDSLLogger.reportWarning("Can't use " + dataType + " as type of " + name + ": not a valid MDSL base type.");
		}
		else { 
			newRaT.setBtype(dataType);
		}
		
		AtomicParameter ap = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		ap.setRat(newRaT);
		return ap;
	}

	// TODO extract more local helpers and move them to helper class

	public static AtomicParameter createMetadataParameter(String name, String dataType) {
		if (!DataTypeTransformationHelpers.isValidBaseType(dataType)) {
			MDSLLogger.reportError("Can't use " + dataType + " as type of " + name + ": not a valid MDSL base type.");
		}
		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(name);
		newRaT.setRole(DataTypeTransformationHelpers.METADATA_ROLE);
		newRaT.setBtype(dataType);
		AtomicParameter ap = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		ap.setRat(newRaT);
		return ap;
	}

	public static AtomicParameter createIDParameter(String name) {
		AtomicParameter ap = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		RoleAndType rat = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		if (name != null) {
			rat.setName(name);
		}
		rat.setRole(DataTypeTransformationHelpers.ID_ROLE);
		rat.setBtype(DataTypeTransformationHelpers.INT);
		ap.setRat(rat);
		return ap;
	}

	public static AtomicParameter createLinkParameter(String name) {
		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(name);
		newRaT.setRole(DataTypeTransformationHelpers.LINK_ROLE);
		newRaT.setBtype(DataTypeTransformationHelpers.STRING); // no string ranges yet ([O] Spring SPeL?)
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

	public static TreeNode turnTypeReferenceIntoTreeNode(TypeReference tr) {
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setTr(EcoreUtil.copy(tr));
		TreeNode tn = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		tn.setPn(spn);
		return tn;
	}

	public static RoleAndType createRoleAndType(String name, String role, String type) {
		RoleAndType rat = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		rat.setName(name);
		rat.setRole(role);
		rat.setBtype(type);
		return rat;
	}

	public static TypeReference createTypeReference(DataContract dt) {
		TypeReference tr = ApiDescriptionFactory.eINSTANCE.createTypeReference();
		tr.setDcref(dt);
		return tr;
	}

	public static DataContract findOrCreateDataType(ServiceSpecification ss, String typeName, ElementStructure es) {
		// TODO search first, then add suffix? helper used in many places
		for (DataContract dc : ss.getTypes()) {
			if (dc.getName().equals(typeName + DataTypeTransformationHelpers.DTO_SUFFIX)) {
				return dc;
			}
		}
		DataContract dc = ApiDescriptionFactory.eINSTANCE.createDataContract();
		dc.setName(typeName + DataTypeTransformationHelpers.DTO_SUFFIX);
		dc.setStructure(es);
		ss.getTypes().add(dc);
		return dc;
	}

	public static DataContract findOrCreateDataTypeByIdentifier(ServiceSpecification ss, String typeIdentifier, ElementStructure es) {
		for (DataContract dc : ss.getTypes()) {
			if (dc.getStructure() != null && TransformationHelpers.nameOf(dc.getStructure()) != null && TransformationHelpers.nameOf(dc.getStructure()).equals(typeIdentifier)) {
				return dc;
			}
		}
		DataContract dc = ApiDescriptionFactory.eINSTANCE.createDataContract();
		dc.setName(DataTypeTransformationHelpers.capitalizeName(typeIdentifier) + DataTypeTransformationHelpers.DTO_SUFFIX);
		dc.setStructure(es);
		ss.getTypes().add(dc);
		return dc;
	}

	public static DataTransferRepresentation createDTRFromTypeReference(TypeReference typeRef) {
		DataTransferRepresentation dtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setTr(typeRef);

		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setNp(spn);
		dtr.setPayload(es);

		return dtr;
	}

	public static void convertToStringType(GenericParameter gp) {
		String gpn = gp.getName();
		if (gpn == null || gpn.isEmpty()) {
			gpn = DataTypeTransformationHelpers.ANONYMOUS;
		}

		RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
		newRaT.setName(gpn);
		newRaT.setRole(DataTypeTransformationHelpers.DATA_ROLE);
		newRaT.setBtype(DataTypeTransformationHelpers.STRING);
		AtomicParameter newAP = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		newAP.setRat(newRaT);

		// casting exceptions are not shown on console (but error log in runtime has an entry):
		SingleParameterNode spn = (SingleParameterNode) gp.eContainer();
		spn.setGenP(null); // needed due to '|' in grammar
		spn.setAtomP(newAP);
	}

	public static void completeDataType(RoleAndType rat, String type) {
		rat.setBtype(type);
	}

	public static void convertInlineTypeToTypeReference(DataTransferRepresentation dtr, String typeName) {
		// navigate to service specification: dtr->op-ec->ss
		ServiceSpecification ss = (ServiceSpecification) dtr.eContainer().eContainer().eContainer();
		
		// move es to new data type, add new data type to ss
		DataContract dt = ApiDescriptionFactory.eINSTANCE.createDataContract();
		dt.setName(DataTypeTransformationHelpers.capitalizeName(typeName));
		dt.setStructure(EcoreUtil.copy(dtr.getPayload())); // copy might not be needed

		// check all data types in ss that suggested data type name is not taken
		boolean typeCouldBeAdded = DataTypeTransformationHelpers.addIfNotPresent(ss, dt);

		if (typeCouldBeAdded) {
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
		} else {
			MDSLLogger.reportError("A data type with the name " + typeName + " already exists. Cannot perform the refactoring.");
		}
	}

	public static String getParameterName(ElementStructure es) {
		// similar helper exists in generator utilities
		if (es.getNp() != null) {
			if (es.getNp().getAtomP() != null) {
				// main use case for this QF
				if (es.getNp().getAtomP().getRat().getName() != null) {
					return DataTypeTransformationHelpers.ANONYMOUS_TYPE;
				} else {
					return es.getNp().getAtomP().getRat().getName();
				}
			} else if (es.getNp().getGenP() != null) {
				es.getNp().getGenP().getName();
			} else if (es.getNp().getTr() != null) {
				es.getNp().getTr().getName();
			} else {
				MDSLLogger.reportError("Can't find name of this parameter type.");
			}
		} else if (es.getPt() != null) {
			return es.getPt().getName();
		} else {
			// must be Parameter Forest or Atomic Parameter List
			MDSLLogger.reportError("Unsupported type of element structure.");
		}
		return DataTypeTransformationHelpers.ANONYMOUS_TYPE;
	}
}

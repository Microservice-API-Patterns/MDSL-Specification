package io.mdsl.transformations;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.exception.MDSLException;

public class MAPDecoratorHelpers {
	public static final String PROCESSING_RESOURCE = "PROCESSING_RESOURCE";
	public static final String INFORMATION_HOLDER_RESOURCE = "INFORMATION_HOLDER_RESOURCE";
	public static final String COLLECTION_RESOURCE = "COLLECTION_RESOURCE";
	public static final String MUTABLE_COLLECTION_RESOURCE = "MUTABLE_COLLECTION_RESOURCE";

	public static final String STATE_CREATION_OPERATION = "STATE_CREATION_OPERATION";
	public static final String EVENT_PROCESSOR = "EVENT_PROCESSOR";
	public static final String RETRIEVAL_OPERATION = "RETRIEVAL_OPERATION";
	public static final String STATE_TRANSITION_OPERATION = "STATE_TRANSITION_OPERATION";
	public static final String BUSINESS_ACTIVITY_PROCESSOR = "BUSINESS_ACTIVITY_PROCESSOR";
	public static final String STATE_REPLACEMENT_OPERATION = "STATE_REPLACEMENT_OPERATION";
	public static final String STATE_DELETION_OPERATION = "STATE_DELETION_OPERATION";
	public static final String COMPUTATION_FUNCTION = "COMPUTATION_FUNCTION";

	private static final String UNSPECIFIED_RESPONSIBILITY = "UnspecifiedResponsibility";
	private static final String COLLECTION_OPERATION = "Collection_Operation";

	// ** endpoint role related

	public static void setRoleToProcessingResource(EndpointContract etype) {
		etype.setPrimaryRole(PROCESSING_RESOURCE);
	}

	public static void setRoleToInformationHolderResource(EndpointContract etype) {
		etype.setPrimaryRole(INFORMATION_HOLDER_RESOURCE);
	}

	public static void setRoleToCollectionResource(EndpointContract endpoint) {
		setRole(endpoint, COLLECTION_RESOURCE);
	}

	public static void addRole(EndpointContract ec, String type) {
		if (ec.getPrimaryRole() == null) {
			ec.setPrimaryRole(type);
		} else {
			// only add if not already present (primary or other roles)
			EList<String> secondaryRoles = ec.getOtherRoles();
			if (!secondaryRoles.contains(type) && !ec.getPrimaryRole().equals(type)) {
				ec.getOtherRoles().add(type);
				// TODO "other roles" feature is not used in generators, not documented much
			}
		}
	}

	public static void setRole(EndpointContract etype, String role) {
		// TODO could also add a validator/QF that suggests to refine IHR a) into MDH etc. and b) CR rather than set it anew

		// check whether primary role is already set
		// TOOD (L) set secondary in that case?
		if (etype.getPrimaryRole() != null && !etype.getPrimaryRole().isEmpty()) {
			System.err.println("[W] A primary endpoint role is already defined and will be overwritten.");
		}

		if (role != null && role.equals(COLLECTION_RESOURCE)) {
			etype.setPrimaryRole(INFORMATION_HOLDER_RESOURCE);
			etype.getOtherRoles().add(role);
		} else if (role != null && role.equals(MUTABLE_COLLECTION_RESOURCE)) {
			etype.setPrimaryRole(INFORMATION_HOLDER_RESOURCE);
			etype.getOtherRoles().add('"' + role + '"'); // TODO update grammar
		} else {
			etype.setPrimaryRole(role);
		}
	}

	// ** operation responsibility helpers

	// used in scenario to endpoint transformation:
	public static void deriveResponsibilityFromName(Operation operation, String name) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		if (name.startsWith("read")) {
			or.setRo(RETRIEVAL_OPERATION);
		} else if (name.startsWith("search")) {
			or.setRo(RETRIEVAL_OPERATION);
		} else if (name.startsWith("lookup")) {
			or.setRo(RETRIEVAL_OPERATION);
		} else if (name.startsWith("performQuery")) {
			or.setRo(RETRIEVAL_OPERATION);
		} else if (name.startsWith("performCommand")) {
			or.setSto(RETRIEVAL_OPERATION);
		} else if (name.startsWith("create")) {
			or.setSco(STATE_CREATION_OPERATION);
			// TODO add "setup"?
		} else if (name.startsWith("update")) {
			or.setSto(STATE_TRANSITION_OPERATION);
		} else if (name.startsWith("modify")) {
			or.setSto(STATE_TRANSITION_OPERATION);
		} else if (name.startsWith("replace")) {
			or.setSro(STATE_REPLACEMENT_OPERATION);
		} else if (name.startsWith("delete")) {
			or.setSdo(STATE_DELETION_OPERATION);
		} else if (name.startsWith("add")) {
			or.setSto(COLLECTION_OPERATION);
		} else if (name.startsWith("remove")) {
			or.setSto(COLLECTION_OPERATION);
			// TODO (L) indicate stateless COMPUTATION_FUNCTION as "compute" or "run"?
		} else {
			or.setOther(UNSPECIFIED_RESPONSIBILITY); // ignored, just a flag
		}

		if (or.getOther() == null) {
			operation.setResponsibility(or);
		}
	}
	
	// caution: the following helpers overwrite an already existing responsibility (only one can be specified)
	
	public static OperationResponsibility setPrimaryResponsibility(String responsibility) throws MDSLException {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		if (responsibility.equals(MAPDecoratorHelpers.EVENT_PROCESSOR)) {
			or.setEp(MAPDecoratorHelpers.EVENT_PROCESSOR);
			// TODO BAP variant
		} else if (responsibility.equals(MAPDecoratorHelpers.STATE_CREATION_OPERATION)) {
			or.setSco(MAPDecoratorHelpers.STATE_CREATION_OPERATION);
		} else if (responsibility.equals(MAPDecoratorHelpers.STATE_REPLACEMENT_OPERATION)) {
			or.setSro(MAPDecoratorHelpers.STATE_REPLACEMENT_OPERATION);
		} else if (responsibility.equals(MAPDecoratorHelpers.STATE_TRANSITION_OPERATION)) {
			or.setSto(MAPDecoratorHelpers.STATE_TRANSITION_OPERATION);
		} else if (responsibility.equals(MAPDecoratorHelpers.STATE_DELETION_OPERATION)) {
			or.setSdo(MAPDecoratorHelpers.STATE_DELETION_OPERATION);
		} else if (responsibility.equals(MAPDecoratorHelpers.RETRIEVAL_OPERATION)) {
			or.setRo(MAPDecoratorHelpers.RETRIEVAL_OPERATION);
		} else if (responsibility.equals(MAPDecoratorHelpers.COMPUTATION_FUNCTION)) {
			or.setCf(MAPDecoratorHelpers.COMPUTATION_FUNCTION);
		} else {
			throw new MDSLException(responsibility + " is not one of the state manipulating operations");
		}
		return or;
	}

	public static OperationResponsibility addStateCreationResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSco(details); // details string not used but has to be set
		operation.setResponsibility(or);
		return or;
	}

	public static OperationResponsibility addStateTransferResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSto(details);
		operation.setResponsibility(or);
		return or;
	}

	// not yet used
	public static OperationResponsibility addStateReplacementResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSro(details);
		operation.setResponsibility(or);
		return or;
	}

	public static OperationResponsibility addDeletionResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSdo(details);
		operation.setResponsibility(or);
		return or;
	}

	public static OperationResponsibility addRetrievalOperationResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setRo(details);
		operation.setResponsibility(or);
		return or;
	}

	public static OperationResponsibility addComputationFunctionResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setCf(details);
		operation.setResponsibility(or);
		return or;
	}

	public static boolean alsoServesAs(EList<String> otherDecorators, String roleName) {
		return otherDecorators.contains(roleName);
	}

	// ** stereotypes in data types

	public static PatternStereotype createTypeDecorator(String stereotype) {
		PatternStereotype result = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		result.setPattern(stereotype);
		return result;
	}

	public static boolean isDecoratedWith(SingleParameterNode spn, String decorator) {
		if (spn == null) {
			return false;
		}
		if (spn.getAtomP() != null) {
			return isDecoratedWith(spn.getAtomP(), decorator.trim());
		}
		if (spn.getTr() != null) {
			return isDecoratedWith(spn.getTr(), decorator.trim());
		}
		if (spn.getGenP() != null) {
			return isDecoratedWith(spn.getGenP(), decorator.trim());
		}
		return false;
	}

	public static boolean isDecoratedWith(TypeReference tr, String patternStereotype) {
		if (tr == null) {
			return false;
		}
		if (tr.getClassifier() != null && tr.getClassifier().getPattern() != null) {
			if (tr.getClassifier().getPattern().equals(patternStereotype)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDecoratedWith(GenericParameter gp, String patternStereotype) {
		// generic parameters are not classified
		return false;
	}

	public static boolean isDecoratedWith(AtomicParameter ap, String patternStereotype) {
		if (ap == null) {
			return false;
		}
		if (ap.getClassifier() != null && ap.getClassifier().getPattern() != null) {
			if (ap.getClassifier().getPattern().equals(patternStereotype)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDecoratedWith(AtomicParameterList apl, String patternStereotype) {
		if (apl == null) {
			return false;
		}
		if (apl.getClassifier() != null && apl.getClassifier().getPattern() != null) {
			if (apl.getClassifier().getPattern().equals(patternStereotype)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDecoratedWith(ParameterTree pt, String patternStereotype) {
		if (pt == null) {
			return false;
		}
		if (pt.getClassifier() != null && pt.getClassifier().getPattern() != null) {
			if (pt.getClassifier().getPattern().equals(patternStereotype)) {
				return true;
			}
		}
		return false;
	}

	public static void setClassifier(TreeNode tn, String stereotype) {
		PatternStereotype ps = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		ps.setPattern(stereotype);

		if (tn.getChildren() != null) {
			tn.getChildren().setClassifier(ps);
			// APL and PF not handled
		} else if (tn.getPn() != null) {
			if (tn.getPn().getAtomP() != null) {
				tn.getPn().getAtomP().setClassifier(ps);
			} else if (tn.getPn().getTr() != null) {
				tn.getPn().getTr().setClassifier(ps);
				// tn.genP() does not have stereotype classifier
			}
		}
	}

	public static void unsetClassifier(TreeNode tn) {
		if (tn.getChildren() != null) {
			tn.getChildren().setClassifier(null);
			// APL and PF not handled
		} else if (tn.getPn() != null) {
			if (tn.getPn().getAtomP() != null) {
				tn.getPn().getAtomP().setClassifier(null);
			} else if (tn.getPn().getTr() != null) {
				tn.getPn().getTr().setClassifier(null);
				// tn.genP() does not have stereotype classifier
			}
		}
	}

	public static boolean isRetrievalOperation(Operation operation) {
		if (operation.getResponsibility() == null) {
			return false;
		}
		if (operation.getResponsibility().getRo() != null && operation.getResponsibility().getRo().equals(RETRIEVAL_OPERATION)) {
			return true;
		}
		return false;
	}

	public static boolean isDeleteOperation(Operation operation) {
		if (operation.getResponsibility() == null) {
			return false;
		}
		if (operation.getResponsibility().getSdo() != null) {
			return true;
		}
		return false;
	}
}

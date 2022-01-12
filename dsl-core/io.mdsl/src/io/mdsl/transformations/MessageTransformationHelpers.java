package io.mdsl.transformations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MDSLLogger;

public class MessageTransformationHelpers {
	static final String WISH_TEMPLATE = "Wish_Template";
	static final String WISH_LIST = "Wish_List";
	final static String LINKED_INFORMATION_HOLDER = "Linked_Information_Holder";
	final static String EMBEDDED_ENTITY = "Embedded_Entity";

	static final String WRAPPER_PREFIX = "wrapped";

	private static final String TEMPLATE_IDENTIFIER = "mockObject";
	private static final String DEFAULT_OUT_AP_NAME = "OutAP";
	private static final String DEFAULT_IN_AP_NAME = "InAP";

	public static DataTransferRepresentation navigateToRequestPayload(DataTransferRepresentation responsePayload) {

		// TODO (L) no longer needed as by-stereotype QF has been sunset: navigate from DTR-out->Op->DTR-in 
		EObject operation = responsePayload.eContainer();

		// check that element actually is an operation
		if (!(operation instanceof Operation)) {
			MDSLLogger.reportError("Add Pagination: error, expected operation");
		}

		Operation opAsOp = (Operation) operation;

		// check that PT actually is in 'delivering' payload
		if (opAsOp.getResponseMessage() != responsePayload) {
			MDSLLogger.reportError("Known limitation: Add Pagination can only be applied to response payloads.");
		}

		return opAsOp.getRequestMessage(); // can be null
	}

	public static SingleParameterNode createLink(String ptName) {
		AtomicParameter lp = DataTypeTransformations.createLinkParameter(ptName);
		lp.setClassifier(MAPDecoratorHelpers.createTypeDecorator(LINKED_INFORMATION_HOLDER));
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setAtomP(lp);
		spn.setGenP(null);
		spn.setTr(null);
		return spn;
	}

	public static String findPName(boolean fromRequest, AtomicParameter ap) {
		String apName = ap.getRat().getName();
		if (apName == null || apName.isEmpty()) {
			if (fromRequest) {
				apName = DEFAULT_IN_AP_NAME;
			} else {
				apName = DEFAULT_OUT_AP_NAME;
			}
		}
		return apName;
	}

	public static void wrapParameterTreeInTreeNodeOrElementStructure(EObject containingElement, ParameterTree pt) {
		TreeNode tn;
		if (containingElement instanceof TreeNode) {
			tn = (TreeNode) containingElement;
			tn.setApl(null);
			tn.setPn(null);
			tn.setChildren(pt);
		} else if (containingElement instanceof ElementStructure) {
			ElementStructure es = (ElementStructure) containingElement;
			es.setApl(null);
			es.setNp(null);
			es.setPt(pt);
			es.setPf(null);
		} else {
			MDSLLogger.reportError("Expected Tree Node or Element Structure, but got instance of " + containingElement.getClass().toString());
		}
	}

	public static ParameterTree createParameterTreeWrapper(ParameterTree toBeWrapped) {
		ParameterTree ptWrapper = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		TreeNode tn = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		tn.setChildren(toBeWrapped);

		String name = toBeWrapped.getName();
		ptWrapper.setName(DataTypeTransformationHelpers.nameForElement(name, WRAPPER_PREFIX)); // was "requestBundle"
		ptWrapper.setFirst(tn);

		return ptWrapper;
	}

	public static ParameterTree createParameterTreeWrapper(SingleParameterNode np) {
		AtomicParameter ap = np.getAtomP();
		if (ap == null) {
			throw new MDSLException("Known limitation: Can only wrap atomic parameters as parameters trees at present.");
		}

		TreeNode tn = DataTypeTransformations.turnAtomicParameterIntoTreeNode(ap);
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();

		pt.setClassifier(EcoreUtil.copy(ap.getClassifier())); // copy still needed?
		ap.setClassifier(null);

		String name = ap.getRat().getName();
		pt.setName(DataTypeTransformationHelpers.nameForElement(name, WRAPPER_PREFIX));
		pt.setFirst(tn);

		return pt;
	}

	// ** Extract/Inline Information Holder helpers

	public static TreeNode findLinkParameterForInformationHolder(Operation op, boolean fromRequest) {
		ElementStructure es;

		if (fromRequest) {
			es = op.getRequestMessage().getPayload();
		} else {
			es = op.getResponseMessage().getPayload();
		}

		if (es.getPt() != null) {
			return TransformationHelpers.findStereotypeInTree(es.getPt(), LINKED_INFORMATION_HOLDER);
		} else if (es.getNp() != null) {
			throw new MDSLException("This refactoring does not support top-level atomic parameters, but parameter trees");
		} else {
			throw new MDSLException("Unsupported type of payload structure."); // not handling APL and PF at present
		}
	}

	public static TreeNode findEmbeddedEntity(Operation op, boolean fromRequest) {
		// find first PT in request or response decorated with <<Embedded_Entity>>
		ParameterTree pt = null;
		if (fromRequest) {
			pt = op.getRequestMessage().getPayload().getPt();
		} else {
			pt = op.getResponseMessage().getPayload().getPt();
		}

		return TransformationHelpers.findStereotypeInTree(pt, EMBEDDED_ENTITY);
	}

	// ** Wish List/Template helpers

	public static ParameterTree createWishTemplate(ParameterTree responsePT) {
		ParameterTree wishTemplate = EcoreUtil.copy(responsePT);
		wishTemplate.setName(TEMPLATE_IDENTIFIER);
		// TODO (M) depending on variant: turn strings etc. into booleans or optional tree nodes recursively
		PatternStereotype mapDecorator = ApiDescriptionFactory.eINSTANCE.createPatternStereotype(); // helper?
		mapDecorator.setPattern(WISH_TEMPLATE);
		wishTemplate.setClassifier(mapDecorator);
		return wishTemplate;
	}

	public static TreeNode findWishList(ParameterTree pt) {
		if (pt == null) {
			return null;
		}

		TreeNode tn1 = pt.getFirst();
		if (nodeIsAWishList(tn1)) {
			return tn1;
		}

		for (TreeNode tn : pt.getNexttn()) {
			if (nodeIsAWishList(tn)) {
				return tn;
			}
		}

		return null;
	}

	public static boolean nodeIsAWishList(TreeNode tn) {
		if (tn.getPn() == null) {
			return false;
		}

		// Wish List element might not be an AP (if not created with QF); will not be found in that case
		AtomicParameter potentialWish = tn.getPn().getAtomP();
		return potentialWish!=null&&potentialWish.getClassifier()!=null
				&&WISH_LIST.equals(potentialWish.getClassifier().getPattern());
	}
}

package io.mdsl.transformations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.AtomicParameterList;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.utils.MDSLLogger;

public class MessageTransformations {

	private static final String SAMPLE_CONTEXT_TYPE = "SampleContext";
	private static final String QOS_IDENTIFIER = "qos";
	private static final String REQUEST_CONDITION_STEREOTYPE = "Request_Condition";
	private static final String KEY_VALUE_MAP = "mapOf";
	private static final String INLINED_INFORMATION_HOLDER_IDENTIFIER = "inlinedInformationHolder";
	private static final String CONTEXT_REPRESENTATION = "Context_Representation";
	private static final String PARAMETER_TREE_MISSING = "Response message does not contain a parameter tree.";

	public static final String CURSOR_FROM_OPERATION = "cursorFromOperation";
	public static final String OFFSET_FROM_OPERATION = "offsetFromOperation";

	final static String PAGE = "page";
	final static String PAGE_SIZE = "pageSize";
	final static String TOTAL_PAGES = "totalPages";

	final static String OFFSET = "offset";
	final static String LIMIT = "limit";
	final static String SIZE = "size";

	final static String CURSOR = "Cursor";
	final static String SELF = "self";
	final static String NEXT = "next";

	private static final String OUT_PARAMETER_NAME_SUFFIX = "-out"; // TODO (L) use better pname
	private static final String PAGINATION_ERROR_DTR_EXPECTED = "Add Pagination: Internal error, expected a Data Transfer Representation";
	private static final String INT_TYPE = "int";
	private static final String STRING_TYPE = "string";
	private static final String DESIRED_ELEMENTS = "desiredElements";
	private static final String REQUESTED = "requested";

	// ** Pagination

	public static void addPagination(ElementStructure es, String variant) {

		// go from ElementStructure to DataTransferRepresentation
		EObject eo = es.eContainer();
		if (!(eo instanceof DataTransferRepresentation)) {
			MDSLLogger.reportError(PAGINATION_ERROR_DTR_EXPECTED);
		}

		DataTransferRepresentation responsePayload = (DataTransferRepresentation) eo;
		ParameterTree pt = es.getPt();

		// check that element actually always is a Parameter Tree
		if (pt == null) {
			MDSLLogger.reportError("Known limitation: Add Pagination can only be applied to Parameter Trees at present.");
		}

		addPaginationMetadataToRequestPayload(responsePayload, variant);
		addPaginationMetadataToResponsePayload(pt, variant);

		PatternStereotype mapDecorator = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		mapDecorator.setPattern("Pagination");

		pt.setClassifier(mapDecorator);
	}

	private static void addPaginationMetadataToResponsePayload(ParameterTree pt, String variant) {
		AtomicParameter chunkSize = null;
		AtomicParameter startingPoint = null;
		AtomicParameter chunkAmount = null;
		AtomicParameter nextLink = null;

		if (variant.equals(OFFSET_FROM_OPERATION)) {
			startingPoint = DataTypeTransformations.createMetadataParameter(OFFSET + OUT_PARAMETER_NAME_SUFFIX, INT_TYPE);
			pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(startingPoint));

			chunkSize = DataTypeTransformations.createMetadataParameter(LIMIT + OUT_PARAMETER_NAME_SUFFIX, INT_TYPE);
			pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(chunkSize));

			chunkAmount = DataTypeTransformations.createMetadataParameter(SIZE, INT_TYPE);
			pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(chunkAmount));

			nextLink = DataTypeTransformations.createLinkParameter(NEXT);
		} else if (variant.equals(CURSOR_FROM_OPERATION)) {
			chunkSize = DataTypeTransformations.createMetadataParameter(PAGE_SIZE, INT_TYPE);
			pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(chunkSize));

			nextLink = DataTypeTransformations.createLinkParameter(NEXT + CURSOR); // special case, class vs. instance level (MDSL type/value)
		} else { // must be default page-based pagination (could check)
			startingPoint = DataTypeTransformations.createMetadataParameter(PAGE, INT_TYPE);
			pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(startingPoint));

			chunkSize = DataTypeTransformations.createMetadataParameter(PAGE_SIZE, INT_TYPE);
			pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(chunkSize));

			chunkAmount = DataTypeTransformations.createMetadataParameter(TOTAL_PAGES, INT_TYPE);
			pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(chunkAmount));

			nextLink = DataTypeTransformations.createLinkParameter(NEXT);
		}

		AtomicParameter selfLink = DataTypeTransformations.createLinkParameter(SELF);
		pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(selfLink));

		pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(nextLink));
	}
	
	// TODO (M) could improve grammar navigation as operation is anchor of these QFs (used to be response payload)

	private static void addPaginationMetadataToRequestPayload(DataTransferRepresentation responsePayload, String variant) {
		// check that operation actually has a request payload and navigate to it
		DataTransferRepresentation requestPayload = MessageTransformationHelpers.navigateToRequestPayload(responsePayload);
		if (requestPayload == null) {
			MDSLLogger.reportError("Can't add to non-existing request payload, so refactoring not applied");
		}

		ElementStructure rpl = requestPayload.getPayload();

		ParameterTree rplPt = null;
		// check that request contains a parameter tree as well
		if (rpl.getPt() != null) {
			rplPt = rpl.getPt();
		} else if (rpl.getNp() != null) {
			// avoid breaking the precondition
			rplPt = MessageTransformationHelpers.createParameterTreeWrapper(rpl.getNp());
			rpl.setNp(null); // needed to avoid serialization errors
			rpl.setPt(rplPt);
		} else {
			MDSLLogger.reportError("Can't apply this refactoring part to non-PT, non-SPN request payload.");
		}
			// MDSL tool constraint: identifiers in request and response messages must be unique

		AtomicParameter chunkSize = null;
		AtomicParameter startingPoint = null;
		if (variant.equals(OFFSET_FROM_OPERATION)) {
			chunkSize = DataTypeTransformations.createMetadataParameter(LIMIT, INT_TYPE);
			rplPt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(chunkSize));
			startingPoint = DataTypeTransformations.createMetadataParameter(OFFSET, INT_TYPE);
			rplPt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(startingPoint));
		} else {
			if (variant.equals(CURSOR_FROM_OPERATION)) {
			} else { // must be default page-based pagination (could check)
				startingPoint = DataTypeTransformations.createMetadataParameter(REQUESTED + DataTypeTransformationHelpers.capitalizeName(PAGE), INT_TYPE);
				rplPt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(startingPoint));
			}
			chunkSize = DataTypeTransformations.createMetadataParameter(REQUESTED + DataTypeTransformationHelpers.capitalizeName(PAGE_SIZE), INT_TYPE);
			rplPt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(chunkSize));
		}
		// note: inserting at the start of the PT would be less straightforward due to due current grammar design
	}

	// ** Wish List and Wish Template

	public static void addWishList(Operation operation) {

		if (operation.getRequestMessage() == null || operation.getRequestMessage().getPayload() == null) {
			MDSLLogger.reportError("Known limitation: Add Wish List can only be applied if request payload is present.");
		}

		if (operation.getResponseMessage() == null || operation.getResponseMessage().getPayload() == null) {
			MDSLLogger.reportError("Known limitation: Add Wish List can only be applied if response payload is present.");
		}

		ParameterTree pt = operation.getRequestMessage().getPayload().getPt();

		// check that element actually contains a parameter tree
		if (pt == null) {
			addParameterTreeWrapperIfNeeded(operation.getRequestMessage());
			pt = operation.getRequestMessage().getPayload().getPt();
		}

		if (!allTopLevelTreeElementsAreOptional(operation.getResponseMessage().getPayload(), false)) {
			MDSLLogger.reportWarning("Wish List will only work if all nodes in response tree are optional.");
		}

		AtomicParameter wl = DataTypeTransformations.createMetadataParameter(DESIRED_ELEMENTS, STRING_TYPE);
		Cardinality card = ApiDescriptionFactory.eINSTANCE.createCardinality();
		card.setZeroOrMore("*");
		wl.setCard(card);

		PatternStereotype mapDecorator = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		mapDecorator.setPattern(MessageTransformationHelpers.WISH_LIST);
		wl.setClassifier(mapDecorator);

		pt.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(wl));
	}

	private static void addParameterTreeWrapperIfNeeded(DataTransferRepresentation message) {
		if(message.getPayload().getNp()==null) {
			// TODO support APL
			MDSLLogger.reportWarning("Not adding parameter tree (not an spn)");
		}
		
		if(message.getPayload().getNp().getAtomP()!=null) {
			MDSLLogger.reportInformation("Wrapping AP in PT in aWL: " + message.getPayload().getNp().getAtomP().getRat().getName());
			MessageTransformations.addParameterTreeWrapper(message.getPayload().getNp().getAtomP());
		}
		// genP should/can be turned into AP with separate QF 
		else if(message.getPayload().getNp().getTr()!=null) {
			addParameterTreeWrapper(message.getPayload().getNp().getTr());
		}
		else {
			MDSLLogger.reportWarning("Not adding parameter tree (unsupported spn type)");
		}
	}

	private static boolean allTopLevelTreeElementsAreOptional(ElementStructure responseMessage, boolean deepCheck) {
		// TODO v55 check optionality of PT elements in response, use it in request (see validator for tree traversal)
		// recursion required if isNested/deep check is true (might contain loops)
		return true;
	}

	public static void addWishTemplate(ElementStructure requestMessage, ElementStructure responseMessage) {
		ParameterTree requestTree = requestMessage.getPt();
		if (requestTree == null) {
			MDSLLogger.reportError("Wish Template can only be added to parameter tree as request payload.");
		}

		if (!allTopLevelTreeElementsAreOptional(responseMessage, true)) {
			MDSLLogger.reportWarning("Wish Template will only work if all nodes in response tree are optional.");
		}

		TreeNode wltn = MessageTransformationHelpers.findWishList(requestTree);
		if (wltn != null) {
			ParameterTree responsePT = responseMessage.getPt();
			if (responsePT == null) {
				MDSLLogger.reportError(PARAMETER_TREE_MISSING);
			} else {
				ParameterTree wishTemplate = MessageTransformationHelpers.createWishTemplate(responsePT);
				// replace Wish List with copied/adjusted response DTR (the template):
				wltn.setPn(null);
				wltn.setChildren(wishTemplate);
			}
		} else if (responseMessage.getPt() != null) {
			ParameterTree wishTemplate = MessageTransformationHelpers.createWishTemplate(responseMessage.getPt());
			TreeNode wttn = ApiDescriptionFactory.eINSTANCE.createTreeNode();
			wttn.setChildren(wishTemplate);
			requestTree.getNexttn().add(wttn);
		} else if (responseMessage.getNp() != null) {
			MDSLLogger.reportError("Response message contain a single parameter node, which is not yet supported in this refactoring.");
		}
		// AP, TR, GenP, APL not supported yet
		else {
			MDSLLogger.reportError(PARAMETER_TREE_MISSING);
		}
	}

	// ** Request Bundle

	public static ElementStructure addRequestBundle(ElementStructure element, boolean applyToRequest) {

		ParameterTree pt = null;
		if (element.getPt() != null) {
			pt = MessageTransformationHelpers.createParameterTreeWrapper(element.getPt());
		} else if (element.getNp() != null) {
			pt = MessageTransformationHelpers.createParameterTreeWrapper(element.getNp());
		} else {
			MDSLLogger.reportError("Known limitation: Add Request Bundle can only be applied to Parameter Trees (and SPNs) at present.");
		}

		Cardinality setCard = ApiDescriptionFactory.eINSTANCE.createCardinality();
		setCard.setAtLeastOne("+");
		pt.setCard(setCard);

		PatternStereotype mapDecorator = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		if (applyToRequest) {
			mapDecorator.setPattern("Request_Bundle");
		} else {// must be response
			mapDecorator.setPattern("Response_Bundle");
		}
		pt.setClassifier(mapDecorator);

		ElementStructure wrapperEs = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		wrapperEs.setPt(pt);

		EObject containingElement = element.eContainer();
		if (containingElement instanceof DataContract) {
			DataContract dt = (DataContract) containingElement;
			dt.setStructure(wrapperEs);
		} else if (containingElement instanceof EventType) {
			EventType de = (EventType) containingElement;
			de.setContent(wrapperEs);
		} else if (containingElement instanceof DataTransferRepresentation) {
			DataTransferRepresentation dtr = (DataTransferRepresentation) containingElement;
			dtr.setPayload(wrapperEs);
		} else {
			MDSLLogger.reportError("Expected Data Type, Event Type or Data Transfer Representation." + containingElement.getClass().toString());
		}

		return wrapperEs;
	}
	
	// ** "Wrap atomic parameter with parameter tree", "Include atomic parameter in key-value map"
	
	public static EObject addParameterTreeWrapper(EObject element) {
		TreeNode tn = null;
		EObject containingElement = null;
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();

		if (element instanceof AtomicParameterList) {
			AtomicParameterList apl = (AtomicParameterList) element;
			pt.setClassifier(EcoreUtil.copy(apl.getClassifier()));
			pt.setName(apl.getName());
			tn = DataTypeTransformations.turnAtomicParameterIntoTreeNode(apl.getFirst());
			pt.setFirst(tn);
			for (AtomicParameter nextAp : apl.getNextap()) {
				tn = DataTypeTransformations.turnAtomicParameterIntoTreeNode(nextAp);
				pt.getNexttn().add(tn);
			}
			pt.setCard(EcoreUtil.copy(apl.getCard()));
			// TODO (L) TN is not the only place where APL can appear, ElementStructure,
			// ParameterTreeList (in PF)?
			containingElement = apl.eContainer();
		} else if (element instanceof AtomicParameter) {
			AtomicParameter ap = (AtomicParameter) element;
			tn = DataTypeTransformations.turnAtomicParameterIntoTreeNode(ap);
			pt.setClassifier(EcoreUtil.copy(ap.getClassifier()));
			pt.setName(DataTypeTransformationHelpers.nameForElement(ap.getRat().getName(), MessageTransformationHelpers.WRAPPER_PREFIX));
			pt.setFirst(tn);
			pt.setCard(EcoreUtil.copy(ap.getCard()));
			containingElement = ap.eContainer().eContainer(); // SPN -> TN or ES
		} else if (element instanceof TypeReference) {
			TypeReference tr = (TypeReference) element;
			tn = DataTypeTransformations.turnTypeReferenceIntoTreeNode(tr);
			pt.setClassifier(EcoreUtil.copy(tr.getClassifier()));
			String trName = DataTypeTransformationHelpers.nameForElement(tr.getName(), MessageTransformationHelpers.WRAPPER_PREFIX);
			pt.setName(trName);
			pt.setFirst(tn);
			pt.setCard(EcoreUtil.copy(tr.getCard()));
			containingElement = tr.eContainer().eContainer(); // SPN -> TN or ES
		}
		// TODO (L) could add support for "PT in PT" here; document type ref support
		else {
			MDSLLogger.reportError("This transformation can only be applied to Atomic Parameter Lists, Atomic Parameters, Type References" + element.getClass().toString());
		}

		MessageTransformationHelpers.wrapParameterTreeInTreeNodeOrElementStructure(containingElement, pt);

		return containingElement;
	}

	public static EObject addKeyValueMapWrapper(AtomicParameter ap) {
		// wrap it similar to what IntroduceDTO does
		// "id":MD<raw> -> {"idMapKey":ID<string>, "idMapValue":MD<raw>}*
		// could also do multi-map: {"idMapKey":ID<string>, "idMapValue":MD<raw>*}*

		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		AtomicParameter key = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
		RoleAndType rat = DataTypeTransformations.createRoleAndType("key", "ID", STRING_TYPE);
		key.setRat(rat);

		TreeNode tn = DataTypeTransformations.turnAtomicParameterIntoTreeNode(key);
		pt.setName(DataTypeTransformationHelpers.nameForElement(ap.getRat().getName(), KEY_VALUE_MAP));
		pt.setFirst(tn);

		TreeNode tn2 = DataTypeTransformations.turnAtomicParameterIntoTreeNode(ap);
		pt.setClassifier(EcoreUtil.copy(ap.getClassifier()));
		pt.setCard(EcoreUtil.copy(ap.getCard()));
		pt.getNexttn().add(tn2);

		EObject containingElement = ap.eContainer().eContainer(); // SPN -> TN or ES

		MessageTransformationHelpers.wrapParameterTreeInTreeNodeOrElementStructure(containingElement, pt);

		return containingElement;
	}

	// ** Extract Information Holder, Inline Information Holder

	public static void extractInformationHolder(Operation operation, boolean fromRequest) {
		TreeNode tn = MessageTransformationHelpers.findEmbeddedEntity(operation, fromRequest);
		if (tn == null) {
			MDSLLogger.reportError("Can't find any <<" + MessageTransformationHelpers.EMBEDDED_ENTITY + ">> decorator in " + operation.getName());
		}
		MAPDecoratorHelpers.unsetClassifier(tn); // no stereotype on this level
		TreeNode originalTreeNode = EcoreUtil.copy(tn);
		ServiceSpecification ss = (ServiceSpecification) operation.eContainer().eContainer();
		String ptName = DataTypeTransformationHelpers.nameOf(tn);
		addEndpoint(originalTreeNode, ss, ptName);

		// replace found PT/SPN TN with new link parameter
		tn.setApl(null);
		tn.setChildren(null);
		tn.setPn(MessageTransformationHelpers.createLink(ptName));
	}

	private static void addEndpoint(TreeNode tn, ServiceSpecification ss, String ptName) {
		// find or create new endpoint (after name check) and add a lookup operation to
		// it, in: ID, out: PT (DTO)
		ElementStructure es2 = wrapInformationHolderNode(tn);
		DataContract dt = DataTypeTransformations.findOrCreateDataType(ss, DataTypeTransformationHelpers.capitalizeName(ptName), es2);
		TypeReference tref = DataTypeTransformations.createTypeReference(dt);
		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, DataTypeTransformationHelpers.capitalizeName(ptName) + "InformationHolderResource");
		MAPDecoratorHelpers.setRole(ec, "INFORMATION_HOLDER_RESOURCE"); // set to secondary if already present?
		Operation ro = OperationTransformationHelpers.createLookupOperation(tref); // turn tref into set cardinality (see IHR QFs)
		// check that operation of this name ("lookup") is not already present
		if (TransformationHelpers.findOperationInContract(ec, ro.getName()) != null) {
			MDSLLogger.reportWarning("Endpoint type " + ec.getName() + " already exposes a " + ro.getName() + " operation, not adding another one");
		} else {
			ec.getOps().add(ro);
		}
	}

	private static ElementStructure wrapInformationHolderNode(TreeNode tn) {
		if (tn.getChildren() != null) {
			return DataTypeTransformations.wrapParameterTreeAsElementStructure(tn.getChildren());
			// APL and PF not handled
		} else if (tn.getPn() != null) {
			if (tn.getPn().getAtomP() != null) {
				return DataTypeTransformations.wrapAtomicParameterAsElementStructure(tn.getPn().getAtomP());
			} else if (tn.getPn().getTr() != null) {
				return DataTypeTransformations.wrapTypeReferenceAsElementStructure(tn.getPn().getTr());
			} else if (tn.getPn().getGenP() != null) {
				return DataTypeTransformations.wrapGenericParameterNodeAsElementStructure(tn.getPn().getGenP());
			}
		}
		return null;
	}

	public static void inlineInformationHolder(Operation op, boolean fromRequest) {
		AtomicParameter ap; // findLinkParameterForInformationHolderInOperation(op, fromRequest);
		TreeNode tn = MessageTransformationHelpers.findLinkParameterForInformationHolder(op, fromRequest);
		if (tn == null) {
			MDSLLogger.reportError("Can't find any <<" + MessageTransformationHelpers.LINKED_INFORMATION_HOLDER + ">> decorated tn in " + op.getName());
		}
		if (tn.getPn() == null) {
			MDSLLogger.reportError("Can't find any <<" + MessageTransformationHelpers.LINKED_INFORMATION_HOLDER + ">> decorated spn in " + op.getName());
		}
		ap = tn.getPn().getAtomP();
		if (ap == null) {
			MDSLLogger.reportError("Can't find any <<" + MessageTransformationHelpers.LINKED_INFORMATION_HOLDER + ">> decorated atomic parameter in " + op.getName());
		}

		String pName = MessageTransformationHelpers.findPName(fromRequest, ap);

		// get service specification
		ServiceSpecification ss = (ServiceSpecification) op.eContainer().eContainer();

		// get DTO to be embedded, create a metadata parameter if not found
		ElementStructure es = DataTypeTransformations.wrapAtomicParameterAsElementStructure(
				DataTypeTransformations.createMetadataParameter(INLINED_INFORMATION_HOLDER_IDENTIFIER, STRING_TYPE));
		DataContract dt = DataTypeTransformations.findOrCreateDataTypeByIdentifier(ss, pName, es);

		TypeReference tr = DataTypeTransformations.createTypeReference(dt);
		String elemName = TransformationHelpers.nameOf(dt.getStructure());
		if (elemName == null || elemName.isEmpty()) {
			tr.setName(INLINED_INFORMATION_HOLDER_IDENTIFIER);
		} else {
			tr.setName(elemName);
		}

		// toggle MAP stereotype decorator
		tr.setClassifier(MAPDecoratorHelpers.createTypeDecorator(MessageTransformationHelpers.EMBEDDED_ENTITY));

		// replace Link AP with type reference to DTO
		SingleParameterNode spn = (SingleParameterNode) ap.eContainer();
		spn.setAtomP(null);
		spn.setTr(tr);
		
		// could remove endpoint that link refers to, but cannot be identified easily and reliably; can be done manually
	}

	public static void addContextRepresentation(Operation operation, String typeName) {
		ServiceSpecification ss = (ServiceSpecification) operation.eContainer().eContainer();
		AtomicParameter mp = DataTypeTransformations.createMetadataParameter(QOS_IDENTIFIER, INT_TYPE);
		TreeNode tn = DataTypeTransformations.wrapAtomicParameterAsTreeNode(mp);
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		pt.setFirst(tn);
		ElementStructure es = DataTypeTransformations.wrapParameterTreeAsElementStructure(pt);
		if(typeName==null ) {
			typeName = SAMPLE_CONTEXT_TYPE;
		}
		pt.setName(DataTypeTransformationHelpers.decapitalizeName(typeName)); // not needed, but nor hurting either
		DataContract contextDTO = DataTypeTransformations.findOrCreateDataType(ss, typeName, es);
		MessageTransformations.addContextRepresentation(operation.getRequestMessage().getPayload(), contextDTO);
	}

	public static void addContextRepresentation(ElementStructure requestMessage, DataContract contextDTO) {
		if (requestMessage == null) {
			MDSLLogger.reportError("addContextRepresentation expects a request message to be present.");
		}
		if (requestMessage.getPt() == null) {
			MDSLLogger.reportError("addContextRepresentation can only be applied to request parameter trees.");
		}
		TypeReference tr = DataTypeTransformations.createTypeReference(contextDTO);
		TreeNode tn = DataTypeTransformations.wrapTypeReferenceAsTreeNode(tr);
		PatternStereotype ps = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		ps.setPattern(CONTEXT_REPRESENTATION);
		tn.getPn().getTr().setClassifier(ps);
		// TODO (L) more user friendly: add at start and move previous first to start of next tn
		requestMessage.getPt().getNexttn().add(tn);
	}

	public static void makeRequestConditional(Operation op, String variant) {
		if (op.getRequestMessage() == null || op.getRequestMessage().getPayload() == null || op.getRequestMessage().getPayload().getPt() == null) {
			MDSLLogger.reportError("This refactoring requires a parameter tree to be present in the request message (if any).");
		}

		TreeNode tn = DataTypeTransformations.wrapAtomicParameterAsTreeNode(DataTypeTransformations.createMetadataParameter(variant, "string"));
		MAPDecoratorHelpers.setClassifier(tn, REQUEST_CONDITION_STEREOTYPE);
		op.getRequestMessage().getPayload().getPt().getNexttn().add(tn);
	}
}

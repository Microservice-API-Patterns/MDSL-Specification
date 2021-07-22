package io.mdsl.transformations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.impl.DataContractImpl;
import io.mdsl.apiDescription.impl.DataTransferRepresentationImpl;
import io.mdsl.apiDescription.impl.EventTypeImpl;
import io.mdsl.apiDescription.impl.OperationImpl;
import io.mdsl.exception.MDSLException;

public class MessageTransformations {
	public static void addPagination(ElementStructure es) {

		ParameterTree pt = es.getPt();

		// check that element actually always is a PT
		if(pt==null) {
			System.err.println("Known limitation: Add Pagination Quick Fix can only be applied to Parameter Trees at present.");
			throw new MDSLException("Known limitation: Add Pagination Quick Fix can only be applied to Parameter Trees at present.");
		}

		// TODO (M) revisit attribute names (see IRC and MAP), use type to define
		// IRC: "page size, page number, total number of elements for Offset-Based Pagination"
		// MAP: text "limit, offset and total size", new 2nd example: "pageSize" : 2, "page" : 0, "totalPages" : 25,"
		AtomicParameter limitMetadata = DataTypeTransformationHelpers.createMetadataParameter("limit", "int");
		AtomicParameter offsetMetadata = DataTypeTransformationHelpers.createMetadataParameter("offset", "int");
		AtomicParameter sizeMetadata = DataTypeTransformationHelpers.createMetadataParameter("size", "int");
		AtomicParameter selfLink = DataTypeTransformationHelpers.createLinkParameter("self");
		AtomicParameter nextLink = DataTypeTransformationHelpers.createLinkParameter("next");

		pt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(limitMetadata));
		pt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(offsetMetadata));
		pt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(sizeMetadata));
		pt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(selfLink));
		pt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(nextLink));

		EObject eo = es.eContainer();
		if(eo.getClass()!=DataTransferRepresentationImpl.class) {
			System.err.println("Add Pagination Quick Fix: Internal error, expected DTR");
			throw new MDSLException("Add Pagination Quick Fix: Internal error, expected DTR");
		}
		DataTransferRepresentation responsePayload = (DataTransferRepresentation) eo;
		addLimitAndOffsetToRequestPayload(responsePayload);
	}

	private static void addLimitAndOffsetToRequestPayload(DataTransferRepresentation responsePayload) {
		// check that operation actually has a request payload and navigate to it
		DataTransferRepresentation requestPayload = navigateToRequestPayload(responsePayload);
		if(requestPayload==null) {
			System.err.println("Can't add to non-existing request payload, so refactoring not applied.");
			throw new MDSLException("Can't add to non-existing request payload, so refactoring not applied");
		}

		ElementStructure rpl = requestPayload.getPayload();
		// MDSL tool constraint: identifiers in request and response messages must be unique
		AtomicParameter limitMetadata = DataTypeTransformationHelpers.createMetadataParameter("limit-in", "int");
		AtomicParameter offsetMetadata = DataTypeTransformationHelpers.createMetadataParameter("offset-in", "int");

		ParameterTree rplPt = rpl.getPt();
		// check that request contains a parameter tree as well
		if(rplPt==null) {
			System.err.println("Can't apply this refactoring part to non-PT request payload.");
			throw new MDSLException("Can't apply this refactoring part to non-PT request payload.");
		}

		// [Q] is inserting at the start of the PT also possible? (less straightforward due to due grammar design) 
		rplPt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(limitMetadata)); 
		rplPt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(offsetMetadata));
	}

	private static DataTransferRepresentation navigateToRequestPayload(DataTransferRepresentation responsePayload) {
		// navigate from DTR-out->Op->DTR-in 
		EObject operation = responsePayload.eContainer();

		// check that element actually is an operation
		if(operation.getClass()!=OperationImpl.class) {
			System.err.println("Add Pagination Quick Fix: error, expected operation");
			throw new MDSLException("Add Pagination Quick Fix: error, expected operation");
		}

		Operation opAsOp = (Operation) operation;

		// check that PT actually is in 'delivering' payload
		if(opAsOp.getResponseMessage() != responsePayload) {
			System.err.println("Known limitation: Add Pagination Quick Fix can only be applied to response payloads.");
			throw new MDSLException("Known limtation: Add Pagination Quick Fix can only be applied to response payloads.");
		}

		return opAsOp.getRequestMessage(); // can be null
	}
	
	public static void addWishList(ElementStructure element, String type) {
		ParameterTree pt = element.getPt();
		
		// check that element actually contains a parameter tree
		if(pt==null) {
			System.err.println("Known limitation: Add Wish List Quick Fix can only be applied to Parameter Trees at present.");
			throw new MDSLException("Known limitation: Add Wish List Quick Fix can only be applied to Parameter Trees at present.");
		}
		
		AtomicParameter wl = DataTypeTransformationHelpers.createMetadataParameter("desiredElements", "string");
		Cardinality card = ApiDescriptionFactory.eINSTANCE.createCardinality();
		card.setZeroOrMore(type);
		wl.setCard(card);
		
	    // TODO (M) remove outer MAP decorator first (or change Validator)
		/*
		PatternStereotype mapDecorator = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		mapDecorator.setPattern("Wish_List");
		wl.setClassifier(mapDecorator);
		*/ 
		
		pt.getNexttn().add(DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(wl));
	}

	public static ElementStructure addRequestBundle(ElementStructure element) {
	
		ParameterTree pt = null;
		if(element.getPt()!=null) {
			pt = createParameterTreeWrapper(element.getPt());
		}
		else if(element.getNp()!=null) {
			pt = createParameterTreeWrapper(element.getNp());
		}
		else {
			System.err.println("Known limitation: Add Request Bundle Quick Fix can only be applied to Parameter Trees and SPNs at present.");
			throw new MDSLException("Known limitation: Add Request Bundle Quick Fix can only be applied to Parameter Trees and SPNs at present.");
		}
		
		Cardinality setCard = ApiDescriptionFactory.eINSTANCE.createCardinality();
		setCard.setAtLeastOne("+");
		pt.setCard(setCard);
		
		/*
		PatternStereotype mapDecorator = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
		mapDecorator.setPattern("Request_Bundle");
		pt.setClassifier(mapDecorator);
		*/ 
		
		ElementStructure wrapperEs = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		wrapperEs.setPt(pt);
		
		EObject containingElement = element.eContainer();
		if(containingElement.getClass().equals(DataContractImpl.class)) {
			DataContract dt = (DataContract) containingElement;
			dt.setStructure(wrapperEs);
		}
		else if(containingElement.getClass().equals(EventTypeImpl.class)) {
			EventType de = (EventType) containingElement;
			de.setContent(wrapperEs);
		}
		else if(containingElement.getClass().equals(DataTransferRepresentationImpl.class)) {
			DataTransferRepresentation dtr = (DataTransferRepresentation) containingElement;
			dtr.setPayload(wrapperEs);
		}
		else {		
			System.err.println("Expected Data Type, Event Type or Data Transfer Representation." + containingElement.getClass().toString());
			throw new MDSLException("Expected Data Type, Event Type or Data Transfer Representation.");
		}
			
		// [R] not navigating to response at present, could set-ify that too (separate transformation?)
		
		return wrapperEs;
	}
	
	// IntroduceParameterTreeDTO has similar helpers, could refactor

	private static ParameterTree createParameterTreeWrapper(SingleParameterNode np) {
		// TODO how about genP and typeRef?
		AtomicParameter ap = np.getAtomP();
		TreeNode tn = DataTypeTransformationHelpers.turnAtomicParameterIntoTreeNode(ap);
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		
		pt.setClassifier(EcoreUtil.copy(ap.getClassifier())); // copy still needed?
		ap.setClassifier(null);
		pt.setName(ap.getRat().getName() + "Wrapper");
		pt.setFirst(tn);
		
		return pt;
	}

	private static ParameterTree createParameterTreeWrapper(ParameterTree toBeWrapped) {
		ParameterTree ptWrapper = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		TreeNode tn = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		tn.setChildren(toBeWrapped);
		
		String name = toBeWrapped.getName();
		if(name==null||name.equals(""))
			name = "requestBundle";
		else 
			name = name + "Wrapper";
		
		ptWrapper.setName(name);
		ptWrapper.setFirst(tn);
		
		return ptWrapper;
	}
}

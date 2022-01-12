package io.mdsl.transformations;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.exception.MDSLException;

public class OperationTransformationHelpers {

	private static final String URI_IDENTIFIER = "uri";
	
	static final String BOOL_TYPE = "bool";
	static final String INT_TYPE = "int";
	static final String LONG_TYPE = "long";
	static final String STRING_TYPE = "string";
	static final String RAW_TYPE = "raw";
	static final String VOID_TYPE = "void";

	static final String DATA_ROLE = "D";
	private static final String METADATA_ROLE = "MD";
	private static final String ID_ROLE = "ID";

	private static final String CONVERT_OPERATION_NAME = "convert";
	private static final String CHECK_OPERATION_NAME = "check";
	private static final String RUN_OPERATION_NAME = "run";
	private static final String UPDATE_RESULT_TYPE_NAME = "updateResult";
	private static final String CHANGE_REQUEST_TYPE_NAME = "changeRequest";
	private static final String LOOKUP_OPERATION_NAME = "lookup";
	private static final String FIND_BY_ID = "findById";
	private static final String FIND_ALL = "findAll";
	private static final String LOOKUP_RESOURCES_VIA_FILTER_OPNAME = "lookupResourcesViaFilter";
	private static final String LOOKUP_RESOURCE_BY_ID_OPNAME = "lookupResourceById";

	private static final String DATA_IDENTIFIER = "data";
	private static final String RESULT_IDENTIFIER = "result";
	private static final String IN_DTO = "inDTO";
	private static final String IN_PARAMETER_IDENTIFIER = "in";
	private static final String OUT_PARAMETER_IDENTIFIER = "out";
	private static final String ITEM_ID = "itemId";
	private static final String RESOURCE_ID = "resourceId";
	private static final String COLLECTION_ID = "collectionId";
	private static final String COLLECTION_ITEM = "collectionItem";
	private static final String RESULT_SET = "resultSet";
	static final String QUERY_FILTER = "queryFilter";
	private static final String SUCCESS_FLAG = "success";
	private static final String EMPTY_PAYLOAD = "emptyPayload";

	// ** MAP decorator related (multiple operations)

	public static void addProcessingResourceOperations(EndpointContract ec) {
		ServiceSpecification ss = (ServiceSpecification) ec.eContainer();

		String contractName = ec.getName();
		TypeReference tref = createDataTypeReferenceWithAtomicParameter(ss, contractName);
		tref.setName(IN_DTO); 
		Operation initOperation = createCreateOperation("initializeResource", tref);
		ec.getOps().add(initOperation);

		TypeReference trefSet = EcoreUtil.copy(tref);
		setCardinalityOfTypeReferenceToZeroOrMore(trefSet);
		Operation queryOperation = createReadOperation("getResourceState", RESOURCE_ID, trefSet);
		ec.getOps().add(queryOperation);

		Operation updateOperation = createUpdateOperation("updateResourceState", EcoreUtil.copy(tref));
		ec.getOps().add(updateOperation);

		Operation deleteOperation = createDeleteOperation("deleteResourceState");
		ec.getOps().add(deleteOperation);
	}

	public static void addInformationHolderOperations(EndpointContract ec) {
		// TODO (L) could check existing operations in ec regarding their names (and responsibilities)

		ServiceSpecification ss = (ServiceSpecification) ec.eContainer();
		String objectName = ec.getName();

		TypeReference tref = createDataTypeReferenceWithAtomicParameter(ss, objectName);
		tref.setName("responseDTO");
		TypeReference trefSet = EcoreUtil.copy(tref);
		// setCardinalityOfTypeReferenceToZeroOrMore(trefSet);

		Operation findAllOperation = createRetrievalOperation(FIND_ALL, trefSet);
		MAPDecoratorHelpers.addRetrievalOperationResponsibility(findAllOperation, FIND_ALL);
		ec.getOps().add(findAllOperation);

		// TODO findbyId is the same as getItem (for collections), so might want to rename for consistency

		Operation findByIdOperation = TransformationHelpers.createOperationWithIDParameterRequestAndTypeReferenceResponse(FIND_BY_ID, RESOURCE_ID, tref);
		MAPDecoratorHelpers.addRetrievalOperationResponsibility(findByIdOperation, FIND_BY_ID);
		ec.getOps().add(findByIdOperation);
	}

	public static void addCollectionOperations(EndpointContract ec, boolean createCollectionMutators) {
		ServiceSpecification ss = (ServiceSpecification) ec.eContainer();
		String objectName = ec.getName();
		DataContract dt = DataTypeTransformations.findOrCreateDataType(ss, objectName, null);
		TypeReference tref = DataTypeTransformations.createTypeReference(dt);
		tref.setName(COLLECTION_ITEM);
		EList<Operation> endpointOperations = ec.getOps();
		if (createCollectionMutators) {
			Operation createCollectionOperation = CollectionHelpers.createCreateCollectionOperation(tref);
			endpointOperations.add(createCollectionOperation);
		}

		// TODO (M) future work: replacement of entire collection? can be modeled as delete/create operation sequence now
		endpointOperations.add(CollectionHelpers.createAddItemOperation(tref));
		endpointOperations.add(CollectionHelpers.createItemAtOperation(tref));
		endpointOperations.add(CollectionHelpers.createUpdateItemOperation(tref));
		endpointOperations.add(CollectionHelpers.createRemoveItemOperation());

		// TODO (M) future work: improve existing QFs or create new one to split by relative URI (including {id})

		if (createCollectionMutators) {
			// could also provide findAllCollections, findCollectionById/getCollection operations
			endpointOperations.add(CollectionHelpers.createDeleteCollectionOperation());
		}
	}

	static void addDataTransferResourceOperations(EndpointContract ec) {
		Operation putOperation = TransformationHelpers.createOperationWithAtomicRequestAndResponse("transferIn", DATA_ROLE, RAW_TYPE, true, null);
		putOperation.getResponseMessage().getPayload().getNp().getAtomP().getRat().setRole(ID_ROLE);
		TransformationHelpers.setAtomicParameterNames(putOperation, DATA_IDENTIFIER, RESOURCE_ID);
		MAPDecoratorHelpers.addStateCreationResponsibility(putOperation, "put");
		ec.getOps().add(putOperation);

		Operation getOperation = TransformationHelpers.createOperationWithAtomicRequestAndResponse("transferOut", ID_ROLE, null, true, RAW_TYPE);
		TransformationHelpers.setAtomicParameterNames(getOperation, RESOURCE_ID, DATA_IDENTIFIER);
		MAPDecoratorHelpers.addRetrievalOperationResponsibility(getOperation, MAPDecoratorHelpers.RETRIEVAL_OPERATION);
		ec.getOps().add(getOperation);
		
		// TODO (M) could offer a PATCHy method to update ongoing transfer (expects a resource id and data, returns success flag)
		// which would be stereotyped with an already existing decorator via addStateReplacementResponsibility()
		// TODO (M) could also provide a delete operation
	}

	static void addLinkLookupOperations(EndpointContract ec) {
		Operation getOperation1 = TransformationHelpers.createOperationWithAtomicParameters(LOOKUP_RESOURCE_BY_ID_OPNAME, ID_ROLE, null, true, ID_ROLE, null);
		getOperation1.getResponseMessage().getPayload().getNp().getAtomP().getRat().setRole("L");
		TransformationHelpers.setAtomicParameterNames(getOperation1, RESOURCE_ID, URI_IDENTIFIER);
		MAPDecoratorHelpers.addStateCreationResponsibility(getOperation1, MAPDecoratorHelpers.STATE_CREATION_OPERATION);
		ec.getOps().add(getOperation1);

		Operation getOperation2 = TransformationHelpers.createOperationWithAtomicParameters(LOOKUP_RESOURCES_VIA_FILTER_OPNAME, METADATA_ROLE, null, true, ID_ROLE, null);
		getOperation2.getResponseMessage().getPayload().getNp().getAtomP().getRat().setRole("L");
		TransformationHelpers.setAtomicParameterNames(getOperation2, "criteria", "uriSet");
		Cardinality card = ApiDescriptionFactory.eINSTANCE.createCardinality();
		card.setZeroOrMore("*"); // value does not matter, but has to be set
		getOperation2.getResponseMessage().getPayload().getNp().getAtomP().setCard(card);
		MAPDecoratorHelpers.addRetrievalOperationResponsibility(getOperation2, MAPDecoratorHelpers.RETRIEVAL_OPERATION);
		ec.getOps().add(getOperation2);
	}

	// ** MAP decorator related (single operations)

	static void addRunOperation(EndpointContract ec) {
		Operation runOperation = createComputationOperation(RUN_OPERATION_NAME, IN_PARAMETER_IDENTIFIER, OUT_PARAMETER_IDENTIFIER);
		MAPDecoratorHelpers.addComputationFunctionResponsibility(runOperation, MAPDecoratorHelpers.COMPUTATION_FUNCTION);
		ec.getOps().add(runOperation);
	}

	static void addCheckOperation(EndpointContract ec) {
		Operation checkOperation = createValidationOperation(CHECK_OPERATION_NAME, DATA_IDENTIFIER, RESULT_IDENTIFIER);
		MAPDecoratorHelpers.addComputationFunctionResponsibility(checkOperation, MAPDecoratorHelpers.COMPUTATION_FUNCTION);
		ec.getOps().add(checkOperation);
	}

	static void addTransformationOperation(EndpointContract ec) {
		Operation checkOperation = createTransformationOperation(CONVERT_OPERATION_NAME, DATA_IDENTIFIER, RESULT_IDENTIFIER);
		MAPDecoratorHelpers.addComputationFunctionResponsibility(checkOperation, MAPDecoratorHelpers.COMPUTATION_FUNCTION);
		ec.getOps().add(checkOperation);
	}

	static Operation createEventProductionOperation(String name) {
		return createDecoratedOperationWithGenericParameters(name, MAPDecoratorHelpers.EVENT_PROCESSOR);
	}

	static Operation createStateManipulatingOperation(String name) {
		return createDecoratedOperationWithGenericParameters(name, MAPDecoratorHelpers.STATE_TRANSITION_OPERATION);
	}

	// ** CRUD operation related

	public static Operation createCreateOperation(String opName, TypeReference typeRef) {
		Operation cop = createOperationWithTypeReferenceRequestAndAtomicDataResponse(opName, typeRef);
		MAPDecoratorHelpers.addStateCreationResponsibility(cop, MAPDecoratorHelpers.STATE_CREATION_OPERATION);
		return cop;
	}

	private static Operation createOperationWithTypeReferenceRequestAndAtomicDataResponse(String opName, TypeReference typeRef) {
		AtomicParameter outP = DataTypeTransformations.createIDParameter(RESOURCE_ID);
		Operation result = TransformationHelpers.createOperationWithAtomicParameterResponse(opName, outP);
		DataTransferRepresentation dtrDto = DataTypeTransformations.createDTRFromTypeReference(typeRef);
		result.setRequestMessage(dtrDto);
		return result;
	}

	public static Operation createUpdateOperation(String opName, TypeReference tref) {
		Operation uop = createOperationWithTypeReferenceRequestAndTypeReferenceResponse(opName, tref);
		setResponsibilityOf(uop, MAPDecoratorHelpers.STATE_TRANSITION_OPERATION);
		return uop;
	}

	private static Operation createOperationWithTypeReferenceRequestAndTypeReferenceResponse(String opName, TypeReference tref) {
		Operation result = ApiDescriptionFactory.eINSTANCE.createOperation();
		result.setName(opName);
		DataTransferRepresentation inDtr = DataTypeTransformations.createDTRFromTypeReference(tref);
		result.setRequestMessage(inDtr);
		tref.setName(CHANGE_REQUEST_TYPE_NAME);

		DataTransferRepresentation outDtr = EcoreUtil.copy(inDtr);
		outDtr.getPayload().getNp().getTr().setName(UPDATE_RESULT_TYPE_NAME);
		result.setResponseMessage(outDtr);

		return result;
	}

	public static Operation createDeleteOperation(String opName) {
		Operation dop = TransformationHelpers.createOperationWithAtomicRequestAndResponse(opName, ID_ROLE, STRING_TYPE, true, BOOL_TYPE);
		TransformationHelpers.setAtomicParameterNames(dop, RESOURCE_ID, SUCCESS_FLAG);
		dop.getResponseMessage().getPayload().getNp().getAtomP().getRat().setRole(METADATA_ROLE);
		setResponsibilityOf(dop, MAPDecoratorHelpers.STATE_DELETION_OPERATION);
		return dop;
	}

	public static Operation createRetrievalOperation(String opName, TypeReference tref) {
		Operation rop = createReadOperation(opName, QUERY_FILTER, tref);
		AtomicParameter requestParameter = rop.getRequestMessage().getPayload().getNp().getAtomP();
		requestParameter.getRat().setRole(METADATA_ROLE);
		requestParameter.getRat().setBtype(STRING_TYPE);
		
		MessageTransformations.addParameterTreeWrapper(requestParameter);
		rop.getRequestMessage().getPayload().getPt().setName("query");
		rop.getRequestMessage().getPayload().getPt().setCard(null);
		Cardinality qcard = ApiDescriptionFactory.eINSTANCE.createCardinality();
		qcard.setZeroOrMore("*"); // value does not matter
		rop.getRequestMessage().getPayload().getPt().getFirst().getPn().getAtomP().setCard(qcard);
		
		TypeReference responseBody = rop.getResponseMessage().getPayload().getNp().getTr();
		MessageTransformations.addParameterTreeWrapper(responseBody);
		rop.getResponseMessage().getPayload().getPt().setName("result");
		Cardinality card = ApiDescriptionFactory.eINSTANCE.createCardinality();
		card.setZeroOrMore("*"); // value does not matter
		rop.getResponseMessage().getPayload().getPt().setCard(card);
		
		return rop;
	}

	// used by addEndpoint, which is used by extractIHR:
	public static Operation createLookupOperation(TypeReference tref) {
		tref.setName(RESULT_SET);
		return createReadOperation(LOOKUP_OPERATION_NAME, RESOURCE_ID, tref);
	}

	public static Operation createReadOperation(String opName, String qpName, TypeReference tref) {
		Operation rop = createOperationWithAtomicDataRequestAndTypeReferenceResponse(opName, qpName, tref);

		if (tref.getCard() != null && (tref.getCard().getAtLeastOne() != null || tref.getCard().getZeroOrMore() != null)) {
			Cardinality card = ApiDescriptionFactory.eINSTANCE.createCardinality();
			card.setAtLeastOne("+"); // value does not matter
			rop.getRequestMessage().getPayload().getNp().getAtomP().setCard(card);
		}

		setResponsibilityOf(rop, MAPDecoratorHelpers.RETRIEVAL_OPERATION);
		return rop;
	}

	static TypeReference findOrCreateTypeReference(ServiceSpecification ss, String name, ElementStructure es) {
		DataContract dt = DataTypeTransformations.findOrCreateDataType(ss, name, es);
		TypeReference tref = DataTypeTransformations.createTypeReference(dt);
		return tref;
	}

	// ** local helpers

	private static Operation createOperationWithAtomicDataRequestAndTypeReferenceResponse(String opName, String inpName, TypeReference tref) {
		AtomicParameter inP = DataTypeTransformations.createAtomicDataParameter(inpName, ID_ROLE, INT_TYPE); // TODO why ID and not D or MD?
		Operation result = TransformationHelpers.createOperationWithAtomicParameterRequest(opName, inP);
		DataTransferRepresentation dtrDto = DataTypeTransformations.createDTRFromTypeReference(tref);
		result.setResponseMessage(dtrDto);
		return result;
	}

	private static Operation createDecoratedOperationWithGenericParameters(String opName, String operationResponsibility) {
		Operation sto = TransformationHelpers.createOperationWithGenericParameters(opName, null, null, true); // no parameter names (yet), has response
		setResponsibilityOf(sto, operationResponsibility);
		return sto;
	}

	private static void setResponsibilityOf(Operation sto, String operationResponsibility) throws MDSLException {
		OperationResponsibility ov = MAPDecoratorHelpers.setPrimaryResponsibility(operationResponsibility);
		sto.setResponsibility(ov);
	}

	private static Operation createComputationOperation(String opName, String inName, String outName) {
		Operation operation = TransformationHelpers.createOperationWithAtomicParameters(
				opName, DATA_ROLE, INT_TYPE, true, DATA_ROLE, LONG_TYPE);
		TransformationHelpers.setAtomicParameterNames(operation, inName, outName);
		return operation;
	}

	private static Operation createTransformationOperation(String opName, String inName, String outName) {
		Operation operation = TransformationHelpers.createOperationWithAtomicParameters(
				opName, DATA_ROLE, null, true, DATA_ROLE, null);
		TransformationHelpers.setAtomicParameterNames(operation, inName, outName);
		return operation;
	}

	private static Operation createValidationOperation(String opName, String inName, String outName) {
		Operation operation = TransformationHelpers.createOperationWithAtomicParameters(
				opName, DATA_ROLE, STRING_TYPE, true, DATA_ROLE, BOOL_TYPE);
		TransformationHelpers.setAtomicParameterNames(operation, inName, outName);
		return operation;
	}

	private static TypeReference createDataTypeReferenceWithAtomicParameter(ServiceSpecification ss, String name) {
		AtomicParameter newAP = DataTypeTransformations.createAtomicDataParameter(DataTypeTransformationHelpers.decapitalizeName(name), null); // was: STRING_TYPE 
		ElementStructure es = DataTypeTransformations.wrapAtomicParameterAsElementStructure(newAP);
		DataContract dt = DataTypeTransformations.findOrCreateDataType(ss, name, es);
		TypeReference tref = DataTypeTransformations.createTypeReference(dt);
		return tref;
	}

	private static void setCardinalityOfTypeReferenceToZeroOrMore(TypeReference trefSet) {
		Cardinality card = ApiDescriptionFactory.eINSTANCE.createCardinality();
		card.setZeroOrMore("*"); // value does not matter
		trefSet.setCard(card);
	}

	static class CollectionHelpers {
		private static final String NEW_ITEM_IDENTIFIER = "newItem";

		public static Operation createCreateCollectionOperation(TypeReference tref) {
			// no real request body, but returning "expecting payload "emptyPayload":MD<void>" (which causes OASgen to generate empty JSON body)
			Operation op = TransformationHelpers.createOperationWithAtomicParameters("createCollection", DATA_ROLE, VOID_TYPE, true, ID_ROLE, INT_TYPE); // no parameter names (yet), has response
			setResponsibilityOf(op, MAPDecoratorHelpers.STATE_CREATION_OPERATION);
			TransformationHelpers.setAtomicParameterNames(op, EMPTY_PAYLOAD, COLLECTION_ID);
			return op;
		}

		public static Operation createAddItemOperation(TypeReference tref) {
			// could call operation "append"? create index stuff ("At") if MAP decorator says so?
			// Operation op = createDecoratedOperationWithGenericParameters("addItem", MAPDecoratorHelpers.STATE_TRANSITION_OPERATION);

			// receive ID<int>
			AtomicParameter idp = DataTypeTransformations.createIDParameter(ITEM_ID);
			Operation op = TransformationHelpers.createOperationWithAtomicParameterResponse("addItem", idp);
			setResponsibilityOf(op, MAPDecoratorHelpers.STATE_TRANSITION_OPERATION);

			// send tuple: expecting payload {"collectionId":ID<int>, "newItem":DTO}
			ParameterTree inTuple = ApiDescriptionFactory.eINSTANCE.createParameterTree();
			AtomicParameter collectionId = DataTypeTransformations.createIDParameter(COLLECTION_ID);
			inTuple.setFirst(DataTypeTransformations.wrapAtomicParameterAsTreeNode(collectionId));
			TreeNode itemDTO = DataTypeTransformations.wrapTypeReferenceAsTreeNode(EcoreUtil.copy(tref));
			itemDTO.getPn().getTr().setName(NEW_ITEM_IDENTIFIER);
			inTuple.getNexttn().add(itemDTO);
			ElementStructure es1 = DataTypeTransformations.wrapParameterTreeAsElementStructure(inTuple);
			createAndSetRequestPayload(op, es1);
			return op;
		}

		// there is a difference between the index (position) and the itemId (key), removeItemAt(position) is different from removeItem(key)!
		// [D] is "IndexedCollection" or "OrderedCollection" missing in EP21 paper?
		// TODO (L) could add index-oriented CRUD methods addItemAt, getItemAt, upadteItemAt, removeItemAt (with a position parameter)

		public static Operation createItemAtOperation(TypeReference tref) {
			Operation op = ApiDescriptionFactory.eINSTANCE.createOperation();
			String opName = DataTypeTransformationHelpers.decapitalizeName("getItem");
			op.setName(opName);
			setResponsibilityOf(op, MAPDecoratorHelpers.RETRIEVAL_OPERATION);

			// send tuple: expecting payload {"collectionId":ID<int>, "itemId":ID<int>}
			ElementStructure es1 = createCollectionItemIdTuple();
			createAndSetRequestPayload(op, es1);

			// return found DTO
			DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			outDtr.setPayload(DataTypeTransformations.wrapTypeReferenceAsElementStructure(EcoreUtil.copy(tref)));
			op.setResponseMessage(outDtr);

			return op;
		}

		public static Operation createUpdateItemOperation(TypeReference tref) {
			Operation op = ApiDescriptionFactory.eINSTANCE.createOperation();
			String opName = DataTypeTransformationHelpers.decapitalizeName("updateItem");
			op.setName(opName);
			setResponsibilityOf(op, MAPDecoratorHelpers.STATE_TRANSITION_OPERATION);

			// TODO simplify (see other operator creators further up): in: DTO, out: DTO
			// send tuple: expecting payload {"collectionId":ID<int>, "itemId":ID<int>, "newItem":DTO}
			ElementStructure es1 = createCollectionItemIdTuple();
			TreeNode itemDTO = DataTypeTransformations.wrapTypeReferenceAsTreeNode(EcoreUtil.copy(tref));
			itemDTO.getPn().getTr().setName(NEW_ITEM_IDENTIFIER);
			es1.getPt().getNexttn().add(itemDTO);
			createAndSetRequestPayload(op, es1);

			// could also return updated DTO
			createAndSetSuccessFlagAsResponsePayload(op);
			return op;
		}

		public static Operation createRemoveItemOperation() {
			Operation op = ApiDescriptionFactory.eINSTANCE.createOperation();
			String opName = DataTypeTransformationHelpers.decapitalizeName("removeItem");
			op.setName(opName);
			setResponsibilityOf(op, MAPDecoratorHelpers.STATE_DELETION_OPERATION);

			// send tuple: expecting payload {"collectionId":ID<int>, "itemId":ID<int>}
			ElementStructure es1 = createCollectionItemIdTuple();
			createAndSetRequestPayload(op, es1);

			createAndSetSuccessFlagAsResponsePayload(op);
			return op;
		}

		public static Operation createDeleteCollectionOperation() {
			Operation op = ApiDescriptionFactory.eINSTANCE.createOperation();
			String opName = DataTypeTransformationHelpers.decapitalizeName("deleteCollection");
			op.setName(opName);
			setResponsibilityOf(op, MAPDecoratorHelpers.STATE_DELETION_OPERATION);

			// TODO (L) could call helper instead
			AtomicParameter collectionId = DataTypeTransformations.createIDParameter(COLLECTION_ID);
			ElementStructure es1 = DataTypeTransformations.wrapAtomicParameterAsElementStructure(collectionId);
			createAndSetRequestPayload(op, es1);

			createAndSetSuccessFlagAsResponsePayload(op);
			return op;
		}

		// ** helpers

		private static ElementStructure createCollectionItemIdTuple() {
			ParameterTree inTuple = ApiDescriptionFactory.eINSTANCE.createParameterTree();
			AtomicParameter collectionId = DataTypeTransformations.createIDParameter(COLLECTION_ID);
			inTuple.setFirst(DataTypeTransformations.wrapAtomicParameterAsTreeNode(collectionId));
			AtomicParameter itemId = DataTypeTransformations.createIDParameter(ITEM_ID);
			inTuple.getNexttn().add(DataTypeTransformations.wrapAtomicParameterAsTreeNode(itemId));
			ElementStructure es1 = DataTypeTransformations.wrapParameterTreeAsElementStructure(inTuple);
			return es1;
		}

		private static void createAndSetRequestPayload(Operation op, ElementStructure es) {
			DataTransferRepresentation dtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			dtr.setPayload(es);
			op.setRequestMessage(dtr);
		}

		private static void createAndSetSuccessFlagAsResponsePayload(Operation op) {
			ElementStructure es = DataTypeTransformations.wrapAtomicParameterAsElementStructure(DataTypeTransformations.createMetadataParameter(SUCCESS_FLAG, BOOL_TYPE));
			DataTransferRepresentation dtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			dtr.setPayload(es);
			op.setResponseMessage(dtr);
		}
	}
}

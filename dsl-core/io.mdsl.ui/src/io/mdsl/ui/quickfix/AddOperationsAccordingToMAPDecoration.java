package io.mdsl.ui.quickfix;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;

public class AddOperationsAccordingToMAPDecoration implements ISemanticModification {
	
	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		EndpointContract ec = (EndpointContract) element;
		
		EList<String> otherRoles = ec.getOtherRoles();
		
		if(ec.getPrimaryRole().equals("PROCESSING_RESOURCE") || alsoServesAs(otherRoles, "PROCESSING_RESOURCE")) {
			addPROperations(ec);
		}
		else if (ec.getPrimaryRole().equals("STATEFUL_PROCESSING_RESOURCE") || alsoServesAs(otherRoles, "STATEFUL_PROCESSING_RESOURCE")) {
			addPROperations(ec);
		}
		
		if(ec.getPrimaryRole().equals("INFORMATION_HOLDER_RESOURCE") || alsoServesAs(otherRoles, "INFORMATION_HOLDER_RESOURCE") ) {
			// TODO add collection fragments here (EP21) 
			addIHROperations(ec);
		}
		else if(ec.getPrimaryRole().equals("MASTER_DATA_HOLDER") || alsoServesAs(otherRoles, "MASTER_DATA_HOLDER") ) {
			addIHROperations(ec); // TODO be more specific?
		}
		else if(ec.getPrimaryRole().equals("OPERATIONAL_DATA_HOLDER") || alsoServesAs(otherRoles, "OPERATIONAL_DATA_HOLDER") ) {
			addIHROperations(ec); // TODO 
		}
		else if(ec.getPrimaryRole().equals("REFERENCE_DATA_HOLDER") || alsoServesAs(otherRoles, "REFERENCE_DATA_HOLDER") ) {
			addIHROperations(ec); // TODO 
		}
		
		if(ec.getPrimaryRole().equals("DATA_TRANSFER_RESOURCE") || alsoServesAs(otherRoles, "DATA_TRANSFER_RESOURCE")) {
			addDTROperations(ec);
		}
		else if(ec.getPrimaryRole().equals("LINK_LOOKUP_RESOURCE") || alsoServesAs(otherRoles, "LINK_LOOKUP_RESOURCE")) {
			addLLROperations(ec);
		}
		else if(ec.getPrimaryRole().equals("STATELESS_PROCESSING_RESOURCE") || alsoServesAs(otherRoles, "STATELESS_PROCESSING_RESOURCE")) {
			// could be an "other" string, using this to add CF op somewhere
			addRunOperation(ec);
		}
		else if(ec.getPrimaryRole().equals("VALIDATION_RESOURCE") || alsoServesAs(otherRoles, "VALIDATION_RESOURCE")) {
			// could be an "other" string, using this to add CF op somewhere
			addCheckOperation(ec);
		}
		else if(ec.getPrimaryRole().equals("TRANSFORMATION_RESOURCE") || alsoServesAs(otherRoles, "TRANSFORMATION_RESOURCE")) {
			// could be an "other" string, using this to add CF op somewhere
			addTransformationOperation(ec);
		}
		else {
			System.out.println("[W] Unknown role(s), not adding any operations.");
		}
	}

	private boolean alsoServesAs(EList<String> otherDecorators, String roleName) {
		for(String role : otherDecorators) {
			if(role.equals(roleName)) {
				return true;
			}
		}
		return false;
	}
	
	// TODO could introduce a secondary role/variant COLLECTION_RESOURCE
	// and create add, remove, search (with proper information for HTTP resource bindings)

	private void addPROperations(EndpointContract ec) {
		// TODO (M) use helpers for story to operation now (or common subset)
		Operation initOperation = createOperationWithParameterNames("initializeResource", "dto", "resourceId");
		addStateCreationResponsibility(initOperation, "create");
		ec.getOps().add(initOperation);
		
		Operation queryOperation = createOperationWithParameterNames("getResourceState", "resourceId", "dto");
		addRetrievalResponsibility(queryOperation, "read");
		ec.getOps().add(queryOperation);
		
		Operation updateOperation = createOperationWithParameterNames("updateResourceState", "resourceIdAndDto", "acknowledgment");
		addStateTransferResponsibility(updateOperation, "update");
		ec.getOps().add(updateOperation);
		
		Operation delete = createOperationWithParameterNames("deleteResourceState", "resourceId", "acknowledgment");
		addDeletionResponsibility(delete, "delete");
		ec.getOps().add(delete);
	}
	
	private void addRunOperation(EndpointContract ec) {
		Operation runOperation = createComputationOperation("run", "in", "out");
		addCFResponsibility(runOperation, "run");
		ec.getOps().add(runOperation);
	}
	
	private void addCheckOperation(EndpointContract ec) {
		Operation checkOperation = createValidationOperation("check", "data", "result");
		addCFResponsibility(checkOperation, "check");
		ec.getOps().add(checkOperation);
	}
	
	private void addTransformationOperation(EndpointContract ec) {
		Operation checkOperation = createValidationOperation("convert", "data", "result");
		addCFResponsibility(checkOperation, "convert");
		ec.getOps().add(checkOperation);
	}

	private void addDTROperations(EndpointContract ec) {
		Operation putOperation = createOperationWithParameterNames("transferIn", "data", "resourceId");
		addStateCreationResponsibility(putOperation, "put");
		ec.getOps().add(putOperation);
		
		Operation getOperation = createOperationWithParameterNames("transferOut", "resourceId", "data");
		addRetrievalResponsibility(getOperation, "get");
		ec.getOps().add(getOperation);
	}
	
	private void addLLROperations(EndpointContract ec) {
		Operation getOperation1 = createOperationWithParameterNames("lookupById", "resourceId", "uri");
		addStateCreationResponsibility(getOperation1, "get");
		ec.getOps().add(getOperation1);
		
		Operation getOperation = createOperationWithParameterNames("lookupByFilter", "criteria", "uriSet");
		addRetrievalResponsibility(getOperation, "get");
		ec.getOps().add(getOperation);
	}

	private void addIHROperations(EndpointContract ec) {
		// TODO (L) could check existing operations in ec regarding their names (and responsibilities?)
		
		Operation findAllOperation = createOperationWithParameterNames("findAll", "queryParameters", "queryResults");
		addRetrievalResponsibility(findAllOperation, "searchAll");
		ec.getOps().add(findAllOperation);
		
		// TODO [R] could/should have even better operation message signatures here, see MAP overview tables 201x
		// RO: query in, paginated result out, etc.
		// TODO (M) use helpers for story to operation now (or common subset)
		
		Operation queryOperation = createOperationWithParameterNames("findById", "id", "dto");
		addRetrievalResponsibility(queryOperation, "getDetails");
		ec.getOps().add(queryOperation);
	}
	
	// caution: the following helpers overwrite an already existing responsibility
	
	private OperationResponsibility addStateCreationResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSco(details); // string not used at all?
		operation.setResponsibility(or);
		return or;
	}
	
	private OperationResponsibility addStateTransferResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSto(details); // string not used at all?
		operation.setResponsibility(or);
		return or;
	}
	
	// not used yet:
	private OperationResponsibility addStateReplacemenentResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSro(details); // string not used at all?
		operation.setResponsibility(or);
		return or;
	}
	
	private OperationResponsibility addDeletionResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSdo(details); // string not used at all?
		operation.setResponsibility(or);
		return or;
	}

	private OperationResponsibility addRetrievalResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setRo(details); // string not used at all?
		operation.setResponsibility(or);
		return or;
	}
	
	private OperationResponsibility addCFResponsibility(Operation operation, String details) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setCf(details); // string not used at all?
		operation.setResponsibility(or);
		return or;
	}

	private Operation createOperationWithParameterNames(String opName, String inName, String outName) {
		Operation operation = TransformationHelpers.createOperationWithGenericParameters(opName, true);
		// this actually is an inlined, replayed version of another QF (should be a method param!):
		operation.getRequestMessage().getPayload().getNp().getGenP().setName(inName);
		operation.getResponseMessage().getPayload().getNp().getGenP().setName(outName);
		return operation;
	}
	
	private Operation createComputationOperation(String opName, String inName, String outName) {
		Operation operation = createOperationWithParameterNames(opName, inName, outName);
		
		AtomicParameter inAP = DataTypeTransformationHelpers.createAtomicDataParameter(inName, "string");		
		changeNodeToAtomicParameter(operation.getRequestMessage().getPayload().getNp(), inAP);
		
		AtomicParameter outAP = DataTypeTransformationHelpers.createAtomicDataParameter(outName, "int");
		changeNodeToAtomicParameter(operation.getResponseMessage().getPayload().getNp(), outAP);
		
		return operation;
	}
	
	private Operation createValidationOperation(String opName, String inName, String outName) {
		Operation operation = createOperationWithParameterNames(opName, inName, outName);
		
		AtomicParameter inAP = DataTypeTransformationHelpers.createAtomicDataParameter(inName, "string");		
		changeNodeToAtomicParameter(operation.getRequestMessage().getPayload().getNp(), inAP);
		
		AtomicParameter outAP = DataTypeTransformationHelpers.createAtomicDataParameter(outName, "bool");
		changeNodeToAtomicParameter(operation.getResponseMessage().getPayload().getNp(), outAP);
		
		return operation;
	}

	private void changeNodeToAtomicParameter(SingleParameterNode node, AtomicParameter outAP) {
		node.setGenP(null); // needed due to '|' in grammar
		node.setAtomP(outAP);
	}
}

package io.mdsl.validation;

import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.transformations.TransformationHelpers;

public class OperationValidator extends AbstractMDSLValidator {
	
	public final static String OPERATION_FOUND = "OPERATION_FOUND";
	
	public final static String NO_ERROR_REPORT = "NO_ERROR_REPORT";
	public final static String NO_SECURITY_POLICY = "NO_SECURITY_POLICY";
	public final static String NO_COMPENSATION = "NO_COMPENSATION";
	
	public final static String MAP_EXTRACT_IHR_POSSIBLE = "MAP_EXTRACT_IHR_POSSIBLE";
	public static final String LINKED_INFORMATION_HOLDER_FOUND_IN_REQUEST = "LINKED_INFORMATION_HOLDER_FOUND_IN_REQUEST"; 
	public static final String LINKED_INFORMATION_HOLDER_FOUND = "LINKED_INFORMATION_HOLDER_FOUND"; 
	public static final String EMBEDDED_ENTITY_DECORATOR = "Embedded_Entity"; 
	public static final String LINKED_INFORMATION_HOLDER_DECORATOR = "Linked_Information_Holder"; 
	public static final String EMBEDDED_ENTITY_FOUND_IN_REQUEST = "EMBEDDED_ENTITY_FOUND_IN_REQUEST"; 
	public static final String EMBEDDED_ENTITY_FOUND = "EMBEDDED_ENTITY_FOUND"; 
	
	private static final String PAGINATION_DECORATOR = "Pagination";
	public final static String MAP_PAGINATION_POSSIBLE = "MAP_PAGINATION_POSSIBLE";
	public final static String MAP_WISH_LIST_POSSIBLE = "MAP_WISH_LIST_POSSIBLE";
	public final static String MAP_WISH_LIST_DECORATED = "MAP_WISH_LIST_DECORATED";
	public final static String MAP_WISH_TEMPLATE_POSSIBLE = "MAP_WISH_TEMPLATE_POSSIBLE";
	public final static String MAP_REQUEST_BUNDLE_POSSIBLE = "MAP_REQUEST_BUNDLE_POSSIBLE";
	public final static String MAP_RESPONSE_BUNDLE_POSSIBLE = "MAP_RESPONSE_BUNDLE_POSSIBLE";
	public static final String REQUEST_IS_PARAMETER_TREE_WITH_OPTIONAL_NODES = "MAP_REQUEST_BUNDLE_POSSIBLE";

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	@Check
	public void lookForOperationsToBeMoved(Operation operation) {
		EndpointContract ec = ((EndpointContract) operation.eContainer());
		info(operation.getName() + " located in endpoint type " + ec.getName(), operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), OPERATION_FOUND);
	}

	@Check
	public void validateMessagePresenceForExchangePattern(Operation nextOp) {
				
		// note: it is valid to model operation w/o any message, for early stage (no MEP to be defined)

		String mep = nextOp.getMep();
		DataTransferRepresentation inDtr = nextOp.getRequestMessage();
		DataTransferRepresentation outDtr = nextOp.getResponseMessage();
		
		if(mep.equals("REQUEST_REPLY")) {
			if(inDtr == null) {
				error(nextOp.getName() + " is a REQUEST_REPLY operation, which expects a request message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
			if(outDtr == null) {
				error(nextOp.getName() + " is a REQUEST_REPLY operation, which must deliver a response message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
		}
		else if(mep.equals("ONE_WAY")) {
			if(inDtr == null) {
				error(nextOp.getName() + " is a ONE_WAY operation, which expects a request message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
			if(outDtr != null) {
				error(nextOp.getName() + " is a ONE_WAY operation, which should not deliver a response message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
		}
		else if(mep.equals("NOTIFICATION")) {
			if(inDtr != null) {
				error(nextOp.getName() + " is a NOTIFICATION operation, which should not expect a request message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
			if(outDtr == null) {
				error(nextOp.getName() + " is a NOTIFICATION operation, which must deliver a response message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
		}
		// TODO what about "other" STRING (warning/info)? how to distinguish from unset MEP? 
	}
	
	@Check
	public void lookForStatusReports(Operation operation) {
		if(operation.getReports()==null)
			info(operation.getName() + " does not define any reports to be returned", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), NO_ERROR_REPORT);
	}
	
	@Check
	public void lookForSecurityPolicy(Operation operation) {
		if(operation.getPolicies()==null)
			info(operation.getName() + " does not define any security policies to be enforced", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), NO_SECURITY_POLICY);
	}
	
	@Check
	public void lookForCompensation(Operation operation) {
		if(operation.getUndo()==null)
			info(operation.getName() + " does not define any compensating action", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), NO_COMPENSATION);
	}
	
	@Check
	public void lookForWishListInRequestPayload(Operation operation) {
		if(operation.getRequestMessage()!=null&&operation.getRequestMessage().getPayload()!=null) {
			ParameterTree pt = operation.getRequestMessage().getPayload().getPt();
			if(pt!=null) {
				if(TransformationHelpers.findStereotypeInTree(pt, "Wish_List")==null)
					info(operation.getName() + " does not contain a Wish List yet", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), MAP_WISH_LIST_POSSIBLE);
			}
			else {
				SingleParameterNode spn = operation.getRequestMessage().getPayload().getNp();
				if(spn==null) { 
					return;
				}
				else {
					if(spn.getAtomP()!=null||spn.getTr()!=null) {
						info(operation.getName() + " does not contain a Wish List yet", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), MAP_WISH_LIST_POSSIBLE);
					}
				}
			}
			// TODO could check that all PT elements are optional `?` and offer another QF if not 
		}	
	}
	
	/*
	@Check
	public void lookForParameterTreeWithOptionalTopLevelFieldsInRequestPayload(Operation operation) {
		if(operation.getRequestMessage().getPayload().getPt()!=null) {
			// TODO (future work) complete implementation: check that all top-level tree nodes are optional; request vs. response
			info(operation.getName() + " expects a top-level parameter tree", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), REQUEST_IS_PARAMETER_TREE_WITH_OPTIONAL_NODES);
		}
	}
	*/
	
	@Check
	public void lookForParameterTreeInRequestPayload(Operation operation) {
		if(operation.getRequestMessage().getPayload().getPt()!=null) {
			info(operation.getName() + " expects a top-level parameter tree", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), MAP_REQUEST_BUNDLE_POSSIBLE);
		}
	}
	
	@Check
	public void lookForParameterTreeInResponsePayload(Operation operation) {
		if(operation.getResponseMessage().getPayload().getPt()!=null) {
			info(operation.getName() + " can be bundled", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), MAP_RESPONSE_BUNDLE_POSSIBLE);
		}
	}
	
	// now offering two validators, one for Pagination and one for Response Bundle 
	
	@Check
	public void lookForPaginationOpportunity(Operation operation) {
		// TODO (L) could also check the operation responsibility (should be a retrieval operation)
		ParameterTree pt = operation.getRequestMessage().getPayload().getPt();
		if(pt!=null) {
			if(TransformationHelpers.findStereotypeInTree(pt, PAGINATION_DECORATOR)==null) {
				if(operation.getResponseMessage().getPayload().getPt()!=null) {
					info(operation.getName() + " can be paginated", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), MAP_PAGINATION_POSSIBLE);
				}
			}
		}
	}
	
	@Check
	public void lookForLinkedInformationHolder(Operation operation) {
		ParameterTree pt = operation.getRequestMessage().getPayload().getPt();
		if(pt!=null) {
			if(TransformationHelpers.findStereotypeInTree(pt, LINKED_INFORMATION_HOLDER_DECORATOR)!=null)
				info(operation.getName() + " request contains a Linked Information Holder that can be extracted", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), LINKED_INFORMATION_HOLDER_FOUND_IN_REQUEST);
		}
		pt = operation.getResponseMessage().getPayload().getPt();
		if(pt!=null) {
			if(TransformationHelpers.findStereotypeInTree(pt, LINKED_INFORMATION_HOLDER_DECORATOR)!=null)
				info(operation.getName() + " contains a Linked Information Holder that can be extracted", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), LINKED_INFORMATION_HOLDER_FOUND);
		}
	}
	
	@Check
	public void lookForEmbeddedEntity(Operation operation) {
		ParameterTree pt = operation.getRequestMessage().getPayload().getPt();
		if(pt!=null) {
			if(TransformationHelpers.findStereotypeInTree(pt, EMBEDDED_ENTITY_DECORATOR)!=null)
				info(operation.getName() + " request contains an Embedded Entity that can be extracted", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), EMBEDDED_ENTITY_FOUND_IN_REQUEST);
		}
		pt = operation.getResponseMessage().getPayload().getPt();
		if(pt!=null) {
			if(TransformationHelpers.findStereotypeInTree(pt, EMBEDDED_ENTITY_DECORATOR)!=null)
				info(operation.getName() + " contains an Embedded Entity that can be extracted", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), EMBEDDED_ENTITY_FOUND);
		}
	}
	
	@Check
	public void wishTemplateCanBeAdded(Operation operation) { 
		if(operation.getRequestMessage()==null||operation.getResponseMessage()==null)
			return;
		if(operation.getRequestMessage().getPayload()==null||operation.getResponseMessage().getPayload()==null)
			return;
		if(operation.getRequestMessage().getPayload().getPt()==null) // only a tree allows the template to be added  
			return;
		// TODO could also check that no Wish_Template is already present in request; should check that entire response has optional elements (new QF?)
		info(operation.getName() + " can receive Wish Template", operation, ApiDescriptionPackage.eINSTANCE.getOperation_Name(), MAP_WISH_TEMPLATE_POSSIBLE);
	}
}
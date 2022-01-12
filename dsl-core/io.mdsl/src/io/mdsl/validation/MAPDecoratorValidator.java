package io.mdsl.validation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;

public class MAPDecoratorValidator extends AbstractMDSLValidator {  
	
	private static final String PROCESSING_RESOURCE = "PROCESSING_RESOURCE";
	private static final String INFORMATION_HOLDER_RESOURCE = "INFORMATION_HOLDER_RESOURCE";
	private static final String REFERENCE_DATA_HOLDER = "REFERENCE_DATA_HOLDER";
	private static final String MASTER_DATA_HOLDER = "MASTER_DATA_HOLDER";
	private static final String OPERATIONAL_DATA_HOLDER = "OPERATIONAL_DATA_HOLDER";
	private static final String DATA_TRANSFER_RESOURCE = "DATA_TRANSFER_RESOURCE";
	private static final String LINK_LOOKUP_RESOURCE = "LINK_LOOKUP_RESOURCE";
	private static final String STATELESS_PROCESSING_RESOURCE = "STATELESS_PROCESSING_RESOURCE";

	private static final String RETRIEVAL_OPERATION = "RETRIEVAL_OPERATION";
	private static final String COMPUTATION_FUNCTION = "COMPUTATION_FUNCTION";
	private static final String STATE_CREATION_OPERATION = "STATE_CREATION_OPERATION";
	private static final String STATE_DELETION_OPERATION = "STATE_DELETION_OPERATION";
	private static final String STATE_REPLACEMENT_OPERATION = "STATE_REPLACEMENT_OPERATION";
	private static final String STATE_TRANSITION_OPERATION = "STATE_TRANSITION_OPERATION";

	public final static String MAP_DECORATOR_FOUND = "MAP_DECORATOR_FOUND";
	public final static String MAP_DECORATOR_MISSING = "MAP_DECORATOR_MISSING";
	public static final String CQRS_ELIGIBLE = "CQRS_ELIGIBLE";

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	@Check
	public void checkCQRSEligibility(EndpointContract endpoint) {
		boolean hasReadOperations = false;
		boolean hasOtherOperations  = false;
		EList<Operation> opList = endpoint.getOps();
		
		for (Operation nextOp : opList) {
			if(RETRIEVAL_OPERATION.equals(getResponsibilityPattern(nextOp)))
				hasReadOperations = true;
			else
				hasOtherOperations = true;
		}
		
		if(hasReadOperations && hasOtherOperations)
			info(endpoint.getName() + " can be CQRSed.", endpoint, ApiDescriptionPackage.eINSTANCE.getEndpointContract_Name(), CQRS_ELIGIBLE);
	}
	
	@Check
	public void checkRoleResponsibilityPatternCombination(EndpointContract nextEndpoint) {
		EList<Operation> opList = nextEndpoint.getOps();
		String role1 = nextEndpoint.getPrimaryRole();

		if (role1 == null || role1.equals("")) {
			info(nextEndpoint.getName() + " has no responsibility.", nextEndpoint, ApiDescriptionPackage.eINSTANCE.getEndpointContract_Name(), MAP_DECORATOR_MISSING); // Literals.ENDPOINT_CONTRACT__NAME);
		}
		else
			info(nextEndpoint.getName() + " has a responsibility.", nextEndpoint, ApiDescriptionPackage.eINSTANCE.getEndpointContract_Name(), MAP_DECORATOR_FOUND);
 
		for (int i = 0; i < opList.size(); i++) {
			Operation nextOp = opList.get(i);

			String responsibility = getResponsibilityPattern(nextOp);
			if (responsibility == null || responsibility.equals("")) {
				continue; // nothing to report, really (could suggest to add)
			}
			
			checkOperationResponsibilitySemantics(nextOp, responsibility);
			checkOperationResponsibilityImpactOnState(nextOp, responsibility);
			
			// TODO (M) could also look at secondary roles
						
			// check that endpoint- and operation-level semantics match/make sense
			if (role1.equals(PROCESSING_RESOURCE)) {
				// no need to report positive case
			} else if (role1.equals(INFORMATION_HOLDER_RESOURCE)) {
				checkCommonDataEndpointConstraint(nextEndpoint, nextOp, responsibility);
			} else if (role1.equals(MASTER_DATA_HOLDER) || role1.equals(OPERATIONAL_DATA_HOLDER) || role1.equals(DATA_TRANSFER_RESOURCE)) {
				; // nothing to be checked (yet)
			} else if (role1.equals(REFERENCE_DATA_HOLDER)) {
				checkCommonDataEndpointConstraint(nextEndpoint, nextOp, responsibility);
				if (responsibility.equals(STATE_CREATION_OPERATION)) {
					warning(nextOp.getName() + " creates state, somewhat unexpected in a reference data holder such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if (responsibility.equals(STATE_TRANSITION_OPERATION)) {
					warning(nextOp.getName() + " updates state, somewhat unexpected in a reference data holder such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if(responsibility.equals(STATE_REPLACEMENT_OPERATION)) {
					warning(nextOp.getName() + " replaces state, somewhat unexpected in a stateless processing resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if(responsibility.equals(STATE_DELETION_OPERATION)) {
					warning(nextOp.getName() + " deletes state, somewhat unexpected in a stateless processing resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
			} else if (role1.equals(LINK_LOOKUP_RESOURCE)) {
				checkCommonDataEndpointConstraint(nextEndpoint, nextOp, responsibility);
				if (responsibility.equals(STATE_TRANSITION_OPERATION)) {
					warning(nextOp.getName() + " updates state, somewhat unexpected in a link lookup resource resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if (responsibility.equals(STATE_CREATION_OPERATION)) {
					warning(nextOp.getName() + " creates state, somewhat unexpected in a link lookup resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if(responsibility.equals(STATE_REPLACEMENT_OPERATION)) {
					warning(nextOp.getName() + " replaces state, somewhat unexpected in a link lookup resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if(responsibility.equals(STATE_DELETION_OPERATION)) {
					warning(nextOp.getName() + " deletes state, somewhat unexpected in a link lookup resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
			} else if (role1.equals(STATELESS_PROCESSING_RESOURCE)) {
				checkCommonDataEndpointConstraint(nextEndpoint, nextOp, responsibility);
				if (responsibility.equals(STATE_CREATION_OPERATION)) {
					error(nextOp.getName() + " creates state, which is unexpected in a stateless processing resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if(responsibility.equals(STATE_TRANSITION_OPERATION)) {
					error(nextOp.getName() + " updates state, which is unexpected in a stateless processing resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if(responsibility.equals(STATE_REPLACEMENT_OPERATION)) {
					error(nextOp.getName() + " replaces state, which is unexpected in a stateless processing resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
				if(responsibility.equals(STATE_DELETION_OPERATION)) {
					error(nextOp.getName() + " deletes state, which is unexpected in a stateless processing resource such as " + nextEndpoint.getName(), nextOp,
						ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
				}
			} 
			else {
				info(nextEndpoint.getName() + " has unknown role " + role1, nextEndpoint, ApiDescriptionPackage.eINSTANCE.getEndpointContract_Name()); // Literals.ENDPOINT_CONTRACT__NAME);
			}

			// could validate suitability of message types for MAP responsibility pattern
			// (for instance, an SCO should not return much data); hard to generalize
		}
	}

	private void checkOperationResponsibilitySemantics(Operation op, String responsibility) {
		if((responsibility.equals(COMPUTATION_FUNCTION)||responsibility.equals(RETRIEVAL_OPERATION))&&(op.getResponseMessage()==null||op.getResponseMessage().getPayload()==null)) {
			error(op.getName() + " is specified to be a " + responsibility + " and should therefore deliver a response payload", op,
					ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
		}
	}
	
	private void checkOperationResponsibilityImpactOnState(Operation operation, String responsibility) {
		if(COMPUTATION_FUNCTION.equals(responsibility) && operation.getSt()!=null)
			error(operation.getName() + " is a " + COMPUTATION_FUNCTION + ", and should therefore not change state. Change the MAP decorator or remove the 'transitions' part.", operation,
					ApiDescriptionPackage.eINSTANCE.getOperation_Responsibility());
		else if(RETRIEVAL_OPERATION.equals(responsibility) && operation.getSt()!=null)
			error(operation.getName() + " is a RETRIEVAL_OPERATION, and should therefore not change state. Change the MAP decorator or remove the 'transitions' part.", operation,
					ApiDescriptionPackage.eINSTANCE.getOperation_Responsibility());
	}

	private void checkCommonDataEndpointConstraint(EndpointContract ep, Operation op, String responsibility) {
		if(responsibility.equals(COMPUTATION_FUNCTION)) {
			warning(op.getName() + " is a " + COMPUTATION_FUNCTION + ", somewhat unexpected in a data-oriented endpoint such as " + ep.getName(), op,
					ApiDescriptionPackage.eINSTANCE.getOperation_Name()); // Literals.OPERATION__NAME);
		} 
	}

	private String getResponsibilityPattern(Operation mdslOperation) {
		OperationResponsibility responsibility = mdslOperation.getResponsibility();
		// working with string constants here; responsibility.getRo() might not return RETRIEVAL_OPERATION
		if (responsibility == null)
			return "";
		if (responsibility.getCf() != null)
			return COMPUTATION_FUNCTION;
		if (responsibility.getSco() != null)
			return STATE_CREATION_OPERATION;
		if (responsibility.getRo() != null)
			return RETRIEVAL_OPERATION; 
		if (responsibility.getSto() != null)
			return STATE_TRANSITION_OPERATION;
		if (responsibility.getSro() != null)
			return STATE_REPLACEMENT_OPERATION;
		if (responsibility.getSdo() != null)
			return STATE_DELETION_OPERATION;
		if (responsibility.getOther() != null) {
			return responsibility.getOther();
		} else
			return ""; // unknown or not yet supported responsibility
	}
}
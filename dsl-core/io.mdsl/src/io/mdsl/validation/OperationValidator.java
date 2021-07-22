package io.mdsl.validation;

import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.Operation;

public class OperationValidator extends AbstractMDSLValidator {
	
	public final static String NO_ERROR_REPORT = "NO_ERROR_REPORT";
	public final static String NO_SECURITY_POLICY = "NO_SECURITY_POLICY";
	public final static String NO_COMPENSATION = "NO_COMPENSATION";
	
	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	// TODO offer quick fixes to correct errors?

	@Check
	public void validateMessagePresenenceForExchangePattern(Operation nextOp) {
				
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
		// TODO test ONE_WAY, NOTIFICATION
		else if( mep.equals("ONE_WAY")) {
			if(inDtr == null) {
				error(nextOp.getName() + " is a ONE_WAY operation, which expects a request message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
			if(outDtr != null) {
				error(nextOp.getName() + " is a ONE_WAY operation, which should not deliver a response message", nextOp, ApiDescriptionPackage.eINSTANCE.getOperation_Mep()); // Literals.OPERATION__MEP);
			}
		}
		else if( mep.equals("NOTIFICATION")) {
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
}
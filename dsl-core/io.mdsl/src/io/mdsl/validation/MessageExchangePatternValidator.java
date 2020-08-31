package io.mdsl.validation;

import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.Operation;

public class MessageExchangePatternValidator extends AbstractDeclarativeValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}

	@Check
	public void validateMessagePresenenceForExchangePattern(Operation nextOp) {
		
		String mep = nextOp.getMep();
		DataTransferRepresentation inDtr = nextOp.getRequestMessage();
		DataTransferRepresentation outDtr = nextOp.getResponseMessage();
		
		if(mep.equals("REQUEST_REPLY")) {
			if(inDtr == null) {
				error(nextOp.getName() + " is a REQUEST_REPLY operation, which expects a request message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
			if(outDtr == null) {
				error(nextOp.getName() + " is a REQUEST_REPLY operation, which must deliver a response message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
		}
		// TODO test ONE_WAY, NOTIFICATION
		else if( mep.equals("ONE_WAY")) {
			if(inDtr == null) {
				error(nextOp.getName() + " is a ONE_WAY operation, which expects a request message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
			if(outDtr != null) {
				error(nextOp.getName() + " is a ONE_WAY operation, which should not deliver a response message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
		}
		else if( mep.equals("NOTIFICATION")) {
			if(inDtr != null) {
				error(nextOp.getName() + " is a NOTIFICATION operation, which should not expect a request message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
			if(outDtr == null) {
				error(nextOp.getName() + " is a NOTIFICATION operation, which must deliver a response message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
		}
		// TODO what about "other" STRING (warning/info)? how to distinguish from unset MEP? 
		
		// note: it is possible to model operation w/o any message, for early stage (no MEP to be defined)
	}
}
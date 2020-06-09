package io.mdsl.validation;

import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.dataTransferRepresentation;
import io.mdsl.apiDescription.operation;

public class MessageExchangePatternValidator extends AbstractDeclarativeValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}

	@Check
	public void validateMessagePresenenceForExchangePattern(operation nextOp) {
		
		String mep = nextOp.getMep();
		dataTransferRepresentation inDtr = nextOp.getRequestMessage();
		dataTransferRepresentation outDtr = nextOp.getResponseMessage();
		
		if(mep.equals("REQUEST_REPLY")) {
			if(inDtr == null) {
				error(nextOp.getName() + " is a REQUEST_REPLY operation, which is expecting a request message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
			if(outDtr == null) {
				error(nextOp.getName() + " is a REQUEST_REPLY operation, which must be delivering a response message", nextOp, ApiDescriptionPackage.Literals.OPERATION__MEP);
			}
		}
		// TODO also check ONE_WAY, NOTIFICATION (?), other STRING (warning)
		// it is possible to model operation w/o any message, for early stage (warning?)
	}
}
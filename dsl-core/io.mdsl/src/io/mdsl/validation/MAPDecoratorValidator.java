package io.mdsl.validation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.endpointContract;
import io.mdsl.apiDescription.operation;
import io.mdsl.apiDescription.operationResponsibility;

public class MAPDecoratorValidator extends AbstractDeclarativeValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}

	@Check
	public void checkRoleResponsibilityPatternCombination(endpointContract nextEndpoint) {

		EList<operation> opList = nextEndpoint.getOps();
		String role1 = nextEndpoint.getPrimaryRole(); 
		// TODO should also look at "other roles"

		if(role1==null || role1.equals("")) {
			info(nextEndpoint.getName() + " has no responsibility ", nextEndpoint, ApiDescriptionPackage.Literals.ENDPOINT_CONTRACT__NAME);
			return; // nothing to report, really (could suggest to add)
		}
		
		for(int i=0; i<opList.size(); i++) {
			operation nextOp = opList.get(i);
			
			String responsibility = getResponsibilityPattern(nextOp);
			
			if(responsibility==null || responsibility.equals(""))
				return; // nothing to report, really (could suggest to add)

			// check that endpoint and operation level semantics match/make sense
			// TODO test more; decide what is ok/not ok
			
			
			if(role1.equals("PROCESSING_RESOURCE")) {
				info(nextOp.getName() + " has responsibility " + responsibility + " (ok in PROCESSING_RESOURCE such as " + nextEndpoint.getName() + ")", nextOp, ApiDescriptionPackage.Literals.OPERATION__NAME);
			}
			// TODO add/test ODH, MDH, DTR, LLR
			else if(role1.equals("INFORMATION_HOLDER_RESOURCE")){
				if(responsibility.equals("COMPUTATION_FUNCTION")) {
					warning(nextOp.getName() + " is a COMPUTATION_FUNCTION, somewhat unexpected in an INFORMATION_HOLDER_RESOURCE such as " + nextEndpoint.getName(), nextOp, ApiDescriptionPackage.Literals.OPERATION__NAME);					
				}
				else {
					info(nextOp.getName() + " has responsibility " + responsibility + " (ok in INFORMATION_HOLDER_RESOURCE such as " + nextEndpoint.getName() + ")", nextOp, ApiDescriptionPackage.Literals.OPERATION__NAME);
				}
			}
			else {
				info(nextEndpoint.getName() + " unknown role " + role1, nextEndpoint, ApiDescriptionPackage.Literals.ENDPOINT_CONTRACT__NAME);
			}
		}
	}
	
	// TODO refactor, mostly copied from generator/converter:
	private String getResponsibilityPattern(operation mdslOperation) {
		operationResponsibility responsibility = mdslOperation.getResponsibility();
		if (responsibility.getCf()!=null)
			return responsibility.getCf();
		if (responsibility.getSco()!=null)
			return responsibility.getSco();
		if (responsibility.getRo()!=null)
			return responsibility.getRo(); // two patterns are mapped to GET, should warn about that
		if (responsibility.getSto()!=null)
			return responsibility.getSto();
		if (responsibility.getOther()!=null) {
			return responsibility.getOther();
		}
		else 
			return ""; // unknown or not yet supported responsibility 
	}
}
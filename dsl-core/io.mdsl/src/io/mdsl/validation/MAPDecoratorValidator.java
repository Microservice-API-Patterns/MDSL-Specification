package io.mdsl.validation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;

public class MAPDecoratorValidator extends AbstractMDSLValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}

	@Check
	public void checkRoleResponsibilityPatternCombination(EndpointContract nextEndpoint) {

		EList<Operation> opList = nextEndpoint.getOps();
		String role1 = nextEndpoint.getPrimaryRole();
		// TODO could also look at "other roles"

		if (role1 == null || role1.equals("")) {
			info(nextEndpoint.getName() + " has no responsibility ", nextEndpoint, ApiDescriptionPackage.Literals.ENDPOINT_CONTRACT__NAME);
			return; // nothing to report, really (could suggest to add)
		}

		for (int i = 0; i < opList.size(); i++) {
			Operation nextOp = opList.get(i);

			String responsibility = getResponsibilityPattern(nextOp);

			if (responsibility == null || responsibility.equals(""))
				continue; // nothing to report, really (could suggest to add)

			checkOperationResponsibilitySemantics(nextOp, responsibility);

			// check that endpoint and operation level semantics match/make sense

			if (role1.equals("PROCESSING_RESOURCE")) {
				// no need to report positive case:
				// info(nextOp.getName() + " has responsibility " + responsibility + " (ok in a
				// processing resource such as " + nextEndpoint.getName() + ")", nextOp,
				// ApiDescriptionPackage.Literals.OPERATION__NAME);
			} else if (role1.equals("INFORMATION_HOLDER_RESOURCE")) {
				checkCommonDataEndpointConstraint(nextEndpoint, nextOp, responsibility);
			} else if (role1.equals("MASTER_DATA_HOLDER") || role1.equals("OPERATIONAL_DATA_HOLDER") || role1.equals("DATA_TRANSFER_RESOURCE")) {
			} else if (role1.equals("REFERENCE_DATA_HOLDER")) {
				checkCommonDataEndpointConstraint(nextEndpoint, nextOp, responsibility);
				if (responsibility.equals("STATE_CREATION_OPERATION") || responsibility.equals("STATE_TRANSITION_OPERATION")) {
					warning(nextOp.getName() + " creates or updates state, somewhat unexpected in a reference data holder such as " + nextEndpoint.getName(), nextOp,
							ApiDescriptionPackage.Literals.OPERATION__NAME);
				} else {
					// no need to report positive case:
					// info(nextOp.getName() + " has responsibility " + responsibility + " (ok in a
					// reference data holder such as " + nextEndpoint.getName() + ")", nextOp,
					// ApiDescriptionPackage.Literals.OPERATION__NAME);
				}
				if (responsibility.equals("STATE_TRANSITION_OPERATION")) {
					warning(nextOp.getName() + " is a STATE_TRANSITION_OPERATION, somewhat unexpected in a reference data holder such as " + nextEndpoint.getName(), nextOp,
							ApiDescriptionPackage.Literals.OPERATION__NAME);
				} else {
					// no need to report positive case:
					// info(nextOp.getName() + " has responsibility " + responsibility + " (ok in a
					// reference data holder such as " + nextEndpoint.getName() + ")", nextOp,
					// ApiDescriptionPackage.Literals.OPERATION__NAME);
				}
			} else if (role1.equals("LINK_LOOKUP_RESOURCE")) {
				checkCommonDataEndpointConstraint(nextEndpoint, nextOp, responsibility);
				if (responsibility.equals("STATE_TRANSITION_OPERATION")) {
					warning(nextOp.getName() + " updates state, somewhat unexpected in a link lookup resource such as " + nextEndpoint.getName(), nextOp,
							ApiDescriptionPackage.Literals.OPERATION__NAME);
				} else {
					// no need to report positive case:
					// info(nextOp.getName() + " has responsibility " + responsibility + " (ok in a
					// link lookup resource such as " + nextEndpoint.getName() + ")", nextOp,
					// ApiDescriptionPackage.Literals.OPERATION__NAME);
				}
			} else {
				info(nextEndpoint.getName() + " unknown role " + role1, nextEndpoint, ApiDescriptionPackage.Literals.ENDPOINT_CONTRACT__NAME);
			}

			// could validate message types and MAP responsibility pattern in a future
			// version
			// (for instance, an SCO should not return much data); not clear yet how to
			// generalize
		}
	}

	private void checkOperationResponsibilitySemantics(Operation operation, String responsibility) {
		if (operation.getResponseMessage() == null && "COMPUTATION_FUNCTION".equals(responsibility))
			warning(operation.getName() + " is a COMPUTATION_FUNCTION, and should therefore return something. Please add a response message.", operation,
					ApiDescriptionPackage.Literals.OPERATION__RESPONSIBILITY);
	}

	private void checkCommonDataEndpointConstraint(EndpointContract ep, Operation op, String responsibility) {
		if (responsibility.equals("COMPUTATION_FUNCTION")) {
			warning(op.getName() + " is a COMPUTATION_FUNCTION, somewhat unexpected in a data-oriented endpoint such as " + ep.getName(), op,
					ApiDescriptionPackage.Literals.OPERATION__NAME);
		} else {
			// no need to report positive case:
			// info(op.getName() + " has responsibility " + responsibility + " (ok in a
			// data-oriented endpoint such as " + ep.getName() + ")", op,
			// ApiDescriptionPackage.Literals.OPERATION__NAME);
		}
	}

	// TODO refactor, adapted from generator/converter:
	private String getResponsibilityPattern(Operation mdslOperation) {
		OperationResponsibility responsibility = mdslOperation.getResponsibility();
		if (responsibility == null)
			return "";
		if (responsibility.getCf() != null)
			return responsibility.getCf();
		if (responsibility.getSco() != null)
			return responsibility.getSco();
		if (responsibility.getRo() != null)
			return responsibility.getRo(); // two patterns are mapped to GET, should warn about that
		if (responsibility.getSto() != null)
			return responsibility.getSto();
		if (responsibility.getOther() != null) {
			return responsibility.getOther();
		} else
			return ""; // unknown or not yet supported responsibility
	}
}
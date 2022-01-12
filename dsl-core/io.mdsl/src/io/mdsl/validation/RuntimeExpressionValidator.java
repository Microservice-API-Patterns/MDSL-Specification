package io.mdsl.validation;

import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.AsyncEndpoint;
import io.mdsl.apiDescription.ConsumptionWhereClauses;
import io.mdsl.apiDescription.CorrelationIdWhereClause;
import io.mdsl.apiDescription.OASSecurity;

public class RuntimeExpressionValidator extends AbstractMDSLValidator {

	private String runtimeExpressionRegex = "\\\"?\\$message.(header|payload)#\\/[A-Za-z0-9_]*\\\"?";

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	@Check
	public void checkCorrelationId(CorrelationIdWhereClause clausole) {

		if (!clausole.getSource().matches(this.runtimeExpressionRegex)) {

			error("Incorrect format for the expression. Format is $message.(payload | header)#/<path>.", clausole,
					ApiDescriptionPackage.eINSTANCE.getCorrelationIdWhereClause_Source()); // Literals.CORRELATION_ID_WHERE_CLAUSE__SOURCE);

		}

	}

	@Check
	public void checkIfExpression(ConsumptionWhereClauses clausole) {

		if (!clausole.getLeftExp().matches(this.runtimeExpressionRegex)) {

			error("Incorrect format for the left expression. Format is $message.(payload | header)#/<path>.", clausole,
					ApiDescriptionPackage.eINSTANCE.getConsumptionWhereClauses_LeftExp()); // Literals.CONSUMPTION_WHERE_CLAUSES__LEFT_EXP);

		}
//		if (!clausole.getRightExpr().matches(this.runtimeExpressionRegex)) {
//
//			error("Incorrect format for the right expression. Format is $message.(payload | header)#/<path>.", clausole,
//					ApiDescriptionPackage.Literals.CONSUMPTION_WHERE_CLAUSES__RIGHT_EXPR);
//
//		}
	}

	@Check
	public void checkAsyncEndpointSecurityPolicyFormat(AsyncEndpoint endpoint) {

		OASSecurity policy = endpoint.getSecurityPolicy();

		if (policy == null)
			return;

		String policyExpression = endpoint.getSecurityPolicyExpression();

		if (policy.getValue() == OASSecurity.BASIC_AUTHENTICATION_VALUE && policyExpression != null) {
			warning("Expression not needed when using BASIC_AUTHENTICATION.", endpoint,
					ApiDescriptionPackage.eINSTANCE.getAsyncEndpoint_SecurityPolicyExpression()); // Literals.ASYNC_ENDPOINT__SECURITY_POLICY_EXPRESSION);
		}

		if (policy.getValue() == OASSecurity.API_KEY_VALUE) {
			if (policyExpression == null) {
				warning("A policy expression is required for API_KEY. Consider to specify where to find the API_KEY in the message. Example: using API_KEY in \"$message.header#/apiKey\"",
						endpoint, ApiDescriptionPackage.eINSTANCE.getAsyncEndpoint_SecurityPolicy()); // Literals.ASYNC_ENDPOINT__SECURITY_POLICY);
			} else if (!policyExpression.matches(this.runtimeExpressionRegex)) {

				error("Incorrect format for the security policy expression. Format is $message.(payload | header)#/<path>.",
						endpoint, ApiDescriptionPackage.eINSTANCE.getAsyncEndpoint_SecurityPolicyExpression()); // Literals.ASYNC_ENDPOINT__SECURITY_POLICY_EXPRESSION);

			}
		}

	}
}

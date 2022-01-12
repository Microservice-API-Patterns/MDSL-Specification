package io.mdsl.transformations;

import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ActionKeyword;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.utils.MDSLLogger;

public class ScenarioTransformations {

	private static final String PERFORM_QUERY_OPNAME = "performQuery";
	private static final String PERFORM_COMMAND_OPNAME = "performCommand";
	private static final String DEFAULT_TARGET = "SomeEntity";
	private static final String REALIZATION_ENDPOINT_NAME_SUFFIX = "RealizationEndpoint";

	// ** transformations

	public EndpointContract addEndpointForScenario(IntegrationScenario scenario, boolean generateOperations) {
		ServiceSpecification ss = (ServiceSpecification) scenario.eContainer();

		String endpointName = scenario.getName() + REALIZATION_ENDPOINT_NAME_SUFFIX;
		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, endpointName);
		ec.setScenario(scenario);

		if (generateOperations) {
			scenario.getStories().forEach(this::addOperationForScenarioStory);
		}

		ss.getContracts().add(ec);
		return ec;
	}

	public void addOperationForScenarioStory(IntegrationStory story) {
		IntegrationScenario scenario = (IntegrationScenario) story.eContainer();
		ServiceSpecification ss = (ServiceSpecification) story.eContainer().eContainer();

		String endpointName = scenario.getName() + REALIZATION_ENDPOINT_NAME_SUFFIX;
		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, endpointName);
		ec.setScenario(scenario);
		
		// decided not to set role decorator of endpoint (PR? IHR?)

		// the keyword enum is set to default of CQRS for plain, no-keyword action!
		// so have to check for plainAction option first
		
		// story.getAction cannot be null, mandatory element of grammar rule
		if (story.getAction().getPlainAction() != null) {
			String actionName = ScenarioTransformationHelpers.getActionName(story.getAction(), "");
			ScenarioTransformationHelpers.addOperation(ss, story, ec, actionName);		
		} else if (story.getAction().getKeyword() == ActionKeyword.CRUD) {
			String crudTarget = getTarget(story);

			ElementStructure es = ScenarioTransformationHelpers.createElementStructureDTO(story, false);
			TypeReference tref = OperationTransformationHelpers.findOrCreateTypeReference(ss, crudTarget, es);
			
			addCreateOperation(ss, crudTarget, ec, EcoreUtil.copy(tref));
			addReadOperation(ss, crudTarget, ec, EcoreUtil.copy(tref)); // needs different signature than C and U
			addUpdateOperation(ss, crudTarget, ec, EcoreUtil.copy(tref));
			addDeleteOperation(ss, crudTarget, ec); // does not need the DTO
			
			// decided not to set role decorator of endpoint (IHR?)
			
		} else if (story.getAction().getKeyword() == ActionKeyword.CQRS) {
			String cqrsTarget = getTarget(story);
			addCommandOperation(ss, story, ec, cqrsTarget); 
			MAPDecoratorHelpers.setRoleToProcessingResource(ec);
			addQueryOperation(ss, story, ec, cqrsTarget);
			MAPDecoratorHelpers.addRole(ec, MAPDecoratorHelpers.INFORMATION_HOLDER_RESOURCE);
			
		} else {
			MDSLLogger.reportError("Unexpected action type:" + story.getAction().getKeyword());
		}
	}

	private String getTarget(IntegrationStory story) {
		String crudTarget = story.getAction().getTarget();
		if (crudTarget == null || crudTarget.isEmpty()) {
			MDSLLogger.reportWarning("Action target is null or empty string");
			crudTarget = DEFAULT_TARGET;
		} else {
			crudTarget = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(crudTarget);
		}
		return crudTarget;
	}

	private static void addCreateOperation(ServiceSpecification ss, String target, EndpointContract ec, TypeReference tref) {
		Operation operationForAction = OperationTransformationHelpers.createCreateOperation("create" + target, tref);
		ec.getOps().add(operationForAction);
	}

	private static void addUpdateOperation(ServiceSpecification ss, String target, EndpointContract ec, TypeReference tref) {
		Operation operationForAction = OperationTransformationHelpers.createUpdateOperation("update" + target, tref);
		ec.getOps().add(operationForAction);
	}

	private static void addReadOperation(ServiceSpecification ss, String target, EndpointContract ec, TypeReference tref) {
		Cardinality setCard = ApiDescriptionFactory.eINSTANCE.createCardinality();
		setCard.setZeroOrMore("*");
		tref.setCard(setCard);
		Operation operationForAction = OperationTransformationHelpers.createReadOperation("read" + target, OperationTransformationHelpers.QUERY_FILTER, tref);
		// Operation operationForAction = OperationTransformationHelpers.createRetrievalOperation("read" + target, tref);
		ec.getOps().add(operationForAction);
	}

	private static void addDeleteOperation(ServiceSpecification ss, String target, EndpointContract ec) {
		Operation operationForAction = OperationTransformationHelpers.createDeleteOperation("delete" + target);
		ec.getOps().add(operationForAction);
	}

	private static void addCommandOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec, String target) {
		ScenarioTransformationHelpers.addOperation(ss, story, ec, PERFORM_COMMAND_OPNAME + target);
	}

	private static void addQueryOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec, String target) {
		ScenarioTransformationHelpers.addQueryOperation(ss, story, ec, PERFORM_QUERY_OPNAME + target);
	}
}

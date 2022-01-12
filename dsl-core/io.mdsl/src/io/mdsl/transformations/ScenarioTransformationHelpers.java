package io.mdsl.transformations;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.Action;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.StoryObject;
import io.mdsl.apiDescription.TreeNode;

public class ScenarioTransformationHelpers {

	static final String IN_PARAMETER_IDENTIFIER = "in";
	static final String OUT_PARAMETER_IDENTIFIER = "data";

	private static final String ANONYMOUS_DTO = "AnonymousDTO";
	private static final String NOOP = "noop";
	
	static Operation addQueryOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec, String operationName) {
		Operation queryOperation = addOperation(ss, story, ec, operationName);
		// swap request and response messages, adjust parameter names
		DataTransferRepresentation dtr = queryOperation.getRequestMessage();
		queryOperation.setRequestMessage(queryOperation.getResponseMessage());
		queryOperation.setResponseMessage(dtr);		
		return queryOperation;
	}
	
	static Operation addOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec, String operationName) {

		// TODO (future work) map/use role and goal? (comment, decorator, new stakeholder property?)
		// TODO (future work) use "add"/"remove" action names/prefixes for even more expressive parameter signature (request, response)

		Operation operationForAction = ApiDescriptionFactory.eINSTANCE.createOperation();
		operationForAction.setName(operationName);
		DataTransferRepresentation indto = createDTRForTargetAndObjects(story, false);
		operationForAction.setRequestMessage(indto);
		AtomicParameter ap = DataTypeTransformations.createAtomicDataParameter(ScenarioTransformationHelpers.OUT_PARAMETER_IDENTIFIER, OperationTransformationHelpers.STRING_TYPE);
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		pt.setFirst(DataTypeTransformations.wrapAtomicParameterAsTreeNode(ap));
		DataTransferRepresentation outdto = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		outdto.setPayload(DataTypeTransformations.wrapParameterTreeAsElementStructure(pt));
		operationForAction.setResponseMessage(outdto);

		MAPDecoratorHelpers.deriveResponsibilityFromName(operationForAction, operationName); // TODO use operation name rather than action name?
		ec.getOps().add(operationForAction);
		
		return operationForAction;
	}

	public static String getActionName(Action action, String name) {
		if (action.getPlainAction() != null) {
			if (name != null && !name.equals(ANONYMOUS_DTO)) {
				return DataTypeTransformationHelpers.replaceSpacesWithUnderscores(action.getPlainAction() + name);
			} else {
				return DataTypeTransformationHelpers.replaceSpacesWithUnderscores(action.getPlainAction());
			}
		}
		if (action.getKeyword() != null) {
			return DataTypeTransformationHelpers.replaceSpacesWithUnderscores(action.getKeyword().getName() + action.getTarget());
		}
		return NOOP;
	}

	public static String getFirstObjectName(IntegrationStory story) {
		if (story.getAction().getTarget() != null) {
			return story.getAction().getTarget();
		}

		if (story.getOn() == null) {
			return ANONYMOUS_DTO;
		} else if (!story.getOn().isEmpty()) {
			return story.getOn().get(0).getObject();
		} else {
			return ANONYMOUS_DTO;
		}
	}

	public static DataTransferRepresentation createDTRForTargetAndObjects(IntegrationStory story, boolean hasSetCardinality) {
		DataTransferRepresentation dtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		ElementStructure es = createElementStructureDTO(story, hasSetCardinality);
		dtr.setPayload(es);
		return dtr;
	}
	
	static ElementStructure createElementStructureDTO(IntegrationStory story, boolean hasSetCardinality) {
		boolean first = true;
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		TreeNode tn;
		AtomicParameter ap;

		EList<StoryObject> on = story.getOn();

		if (story.getAction().getTarget() != null) {
			ap = DataTypeTransformations.createAtomicDataParameter(DataTypeTransformationHelpers.decapitalizeName(story.getAction().getTarget()), null); // TODO tbd type
			tn = DataTypeTransformations.wrapAtomicParameterAsTreeNode(ap);
			pt.setFirst(tn);
			first = false;
		}

		if (hasSetCardinality) {
			Cardinality setCard = ApiDescriptionFactory.eINSTANCE.createCardinality();
			setCard.setZeroOrMore("*");
			pt.setCard(setCard);
		}

		for (StoryObject object : on) {
			ap = DataTypeTransformations.createAtomicDataParameter(DataTypeTransformationHelpers.decapitalizeName(object.getObject()), null); // TODO tbd type
			tn = DataTypeTransformations.wrapAtomicParameterAsTreeNode(ap);
			if (first) {
				pt.setFirst(tn);
				first = false;
			} else {
				pt.getNexttn().add(tn);
			}
		}

		if (first) {
			// no object present (plain action/no keyword action and no other objects present), so adding a dummy/default one
			ap = DataTypeTransformations.createAtomicDataParameter(IN_PARAMETER_IDENTIFIER, null); // TODO tbd type
			tn = DataTypeTransformations.wrapAtomicParameterAsTreeNode(ap);
			pt.setFirst(tn);
		}

		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setPt(pt);
		return es;
	}
}

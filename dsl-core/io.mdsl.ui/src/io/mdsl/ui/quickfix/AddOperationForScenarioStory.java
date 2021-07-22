package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.transformations.ScenarioTransformationHelpers;
import io.mdsl.transformations.MAPDecoratorTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;

public class AddOperationForScenarioStory implements ISemanticModification {
	
	// TODO (M) align/enhance operation/message signature design across quick fixes and helpers
		
	AddOperationForScenarioStory() {
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
	
		IntegrationStory story = (IntegrationStory) element;
		IntegrationScenario scenario = (IntegrationScenario) story.eContainer();
		ServiceSpecification ss = (ServiceSpecification) story.eContainer().eContainer();
			
		String endpointName = scenario.getName() + "RealizationEndpoint";
		EndpointContract ec = TransformationHelpers.findOrCreateEndpointType(ss, endpointName);
		ec.setScenario(scenario); 
		
		// assignRole(ec); // taken out deliberately
		
		// ActionKeyword akw = story.getAction().getKeyword();

		// the keyword enum is set to default of CQRS for plain, no-keyword action!
		// so have to check for plainAction option first 
		if(story.getAction().getPlainAction()!=null) {	
			String actionName = ScenarioTransformationHelpers.getActionName(story.getAction(), ""); // taken out in V5.3.3 
					// ScenarioTransformationHelpers.getFirstObjectName(story));
			addOperationWithName(ss, story, ec, actionName);
		}
		else if(story.getAction().getKeyword()!=null && story.getAction().getKeyword().toString().equals("CRUD")) {
			String crudTarget = story.getAction().getTarget();
					
			if(crudTarget==null || crudTarget.equals("")) {
				// TODO additional objects from grammar not picked up here yet, see addOperationWithName
				// DataTransferRepresentation dtrdto = ScenarioTransformationHelpers.createDTOForTargetAndObjects(story, false);
				System.err.println("[W] CRUD target is null or empty string");
				crudTarget="SomeEntity";
			}
			
			addCreateOperation(ss, story, ec, "create", crudTarget);
			addReadOperation(ss, story, ec, "read", crudTarget); // needs different signature than C and U
			addUpdateOperation(ss, story, ec, "update", crudTarget);
			addDeleteOperation(ss, story, ec, "delete", crudTarget); // D does not need the DTO
		}
		else if(story.getAction().getKeyword()!=null && story.getAction().getKeyword().toString().equals("CQRS")) {
			String crudTarget = story.getAction().getTarget();
			if(crudTarget==null || crudTarget.equals("")) {
				System.err.println("[W] CRUD target is null or empty string");
				crudTarget="RegardingSomeEntity";
			}
			addOperationWithName(ss, story, ec, "sendCommand" + crudTarget);
			addOperationWithName(ss, story, ec, "sendQuery" + crudTarget);
		}
		else
			System.err.println("[E] Unexpected action type.");
	}

	/*
	// TODO move to helper if needed
	private void assignRole(EndpointContract ec, String type) {
		if(ec.getPrimaryRole()==null)
			ec.setPrimaryRole(type);
		else {
			// only add if not already present (primary or other roles)
			EList<String> secondaryRoles = ec.getOtherRoles();
			if(!secondaryRoles.contains(type) && !ec.getPrimaryRole().equals(type))
				ec.getOtherRoles().add(type); 
			// TODO other roles feature is not used in generators, not documented much
		}
	}
	*/
	
	private void addCreateOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec,
			String actionName, String objectName) {
		ElementStructure es = ScenarioTransformationHelpers.createElementStructureDTO(story, false);
		DataContract dt = findOrCreateDataType(ss, objectName, es);
		TypeReference tref = createTypeReference(ss, dt);
		
		Operation operationForAction = TransformationHelpers.createCreateOperation(story, actionName+objectName, tref);
		ec.getOps().add(operationForAction);	
	}

	private TypeReference createTypeReference(ServiceSpecification ss, DataContract dt) {
		TypeReference tr = ApiDescriptionFactory.eINSTANCE.createTypeReference();
		tr.setDcref(dt);
		return tr;
	}

	private DataContract findOrCreateDataType(ServiceSpecification ss, String objectName, ElementStructure es) {
		for(DataContract dc : ss.getTypes()) {
			if(dc.getName().equals(objectName+"DTO"))
				return dc;
		}
		DataContract dc = ApiDescriptionFactory.eINSTANCE.createDataContract();
		dc.setName(objectName+"DTO");
		dc.setStructure(es);
		ss.getTypes().add(dc);
		return dc;
	}

	private void addUpdateOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec,
			String actionName, String objectName) {
		ElementStructure es = ScenarioTransformationHelpers.createElementStructureDTO(story, false);
		DataContract dt = findOrCreateDataType(ss, objectName, es);
		TypeReference tref = createTypeReference(ss, dt);
		Operation operationForAction = ScenarioTransformationHelpers.createUpdateOperation(story, actionName+objectName, tref);
		ec.getOps().add(operationForAction);	
	}
	
	private void addReadOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec, String actionName, String objectName) {
		ElementStructure es = ScenarioTransformationHelpers.createElementStructureDTO(story, false);
		DataContract dt = findOrCreateDataType(ss, objectName, es);
		TypeReference tref = createTypeReference(ss, dt);
		Cardinality setCard = ApiDescriptionFactory.eINSTANCE.createCardinality();
		setCard.setZeroOrMore("*");
		tref.setCard(setCard);
		Operation operationForAction = ScenarioTransformationHelpers.createRetrievalOperation(story, actionName+objectName, tref);
		ec.getOps().add(operationForAction);	
	}

	private void addDeleteOperation(ServiceSpecification ss, IntegrationStory story, EndpointContract ec,
			String actionName, String objectName) {
		Operation operationForAction = TransformationHelpers.createDeleteOperation(actionName+objectName);
		ec.getOps().add(operationForAction);
	}

	public void addOperationWithName(ServiceSpecification ss, IntegrationStory story, EndpointContract ec, String actionName) {
		Operation operationForAction = TransformationHelpers.createOperationWithGenericParameters(actionName, true);
		DataTransferRepresentation dtrdto = ScenarioTransformationHelpers.createDTOForTargetAndObjects(story, false);
		operationForAction.setRequestMessage(dtrdto);
		
		// TODO (M) should use new CRUD helpers here too (more expressive operation signatures, e.g. DTO for objects in story sentence)
		
		// use story objects and add/remove to define full parameter signature (request, response)
		MAPDecoratorTransformationHelpers.deriveResponsibilityFromName(operationForAction, actionName);
		ec.getOps().add(operationForAction);
		
		// TODO (L) map/use role and goal? (comment, decorator, new stakeholder property?)
	}
}

package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.EventTypes;
import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.TransformationHelpers;

class AddEndpointTypeSupportingFlow implements ISemanticModification {
	// TODO (L) could go for AsyncMDSL channel too (later transformation available)
	
	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		Orchestration flow = (Orchestration) element;
		ServiceSpecification ss = (ServiceSpecification) flow.eContainer();
		
		EndpointContract ec = TransformationHelpers.createUniqueEndpoint(ss, flow.getName()+"Endpoint");
		ec.setFlow(flow); // grammar: either flow or scenario can be referenced at present
		ec.setPrimaryRole("PROCESSING_RESOURCE");

		// could add operation(s) for flow steps and/or events that are received here
		// call AddOperationToEndpointType.apply (having found the flow steps and cis in them, see below)
		// but decided to keep the steps small("big" reengineering step better fits a menu-level transformation, see AsyncMDSL)
		
		// TODO (L) could make this optional, could use a different event name
		Event re = createFlowInitiationEvent(ss, flow);
		ec.getEvents().add(re);
		ss.getContracts().add(ec);

	}

	private Event createFlowInitiationEvent(ServiceSpecification ss, Orchestration flow) {
		EventType de = DataTypeTransformationHelpers.addEventTypeIfNotPresent(ss, flow.getName()+"Initiated"); 
		EventTypes des = ApiDescriptionFactory.eINSTANCE.createEventTypes();
		des.getEvents().add(de);
		ss.getEvents().add(des);
		
		Event re = ApiDescriptionFactory.eINSTANCE.createEvent();
		re.setType(de);
		
		return re; 
	}
}
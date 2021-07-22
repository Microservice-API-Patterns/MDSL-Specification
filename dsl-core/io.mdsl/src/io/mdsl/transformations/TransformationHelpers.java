package io.mdsl.transformations;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.apiDescription.Action;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.CommandTypes;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.EventTypes;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.StoryObject;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.apiDescription.impl.EndpointContractImpl;
import io.mdsl.exception.MDSLException;

public class TransformationHelpers {

	// ** endpoint helpers

	public static EndpointContract findOrCreateEndpointType(ServiceSpecification ss, String endpointName) {
		for(EObject eccc : ss.getContracts()) {
			// could be channel too:
			if(eccc.getClass() == EndpointContractImpl.class && ((EndpointContract) eccc).getName().equals(endpointName))
				return (EndpointContract) eccc;
		} 
		EndpointContract ec = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		ec.setName(endpointName); // TODO more required here?
		ss.getContracts().add(ec);
		return ec;
	}

	public static EndpointContract createUniqueEndpoint(ServiceSpecification ss, String suggestedName) {		
		for(EObject contractOrChannel : ss.getContracts()) {
			// is this loop needed here? or is direct access possible?
			if(contractOrChannel.getClass()==EndpointContractImpl.class) {
				EndpointContract ec = (EndpointContract) contractOrChannel;
				if(ec.getName().equals(suggestedName)) {
					reportError("Can't add an endpoint contract named " + suggestedName + " because that name is already taken by another endpoint. Please rename the flow.");
				}
			}
			else 
				System.out.println(contractOrChannel.getClass()); // debug only
		}

		EndpointContract ec = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		ec.setName(suggestedName);	

		ss.getContracts().add(ec);
		return ec;
	}


	public static EventType findOrCreateUniqueEventType(ServiceSpecification ss, String suggestedName) {	
		List<EventType> eventTypes = EcoreUtil2.eAllOfType(ss, EventType.class);

		for(EventType event: eventTypes) {
			if(event.getName().equals(suggestedName)) {
				// reportError("An event type of name " + suggestedName + " already exists, can't create flow step. Rename the story action.");
				return event;
			}
		}

		// prepare domain event
		EventType de = ApiDescriptionFactory.eINSTANCE.createEventType(); 
		// TODO [R] put elem structure placeholder P or "eventData":D?
		de.setName(suggestedName);

		EventTypes eventList = ApiDescriptionFactory.eINSTANCE.createEventTypes();
		eventList.getEvents().add(de); 
		ss.getEvents().add(eventList);
		return de;
	}

	public static CommandType findOrCreateUniqueCommandType(ServiceSpecification ss, String suggestedName) {		
		List<CommandType> commandTypes = EcoreUtil2.eAllOfType(ss, CommandType.class);

		for(CommandType command: commandTypes) {
			if(command.getName().equals(suggestedName)) {
				return command;
				// TransformationUtilities.reportError("A command type called " + suggestedName + " already exists, can't create flow step. Rename the story action.");
			}
		}

		CommandType ct = ApiDescriptionFactory.eINSTANCE.createCommandType(); 
		// TODO [R] add some command data (in, out, inFlight)
		ct.setName(suggestedName);
		CommandTypes cmdTypeList = ApiDescriptionFactory.eINSTANCE.createCommandTypes();
		cmdTypeList.getCommands().add(ct);
		ss.getCommands().add(cmdTypeList);
		return ct;
	}

	// ** operation helpers

	public static Operation findOperationInContract(EndpointContract ec, String opName) {
		for(Operation nextOp : ec.getOps()) 
			if(nextOp.getName().equals(opName))
				return nextOp;
		// TODO could create operation if not found (as in endpoint helper)
		return null;
	}
	
	// TODO (M) merge these two methods
	
	public static boolean operationExistsInContract(EndpointContract ec, String opName) {
		for(Operation operation : ec.getOps()) {
			if(operation.getName().equals(opName))
				return true;
		}
		return false;
	}

	public static  Operation createStateManipulatingOperation(String opName, String operationRole) {
		Operation sto = TransformationHelpers.createOperationWithGenericParameters(opName, true);
		OperationResponsibility ov = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		String roleType = operationRole; // type string used at all?
		if(operationRole.equals("EVENT_PROCESSOR"))
			ov.setEp(roleType);
		else if(operationRole.equals("STATE_CREATION_OPERATION"))
			ov.setSco(roleType);
		else if(operationRole.equals("STATE_REPLACMENT_OPERATION"))
			ov.setSro(roleType);
		else if(operationRole.equals("STATE_TRANSITION_OPERATION"))
			ov.setSto(roleType);
		else if(operationRole.equals("STATE_DELETION_OPERATION"))
			ov.setSdo(roleType);

		sto.setResponsibility(ov);

		return sto;
	}

	// TODO generalize or use same helpers for MAP QFs (+PR, +IHR etc.)

	// could return void:
	public static Operation createRetrievalOperations(EndpointContract ec, Event event, String type) {
		Operation result = ApiDescriptionFactory.eINSTANCE.createOperation();
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setRo(type); // TODO is type parameter used at all?
		result.setResponsibility(or);

		EventType et = event.getType(); // can't be null (due to grammar)
		result.setName("get" + et.getName() + "Events"); 
		if(et!=null && et.getContent()!=null) {
			DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			ElementStructure pl = EcoreUtil.copy(et.getContent());

			Cardinality setCardinality = ApiDescriptionFactory.eINSTANCE.createCardinality();
			setCardinality.setZeroOrMore("*"); // why does a string have to be passed?
			// use cardinality in element structure wrapping that of the event type (pt, tr, ap)
			DataTypeTransformationHelpers.setCardinality(pl, setCardinality);

			outDtr.setPayload(pl);
			result.setResponseMessage(outDtr);
		}
		else {
			TransformationHelpers.reportError("Can't create retrieval operation(s) for an event type of unknown structure");
		}

		AtomicParameter eventSelection = DataTypeTransformationHelpers.createMetadataParameter("eventQuery", "string");			
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		inDtr.setPayload(DataTypeTransformationHelpers.wrapAtomicParameterAsElementStructure(eventSelection));
		result.setRequestMessage(inDtr);

		// could not add if already present, accepting duplicates and validation errors at present 
		ec.getOps().add(result);

		return result;
	}

	// could return void:
	public static Operation createEventProcessorOperation(EndpointContract ec, Event event, String type) {
		Operation result = ApiDescriptionFactory.eINSTANCE.createOperation();
		result.setMep("ONE_WAY");
		OperationResponsibility ov = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		ov.setEp(type);
		result.setResponsibility(ov);
		EventType et = event.getType(); // can't be null (due to grammar)

		result.setName("receive" + et.getName() + "Event");
		if(et!=null && et.getContent()!=null) {
			DataTransferRepresentation dtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			dtr.setPayload(EcoreUtil.copy(et.getContent()));
			result.setRequestMessage(dtr);
		}
		else {
			TransformationHelpers.reportError("Can't create an event processor operation for an event type of unknown structure");
		}

		// could not add if already present, accepting duplicates and validation errors at present 
		ec.getOps().add(result);

		return result;
	}

	// TODO (M) use these new methods when creating ops one by one

	public static Operation createCreateOperation(IntegrationStory story, String opName, TypeReference typeRef) {
		Operation cop = TransformationHelpers.createOperationWithAtomicParameters(opName, "D", "string", true, "string");
		DataTransferRepresentation dtrDto = createDTRFromTypeReference(typeRef);

		cop.setRequestMessage(dtrDto);	
		cop.getResponseMessage().getPayload().getNp().getAtomP().getRat().setName("successFlag");
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSco(opName);
		cop.setResponsibility(or);
		return cop;
	}

	static DataTransferRepresentation createDTRFromTypeReference(TypeReference typeRef) {
		DataTransferRepresentation dtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setTr(typeRef);

		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setNp(spn);
		dtr.setPayload(es);

		return dtr;
	}

	public static Operation createDeleteOperation(String opName) {
		Operation dop = TransformationHelpers.createOperationWithAtomicParameters(opName, "ID", "string", true, "bool");
		dop.getRequestMessage().getPayload().getNp().getAtomP().getRat().setName("resourceId");
		dop.getResponseMessage().getPayload().getNp().getAtomP().getRat().setName("successFlag");
		OperationResponsibility ov = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		ov.setSdo(opName);
		dop.setResponsibility(ov);
		return dop;
	}

	public static Operation createOperationWithAtomicParameters(String name, String requestRole, String requestType, boolean deliversResponse, String responseType) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();

		String opName = DataTypeTransformationHelpers.decapitalizeName(name);
		newOp.setName(opName); 

		AtomicParameter inP = DataTypeTransformationHelpers.createAtomicDataParameter(opName + "RequestBody", requestType);	
		inP.getRat().setRole(requestRole);
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		inDtr.setPayload(DataTypeTransformationHelpers.wrapAtomicParameterAsElementStructure(inP));
		newOp.setRequestMessage(inDtr);

		if(deliversResponse) {
			DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			AtomicParameter outP = DataTypeTransformationHelpers.createAtomicDataParameter(opName + "ResponseBody", responseType);
			// outP.getRat().setRole(responseRole);
			outDtr.setPayload(DataTypeTransformationHelpers.wrapAtomicParameterAsElementStructure(outP));
			newOp.setResponseMessage(outDtr);
		}

		return newOp;
	}

	public static Operation createOperationWithGenericParameters(String name, boolean deliversResponse) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();

		// TODO check that operation name does not exist; make unique if so

		String c1 = DataTypeTransformationHelpers.decapitalizeName(name);
		newOp.setName(c1); 

		GenericParameter inP = DataTypeTransformationHelpers.createGenericParameter(DataTypeTransformationHelpers.decapitalizeName(name) + "RequestBody");			
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		inDtr.setPayload(DataTypeTransformationHelpers.wrapGenericParameterNodeAsElementStructure(inP));
		newOp.setRequestMessage(inDtr);

		if(deliversResponse) {
			DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			outDtr.setPayload(DataTypeTransformationHelpers.wrapGenericParameterNodeAsElementStructure(DataTypeTransformationHelpers.createGenericParameter(DataTypeTransformationHelpers.decapitalizeName(name) + "ResponseBody")));
			newOp.setResponseMessage(outDtr);
		}

		return newOp;
	}

	public static String reportError(String message) {
		System.err.println(message);
		throw new MDSLException(message);
	}
}

package io.mdsl.transformations;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ServiceSpecification;

public class EndpointTransformations {

	private static final String ONE_WAY_MEP = "ONE_WAY";
	private static final String GET_PREFIX = "get";
	private static final String EVENT_PROCESSOR_NAME_PREFIX = "receive";
	private static final String EVENT_SUFFIX = "Event";
	private static final String EVENTS_SUFFIX = "Events";
	private static final String COMMANDS_SUFFIX = "Commands";
	private static final String QUERIES_SUFFIX = "Queries";
	private static final String EVENT_QUERY_PARAMETER_NAME = "eventQuery";

	public static void addEventManagementOperations(EndpointContract ec, String eventName) {
		for (Event event : ec.getEvents()) {
			if (event.getType().getName().equals(eventName)) {
				EndpointTransformations.createEventProcessorOperation(ec, event, MAPDecoratorHelpers.EVENT_PROCESSOR);
				EndpointTransformations.createRetrievalOperationsForEvent(ec, event, MAPDecoratorHelpers.RETRIEVAL_OPERATION);
			}
		}
	}

	public static void createEventProcessorOperation(EndpointContract ec, Event event, String type) {
		Operation result = ApiDescriptionFactory.eINSTANCE.createOperation();
		result.setMep(ONE_WAY_MEP);
		OperationResponsibility ov = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		ov.setEp(type);
		result.setResponsibility(ov);
		EventType et = event.getType(); // can't be null (due to grammar)

		result.setName(EVENT_PROCESSOR_NAME_PREFIX + et.getName() + EVENT_SUFFIX);
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		if (et != null && et.getContent() != null) {
			inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			inDtr.setPayload(EcoreUtil.copy(et.getContent()));
			result.setRequestMessage(inDtr);
		} else {
			GenericParameter gp = DataTypeTransformations.createGenericParameter("event");
			ElementStructure responsePayload = DataTypeTransformations.wrapGenericParameterNodeAsElementStructure(gp);
			inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			inDtr.setPayload(responsePayload);
			result.setRequestMessage(inDtr);
		}
		result.setRequestMessage(inDtr);
		// accepting duplicates and validation errors at present; could not add if already present
		ec.getOps().add(result);
	}

	public static void createRetrievalOperationsForEvent(EndpointContract ec, Event event, String type) {

		// TODO future work: support snapshots (additional RETRIEVAL_OPERATION, which should not map to HTTP GET as well)

		Operation result = ApiDescriptionFactory.eINSTANCE.createOperation();
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setRo(MAPDecoratorHelpers.RETRIEVAL_OPERATION);
		result.setResponsibility(or);

		EventType et = event.getType(); // can't be null (due to grammar)
		result.setName(GET_PREFIX + et.getName() + EVENTS_SUFFIX);
		DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		if (et != null && et.getContent() != null) {
			outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			ElementStructure responsePayload = EcoreUtil.copy(et.getContent());

			Cardinality setCardinality = ApiDescriptionFactory.eINSTANCE.createCardinality();
			setCardinality.setZeroOrMore("*"); 
			// use cardinality in element structure wrapping that of the event type (pt, tr, ap)
			DataTypeTransformations.setCardinality(responsePayload, setCardinality);

			outDtr.setPayload(responsePayload);
			result.setResponseMessage(outDtr);
		} else {
			GenericParameter gp = DataTypeTransformations.createGenericParameter("eventResponse");
			ElementStructure responsePayload = DataTypeTransformations.wrapGenericParameterNodeAsElementStructure(gp);
			outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			outDtr.setPayload(responsePayload);
			result.setResponseMessage(outDtr);
		}
		result.setResponseMessage(outDtr);

		AtomicParameter eventSelection = DataTypeTransformations.createMetadataParameter(EVENT_QUERY_PARAMETER_NAME, "string");
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		inDtr.setPayload(DataTypeTransformations.wrapAtomicParameterAsElementStructure(eventSelection));
		result.setRequestMessage(inDtr);
		
		// could not add if already present, accepting duplicates and validation errors at present
		ec.getOps().add(result);
	}

	public EObject separateCommandsFromQueries(EndpointContract ept) {
		boolean hasWriters = false;
		ArrayList<Operation> readers = new ArrayList<>();
		// group operations into readers and writers
		for (Operation op : ept.getOps()) {
			OperationResponsibility opResp = op.getResponsibility();
			if (opResp != null && opResp.getRo() != null) {
				readers.add(op);
			} else {
				hasWriters = true;
			}
		}

		// leave STOs, STOs, CFOs and NNs where they are

		if (!readers.isEmpty() && hasWriters) {
			// move readers to new endpoint
			ept.getOps().removeAll(readers);
			ServiceSpecification ss = (ServiceSpecification) ept.eContainer();
			EndpointContract queryEndpointType = addReadModel(ss, ept.getName() + QUERIES_SUFFIX, readers);
			
			if(ept.getScenario()!=null) {
				queryEndpointType.setScenario(ept.getScenario());
			}
			
			if(ept.getFlow()!=null) {
				queryEndpointType.setFlow(ept.getFlow());
			}
			
			// TODO (L) primary used (PR/IHR); could copy all existing roles to secondary roles 
			
			// rename old endpoint
			ept.setName(ept.getName() + COMMANDS_SUFFIX);

			// move relevant parts of HTTP bindings (can be zero or more) of old endpoint too
			for (Operation nextRetrievalOperation : readers) {
				TransformationHelpers.findAndAdjustHTTPResourceBindingOfOperation(ss, ept, nextRetrievalOperation, queryEndpointType);
			}

			return ss;
		}

		return null;
	}

	private EndpointContract addReadModel(ServiceSpecification ss, String newEndpointName, Collection<Operation> readers) {
		EndpointContract readEPT = TransformationHelpers.findOrCreateEndpointType(ss, newEndpointName);
		readEPT.getOps().addAll(readers);
		return readEPT;
	}
}

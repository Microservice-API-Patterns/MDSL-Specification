package io.mdsl.transformations;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.CommandType;
import io.mdsl.apiDescription.CommandTypes;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.EventTypes;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;

public class TransformationHelpers {

	private static final String DATA_TYPE_ROLE = "D";
	private static final String REQUEST_BODY_IDENTIFIER = "RequestData";
	private static final String RESPONSE_BODY_IDENTIFIER = "Result";

	// ** story/scenario helpers

	public static IntegrationScenario findScenarioInSpecifcation(ServiceSpecification ss, String scenarioName) {
		for (IntegrationScenario is : ss.getScenarios()) {
			if (is.getName().equals(scenarioName)) { // could be channel too
				return is;
			}
		}
		return null;
	}

	// ** endpoint helpers

	public static EndpointContract findOrCreateEndpointType(ServiceSpecification ss, String suggestedName) {

		for (EObject eccc : ss.getContracts()) {
			if (eccc instanceof EndpointContract && ((EndpointContract) eccc).getName().equals(suggestedName)) { // could be channel too
				return (EndpointContract) eccc;
			}
		}
		// not found, so create:
		return createEndpointType(ss, suggestedName);
	}

	private static EndpointContract createEndpointType(ServiceSpecification ss, String endpointName) {
		EndpointContract ec = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		ec.setName(endpointName);
		ss.getContracts().add(ec);
		return ec;
	}

	public static EventType findOrCreateUniqueEventType(ServiceSpecification ss, String suggestedName) {
		List<EventType> eventTypes = EcoreUtil2.eAllOfType(ss, EventType.class);

		for (EventType event : eventTypes) {
			if (event.getName().equals(suggestedName)) {
				return event;
			}
		}

		// prepare domain event
		EventType de = ApiDescriptionFactory.eINSTANCE.createEventType();
		de.setName(suggestedName);

		EventTypes eventList = ApiDescriptionFactory.eINSTANCE.createEventTypes();
		eventList.getEvents().add(de);
		ss.getEvents().add(eventList);
		return de;
	}

	// TODO (M) offer boolean flag: append to existing event/command list (which one?) or create new
	
	public static CommandType findOrCreateUniqueCommandType(ServiceSpecification ss, String suggestedName) {
		List<CommandType> commandTypes = EcoreUtil2.eAllOfType(ss, CommandType.class);

		for (CommandType command : commandTypes) {
			if (command.getName().equals(suggestedName)) {
				return command;
			}
		}

		CommandType ct = ApiDescriptionFactory.eINSTANCE.createCommandType();
		ct.setName(suggestedName);
		CommandTypes cmdTypeList = ApiDescriptionFactory.eINSTANCE.createCommandTypes();
		cmdTypeList.getCommands().add(ct);
		ss.getCommands().add(cmdTypeList);
		return ct;
	}

	// ** operation helpers

	public static Operation findOperationInContract(EndpointContract ec, String opName) {
		for (Operation nextOp : ec.getOps()) {
			if (nextOp.getName().equals(opName)) {
				return nextOp;
			}
		}
		return null;
	}

	public static Operation createOperationWithAtomicRequestAndResponse(String name, String requestRole, String requestType, boolean deliversResponse, String responseType) {
		return createOperationWithAtomicParameters(name, requestRole, requestType, deliversResponse, DATA_TYPE_ROLE, responseType);
	}

	public static Operation createOperationWithAtomicParameters(String name, String requestRole, String requestType, boolean deliversResponse, String responseRole, String responseType) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();

		String opName = DataTypeTransformationHelpers.decapitalizeName(name);
		newOp.setName(opName);

		AtomicParameter inP = DataTypeTransformations.createAtomicDataParameter(opName + REQUEST_BODY_IDENTIFIER, requestType);
		inP.getRat().setRole(requestRole);
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		inDtr.setPayload(DataTypeTransformations.wrapAtomicParameterAsElementStructure(inP));
		newOp.setRequestMessage(inDtr);

		if (deliversResponse) {
			DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			AtomicParameter outP = DataTypeTransformations.createAtomicDataParameter(opName + RESPONSE_BODY_IDENTIFIER, responseType);
			outP.getRat().setRole(responseRole);
			outDtr.setPayload(DataTypeTransformations.wrapAtomicParameterAsElementStructure(outP));
			newOp.setResponseMessage(outDtr);
		}

		return newOp;
	}

	public static Operation createOperationWithAtomicParameterRequest(String opName, AtomicParameter ap) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();
		newOp.setName(opName);
		DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		outDtr.setPayload(DataTypeTransformations.wrapAtomicParameterAsElementStructure(ap));
		newOp.setRequestMessage(outDtr);
		return newOp;
	}

	public static Operation createOperationWithAtomicParameterResponse(String opName, AtomicParameter ap) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();
		newOp.setName(opName);
		DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		outDtr.setPayload(DataTypeTransformations.wrapAtomicParameterAsElementStructure(ap));
		newOp.setResponseMessage(outDtr);
		return newOp;
	}

	public static void setAtomicParameterNames(Operation operation, String inp, String outp) {
		try {
			operation.getRequestMessage().getPayload().getNp().getAtomP().getRat().setName(inp);
			operation.getResponseMessage().getPayload().getNp().getAtomP().getRat().setName(outp);
		} catch (Exception e) {
			throw new MDSLException("Operation " + operation.getName() + " should expect and deliver a single atomic parameter.");
		}
	}

	public static Operation createOperationWithIDParameterRequestAndTypeReferenceResponse(String name, String inParameterName, TypeReference responseType) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();
		String opName = DataTypeTransformationHelpers.decapitalizeName(name);
		newOp.setName(opName);

		AtomicParameter inP = DataTypeTransformations.createIDParameter(/* opName + REQUEST_BODY */ inParameterName);
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		inDtr.setPayload(DataTypeTransformations.wrapAtomicParameterAsElementStructure(inP));
		newOp.setRequestMessage(inDtr);

		DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		outDtr.setPayload(DataTypeTransformations.wrapTypeReferenceAsElementStructure(responseType));
		newOp.setResponseMessage(outDtr);

		return newOp;
	}

	public static Operation createOperationWithGenericParameters(String opName, String inName, String outName, boolean deliversResponse) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();
		String decapitalizedOpName = DataTypeTransformationHelpers.decapitalizeName(DataTypeTransformationHelpers.replaceSpacesWithUnderscores(opName));
		newOp.setName(decapitalizedOpName);

		GenericParameter inP = DataTypeTransformations.createGenericParameter(DataTypeTransformationHelpers.decapitalizeName(opName) + REQUEST_BODY_IDENTIFIER);
		if (inName != null) {
			inP.setName(inName); // could set to default
		}
		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		inDtr.setPayload(DataTypeTransformations.wrapGenericParameterNodeAsElementStructure(inP));
		newOp.setRequestMessage(inDtr);

		if (deliversResponse) {
			DataTransferRepresentation outDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
			outDtr.setPayload(DataTypeTransformations.wrapGenericParameterNodeAsElementStructure(DataTypeTransformations.createGenericParameter(DataTypeTransformationHelpers.decapitalizeName(opName) + RESPONSE_BODY_IDENTIFIER)));
			newOp.setResponseMessage(outDtr);
			if (outName != null) { // could set to default
				newOp.getResponseMessage().getPayload().getNp().getGenP().setName(outName);
			}
		}

		return newOp;
	}

	// ** operation binding helpers

	public static void findAndAdjustHTTPResourceBindingOfOperation(ServiceSpecification mdslSpecRoot, EndpointContract sourceEndpointType, Operation operation, EndpointContract targetEndpointType) {
		// look at all providers that expose the old/source endpoint contract via HTTP (or first one only in MVP)
		MDSLSpecificationWrapper msw = new MDSLSpecificationWrapper(new ServiceSpecificationAdapter(mdslSpecRoot));
		EndpointInstance httpb = msw.findFirstProviderAndHttpBindingFor(sourceEndpointType);
		if (httpb == null) {
			return;
		}

		// if operation is compensated by another operation in same endpoint, it cannot be moved (exception)
		HTTPOperationBinding opBindingToBeMoved = HTTPBindingTransformations.findFirstOperationBindingInEndpointInstance(operation.getName(), httpb);
		if (opBindingToBeMoved == null) {
			throw new MDSLException("Can't move operation " + operation.getName() + " because no binding for it was found in first HTTP resource.");
		}

		// remove operation binding as operation is moved out *is* needed here, EMF cannot take care of it. the get(0)s are ok here (already checked)
		try {
			HTTPResourceBinding currentResource = (HTTPResourceBinding) opBindingToBeMoved.eContainer();
			currentResource.getOpsB().remove(opBindingToBeMoved);
		} catch (Exception e) {
			throw new MDSLException("Source HTTP resource/operation binding does not have the expected structure.");
		}

		// if moving to an existing endpoint: check whether it already has a binding, create one if not
		EndpointInstance httpb2 = msw.findFirstProviderAndHttpBindingFor(targetEndpointType);
		if (httpb2 == null) {
			// when moving to a new endpoint: create an HTTP binding and move operation binding from source binding to it
			HTTPBindingTransformations hbts = new HTTPBindingTransformations();
			hbts.addBinding(targetEndpointType);
		} else {
			// finally, move operation binding because all preconditions have been checked, binding already exists
			try {
				httpb2.getPb().get(0).getProtBinding().getHttp().getEb().get(0).getOpsB().add(opBindingToBeMoved);				
			} catch (Exception e) {
				throw new MDSLException("Target HTTP resource/operation binding should have at least one HTTP protocol binding with one resource.");
			}
		}

		// note: not checking that the move may cause verb mapping conflicts in OASgen (other QFs can fix those)
	}

	// ** misc navigation helpers

	public static ElementStructure findElementStructureOf(TreeNode node) {
		if (node.getApl() != null) {
			return null; // NYI;
		} else if (node.getChildren() != null) {
			return DataTypeTransformations.wrapParameterTreeAsElementStructure(node.getChildren());
		} else if (node.getPn().getAtomP() != null) {
			return DataTypeTransformations.wrapAtomicParameterAsElementStructure(node.getPn().getAtomP());
		} else if (node.getPn().getGenP() != null) {
			return DataTypeTransformations.wrapGenericParameterNodeAsElementStructure(node.getPn().getGenP());
		} else if (node.getPn().getTr() != null) {
			return DataTypeTransformations.wrapTypeReferenceAsElementStructure(node.getPn().getTr());
		}

		return null;
	}

	public static boolean hasOperationOfName(EndpointContract endpointType, Operation opToBeMoved) {
		
		endpointType.getOps().stream()
			.map(Operation::getName) // this maps all operations to their name
			.anyMatch(name -> name.equals(opToBeMoved.getName())); // so we can then compare the name

		return false;
	}

	// TODO known limitation: case that PT is contained in element structure (e.g., in top of payload) not covered
	public static TreeNode findStereotypeInTree(ParameterTree pt, String decorator) {
		if (pt == null) {
			return null;
		}

		if (MAPDecoratorHelpers.isDecoratedWith(pt, decorator)) {
			EObject parent = pt.eContainer();
			if (parent instanceof TreeNode) {
				return (TreeNode) pt.eContainer();
			} else {
				return null;
			}
		}

		TreeNode firstNode = findStereotypeInTreeNode(pt.getFirst(), decorator);
		if (firstNode != null) {
			return firstNode;
		}

		for (TreeNode tnSibling : pt.getNexttn()) {
			TreeNode childNode = findStereotypeInTreeNode(tnSibling, decorator);
			if (childNode != null) {
				return childNode;
			}
		}

		return null;
	}

	public static TreeNode findStereotypeInTreeNode(TreeNode tn, String decorator) {
		if (tn.getApl() != null) {
			if (MAPDecoratorHelpers.isDecoratedWith(tn.getApl(), decorator)) {
				return tn;
			}
		} else if (tn.getPn() != null) {
			if (MAPDecoratorHelpers.isDecoratedWith(tn.getPn(), decorator)) {
				return tn;
			}
		} else if (tn.getChildren() != null) {
			if (MAPDecoratorHelpers.isDecoratedWith(tn.getChildren(), decorator)) {
				return tn;
			}

			// recurse:
			return findStereotypeInTree(tn.getChildren(), decorator);
		} else {
			throw new MDSLException("Unexpected tree node type."); // should not get here
		}

		return null;
	}

	public static EventType findOrCreateEventType(ServiceSpecification ss, String suggestedName) {
		boolean alreadyPresent = false;
		EventType result = null;
	
		for (EventTypes eventTypes : ss.getEvents()) {
			for (EventType eventType : eventTypes.getEvents()) {
				if (eventType.getName().equals(suggestedName)) {
					alreadyPresent = true;
					result = eventType;
					System.err.println("[W] " + suggestedName + " already exists as a domain event, not adding it.");
				}
			}
		}
	
		if (!alreadyPresent) {
			EventType de = ApiDescriptionFactory.eINSTANCE.createEventType();
			de.setName(suggestedName);
			AtomicParameter flag = DataTypeTransformations.createMetadataParameter("eventDetails", DataTypeTransformationHelpers.STRING);
			// could also add other event data
			de.setContent(DataTypeTransformations.wrapAtomicParameterAsElementStructure(flag));
	
			EventTypes des = ApiDescriptionFactory.eINSTANCE.createEventTypes();
			des.getEvents().add(de);
			ss.getEvents().add(des);
	
			return de;
		}
	
		return result;
	}
	
	public static String trimRoleName(String identifier) {
		return identifier.replaceAll("\"", "");
	}

	public static String nameOf(ElementStructure es) {
		if (es.getPt() != null) {
			return es.getPt().getName();
			// APL and PF not supported
		} else if (es.getNp() != null) {
			return DataTypeTransformationHelpers.nameOf(es.getNp());
		}
		return null;
	}
	
	public static void reportError(String message) {
		MDSLLogger.reportError(message);
	}

	public static void reportWarning(String message) {
		MDSLLogger.reportWarning(message);
	}
}

package io.mdsl.transformations;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.EndpointList;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.HTTPParameterBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.HTTPVerb;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.ProtocolBinding;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MDSLLogger;
import io.swagger.v3.oas.models.PathItem.HttpMethod;

public class HTTPBindingTransformations {

	private static final String DEFAULT_RESOURCE_NAME = "defaultResourceName";
	private static final String PROVIDER_SUFFIX = "Provider";
	private static final String HOME = "Home";
	private final static String DEFAULT_LOCATION = "http://localhost:8080";
	private static final String HTTP_BINDING = "HTTP";
	private static final String IDENTIFIER_ROLE = "Identifier";
	private static final String ID_ROLE = "ID";
	
	private static int resourceCounter = 1; // have a global counter?

	public HTTPBindingTransformations() {
	}

	// ** transformations

	public MDSLResource addBinding(EndpointContract contract) {

		ServiceSpecification ss = (ServiceSpecification) contract.eContainer();
		String contractName = contract.getName();
		Provider httpBindingProvider = ApiDescriptionFactory.eINSTANCE.createProvider();
		httpBindingProvider.setName(contractName + PROVIDER_SUFFIX);

		// add operations, apply verb mapping heuristics, put default URI and element mapping
		// TODO future work: bind/add MIME types, errors, status

		EndpointList epl = ApiDescriptionFactory.eINSTANCE.createEndpointList();
		epl.setContract(contract);
		httpBindingProvider.getEpl().add(epl);

		EndpointInstance epi = ApiDescriptionFactory.eINSTANCE.createEndpointInstance();
		epi.setLocation(DEFAULT_LOCATION); // TODO grammar cleanup/simplification?
		TechnologyBinding tb = ApiDescriptionFactory.eINSTANCE.createTechnologyBinding();
		ProtocolBinding pb = ApiDescriptionFactory.eINSTANCE.createProtocolBinding();
		HTTPBinding httpBinding = createHTTPBindingWithOperations(contractName + HOME, contract.getOps()); // name was defaultResourceName
		pb.setHttp(httpBinding);
		tb.setProtBinding(pb);
		epi.getPb().add(tb);
		epl.getEndpoints().add(epi);

		addBindingIfNameAvailable(ss, httpBindingProvider);

		return new MDSLResource(ss.eResource());
	}

	public void addHttpResourceDuringBindingSplit(HTTPResourceBinding hrb) {
		HTTPBinding hp = (HTTPBinding) hrb.eContainer();
		String resourceName = DataTypeTransformationHelpers.decapitalizeName(getEndpointTypeName(hp));

		HTTPResourceBinding newHrb = ApiDescriptionFactory.eINSTANCE.createHTTPResourceBinding();
		newHrb.setName(resourceName + resourceCounter++);
		newHrb.setUri(hrb.getUri()); // TODO (M) v55 might not be unique; was: "/" + resourceName + resourceCounter

		// find index of operations that map to verbs that already are in use and move them to new resource (might again have to be split)
		HashMap<HTTPVerb, Boolean> verbUsage = new HashMap<>();
		ArrayList<HTTPOperationBinding> hobsToBeMoved = new ArrayList<>();

		for (HTTPOperationBinding hob : hrb.getOpsB()) {
			HTTPVerb verb = hob.getMethod();
			if (verbUsage.get(verb) != null && verbUsage.get(verb).booleanValue() == true) {
				hobsToBeMoved.add(hob);
			} else {
				verbUsage.put(verb, true);
			}
		}

		for (HTTPOperationBinding hob : hobsToBeMoved) {
			hrb.getOpsB().remove(hob);
			newHrb.getOpsB().add(hob);
		}

		// could check that no resource of same name exists already (but also ok to have temporary name clash) 
		hp.getEb().add(newHrb);
	}

	public void addHttpParameterBindingsForElements(HTTPOperationBinding hopb) {
		String opName = hopb.getBoundOperation();

		TechnologyBinding tb = (TechnologyBinding) hopb.eContainer().eContainer().eContainer().eContainer();
		EndpointList el = (EndpointList) tb.eContainer().eContainer();
		Operation op = TransformationHelpers.findOperationInContract(el.getContract(), opName);

		bindElementsOfOperation(hopb, op);
	}

	public void addHttpResourceForURITemplate(HTTPOperationBinding hopb, String relativeUri) {
		HTTPResourceBinding oldResource = (HTTPResourceBinding) hopb.eContainer();
		HTTPBinding hb = (HTTPBinding) hopb.eContainer().eContainer();

		if (hb == null) {
			throw new MDSLException("HTTP binding must not be null.");
		}

		// create new resource and binding
		HTTPResourceBinding newResource;
		String newResourceName = oldResource.getName() + "_" + hopb.getBoundOperation();

		newResource = createNewHTTPResource(newResourceName, oldResource.getUri(), relativeUri);

		HTTPOperationBinding hobCopy = EcoreUtil.copy(hopb);

		// remove operation from old binding
		oldResource.getOpsB().remove(hopb);

		// add operation to new binding
		newResource.getOpsB().add(hobCopy);

		// add new resource
		hb.getEb().add(newResource);
	}

	public void addURITemplateToExistingHttpResource(HTTPOperationBinding hobp, String uriTemplate) {
		HTTPResourceBinding hrb = (HTTPResourceBinding) hobp.eContainer();
		hrb.setUri(hrb.getUri() + "/" + uriTemplate);
	}

	// ** binding helpers

	private static void addBindingIfNameAvailable(ServiceSpecification ss, Provider httpBindingProvider) {
		for (EObject providerOrBroker : ss.getProviders()) {
			if ((providerOrBroker instanceof Provider) && ((Provider) providerOrBroker).getName().equals(httpBindingProvider.getName())) {
				MDSLLogger.reportError("A provider with the name " + httpBindingProvider.getName() + " already exists. Not adding binding.");
				return;
			}
			// else: must be AsyncMDSL broker; check name anyway?
		}
		ss.getProviders().add(httpBindingProvider);
	}

	public static HTTPOperationBinding findFirstOperationBindingInEndpointInstance(String opName, EndpointInstance httpb) {
		HTTPOperationBinding opBindingToBeMoved = null;
		try {
			if (httpb.getPb().isEmpty()) {
				return null;
			}
			if (httpb.getPb().size() > 1) {
				MDSLLogger.reportWarning("More than one protocol binding found at " + httpb.getLocation() + " , using first one.");
			}

			if (httpb.getPb().get(0).getProtBinding().getHttp().getEb().isEmpty()) {
				return null;
			}
			if (httpb.getPb().get(0).getProtBinding().getHttp().getEb().size() > 1) {
				MDSLLogger.reportWarning("More than one HTTP binding found under " + httpb.getLocation() + " , using first one.");
			}

			for (HTTPResourceBinding htrb : httpb.getPb().get(0).getProtBinding().getHttp().getEb()) {
				for (HTTPOperationBinding htop : htrb.getOpsB()) {
					if (htop.getBoundOperation().equals(opName)) {
						return htop;
					}
				}
			}
		} catch (Exception e) {
			MDSLLogger.reportError(opName + " has an unexpected provider/binding structure.");
		}
		return opBindingToBeMoved;
	}

	private static HTTPBinding createHTTPBindingWithOperations(String homeResourceName, EList<Operation> operations) {
		HTTPBinding result = ApiDescriptionFactory.eINSTANCE.createHTTPBinding();
		result.setHttp(HTTP_BINDING);
		HTTPResourceBinding resource = ApiDescriptionFactory.eINSTANCE.createHTTPResourceBinding();
		resource.setName(homeResourceName);
		resource.setUri("/" + DataTypeTransformationHelpers.decapitalizeName(homeResourceName));
		result.getEb().add(resource);

		for (Operation operation : operations) {
			HTTPOperationBinding opB = ApiDescriptionFactory.eINSTANCE.createHTTPOperationBinding();
			opB.setBoundOperation(operation.getName());
			HTTPVerb verb = mapOperationToMethod(operation);
			opB.setMethod(verb);

			// add specific element bindings (according to chosen HTTP verb)
			mapElementsToParameters(opB, operation);
			// less user friendly:
			// opB.setGlobalBinding(defaultElementMappingFor(verb));

			resource.getOpsB().add(opB);
		}

		return result;
	}

	private static void mapElementsToParameters(HTTPOperationBinding opBinding, Operation operation) {
		bindElementsOfOperation(opBinding, operation);
	}

	private static void bindElementsOfOperation(HTTPOperationBinding opBinding, Operation op) {
		if (op == null) {
			MDSLLogger.reportError("AddHttpParameterBinding did not find the bound operation.");
		}

		// find request message, check whether it is PT
		if (op.getRequestMessage() == null) {
			MDSLLogger.reportError("The bound operation does not have a request message.");
		}
		if (op.getRequestMessage().getPayload() == null) {
			MDSLLogger.reportError("The bound operation does not have a request message payload.");
		}

		ParameterTree pt = op.getRequestMessage().getPayload().getPt();
		if (pt != null) {
			// could call MDSLSpecWrapper.isAtomicOrIsFlatParameterTree to make sure this binding generation and oasgen are in line
			// workaround was (no longer needed):
			// addParameterBindingsForNode(opBinding, pt);
			TreeNode tn = pt.getFirst();
			addParameterBindingsForNode(opBinding, tn);
			for(TreeNode child : pt.getNexttn()) {
				addParameterBindingsForNode(opBinding, child);
			}
			opBinding.setGlobalBinding(null);
		} else if (op.getRequestMessage().getPayload().getNp() != null) {
			addParameterBindingForSPN(opBinding, op.getRequestMessage().getPayload().getNp());
			opBinding.setGlobalBinding(null);
		}

		else {
			// TODO (L) also handle single APL and PF
			MDSLLogger.reportError("AddHttpParameterBinding only supports Parameter Trees and Atomic Parameters as request structures.");
		}
	}

	/*
	private static void addParameterBindingsForNode(HTTPOperationBinding opb, ParameterTree pt) {
		// create a binding per child; use HTTP verb to decide where to map to
		HTTPParameterBinding hpb = ApiDescriptionFactory.eINSTANCE.createHTTPParameterBinding();
		String elementName = pt.getName();
		if (elementName == null || elementName.isEmpty()) {
			elementName = "treeParameter";
		}
		hpb.setBoundParameter(elementName);
		HTTPParameter value = defaultParameterMappingFor(opb.getMethod());
		hpb.setParameterMapping(value);
		opb.getParameterBindings().add(hpb);
	}
	*/

	private static void addParameterBindingsForNode(HTTPOperationBinding opb, TreeNode tn) { 
		// create a binding per child; use HTTP verb to decide where to map to HTTPParameterBinding 
		HTTPParameterBinding hpb = ApiDescriptionFactory.eINSTANCE.createHTTPParameterBinding(); 
		String elementName = DataTypeTransformationHelpers.nameOf(tn); 
		hpb.setBoundParameter(elementName); 
		HTTPParameter value = defaultParameterMappingFor(opb.getMethod());
		// check whether tn contains an AP that has ID role 
		if(tn.getPn()!=null) { 
			SingleParameterNode spn = tn.getPn(); 
			if(hasIdentifierRole(spn)) {
				value = HTTPParameter.PATH; 
			}
		}
		hpb.setParameterMapping(value); 
		opb.getParameterBindings().add(hpb); 
	}

	private static void addParameterBindingForSPN(HTTPOperationBinding opb, SingleParameterNode spn) {

		HTTPParameterBinding hpb = ApiDescriptionFactory.eINSTANCE.createHTTPParameterBinding();
		// note: the name might be "anonymousNode", which will not be found by IDE Validator:
		hpb.setBoundParameter(DataTypeTransformationHelpers.nameOf(spn));
		HTTPParameter value = defaultParameterMappingFor(opb.getMethod());

		if (spn.getAtomP() != null && hasIdentifierRole(spn)) {
			value = HTTPParameter.PATH;
		}
		// TODO (M) tbd: investigate element structure of referenced type in case of a tref?

		hpb.setParameterMapping(value);
		opb.getParameterBindings().add(hpb);
	}

	private static HTTPResourceBinding createNewHTTPResource(String name, String uri, String relativeUri) {
		HTTPResourceBinding newHrb = ApiDescriptionFactory.eINSTANCE.createHTTPResourceBinding();

		if (name != null) {
			newHrb.setName(name);
		} else {
			newHrb.setName(DEFAULT_RESOURCE_NAME);
		}

		newHrb.setUri(uri + "/" + relativeUri);

		return newHrb;
	}

	// ** HTTP related:

	private static HTTPVerb mapOperationToMethod(Operation operation) {
		if (operation.getResponsibility() != null) {

			OperationResponsibility opRespo = operation.getResponsibility();
			if (opRespo.getSco() != null) {
				return HTTPVerb.PUT;
			}
			if (opRespo.getRo() != null) {
				return HTTPVerb.GET;
			}
			if (opRespo.getCf() != null) {
				return HTTPVerb.POST;
			}
			if (opRespo.getSro() != null) {
				return HTTPVerb.PATCH;
			}
			if (opRespo.getSdo() != null) {
				return HTTPVerb.DELETE;
			}
			if(opRespo.getOther() != null) {
				if(opRespo.getOther().equals("PUT")) {
					return HTTPVerb.PUT;
				}
				else if(opRespo.getOther().equals("GET")) {
						return HTTPVerb.GET;
					}
				else if(opRespo.getOther().equals("POST")) {
					return HTTPVerb.POST;
				}
				else if(opRespo.getOther().equals("PATCH")) {
					return HTTPVerb.PATCH;
				}
				else if(opRespo.getOther().equals("DELETE")) {
					return HTTPVerb.DELETE;
				}
			}
		}

		// copied and adopted from Endpoint2PathConverter:
		if (operation.getName().startsWith("create")) // changed
			return HTTPVerb.POST; // not needed as default return is HttpMethod.POST
		if (operation.getName().startsWith("addTo") || operation.getName().startsWith("add_to")) 
			return HTTPVerb.POST; // not needed as default return is HttpMethod.POST
		if (operation.getName().startsWith("get") || operation.getName().startsWith("read")
				|| operation.getName().startsWith("retrieve") || operation.getName().startsWith("search"))
			return HTTPVerb.GET;
		if (operation.getName().startsWith("put") || operation.getName().startsWith("replace"))
			return HTTPVerb.PUT;
		if (operation.getName().startsWith("patch") || operation.getName().startsWith("update")
				|| operation.getName().startsWith("modify"))
			return HTTPVerb.PATCH;
		if (operation.getName().startsWith("delete") || operation.getName().startsWith("remove"))
			return HTTPVerb.DELETE;
		
		// could add a detailed logging message
		// MDSLLogger.reportDetailedInformation("message");

		// last resort: map to POST as default
		return HTTPVerb.POST;

	}

	private static HTTPParameter defaultParameterMappingFor(HTTPVerb method) {
		switch (method) {
		case POST:
		case PUT:
		case PATCH:
			return HTTPParameter.BODY;
		case DELETE:
			return HTTPParameter.PATH;
		case GET:
			return HTTPParameter.QUERY;
		default:
			return HTTPParameter.BODY;
		}
	}

	// ** local helpers

	private static boolean hasIdentifierRole(SingleParameterNode spn) {
		if (spn.getAtomP() != null) {
			return spn.getAtomP().getRat().getRole().equals(ID_ROLE) || spn.getAtomP().getRat().getRole().equals(IDENTIFIER_ROLE);
		} 
		else if (spn.getGenP() != null) {
			return false;
			
		}
		else if (spn.getTr() != null) {
			ElementStructure es = spn.getTr().getDcref().getStructure();
			if(es.getNp()!=null) {
				// this will loop infinitely if type definition is recursive (known limitation, also elsewhere):
				return hasIdentifierRole(es.getNp());
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	private static String getEndpointTypeName(HTTPBinding hp) {
		// httpb -> protb -> techb -> ei -> el
		EndpointList el = (EndpointList) hp.eContainer().eContainer().eContainer().eContainer();
		return el.getContract().getName();
	}
}
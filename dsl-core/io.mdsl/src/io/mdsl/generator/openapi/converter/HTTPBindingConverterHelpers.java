package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.CustomMediaType;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.HTTPParameterBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.HTTPTypeBinding;
import io.mdsl.apiDescription.HTTPVerb;
import io.mdsl.apiDescription.LinkContract;
import io.mdsl.apiDescription.MediaTypeList;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RelationshipLink;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.StandardMediaType;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;

public class HTTPBindingConverterHelpers {

	private static final String DEFAULT_MEDIA_TYPE = "application/json"; 

	// ** finders 
	
	public static List<String> findMediaTypeForRequest(Operation mdslOperation, HTTPResourceBinding binding) {
		HTTPOperationBinding opB = findOperationBindingFor(mdslOperation.getName(), binding);

		if(opB==null || opB.getInContentTypes()==null) {
			List<String> defaultTypeList = new ArrayList<String>();
			defaultTypeList.add(DEFAULT_MEDIA_TYPE);
			return defaultTypeList;
		}

		// TODO also work with MIME type info in links (in endpoint type, in binding)

		return findMediaTypes(opB.getInContentTypes());		
	}

	public static List<String> findMediaTypeForResponse(Operation mdslOperation, HTTPResourceBinding binding) {
		HTTPOperationBinding opB = findOperationBindingFor(mdslOperation.getName(), binding);

		if(opB==null || opB.getOutContentTypes()==null) {
			List<String> defaultTypeList = new ArrayList<String>();
			defaultTypeList.add(DEFAULT_MEDIA_TYPE);
			return defaultTypeList;
		}

		return findMediaTypes(opB.getOutContentTypes());	
	}

	private static List<String> findMediaTypes(MediaTypeList mimeTypes) {
		List<String> result = new ArrayList<>();
		if(mimeTypes.getCmt()!=null) {
			EList<CustomMediaType> cmtl = mimeTypes.getCmt();
			if(cmtl!=null) {
				cmtl.forEach(cmt->result.add(cmt.getValue()));
			}
		}
		if(mimeTypes.getSmt()!=null) {
			EList<StandardMediaType> smtl = mimeTypes.getSmt();
			if(smtl!=null&&smtl.size()>0)
				smtl.forEach(smt->result.add(smt.getIanaName())); 
		}
		return result;
	}

	public static HTTPParameter defaultBindingFor(HttpMethod verb) {
		if(verbIsAllowedToHaveRequestBody(verb)) {
			// POST, PUT, PATCH:
			return HTTPParameter.BODY;
		}
		else { 
			// GET and DELETE (not using PATH for DELETE due to downstream tool constraints):
			return HTTPParameter.QUERY;
		}
	}

	public static HttpMethod mapMethod(Operation operation, HTTPResourceBinding binding) {
		HttpMethod result = null;

		// option 1: work with HTTPOperationBinding
		HTTPVerb verb = findVerbBindingFor(operation.getName(), binding);
		if(verb!=null)
			return mapMethodViaBinding(verb);

		// options 2 and 3: decorators (MAP or HTTP)
		OperationResponsibility reponsibility = operation.getResponsibility(); // only 1 at present
		if(reponsibility!=null) {
			result = mapMethodViaDecorator(reponsibility);
			if(result!=null) {
				return result;
			}
		}

		// option 4: name prefixes (heuristic, see "The Design of Web APIs")
		return mapMethodByName(operation);
	}

	public static HTTPVerb findVerbBindingFor(String operation, HTTPResourceBinding binding) {
		HTTPOperationBinding opB = findOperationBindingFor(operation, binding);
		if(opB!=null)
			return opB.getMethod();
		else 
			return null;
	}

	public static HTTPOperationBinding findOperationBindingFor(String operation, HTTPResourceBinding binding) {
		if(binding==null)
			return null;

		EList<HTTPOperationBinding> operationBindings = binding.getOpsB();
		for(int i=0;i<operationBindings.size();i++) {
			HTTPOperationBinding opB = operationBindings.get(i);
			if(opB.getBoundOperation().equals(operation)) {
				return opB;
			}
		}
		return null;
	}

	public static HTTPParameter findParameterBindingFor(String operation, String parameterName, HTTPResourceBinding binding) {
		if(binding==null)
			return null;

		HTTPOperationBinding opB = findOperationBindingFor(operation, binding);

		if(opB==null)
			return null; // no binding, so default mapping of "body" (?)

		if(opB.getGlobalBinding()!=null) {
			return opB.getGlobalBinding().getParameterMapping();
		}

		EList<HTTPParameterBinding> parameterBindings = opB.getParameterBindings();
		for(int i=0;i<parameterBindings.size();i++) {
			HTTPParameterBinding pB = parameterBindings.get(i);
			if(pB.getBoundParameter().equals(parameterName)) {
				return pB.getParameterMapping();
			}
		}

		if(parameterName==null) {
			MDSLLogger.reportInformation("No element binding for anonymous parameter found in binding " + binding.getName());
		}
		else {
			MDSLLogger.reportInformation("No element binding of \"" + parameterName + "\" found in binding " + binding.getName());
		}
		return null;
	}

	// ** link management 

	public static void handleLinks(Operation mdslOperation, HTTPResourceBinding binding, ApiResponse httpResponse) {
		// add link objects if present in MDSL endpoint type, see https://swagger.io/specification/#linkObject

		EList<RelationshipLink> hypermediaRelations = mdslOperation.getRelations();
		for(int j=0;j<hypermediaRelations.size();j++) {
			MDSLLogger.reportInformation("Processing link relations in " + mdslOperation.getName()); 
			RelationshipLink nextLink = hypermediaRelations.get(j);

			HTTPTypeBinding ltb = findLinkTypeBindingFor(nextLink.getLcref().getName(), binding);

			if(ltb==null)
				MDSLLogger.reportWarning("No binding found for " + nextLink.getName() + ", skipping this link." );
			else 
				handleSingleLink(nextLink, ltb, httpResponse);
		}

		Operation compensatingOperation = mdslOperation.getUndo();
		if(compensatingOperation!=null) {
			Link link = new Link().operationId(compensatingOperation.getName()); // operationRefRecommended
			httpResponse.link("compensatingOperation", link);
		}
		else {
			MDSLLogger.reportInformation("No compensating action found for " + mdslOperation.getName()); 
		}
	}

	public static HTTPTypeBinding findLinkTypeBindingFor(String name, HTTPResourceBinding binding) {
		EList<HTTPTypeBinding> tps = binding.getTB();
		for(HTTPTypeBinding tb : tps) {
			if(tb.getLt()!=null && tb.getLt().getName().equals(name)) {
				return tb;
			}
		}
		return null;
	}

	public static void handleSingleLink(RelationshipLink mdslLink, HTTPTypeBinding linkBinding, ApiResponse httpResponse) {

		String opId = "";
		if(linkBinding!=null)
			opId = ((HTTPResourceBinding)linkBinding.eContainer()).getName() + "-"; 

		if(mdslLink.getLcref().getOperation()!=null)
			opId += mdslLink.getLcref().getOperation();
		else
			opId = "unknownOperation";

		// TODO null check, use endpointResource-operation tuple for operationId
		Link oasLink = new Link().operationId(opId); 
		// TODO operationRefRecommended (but only works in same server)

		LinkContract refedLinkType = mdslLink.getLcref();
		if(refedLinkType!=null && refedLinkType.getDataType()!=null && refedLinkType.getDataType().getName()!=null) {
			oasLink.parameters(WordUtils.uncapitalize(mdslLink.getName()), refedLinkType.getDataType().getName());
			if(refedLinkType.getOperation()!=null)
				oasLink.description("Targeted operation: " + refedLinkType.getOperation());
			else 
				oasLink.description("Unknown target.");
		}

		// could also render entire info into single HAL or JSON-LD
		if(linkBinding.getHml().getLocal()!=null)
			oasLink.parameters("resource", linkBinding.getHml().getLocal().getName()); // uncapitalize name here too?
		else if(linkBinding.getHml().getExternal()!=null)
			oasLink.parameters("resource", linkBinding.getHml().getExternal());

		if(linkBinding.getHml().getVerb()!=null)
			oasLink.parameters("verb", linkBinding.getHml().getVerb().getName()); // uncapitalize name here too?

		if(linkBinding.getHml().getCmt()!=null)
			oasLink.parameters("cmt", linkBinding.getHml().getCmt().getName()); // uncapitalize name here too?

		// OAS link object has parameters, request body, headers, server
		// see https://swagger.io/docs/specification/links/ 

		// TODO (future work) check input (is optional) and add it to parameter/body element
		// need to work with parameter mapping for that: id, type name, mapping def. (body vs. query etc.)

		Server server = new Server();
		String targetAddress = null;
		if(mdslLink.getLcref().getUrn()!=null)
			targetAddress=mdslLink.getLcref().getUrn();
		if(linkBinding.getHml()!=null&&linkBinding.getHml().getExternal()!=null)
			targetAddress=linkBinding.getHml().getExternal();
		if(targetAddress!=null) {
			server.url(targetAddress);  
			oasLink.server(server);
		}

		httpResponse.link(WordUtils.uncapitalize(mdslLink.getName()), oasLink);
	}

	// ** MAP heuristics

	public static HttpMethod mapMethodViaDecorator(OperationResponsibility responsibility) {
		if (responsibility == null)
			return null; 
		else {
			// option 2: MAPs
			if (responsibility.getCf() != null)
				return HttpMethod.POST; // could also be GET (in parameters: OAS "deepObject")
			if (responsibility.getSco() != null)
				return HttpMethod.PUT; // could also be POST 
			if (responsibility.getRo() != null)
				return HttpMethod.GET; // also must be POSTed sometimes (requiring explicit binding)
			if (responsibility.getSto() != null)
				return HttpMethod.PATCH; // could also be PUT
			// new in V4.5
			if (responsibility.getSro() != null)
				return HttpMethod.PUT;
			if (responsibility.getSdo() != null)
				return HttpMethod.DELETE;

			// option 3: HTTP direct (defined as responsibility decorator)
			if (responsibility.getOther() != null && responsibility.getOther().equals("POST"))
				return HttpMethod.POST;
			if (responsibility.getOther() != null && responsibility.getOther().equals("PUT"))
				return HttpMethod.PUT;
			if (responsibility.getOther() != null && responsibility.getOther().equals("PATCH"))
				return HttpMethod.PATCH;
			if (responsibility.getOther() != null && responsibility.getOther().equals("GET"))
				return HttpMethod.GET;
			if (responsibility.getOther() != null && responsibility.getOther().equals("DELETE"))
				return HttpMethod.DELETE;
			// the verbs are not used much, and some servers, proxies etc. block them
			if (responsibility.getOther() != null && responsibility.getOther().equals("OPTIONS"))
				return HttpMethod.OPTIONS;
			if (responsibility.getOther() != null && responsibility.getOther().equals("HEAD"))
				return HttpMethod.HEAD;
			if (responsibility.getOther() != null && responsibility.getOther().equals("TRACE"))
				return HttpMethod.TRACE;
		}
		return null;
	}

	public static HttpMethod mapMethodViaBinding(HTTPVerb verb) {
		if (verb != null && verb == HTTPVerb.GET)
			return HttpMethod.GET;
		if (verb != null && verb == HTTPVerb.PUT)
			return HttpMethod.PUT;
		if (verb != null && verb == HTTPVerb.POST)
			return HttpMethod.POST;
		if (verb != null && verb == HTTPVerb.PATCH)
			return HttpMethod.PATCH;
		if (verb != null && verb == HTTPVerb.OPTIONS)
			return HttpMethod.OPTIONS;
		if (verb != null && verb == HTTPVerb.HEAD)
			return HttpMethod.HEAD;
		if (verb != null && verb == HTTPVerb.TRACE)
			return HttpMethod.TRACE;
		if (verb != null && verb == HTTPVerb.DELETE)
			return HttpMethod.DELETE;
		return null;
	}

	public static HttpMethod mapMethodByName(Operation operation) {
		if (operation.getName().startsWith("create")) // changed
			return HttpMethod.POST; // not needed as default return is HttpMethod.POST
		if (operation.getName().startsWith("addTo") || operation.getName().startsWith("add_to")) 
			return HttpMethod.POST; // not needed as default return is HttpMethod.POST
		if (operation.getName().startsWith("get") || operation.getName().startsWith("read")
				|| operation.getName().startsWith("retrieve") || operation.getName().startsWith("search"))
			return HttpMethod.GET;
		if (operation.getName().startsWith("put") || operation.getName().startsWith("replace"))
			return HttpMethod.PUT;
		if (operation.getName().startsWith("patch") || operation.getName().startsWith("update")
				|| operation.getName().startsWith("modify"))
			return HttpMethod.PATCH;
		if (operation.getName().startsWith("delete") || operation.getName().startsWith("remove"))
			return HttpMethod.DELETE;

		// last resort: map to POST as default
		MDSLSpecificationWrapper.logInformation("No verb mapping heuristic found for " + operation.getName());
		return HttpMethod.POST;
	}

	public static boolean verbIsAllowedToHaveRequestBody(HttpMethod verb) {
		return !(verb == HttpMethod.GET || verb == HttpMethod.DELETE);
	}

	// ** tree modifiers
	
	public static void addToNewParameterTree(ParameterTree newParameterTree, ParameterTree children) {
		TreeNode newTreeNode = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		newTreeNode.setChildren(EcoreUtil.copy(children)); // TODO might not be needed here, not storing it
		if(newParameterTree.getFirst()==null) {
			newParameterTree.setFirst(newTreeNode);
		}
		else {
			newParameterTree.getNexttn().add(newTreeNode);
		}
	}

	public static void addToNewParameterTree(ParameterTree newParameterTree, GenericParameter genP) {
		TreeNode newTreeNode = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setGenP(EcoreUtil.copy(genP));
		newTreeNode.setPn(spn); 
		if(newParameterTree.getFirst()==null) {
			newParameterTree.setFirst(newTreeNode);
		}
		else {
			newParameterTree.getNexttn().add(newTreeNode);
		}
	}

	public static void addToNewParameterTree(ParameterTree newParameterTree, AtomicParameter atomP) {
		TreeNode newTreeNode = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setAtomP(EcoreUtil.copy(atomP));
		newTreeNode.setPn(spn); 
		if(newParameterTree.getFirst()==null) {
			newParameterTree.setFirst(newTreeNode);
		}
		else {
			newParameterTree.getNexttn().add(newTreeNode);
		}
	}
	
	public static boolean hasAtLeastOneNode(ParameterTree parameterTree) {
		if(parameterTree.getFirst()==null) {
			return false;
		}
		else {
			return true;
		}
	}

	public static void addToNewParameterTree(ParameterTree newParameterTree, TypeReference tr) {
		TreeNode newTreeNode = ApiDescriptionFactory.eINSTANCE.createTreeNode();
		SingleParameterNode spn = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
		spn.setTr(EcoreUtil.copy(tr));
		newTreeNode.setPn(spn); 
		if(newParameterTree.getFirst()==null) {
			newParameterTree.setFirst(newTreeNode);
		}
		else {
			newParameterTree.getNexttn().add(newTreeNode);
		}
	}
}

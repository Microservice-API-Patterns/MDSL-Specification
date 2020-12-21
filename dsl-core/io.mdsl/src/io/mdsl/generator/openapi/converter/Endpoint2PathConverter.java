package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.HTTPTypeBinding;
import io.mdsl.apiDescription.HTTPVerb;
import io.mdsl.apiDescription.LinkContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.RelationshipLink;
import io.mdsl.apiDescription.SecurityBinding;
import io.mdsl.apiDescription.SecurityPolicies;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.StatusReport;
import io.mdsl.apiDescription.StatusReports;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MAPLinkResolver;
import io.mdsl.utils.MDSLSpecificationWrapper;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Converts an MDSL endpoint to an OpenAPI path item
 * 
 * @author ska, socadk
 *
 */
public class Endpoint2PathConverter {
	
	// TODO (tbd) set OAS version in generated file to 3.0.3

	// private static final String DEFAULT_MEDIA_TYPE = "application/json"; 
	private static final String DEFAULT_RESPONSE_NAME = "200";

	private DataType2SchemaConverter dataType2SchemaConverter; // TODO move to method?
	private DataType2ParameterConverter dataType2ParameterConverter; // TODO move to method?
	// private ServiceSpecificationAdapter mdslSpecification;
	private MDSL2OpenAPIConverter mdsl2OpenAPIConverter;
	private MDSLSpecificationWrapper mdslWrapper;
	
	public Endpoint2PathConverter(ServiceSpecificationAdapter apiDescriptionToBeConverted, MDSL2OpenAPIConverter mdsl2OpenAPIConverter) {
		// this.mdslSpecification = apiDescriptionToBeConverted;
		this.dataType2SchemaConverter = new DataType2SchemaConverter();
		this.dataType2ParameterConverter = new DataType2ParameterConverter(apiDescriptionToBeConverted);
		this.mdslWrapper = new MDSLSpecificationWrapper(apiDescriptionToBeConverted);
		this.mdsl2OpenAPIConverter = mdsl2OpenAPIConverter;
	}

	public PathItem convertMetadataAndOperations(EndpointContract endpointType, HTTPResourceBinding binding) {
		PathItem pathItemForResource = new PathItem();
		// path.setDescription(mapEndpointPattern(endpoint)); // does not look nice in Swagger editor 
		pathItemForResource.setSummary(MAPLinkResolver.explainRolePattern(endpointType));
		HashMap<HttpMethod, String> alreadyUsedVerbs = new HashMap<HttpMethod, String>();
		
		// TODO refactor (not respecting DRY):
		if(binding==null) {
			// bind all operations in single resource
			for (Operation operation : endpointType.getOps()) {
				HttpMethod verb = mapMethod(operation, binding);
				if (alreadyUsedVerbs.containsKey(verb)) {					
					throw new MDSLException("Mapping conflict in default resource " +  " (" + operation.getName() + "): operation " + alreadyUsedVerbs.get(verb) + " already maps to " + verb.toString()
					+ ". Start operation names with 'create', 'read', 'update', 'delete', add decorators (MAP responsibilities or HTTP verbs) or bind to mulitple resources.");
				}
				else {
					alreadyUsedVerbs.put(verb, operation.getName());
				}
				pathItemForResource.operation(verb, convertOperation(endpointType, operation, verb, binding));
			}
		}
		else {
			// only work with operations for which a binding exists in this resource
			for (Operation operation : endpointType.getOps()) {
				HTTPOperationBinding opb = mdslWrapper.findOperationBindingFor(operation.getName(), binding);
				
				if(opb==null) {
					mdslWrapper.logWarning("Operation *not* bound in resource " + binding.getName() + " " + operation.getName());
				}
				else {
					mdslWrapper.logInformation("Operation bound in this resource:" + binding.getName() + " " + operation.getName());

					HttpMethod verb = mapMethod(operation, binding);
					if (alreadyUsedVerbs.containsKey(verb)) {					
						throw new MDSLException("Mapping conflict in resource " + binding.getName() + " (" + operation.getName() + "): operation " + alreadyUsedVerbs.get(verb) + " already maps to " + verb.toString()
						+ ". Start operation names with 'create', 'read', 'update', 'delete', add decorators (MAP responsibilities or HTTP verbs) or bind to mulitple resources.");
					}
					else {
						alreadyUsedVerbs.put(verb, operation.getName());
					}
					pathItemForResource.operation(verb, convertOperation(endpointType, operation, verb, binding));

				}
			}
		}
		return pathItemForResource;
	}

	private io.swagger.v3.oas.models.Operation convertOperation(EndpointContract endpointType, Operation mdslOperation, HttpMethod verb, HTTPResourceBinding binding) {
		io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
		
		if(binding!=null)
			operation.setOperationId(binding.getName() + '-' + mdslOperation.getName()); // resource name needed to make operationId unique in OAS
		else 
			operation.setOperationId(mdslOperation.getName()); 
		
		operation.setSummary(MAPLinkResolver.explainResponsibilityPattern(mdslOperation));
		operation.setDescription(MAPLinkResolver.provideLinktoMAPWebsite(mdslOperation));
		List<String> tags = new ArrayList<String>();
		Tag rtag = mdsl2OpenAPIConverter.createTag(endpointType, binding, false);
		tags.add(rtag.getName());
		// tags.add(endpointType.getName());
		operation.setTags(tags);
		
		handleRequestMessage(endpointType, mdslOperation, verb, binding, operation);
		handleResponseMessages(mdslOperation, binding, operation);
		
		// TODO (M) support/handle endpoint-level security default (OAS concept? PathItem/Tags?)
		List<SecurityRequirement> securityRequrementList = handleSecurity(endpointType, mdslOperation, binding);
		if(securityRequrementList!=null)
			securityRequrementList.forEach(requirement->operation.addSecurityItem(requirement));
		
		return operation;
	}

	private void handleHeaders(EndpointContract endpointType, Operation mdslOperation, HttpMethod verb,
			HTTPResourceBinding binding, io.swagger.v3.oas.models.Operation operation, List<Parameter> parameterList) {
		ElementStructure headers = mdslOperation.getRequestMessage().getHeaders();
		if(headers==null)
			return;
	
		convertSingleRepresentationElement(endpointType, mdslOperation, verb, binding, operation, 
				HTTPParameter.HEADER, parameterList, headers, null); // ignoring any binding info 
	}

	private void handleRequestMessage(EndpointContract endpointType, Operation mdslOperation, HttpMethod verb,
			HTTPResourceBinding binding, io.swagger.v3.oas.models.Operation operation) {
		
		// 'expecting' part of operation signature 
		HTTPParameter boundParameter = HTTPParameter.BODY; // default
		List<Parameter> parameterList = new ArrayList<Parameter>();
		
		if(mdslWrapper.operationHasHeader(mdslOperation)) {
			// header should only have AP(L), can/must be mapped to OAS header parameters (binding ignored)
			handleHeaders(endpointType, mdslOperation, verb, binding, operation, parameterList);
		}
		
		if (mdslWrapper.operationHasPayload(mdslOperation)) {
			
			mdslWrapper.logInformation("Mapping payload of operation: " + mdslOperation.getName());
			ElementStructure operationPayload = mdslOperation.getRequestMessage().getPayload();
		    List<String> mediaTypes =  mdslWrapper.findMediaTypeForRequest(mdslOperation, binding);

			convertSingleRepresentationElement(endpointType, mdslOperation, verb, binding, operation, boundParameter,
					parameterList, operationPayload, mediaTypes);
		}
	}

	private void convertSingleRepresentationElement(EndpointContract endpointType, Operation mdslOperation,
			HttpMethod verb, HTTPResourceBinding binding, io.swagger.v3.oas.models.Operation operation,
			HTTPParameter boundParameter, List<Parameter> parameterList, ElementStructure operationPayload,
			List<String> mediaTypes) {
		
		if(operationPayload!=null && mdslWrapper.isAtomicOrIsFlatParameterTree(operationPayload)) {
			mdslWrapper.logInformation(" has atomic parameters only (or a flat tree element structure).");
			handleSimplePayload(endpointType, mdslOperation, verb, binding, operation, parameterList,
					operationPayload, mediaTypes);
		}
		else if(operationPayload!=null) {
			mdslWrapper.logInformation("This is a nested parameter tree or forest (or flat one with '*' or '+' cardinality)."); 
			handleComplexPayload(mdslOperation, verb, binding,
				// TODO simplify signature after semi-automatic refactoring:
				operation, boundParameter, operationPayload, mediaTypes, false);
		}
		else {
			// should not get here:
			mdslWrapper.logWarning("Empty or unsupported request payload structure in " + mdslOperation.getName());
		}
	}

	private void handleSimplePayload(EndpointContract endpointType, Operation mdslOperation, HttpMethod verb,
			HTTPResourceBinding binding, io.swagger.v3.oas.models.Operation operation, List<Parameter> parameterList,
			ElementStructure operationPayload, List<String> mediaTypes) {
		HTTPParameter boundParameter = defaultBindingFor(verb);
		List<AtomicParameter> apsInOp = mdslWrapper.extractElements(operationPayload);
		
		// TODO (handle cardinality here too: array of APL is *not* flat (?)
		
		for(int i=0;i<apsInOp.size();i++) {
			AtomicParameter nextParameter = apsInOp.get(i);
			if(nextParameter.getRat().getName()==null) {
				// a parameter that does not have a name cannot be bound
				boundParameter = this.defaultBindingFor(verb);
				mdslWrapper.logInformation("(" + verb.name() + "): anonymous parameter bound to: " + boundParameter.getLiteral()); 
			}
			else {
				boundParameter = mdslWrapper.findParameterBindingFor(mdslOperation.getName(), nextParameter.getRat().getName(), binding);
				if(boundParameter!=null)
					mdslWrapper.logInformation("(" + verb.name() + "): " + nextParameter.getRat().getName() + " bound to: " + boundParameter.getLiteral());
				else {
					boundParameter = this.defaultBindingFor(verb);
					// mdslWrapper.log("[D, MS] (" + verb.name() + "): " + nextParameter.getRat().getName() + " no binding found, using default: " + boundParameter.getLiteral()); 
				}
			}
			
			if(boundParameter.equals(HTTPParameter.BODY)) {
				if(!verbIsAllowedToHaveRequestBody(verb)) {
					throw new MDSLException("Unsupported verb-parameterType combination: " + verb.name() + " and " + boundParameter.getLiteral() + " cannot be used together.");
				}
				
				// TODO (H) does this replace the previous body elements? only useful for globally bound operations right now. need an addToRequestBody helper! 
				// mdslWrapper.logInformation("[NYI] Add this parameter to body: " + nextParameter.getRat().getName());
				operation.requestBody(createRequestBody(mdslOperation.getRequestMessage().getPayload(), mediaTypes));
			}
			else {
				// must be query, path, cookie; note: single representation element can explode to multiple parameters!
				List<Parameter> pl = this.dataType2ParameterConverter.convertSingleRepresentationElementToOneParameter(nextParameter, verb, boundParameter);
				parameterList.addAll(pl);
			}
		}
		operation.parameters(parameterList);
	}

	private void handleComplexPayload(
			Operation mdslOperation, 
			HttpMethod verb, 
			HTTPResourceBinding binding,
			io.swagger.v3.oas.models.Operation operation, 
			HTTPParameter boundParameter,
			ElementStructure operationPayload, 
			List<String> mediaTypes,
			boolean externalCardinality) {
		
		if(operationPayload.getPt() != null) {
			String tname = operationPayload.getPt().getName();
			
			// handle cardinality
			if(treeHasMultiplicity(operationPayload.getPt())) {
				// mdslWrapper.logInformation("[E] Need to handle cardinality of PT");
				externalCardinality = true;
			}
			
			boundParameter = mdslWrapper.findParameterBindingFor(mdslOperation.getName(), tname, binding);
			if(boundParameter==null) {
				// no binding, so use default (BODY for complex payload):
				boundParameter = HTTPParameter.BODY;
			}
			
			// mdslWrapper.logInformation("[MX] (" + verb.name() + "): "+ tname + " bound to: " + boundParameter.getLiteral()); 
			
			if(boundParameter==HTTPParameter.BODY) {
				if(verbIsAllowedToHaveRequestBody(verb)) {
					operation.requestBody(createRequestBody(mdslOperation.getRequestMessage().getPayload(), mediaTypes));
				}
				else 
					throw new MDSLException("Unsupported HTTPVerb-HTTPParameterType combination in " 
							+ mdslOperation.getName() + ": "
							+ verb.name() + " and " + boundParameter.getLiteral() + " cannot be used together.");
			}
			else {
				// must be query, path, cookie; note: single representation element can explode to multiple parameters!
				Parameter parameter = this.dataType2ParameterConverter.convertTree(operationPayload.getPt(), verb, boundParameter, externalCardinality);
				operation.addParametersItem(parameter);
			}
		} else if(operationPayload.getPf() != null) {
			mdslWrapper.logWarning(verb.name() + "): parameter forest bound to body"); 
			
			if(!verbIsAllowedToHaveRequestBody(verb)) {
				// forests are always mapped to body at present (not ok for GET and DELETE)
				throw new MDSLException("Known limitation: Parameter Forests can only be mapped to BODY at present, which is not possible for " + verb);
			}	
			
			// TODO cardinality (if possible and not done in DataType2SchemaConverter)
			
			operation.requestBody(createRequestBody(mdslOperation.getRequestMessage().getPayload(), mediaTypes));
		} else if (operationPayload.getNp().getTr() != null) {
			// call same method for references type:
			TypeReference referencedType = operationPayload.getNp().getTr();
			if(referencedType==null||referencedType.getDcref()==null) {
				throw new MDSLException("Type reference does not point at valid element structure (data type).");
			}

			boolean extCard = false;
			if(referenceHasMultiplicity(operationPayload.getNp().getTr())) {
				// mdslWrapper.logInformation("Need to handle cardinality of tref (NYI)");
				extCard = true;
			}
			ElementStructure payloadInReferencedType = referencedType.getDcref().getStructure();
			handleComplexPayload(mdslOperation, verb, binding, operation, boundParameter, payloadInReferencedType, mediaTypes, extCard);

		} else if (operationPayload.getNp().getGenP()!=null) {
			throw new MDSLException("Unexpected (complex) element structure: can't handle P and id-only here");
		}
		else {
			throw new MDSLException("Unexpected (complex) element structure.");
		}
	}
	
	private boolean treeHasMultiplicity(ParameterTree pt) {
		if(pt==null || pt.getCard()==null) 
			return false;
		
		return pt.getCard().getZeroOrOne()!=null
				|| pt.getCard().getAtLeastOne()!=null
				|| pt.getCard().getZeroOrMore()!=null;
	}
	
	private boolean referenceHasMultiplicity(TypeReference tr) {
		if(tr==null || tr.getCard()==null) 
			return false;
					
		return tr.getCard().getZeroOrOne()!=null
				|| tr.getCard().getAtLeastOne()!=null
				|| tr.getCard().getZeroOrMore()!=null;
	}
 	
	private HTTPParameter defaultBindingFor(HttpMethod verb) {
		if(verbIsAllowedToHaveRequestBody(verb)) {
			// POST, PUT, PATCH:
			return HTTPParameter.BODY;
		}
		else { 
			// GET and DELETE:
			return HTTPParameter.QUERY;
		}
	}

	private RequestBody createRequestBody(ElementStructure requestPayload, List<String> mediaTypes) {
		if(requestPayload!=null) {	    
			// TODO (M) add parameter description (name) 
			
			if(mediaTypes==null)
				mdslWrapper.logWarning("At least one media type must be defined.");
			else { 
				
				RequestBody result = new RequestBody();
				Content c = new Content();
				// mediaTypes.forEach(mediaType->c.addMediaType(mediaType, new MediaType().schema(getSchema4RequestOrResponseStructure(requestPayload))));
			
				MediaType item = new MediaType().schema(getSchema4RequestOrResponseStructure(requestPayload));
			
				for(int i=0;i<mediaTypes.size();i++) {
					c.addMediaType(mediaTypes.get(i), item);		
				}
			
				result.setContent(c);
				return result;
			}
			return null;
		}
		else 
			return null;
	}

	private void handleResponseMessages(Operation mdslOperation, HTTPResourceBinding binding,
			io.swagger.v3.oas.models.Operation operation) {
		
		// 'delivering' part of operation signature 
		if (mdslWrapper.operationHasReturnValue(mdslOperation)) {
		    List<String> mediaTypes =  mdslWrapper.findMediaTypeForResponse(mdslOperation, binding);
			
			ApiResponse apiResponse = createAPIResponse(mdslOperation.getResponseMessage().getPayload(), mdslOperation.getName() + " successful execution", mediaTypes);
			ApiResponses responseList = new ApiResponses().addApiResponse(DEFAULT_RESPONSE_NAME, apiResponse);

			handleLinks(mdslOperation, binding, apiResponse); 
			
			// TODO cardinality of response body (hopefully done in DataType2SchemaConverter)?
				
			// handle 'reporting`
			// (V5 input example) reporting error orderCreated "text":D<string> (more options available)
			if (mdslWrapper.operationHasReturnValueWithReports(mdslOperation)) {
				StatusReports reports = mdslOperation.getReports();
				EList<StatusReport> rl = reports.getReportList();

				for(int i=0;i<rl.size();i++) {
					ApiResponse reportResponse = null;
					String code = "x-999";
					String reportText = "tbd";
					StatusReport report = rl.get(i);
					String reportNameInEndpointType = report.getName();
					ElementStructure reportDataForResponse = report.getReportData();
							
					if(reportDataForResponse!=null) {
						reportResponse = createAPIResponse(reportDataForResponse, mdslOperation.getName() + ": " + reportText, mediaTypes);

						code = mdslWrapper.findReportCodeInBinding(mdslOperation.getName(), reportNameInEndpointType, binding);
						reportText = mdslWrapper.findReportTextInBinding(mdslOperation.getName(), reportNameInEndpointType, binding);
						reportResponse.description(reportText);
						
						// TODO cardinality of report response? 
						
					    // TODO (tbd) are there any more links in reports?
					    // TODO (tbd) what about media types of error responses?
						
						responseList.addApiResponse(code, reportResponse);
					}
				}
			}
			operation.responses(responseList);
		} else {
			operation.responses(new ApiResponses().addApiResponse(DEFAULT_RESPONSE_NAME, new ApiResponse().description("no return value").content(new Content())));
		}
	}

	private void handleLinks(Operation mdslOperation, HTTPResourceBinding binding, ApiResponse httpResponse) {
		// add link objects if present in MDSL endpoint type, see https://swagger.io/specification/#linkObject

		EList<RelationshipLink> hypermediaRelations = mdslOperation.getRelations();
		for(int j=0;j<hypermediaRelations.size();j++) {
			mdslWrapper.logInformation("Processing link relations in " + mdslOperation.getName()); 
			RelationshipLink nextLink = hypermediaRelations.get(j);
			
			HTTPTypeBinding ltb = mdslWrapper.findLinkTypeBindingFor(nextLink.getLcref().getName(), binding);

			// TODO the warning does not seem to make it (?(
			if(ltb==null)
				mdslWrapper.logWarning("No binding found for " + nextLink.getName() + ", skipping this link." );
			else 
				handleSingleLink(nextLink, ltb, httpResponse);
		}

		Operation compensatingOperation = mdslOperation.getUndo();
		if(compensatingOperation!=null) {
			// mdslWrapper.logInformation("[C] Compensating action found for " + mdslOperation.getName() + ": " + compensatingOperation.getName()); 
			Link link = new Link().operationId(compensatingOperation.getName()); // operationRefRecommended
			httpResponse.link("compensatingOperation", link);
		}
		else {
			; // mdslWrapper.logInformation("[C] No compensating action found for " + mdslOperation.getName()); 
		}
	}
	
	private void handleSingleLink(RelationshipLink abstractLink, HTTPTypeBinding linkBinding, ApiResponse httpResponse) {

		String opId = "";
		if(linkBinding!=null)
			opId = ((HTTPResourceBinding)linkBinding.eContainer()).getName() + "-"; 
		
		if(abstractLink.getLcref().getOperation()!=null)
			opId += abstractLink.getLcref().getOperation();
		else
			opId = "unknownOperation";
			
		// TODO null check, use endpointResource-operation tuple for opId
		Link link = new Link().operationId(opId); 
		// TODO operationRefRecommended (but only works in same server)
		
		LinkContract refedLinkType = abstractLink.getLcref();
		if(refedLinkType!=null && refedLinkType.getDataType()!=null && refedLinkType.getDataType().getName()!=null) {
			link.parameters(abstractLink.getName(), refedLinkType.getDataType().getName());
			if(refedLinkType.getOperation()!=null)
				link.description("Targeted operation: " + refedLinkType.getOperation());
			else 
				link.description("Unknown target.");
		}
		// else 
		//	link.parameters(abstractLink.getName(), "unknown link name and target");
		
		// could also render entire info into single HAL or JSON-LD
		if(linkBinding.getHml().getLocal()!=null)
			link.parameters("resource", linkBinding.getHml().getLocal().getName());
		else if(linkBinding.getHml().getExternal()!=null)
			link.parameters("resource", linkBinding.getHml().getExternal());
		
		if(linkBinding.getHml().getVerb()!=null)
			link.parameters("verb", linkBinding.getHml().getVerb().getName());
		
		if(linkBinding.getHml().getCmt()!=null)
			link.parameters("cmt", linkBinding.getHml().getCmt().getName());
		
		// OAS link object has parameters, request body, headers, server
		// see https://swagger.io/docs/specification/links/ 
		
		// TODO check input (is optional) and add it to parameter/body element
		// need to work with parameter mapping for that: id, type name, mapping def. (body vs. query etc.)
		
		Server server = new Server();
		String targetAddress = null;
		if(abstractLink.getLcref().getUrn()!=null)
			targetAddress=abstractLink.getLcref().getUrn();
		if(linkBinding.getHml()!=null&&linkBinding.getHml().getExternal()!=null)
			targetAddress=linkBinding.getHml().getExternal();
		if(targetAddress!=null) {
			server.url(targetAddress);  
			link.server(server);
		}
		
		httpResponse.link(abstractLink.getName(), link); // TODO decapitalize only first char
	}
	
	private List<SecurityRequirement> handleSecurity(EndpointContract endpointType, Operation mdslOperation, HTTPResourceBinding binding) {
		// https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#securitySchemeObject 
		// future work: check what WSDL, gRPC another target protocols need  
				
		List<SecurityRequirement> ss = new ArrayList<SecurityRequirement>(); 
		SecurityPolicies securityPolicies = mdslOperation.getPolicies();
		if(securityPolicies == null)
			return null;
		
		List<SecurityPolicy> secPols = securityPolicies.getPolicyList();
		for(int i=0;i<secPols.size();i++){
			SecurityPolicy securityPolicy = secPols.get(i);
			String spName = securityPolicy.getName();
			
			SecurityBinding boundPolicy = mdslWrapper.findPolicyInBinding(mdslOperation.getName(), spName, binding);
			
			this.mdsl2OpenAPIConverter.convertPolicy2SecurityScheme(securityPolicy, boundPolicy);
						
			if(boundPolicy!=null) {
				mdslWrapper.logInformation("Found a security policy " + spName + " for " + mdslOperation.getName()); 
					// + ": binding-level identifier is: " + boundPolicy.getDetails().getNp().getAtomP().getRat().getName());
				SecurityRequirement sr = new SecurityRequirement(); 
				sr.addList(spName); // does OAS know a "policyDescription"?
				ss.add(sr); 
			}
			else 
				mdslWrapper.logWarning("Found a security policy (but no binding)" + spName + " for " + mdslOperation.getName() + ". Skipping it so that the generated OpenAPI validates.");
		}
		
		return ss;
	}

	private ApiResponse createAPIResponse(ElementStructure responsePayload, String description, List<String> mediaTypes) {
		Content c = new Content();
		mediaTypes.forEach(mediaType->c.addMediaType(mediaType, new MediaType().schema(getSchema4RequestOrResponseStructure(responsePayload))));
		return new ApiResponse().description(description).content(c);
	}

	private Schema getSchema4RequestOrResponseStructure(ElementStructure payload) {
		if (payload.getNp() != null && payload.getNp().getTr() != null) {
			// case: reference to 'data type' declaration
			TypeReference tr = payload.getNp().getTr();
			return this.dataType2SchemaConverter.mapCardinalities(tr.getCard(), new Schema<>().$ref(DataType2SchemaConverter.REF_PREFIX + tr.getDcref().getName()));
		} else {
			// case: data structure defined inline in MDSL
			return this.dataType2SchemaConverter.convert(payload);
		}
	}

	private HttpMethod mapMethod(Operation operation, HTTPResourceBinding binding) {
		HttpMethod result = null;
		
		// option 1: work with HTTPOperationBinding
		HTTPVerb verb = mdslWrapper.findVerbBindingFor(operation.getName(), binding);
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

	private HttpMethod mapMethodViaDecorator(OperationResponsibility reponsibility) {
		if (reponsibility == null)
			// mdslWrapper.logInformation("No responsibility defined for " + operation.getName());
			return null; 
		else {
			// option 2: MAPs
			if (reponsibility.getCf() != null)
				return HttpMethod.POST; // could also be GET (in parameters: OAS "deepObject")
			if (reponsibility.getSco() != null)
				return HttpMethod.PUT; // could also be POST 
			if (reponsibility.getRo() != null)
				return HttpMethod.GET; // also must be POSTed sometimes (requiring explicit binding)
			if (reponsibility.getSto() != null)
				return HttpMethod.PATCH; // could also be PUT
			// new in V4.5
			if (reponsibility.getSro() != null)
				return HttpMethod.PUT;
			if (reponsibility.getSdo() != null)
				return HttpMethod.DELETE;
		
			// option 3: HTTP direct (defined as responsibility decorator)
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("POST"))
				return HttpMethod.POST;
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("PUT"))
				return HttpMethod.PUT;
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("PATCH"))
				return HttpMethod.PATCH;
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("GET"))
				return HttpMethod.GET;
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("DELETE"))
				return HttpMethod.DELETE;
			// the verbs are not used much, and some servers, proxies etc. block them
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("OPTIONS"))
				return HttpMethod.OPTIONS;
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("HEAD"))
				return HttpMethod.HEAD;
			if (reponsibility.getOther() != null && reponsibility.getOther().equals("TRACE"))
				return HttpMethod.TRACE;
		}
		return null;
	}

	private HttpMethod mapMethodViaBinding(HTTPVerb verb) {
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

	private HttpMethod mapMethodByName(Operation operation) {
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

		// last report: map to POST as default
		mdslWrapper.logInformation("No heuristic found for " + operation.getName() + ", mapping to POST");
		return HttpMethod.POST;
	}
			
	private boolean verbIsAllowedToHaveRequestBody(HttpMethod verb) {
		return !(verb == HttpMethod.GET || verb == HttpMethod.DELETE);
	}
}

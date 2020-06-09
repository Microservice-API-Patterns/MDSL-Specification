package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;

import com.google.common.collect.Sets;

import io.mdsl.apiDescription.atomicParameter;
import io.mdsl.apiDescription.elementStructure;
import io.mdsl.apiDescription.endpointContract;
import io.mdsl.apiDescription.operation;
import io.mdsl.apiDescription.operationResponsibility;
import io.mdsl.apiDescription.securityPolicy;
import io.mdsl.apiDescription.singleParameterNode;
import io.mdsl.apiDescription.statusReport;
import io.mdsl.apiDescription.typeReference;
import io.mdsl.exception.MDSLException;
import io.swagger.v3.oas.models.Operation;
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
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Converts an MDSL endpoint to an OpenAPI path item
 * 
 * @author ska, zio
 *
 */
public class Endpoint2PathConverter {

	private static final String MEDIA_TYPE = "application/json";
	private static final String DEFAULT_RESPONSE_NAME = "200";

	private DataType2SchemaConverter dataType2SchemaConverter;

	public Endpoint2PathConverter() {
		this.dataType2SchemaConverter = new DataType2SchemaConverter();
	}

	public PathItem convert(endpointContract endpoint) {
		PathItem path = new PathItem();
		path.setDescription(mapEndpointPattern(endpoint));
		path.setSummary(mapRolePattern(endpoint)); 
		Set<HttpMethod> alreadyUsedVerbs = Sets.newHashSet();
		for (operation operation : endpoint.getOps()) {
			HttpMethod verb = mapMethodByPattern(operation);
			if (verb == null) {
				verb = mapMethodByName(operation);
			}

			// TODO (medium prio) add operation name(s) to error message		
			if (alreadyUsedVerbs.contains(verb))
				throw new MDSLException("Mapping conflict (" + operation.getName() + "): another operation that maps to " + verb.toString()
						+ " already exists. Define distinct responsibilities via MAP decorators or HTTP verbs, or use create, read, update, delete as prefix.");
			else
				alreadyUsedVerbs.add(verb);
			path.operation(verb, convert(endpoint.getName(), operation, verb));
		}
		return path;
	}

	private Operation convert(String endpointName, operation mdslOperation, HttpMethod verb) {
		Operation operation = new Operation();
		operation.setOperationId(mdslOperation.getName());
		operation.setSummary(mapResponsibilityPattern(mdslOperation)); 
		operation.setDescription(provideLinktoMAPWebsite(mdslOperation));
		List<String> tags = new ArrayList<String>();
		tags.add(endpointName);
		operation.setTags(tags);

		// TODO (medium prio, when full binding is there) handle endpoint-level default

		/*
		// just a demo: "type: http scheme: basic", need to handle all four types from OAS spec. (they all have different APLs!
		// https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#securitySchemeObject
		// TODO move to binding and check what WSDL, gRPC and other target protocols need
		securityPolicy secPol = mdslOperation.getSp();
		if (secPol != null) {
			EList<elementStructure> so = secPol.getSecurityObject();
			elementStructure secPolType = so.get(0);
			
			// elementStructure secPolDescription = so.get(1);
			
			// TODO move to utility method (reporting needs almost the same)
		
			singleParameterNode spn = secPolType.getNp();
			if(spn==null) {
				throw new MDSLException("Only simple parameters can be used in policies.");
			}
			atomicParameter apn = spn.getAtomP();
			if(apn==null) {
				throw new MDSLException("Only atomic parameters can be used in policies.");
			}
			String policyType = apn.getRat().getName();
			if(policyType==null) {
				throw new MDSLException("The policy should have a name, which should match OAS conventions (in this version).");
			}
	
			// String policyDescription =
			// secPolDescription.getNp().getAtomP().getRat().getName();

			SecurityRequirement sr = new SecurityRequirement();
			sr.addList(policyType); // policyDescription);
			List<SecurityRequirement> ss = new ArrayList<SecurityRequirement>();
			ss.add(sr);
			operation.setSecurity(ss);
		}
		*/

		// handle 'expecting': uses requestBody, but creates parameters for GET and
		// DELETE case for now (only in case its an atomicParameterList)
		if (operationHasPayload(mdslOperation) && verbIsAllowedToHaveRequestBody(verb)) {
			operation.requestBody(createRequestBody(mdslOperation.getRequestMessage().getPayload()));
		} else if (operationHasPayload(mdslOperation)) {
			operation.parameters(createParameterList(mdslOperation.getRequestMessage().getPayload()));
		}

		// handle 'delivering'
		if (operationHasReturnValue(mdslOperation)) {
			ApiResponses responseList = new ApiResponses().addApiResponse(DEFAULT_RESPONSE_NAME, createAPIResponse(
					mdslOperation.getResponseMessage().getPayload(), "response message payload (success case)"));
			// TODO (medium prio) add pattern stereotype to description (if present)?

			// handle 'reporting`
			// TODO this is not a full solution yet, but a PoC
			// input example: reporting error "404":D<string>
			if (operationHasReturnValueWithReports(mdslOperation)) {
				statusReport repData = mdslOperation.getReportData();
				EList<elementStructure> rml = repData.getReportMessage();
				// TODO (high prio) work with all elements, not just first:
				elementStructure rm1 = rml.get(0);

				singleParameterNode spn = rm1.getNp();
				if(spn==null) {
					throw new MDSLException("Only simple parameters can be used in reports.");
				}
				atomicParameter apn = spn.getAtomP();
				if(apn==null) {
					throw new MDSLException("Only atomic parameters can be used in reports.");
				}
				String code = apn.getRat().getName();
				if(code!=null) {
					Integer.parseInt(code);
					// could catch and re-throw Exception 
				}
				else {
					throw new MDSLException("The reports should have a name, which should be a numeric status code (in this version).");
				}
				responseList.addApiResponse(unquoteString(code),
						createAPIResponse(rm1, "response message payload (error case)"));
			}

			operation.responses(responseList);
		} else {
			operation.responses(new ApiResponses().addApiResponse(DEFAULT_RESPONSE_NAME,
					new ApiResponse().description("no return value").content(new Content())));
		}
		return operation;
	}

	private boolean operationHasReturnValueWithReports(operation mdslOperation) {
		// TODO are really all three checks needed here? check grammar (low prio)
		return mdslOperation.getResponseMessage() != null && mdslOperation.getResponseMessage().getPayload() != null
				&& mdslOperation.getReportData() != null;
	}

	private String provideLinktoMAPWebsite(operation mdslOperation) {
		operationResponsibility responsibility = mdslOperation.getResponsibility();
		String uri = "https://microservice-api-patterns.org/patterns/responsibility/";
		String patternName;

		if (responsibility == null)
			return "unspecified operation responsibility";

		if (responsibility.getOther() != null) {
			return unquoteString(responsibility.getOther());
		}

		if (responsibility.getCf() != null) {
			patternName = "Computation Function";
			uri += "operationResponsibilities/ComputationFunction.html";
		} else if (responsibility.getSco() != null) {
			patternName = "State Creation Operation";
			uri += "operationResponsibilities/StateCreationOperation.html";
		} else if (responsibility.getRo() != null) {
			patternName = "Retrieval Operation";
			uri += "operationResponsibilities/RetrievalOperation.html";
		} else if (responsibility.getSto() != null) {
			patternName = "State Transition Operation";
			uri += "operationResponsibilities/StateTransitionOperation.html";
		} else
			return "unspecified operation responsibility";
		;
		return "This operation realizes the " + patternName + " pattern, described [on the MAP website](" + uri + ").";
	}
	
	public static String mapRolePattern(endpointContract mdslEndpoint) {
		String role1 = mdslEndpoint.getPrimaryRole();
		// TODO should also work with additional roles
		if (role1 == null)
			return null;

		if (role1.equals("PROCESSING_RESOURCE"))
			return "activity-oriented endpoint"; 
		else if (role1.equals("INFORMATION_HOLDER_RESOURCE"))
			return "general data-oriented endpoint"; 
		else if (role1.equals("OPERATIONAL_DATA_HOLDER"))
			return "data-oriented endpoint, short-lived"; 
		else if (role1.equals("MASTER_DATA_HOLDER"))
			return "data-oriented endpoint, long-lived"; 
		else if (role1.equals("REFERENCE_DATA_HOLDER"))
			return "data-oriented endpoint, immutable"; 
		else if (role1.equals("DATA_TRANSFER_RESOURCE"))
			return "data-oriented endpoint, temporary"; 
		else if (role1.equals("LINK_LOOKUP_RESOURCE"))
			return "data-oriented endpoint: directory"; 
		
		return role1;
	}

	public String mapResponsibilityPattern(operation mdslOperation) {
		operationResponsibility responsibility = mdslOperation.getResponsibility();
		if (responsibility == null)
			return null;

		if (responsibility.getCf() != null)
			return "no read, no write"; // return responsibility.getCf();
		if (responsibility.getSco() != null)
			return "write only"; // return responsibility.getSco();
		if (responsibility.getRo() != null)
			return "read only"; // return responsibility.getRo(); 
		if (responsibility.getSto() != null)
			return "read and write"; // return responsibility.getSto();
		
		if (responsibility.getOther() != null) {
			return unquoteString(responsibility.getOther());
		} else
			return ""; // unknown or not yet supported responsibility
	}

	private boolean verbIsAllowedToHaveRequestBody(HttpMethod verb) {
		return !(verb == HttpMethod.GET || verb == HttpMethod.DELETE);
	}

	private RequestBody createRequestBody(elementStructure requestPayload) {
		return new RequestBody().content(new Content().addMediaType(MEDIA_TYPE,
				new MediaType().schema(getSchema4RequestOrResponseStructure(requestPayload))));
	}

	private List<Parameter> createParameterList(elementStructure requestPayload) {
		return new DataType2ParameterConverter().convert(requestPayload);
	}

	private ApiResponse createAPIResponse(elementStructure responsePayload, String description) {
		return new ApiResponse().description(description).content(new Content().addMediaType(MEDIA_TYPE,
				new MediaType().schema(getSchema4RequestOrResponseStructure(responsePayload))));
	}

	private Schema getSchema4RequestOrResponseStructure(elementStructure payload) {
		if (payload.getNp() != null && payload.getNp().getTr() != null) {
			// case: reference to 'data type' declaration
			typeReference tr = payload.getNp().getTr();
			return this.dataType2SchemaConverter.mapCardinalities(tr.getCard(),
					new Schema<>().$ref(DataType2SchemaConverter.REF_PREFIX + tr.getDcref().getName()));
		} else {
			// case: data structure defined inline in MDSL
			return this.dataType2SchemaConverter.convert(payload);
		}
	}

	private boolean operationHasPayload(operation mdslOperation) {
		return mdslOperation.getRequestMessage() != null && mdslOperation.getRequestMessage().getPayload() != null;
	}

	private boolean operationHasReturnValue(operation mdslOperation) {
		return mdslOperation.getResponseMessage() != null && mdslOperation.getResponseMessage().getPayload() != null;
	}

	private String unquoteString(String string) {
		if (string == null || "".equals(string))
			return "";
		return string.replace("\"", "");
	}

	private String mapEndpointPattern(endpointContract endpoint) {
		String summary = "";
		if (endpoint.getPrimaryRole() != null && !"".equals(endpoint.getPrimaryRole()))			// TODO (high prio) lower case pattern name, MAP URI (see operation decorator)
			summary = "MAP link: " + endpoint.getPrimaryRole() + " available at [the MAP website](https://microservice-api-patterns.org/)";
		
		// TODO provide exact links, also map other endpoint pattern roles 
		//if (!endpoint.getOtherRoles().isEmpty())
		// 	summary = summary + ", " + String.join(", ", endpoint.getOtherRoles());
		return summary;
	}

	private HttpMethod mapMethodByPattern(operation operation) {
		operationResponsibility reponsibility = operation.getResponsibility(); // only 1 at present
		if (reponsibility == null)
			return null;

		// option 1: MAPs
		
		if (reponsibility.getCf() != null)
			return HttpMethod.POST; // could also be GET
		if (reponsibility.getSco() != null)
			return HttpMethod.PUT; // could also be POST
		if (reponsibility.getRo() != null)
			return HttpMethod.GET; // also must be POSTed sometimes (!)
		if (reponsibility.getSto() != null)
			return HttpMethod.PATCH; // could also be PUT
		
		// option 2: HTTP direct (TODO feature in documentation if this satys)
		if (reponsibility.getOther() !=null && reponsibility.getOther().equals("POST"))
			return HttpMethod.POST;
		if (reponsibility.getOther() !=null && reponsibility.getOther().equals("PUT"))
			return HttpMethod.PUT;
		if (reponsibility.getOther() !=null && reponsibility.getOther().equals("GET"))
			return HttpMethod.GET;
		if (reponsibility.getOther() !=null && reponsibility.getOther().equals("DELETE"))
			return HttpMethod.DELETE;
		if (reponsibility.getOther() !=null && reponsibility.getOther().equals("HEAD"))
			return HttpMethod.HEAD;
		if (reponsibility.getOther() !=null && reponsibility.getOther().equals("OPTIONS"))
			return HttpMethod.OPTIONS;
		if (reponsibility.getOther() !=null && reponsibility.getOther().equals("TRACE"))
			return HttpMethod.TRACE;

		return null;
	}

	private HttpMethod mapMethodByName(operation operation) {
		if (operation.getName().startsWith("create")) // changed
			return HttpMethod.POST; // not needed as default return is HttpMethod.POST 
		if (operation.getName().startsWith("get") || operation.getName().startsWith("read"))
			return HttpMethod.GET;
		if (operation.getName().startsWith("put"))
			return HttpMethod.PUT;
		if (operation.getName().startsWith("patch") || operation.getName().startsWith("update"))
			return HttpMethod.PATCH;
		if (operation.getName().startsWith("delete"))
			return HttpMethod.DELETE;
		
		// the verbs are not used much, and some servers, proxies etc. block them
//		if (operation.getName().startsWith("head"))
//			return HttpMethod.HEAD;
//		if (operation.getName().startsWith("options"))
//			return HttpMethod.OPTIONS;
//		if (operation.getName().startsWith("trace"))
//			return HttpMethod.TRACE;
		
		return HttpMethod.POST;
	}

}

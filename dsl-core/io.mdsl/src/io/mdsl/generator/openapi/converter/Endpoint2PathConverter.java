package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.SecurityBinding;
import io.mdsl.apiDescription.SecurityPolicies;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.StatusReport;
import io.mdsl.apiDescription.StatusReports;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MAPLinkResolver;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;
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
 * @author ska, socadk
 *
 */
public class Endpoint2PathConverter {
	
	private static final String CORRECTIVE_ACTION_TEXT = ". Rename operation to start with 'create', 'read', 'update', 'delete', add decorators (MAP responsibilities or HTTP verbs), assign MAP decorator, or bind to multiple resources.";
	private static final String METHOD_SUFFIX = " method)";
	private static final String DEFAULT_RESPONSE_NAME = "200";
	private static final String NO_RETURN_VALUE = "no return value";
	private static final String SUCCESSFUL_EXECUTION = " successful execution";
	private static final String X_999_CODE = "x-999";
	private static final String TBD_TEXT = "tbd";

	private DataType2SchemaConverter dataType2SchemaConverter; 
	private DataType2ParameterConverter dataType2ParameterConverter; 
	private MDSL2OpenAPIConverter mdsl2OpenAPIConverter;
	private MDSLSpecificationWrapper mdslWrapper;
	
	private EndpointContract endpointType;
	private Operation mdslOperation;
	private HTTPResourceBinding httpBinding;
	private HttpMethod httpVerb;
	private List<String> mediaTypes = null;
	private io.swagger.v3.oas.models.Operation oasOperation;
	
	private boolean eventMappingEnabled = false;
	
	public Endpoint2PathConverter(ServiceSpecificationAdapter apiDescriptionToBeConverted, MDSL2OpenAPIConverter mdsl2OpenAPIConverter) {
		this.dataType2SchemaConverter = new DataType2SchemaConverter();
		this.dataType2ParameterConverter = new DataType2ParameterConverter(apiDescriptionToBeConverted);
		this.mdslWrapper = new MDSLSpecificationWrapper(apiDescriptionToBeConverted);
		this.mdsl2OpenAPIConverter = mdsl2OpenAPIConverter;
	}

	public PathItem convertMetadataAndOperations(EndpointContract endpointType, HTTPResourceBinding binding) {
		this.endpointType = endpointType;
		this.httpBinding = binding;
		
		PathItem pathItemForResource = new PathItem();
		// does not look good in some OAS tools:
		// pathItemForResource.setDescription(endpointType.getName()); 
		
		pathItemForResource.setSummary(MAPLinkResolver.explainRolePattern(endpointType));
		HashMap<HttpMethod, String> alreadyUsedVerbs = new HashMap<HttpMethod, String>();
				
		if(httpBinding==null) {
			// bind all operations in single resource
			for (Operation operation : endpointType.getOps()) {
				httpVerb = HTTPBindingConverterHelpers.mapMethod(operation, httpBinding);
				if (alreadyUsedVerbs.containsKey(httpVerb)) {					
					throw new MDSLException("Mapping conflict in default resource " +  " (" + operation.getName() + "): operation " 
						+ alreadyUsedVerbs.get(httpVerb) + " already maps to " + httpVerb.toString() + CORRECTIVE_ACTION_TEXT);
				}
				else {
					alreadyUsedVerbs.put(httpVerb, operation.getName());
				}
				pathItemForResource.operation(httpVerb, convertOperation(operation));
			}
			if(eventMappingEnabled) {
				for (Event event : endpointType.getEvents()) {
					addEventToDescription(pathItemForResource, event);
					pathItemForResource.operation(PathItem.HttpMethod.POST, convertEvent(event, HttpMethod.POST));
				}
			}
		}
		else {			
			// note: only working with operations for which a binding exists in this resource
			for (Operation operation : endpointType.getOps()) {
				httpVerb = HTTPBindingConverterHelpers.mapMethod(operation, httpBinding);
				HTTPOperationBinding opb = HTTPBindingConverterHelpers.findOperationBindingFor(operation.getName(), this.httpBinding);
				
				if(opb==null) {
					MDSLLogger.reportInformation("Operation " + operation.getName() + " not bound in resource " + binding.getName());
				}
				else {
					MDSLLogger.reportInformation("Operation " + operation.getName() + " bound in resource " + binding.getName());
					HttpMethod verb = HTTPBindingConverterHelpers.mapMethod(operation, binding);
					if (alreadyUsedVerbs.containsKey(verb)) {					
						throw new MDSLException("Mapping conflict in resource " + binding.getName() + " (" + operation.getName() + "): operation " 
							+ alreadyUsedVerbs.get(verb) + " already maps to " + verb.toString() + CORRECTIVE_ACTION_TEXT);
					}
					else {
						alreadyUsedVerbs.put(verb, operation.getName());
					}
					pathItemForResource.operation(verb, convertOperation(operation));
				}
			}
			// TODO (future work) handle bound events here too
		}
		return pathItemForResource;
	}

	private io.swagger.v3.oas.models.Operation convertOperation(Operation mdslOperation) {
		this.mdslOperation = mdslOperation;
		this.oasOperation = new io.swagger.v3.oas.models.Operation();
		
		if(httpBinding!=null) {
			// note: resource name required to make operationId unique in OAS
			this.oasOperation.setOperationId(httpBinding.getName() + '-' + mdslOperation.getName()); 
		}
		else {
			this.oasOperation.setOperationId(mdslOperation.getName()); 
		}
		
		// use operation name so that OAS tools from Swagger show it prominently
		String stateImpactText = MAPLinkResolver.explainResponsibilityPattern(mdslOperation);
		if(stateImpactText!=null && !stateImpactText.equals("")) {
			oasOperation.setSummary(mdslOperation.getName() + " (" + MAPLinkResolver.explainResponsibilityPattern(mdslOperation) + METHOD_SUFFIX);
		}
		else {
			oasOperation.setSummary(mdslOperation.getName());
		}
		
		oasOperation.setDescription(MAPLinkResolver.specifyResponsibilityWithMAPLinkIfPossible(mdslOperation));
		
		List<String> tags = new ArrayList<String>();
		Tag rtag = mdsl2OpenAPIConverter.createTag(endpointType, httpBinding, false);
		tags.add(rtag.getName());
		oasOperation.setTags(tags);
		
		convertRequestMessage();
		convertResponseMessages();
		
		// TODO (future work) support/handle endpoint-level security default (OAS concept? PathItem/Tags?)
		List<SecurityRequirement> securityRequrementList = handleSecurity();
		if(securityRequrementList!=null)
			securityRequrementList.forEach(requirement->oasOperation.addSecurityItem(requirement));
		
		return oasOperation;
	}
	
	// ** events
	
	private void addEventToDescription(PathItem resource, Event event) {
		// TODO (future work) map event reception (if at all)
		if(resource.getDescription()==null)
			resource.setDescription("Receiving event(s): " + event.getType().getName());
		else
			resource.setDescription(resource.getDescription() + ", " + event.getType().getName());
	}

	private io.swagger.v3.oas.models.Operation convertEvent(Event event, HttpMethod verb) {
		io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();

		// TODO (future work) use event binding 

		List<String> tags = new ArrayList<String>();
		Tag rtag = mdsl2OpenAPIConverter.createTag(this.endpointType, httpBinding, false);
		tags.add(rtag.getName());
		operation.setTags(tags);
		
		// TODO handleEventMessage(endpointType, event, verb, binding, operation);
		// TODO response messages
		// TODO handle security (in grammar?)
		
		return operation;
	}
	
	// ** request 

	// 'expecting' part of operation signature 
	private void convertRequestMessage() {
	
		List<Parameter> parameterList = new ArrayList<Parameter>();
		this.mediaTypes =  HTTPBindingConverterHelpers.findMediaTypeForRequest(mdslOperation, httpBinding);
		
		if(mdslWrapper.operationHasHeader(mdslOperation)) {
			// header should only have AP(L), can/must be mapped to OAS header parameters (binding ignored)
			convertHeaders(parameterList, mediaTypes);
		}
		
		if (mdslWrapper.operationHasPayload(mdslOperation)) {			
			ElementStructure operationPayload = mdslOperation.getRequestMessage().getPayload();
			convertElementStructure(operationPayload, parameterList);
		}
	}
	
	private void convertHeaders(List<Parameter> parameterList, List<String> mediaTypes) {
		ElementStructure headerSpecification = this.mdslOperation.getRequestMessage().getHeaders();
		if(headerSpecification==null)
			return;
		
		// TODO (future work) support JWT as a special case? 
	
		List<AtomicParameter> headers = mdslWrapper.extractAtomicElements(headerSpecification);
		headers.forEach(header->convertRoleAndTypeAtom(header, HTTPParameter.HEADER, parameterList, mediaTypes)); 
	}

	private void convertElementStructure(ElementStructure content, List<Parameter> parameterList) {
		if(content==null) {
			MDSLLogger.reportWarning("Skipping empty or  payload structure in " + mdslOperation.getName() + " in " + endpointType.getName());
		}
		
		if(mdslWrapper.isSimplePayload(content)) {
			MDSLLogger.reportInformation(mdslOperation.getName() + " has atomic parameters only (embedded or type referenced).");
			convertSimplePayload(content, parameterList);
			if(parameterList.size()>0) {
				oasOperation.parameters(parameterList);
				// body not set here
			}
			return;
		}
		else {
			// must be complex payload
			MDSLLogger.reportInformation("Converting a nested parameter tree or forest (or flat one, e.g., with '*' or '+' cardinality or type reference)."); 
			convertComplexPayload(content, parameterList, false);
			return;
		}
	}
	
	private void convertSimplePayload(ElementStructure content, List<Parameter> parameterList) {
		if(content.getNp()!=null) { 
			convertSingleParameterNodeInElementStructure(content, parameterList);
		}
		else if(content.getApl()!=null) {
			convertAtomicParameterListInElementStructure(content, parameterList);
		}
		else if(content.getPt()!=null) {
			// should be flat according to isSimplePayload check, so can be handled just like APL
			convertAtomicParameterListInElementStructure(content, parameterList);
		}
		// Parameter Forest case not handled here
		else {
			MDSLLogger.reportError("Payload is not simple enough to be processed this way.");
		}
	}

	private void convertSingleParameterNodeInElementStructure(ElementStructure content, List<Parameter> parameterList)
			throws MDSLException {
		HTTPParameter parameterBinding;
		String parameterName;

		if(content.getNp().getTr()!=null) {
			// special case: type reference that might contain binding information
			MDSLLogger.reportInformation(this.mdslOperation.getName() + ": binding type reference payload " + content.getNp().getTr().getName());
			convertTopLevelTypeReference(content, parameterList);
		}
		else {
			// regular SPN case: AP or GenP (could also be bound individually!)
			MDSLLogger.reportDetailedInformation(this.mdslOperation.getName() + ": binding type reference payload regularly (embedded case)");
			parameterName = nameOfAtom(content);
			parameterBinding = bindParameterUseDefaultIfNoExplicitBindingExists(parameterName);

			if(parameterBinding.equals(HTTPParameter.BODY)) {
				MDSLLogger.reportInformation(mdslOperation.getName() + ": converting simple payload to request body");
				checkAndPerformBodyMappingOfTopLevelPayload(mediaTypes, parameterBinding);
			}
			else {
				MDSLLogger.reportInformation(mdslOperation.getName() + ": converting simple payload to request parameter(s)");
				// must be query, path, cookie; note: single representation element (MDSL parameter) can explode to multiple OAS/HTTP parameters
				List<Parameter> pl = this.dataType2ParameterConverter.convertSingleParameterNodeToOneParameter(content.getNp(), parameterBinding);
				parameterList.addAll(pl);
			}
		}
	}

	private void convertTopLevelTypeReference(ElementStructure content, List<Parameter> parameterList) {
		HTTPParameter parameterBinding;
		String parameterName;
		// note: short cut for this special case:
		parameterName = content.getNp().getTr().getName();
		parameterBinding = findLevel0Binding(parameterName);
		if(parameterBinding!=null) {
			MDSLLogger.reportInformation("Binding type reference payload on level 0 explicitly");
			handleLevel0TypeReferenceBinding(content, parameterList, mediaTypes, parameterBinding);
			return;
		}
		
		// try to find PT behind TR, look for binding for it
		boolean hasAtLeastOneLevel1Binding = hasLevel1Binding(parameterName, content.getNp().getTr());
		if(hasAtLeastOneLevel1Binding) {
			MDSLLogger.reportInformation("Binding type reference payload on level 1 explicitly");
			// note: this activates the parameter/body stitching 
			convertComplexPayload(content, parameterList, false);
			return;
		}
		else {
			MDSLLogger.reportInformation("Binding type reference payload on level 0 (default)?");
			parameterBinding = HTTPBindingConverterHelpers.defaultBindingFor(httpVerb);
			handleLevel0TypeReferenceBinding(content, parameterList, mediaTypes, parameterBinding);
			return;	
		}
	}

	private void handleLevel0TypeReferenceBinding(ElementStructure content, List<Parameter> parameterList,
			List<String> mediaTypes, HTTPParameter parameterBinding) {
		if(parameterBinding==HTTPParameter.BODY) {
			RequestBody requestBody = createRequestBodyForSingleParameterNode(content.getNp(), mediaTypes);
			oasOperation.setRequestBody(requestBody);
		}
		else {
			List<Parameter> parameters = dataType2ParameterConverter.convertSingleParameterNodeToOneParameter(content.getNp(), parameterBinding);
			parameterList.addAll(parameters);
		}
	}

	private HTTPParameter findLevel0Binding(String parameterName) {
		return HTTPBindingConverterHelpers.findParameterBindingFor(this.mdslOperation.getName(), parameterName, this.httpBinding);
	}
	
	private boolean hasLevel1Binding(String parameterName, TypeReference typeReference) {
		boolean foundAtLeastOneBindingForTreeElement = false;
		
		// note: using one simple case as L1 indicator only: first tree child is AP/GenP/TR, which is bound
		
		ElementStructure es = typeReference.getDcref().getStructure();
		ParameterTree pt = es.getPt(); // APL not supported, others a/ar
		
		if(pt==null ) {
			return false;
		}
		
		if(pt.getFirst().getPn()==null) {
			// TODO v55 this actually is an option that could be supported (edge case)
			return false;
		}
		
		if(pt.getFirst().getPn().getAtomP()!=null) {
			parameterName = pt.getFirst().getPn().getAtomP().getRat().getName();
		}
		else if (pt.getFirst().getPn().getGenP()!=null) {
			parameterName = pt.getFirst().getPn().getGenP().getName();
		}
		// bug fix (done):
		else if (pt.getFirst().getPn().getTr()!=null) {
			parameterName = pt.getFirst().getPn().getTr().getName();
		}
		
		HTTPParameter pb = HTTPBindingConverterHelpers.findParameterBindingFor(this.mdslOperation.getName(), parameterName, this.httpBinding);
		
		if(pb!=null) {
			foundAtLeastOneBindingForTreeElement = true;
		}
		else {
			MDSLLogger.reportInformation("Checked first tree node only to decide whether level 1 binding should be performed.");
			// TODO v55 try other tree elements too (APL)
		}
		
		return foundAtLeastOneBindingForTreeElement;
	}

	private void convertAtomicParameterListInElementStructure(ElementStructure operationPayload, List<Parameter> parameterList) {
		ParameterTree bodyElements = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		bodyElements.setName("request body elements for " + mdslOperation.getName());
		HTTPParameter boundParameter = null; // HTTPBindingConverterHelpers.defaultBindingFor(httpVerb);
		List<AtomicParameter> apsInOp = mdslWrapper.extractAtomicElements(operationPayload);
		MDSLLogger.reportDetailedInformation("Converting simple payload, operating on a generated pseudo-APL: " + apsInOp.size());
				
		for(int i=0;i<apsInOp.size();i++) {
			AtomicParameter nextParameter = apsInOp.get(i);
			if(nextParameter.getRat().getName()==null) {
				// note: a parameter that does not have a name cannot be bound individually 
				// (unless binding has an entry for some surrogate name); how about global bindings?
				MDSLLogger.reportDetailedInformation("Binding atomic parameter: " + nextParameter.getRat().getName());
				boundParameter = HTTPBindingConverterHelpers.defaultBindingFor(httpVerb);
			}
			else {
				MDSLLogger.reportDetailedInformation("Binding anonymouss atomic parameter");
				boundParameter = bindParameterUseDefaultIfNoExplicitBindingExists(nextParameter.getRat().getName());
			}
			
			logMapping(mdslOperation, httpVerb, nextParameter.getRat().getName(), boundParameter);
			
			if(boundParameter.equals(HTTPParameter.BODY)) {
				if(!HTTPBindingConverterHelpers.verbIsAllowedToHaveRequestBody(httpVerb)) {
					throw new MDSLException("Unsupported verb-parameterType combination for operation " + mdslOperation.getName() + ": " + httpVerb.name() + " and " + boundParameter.getLiteral() + " cannot be used together.");
				}
				else {
					HTTPBindingConverterHelpers.addToNewParameterTree(bodyElements, nextParameter);
				}
			}
			else {
				// must be query, path, cookie; note: single representation element (MDSL parameter) can explode to multiple OAS/HTTP parameters
				List<Parameter> pl = this.dataType2ParameterConverter.convertAtomicParameterToOneParameter(nextParameter, boundParameter);
				parameterList.addAll(pl);
			}
		}
		if(parameterList.size()>0) {
			oasOperation.parameters(parameterList);
		}
		schemaForCollectedBodyElements(bodyElements, oasOperation, mediaTypes);
	}

	private String nameOfAtom(ElementStructure operationPayload) {
		if(operationPayload.getNp()==null) {
			throw new MDSLException("Can only name atoms that appear in single parameter nodes.");
		}
		if(operationPayload.getNp().getAtomP()!=null) {
			return operationPayload.getNp().getAtomP().getRat().getName();
		}
		else if(operationPayload.getNp().getGenP()!=null) {
			return operationPayload.getNp().getGenP().getName();
		}
		else if(operationPayload.getNp().getTr()!=null) {
			// TODO check that TR references atom already here (to make sure call terminates)
			return nameOfAtom(operationPayload.getNp().getTr().getDcref().getStructure());
		}
		return null;
	}

	private void convertRoleAndTypeAtom(AtomicParameter ap, HTTPParameter parameterBinding, 
			List<Parameter> parameterList, List<String> mediaTypes) throws MDSLException {
		if(parameterBinding.equals(HTTPParameter.BODY)) {
			checkAndPerformBodyMappingOfTopLevelPayload(mediaTypes, parameterBinding);
		}
		else {
			// must be query, path, cookie; note: single representation element (MDSL parameter) can explode to multiple OAS/HTTP parameters
			List<Parameter> pl = this.dataType2ParameterConverter.convertAtomicParameterToOneParameter(ap, parameterBinding);
			parameterList.addAll(pl);
		}
	}
	
	private void convertGenericAtom(GenericParameter genP, HTTPParameter parameterBinding, 
			List<Parameter> parameterList, List<String> mediaTypes) throws MDSLException {
		if(parameterBinding.equals(HTTPParameter.BODY)) {
			checkAndPerformBodyMappingOfTopLevelPayload(mediaTypes, parameterBinding);
		}
		else {
			// must be query, path, cookie; note: single representation element (MDSL parameter) can explode to multiple OAS/HTTP parameters
			Parameter p = this.dataType2ParameterConverter.convertGenericParameter(genP, parameterBinding);
			parameterList.add(p);
		}
	}

	private void checkAndPerformBodyMappingOfTopLevelPayload(List<String> mediaTypes, HTTPParameter parameterBinding)
			throws MDSLException {
		if(!HTTPBindingConverterHelpers.verbIsAllowedToHaveRequestBody(httpVerb)) {
			throw new MDSLException("Unsupported verb-parameterType combination for operation " + mdslOperation.getName() + ": " + httpVerb.name() + " and " + parameterBinding.getLiteral() + " cannot be used together.");
		}
		
		ElementStructure requestPayload = mdslOperation.getRequestMessage().getPayload();
		oasOperation.requestBody(this.createRequestBodyForTopLevelPayload(requestPayload, mediaTypes));
	}
	
	private void convertComplexPayload(
			ElementStructure operationPayload, List<Parameter> parameterList, boolean externalCardinality) {
		
		if(operationPayload.getPt() != null) {
			convertParameterTree(operationPayload.getPt(), mediaTypes, externalCardinality);
		} else if (operationPayload.getPf() != null) {
			convertParameterForest(mediaTypes);
		} else if (operationPayload.getNp()!=null&&operationPayload.getNp().getTr() != null) {
			// (PoC) must have cardinality of '*' or '+', would be simplePayload otherwise
			convertTypeReferenceInElementStructure(operationPayload, parameterList);
		} else if (operationPayload.getNp()!=null&&operationPayload.getNp().getAtomP()!=null) {
			// (PoC) must have cardinality of '*' or '+', would be simplePayload otherwise
			convertAtomicParameterInElementStructure(operationPayload, parameterList);
		} else if (operationPayload.getNp()!=null&&operationPayload.getNp().getGenP()!=null) {
			convertGenericParameterInElementStructure(operationPayload, parameterList);
		}
		else if (operationPayload.getApl()!=null) {
			convertAtomicParameterListInElementStructure(operationPayload, parameterList);
		}
		else {
			throw new MDSLException("Unexpected (complex) element structure in operation " + mdslOperation.getName());
		}
	}

	private void convertGenericParameterInElementStructure(ElementStructure operationPayload, List<Parameter> parameterList) {
		// should not get here usually, handled by isSimplePayload check and logic
		HTTPParameter parameterBinding = bindParameterUseDefaultIfNoExplicitBindingExists(operationPayload.getNp().getGenP().getName());
		convertGenericAtom(operationPayload.getNp().getGenP(), parameterBinding, parameterList, mediaTypes);
		for(Parameter parameterItem : parameterList) {
			oasOperation.addParametersItem(parameterItem);
		}
	}

	private void convertAtomicParameterInElementStructure(ElementStructure operationPayload, List<Parameter> parameterList) throws MDSLException {
		HTTPParameter parameterBinding = bindParameterUseDefaultIfNoExplicitBindingExists(operationPayload.getNp().getAtomP().getRat().getName());
		// should not get here usually, handled by isSimplePayload check and logic
		convertRoleAndTypeAtom(operationPayload.getNp().getAtomP(), parameterBinding, parameterList, mediaTypes);
		for(Parameter parameterItem : parameterList) {
			oasOperation.addParametersItem(parameterItem);
		}
	}
	
	private void convertTypeReferenceInElementStructure(ElementStructure messageElement, List<Parameter> parameterList) throws MDSLException {
		HTTPParameter parameterBinding = HTTPBindingConverterHelpers.findParameterBindingFor(mdslOperation.getName(), messageElement.getNp().getTr().getName(), httpBinding);
		MDSLLogger.reportInformation(mdslOperation.getName() + ": convertTypeReferenceInElementStructure converting type reference " + messageElement.getNp().getTr().getName());
		if(parameterBinding!=null) {
			MDSLLogger.reportInformation("Binding entire type reference: " + parameterBinding.getLiteral());
			convertTypeReferenceAndSetEntireRequestBody(messageElement.getNp().getTr(), parameterBinding, parameterList, mediaTypes);
		}
		else {			
			// note: this caused inner tree elements to be traversed, rather than entire PT as one binding entity (fixed)
			ElementStructure referencedType = messageElement.getNp().getTr().getDcref().getStructure();
			if(referencedType.getPt()!=null) {
				MDSLLogger.reportDetailedInformation("Following type reference to locate binding (is PT), reference name is " + messageElement.getNp().getTr().getName());
				convertParameterTree(referencedType.getPt(), mediaTypes, false);
				return;
			}
			else if(referencedType.getNp()!=null) {
				MDSLLogger.reportDetailedInformation("Following type reference to locate binding (is SPN), reference name is " + messageElement.getNp().getTr().getName());
				convertSimplePayload(referencedType, parameterList);
				return;
			}
			else if(referencedType.getApl()!=null) {
				MDSLLogger.reportDetailedInformation("Following type reference to locate binding (is APL), reference name is " + messageElement.getNp().getTr().getName());
				convertAtomicParameterListInElementStructure(messageElement, parameterList);
				return;
			}
		}
	}
				
	private void convertTypeReferenceAndSetEntireRequestBody(TypeReference referencedType, HTTPParameter parameterBinding, List<Parameter> parameterList, List<String> mediaTypes) {
		boolean extCard = false;
		if(mdslWrapper.referenceHasMultiplicity(referencedType)) {
			extCard = true;
		}
		
		if(parameterBinding.equals(HTTPParameter.BODY)) {
			if(!HTTPBindingConverterHelpers.verbIsAllowedToHaveRequestBody(httpVerb)) {
				throw new MDSLException("Unsupported verb-parameterType combination for operation " + mdslOperation.getName() + ": " + httpVerb.name() + " and " + parameterBinding.getLiteral() + " cannot be used together.");
			}
			
			MDSLLogger.reportInformation(mdslOperation.getName() + " creating scheme reference for " + referencedType.getName() + " -> " + referencedType.getDcref().getName());
			// not passing extCard in because createSchemaForTypeReference call mapCardinalities, which checks cardinality of referencedType 
			Schema requestPayload = dataType2SchemaConverter.createSchemaForTypeReference(referencedType); 
			
			Content c = createContentFromSchemaAndMediaTypes(mediaTypes, requestPayload);
			RequestBody rb = new RequestBody();
			rb.setContent(c);
			rb.setDescription("Request body for type " + referencedType.getName()); 
			oasOperation.requestBody(rb);
		}
		else {
			// note: this indirection starts at the top of the logic again
			ElementStructure referencedStructure = referencedType.getDcref().getStructure();
			convertComplexPayload(referencedStructure, parameterList, extCard);
		}
	}
	
	// TODO could make mediaTypes a class instance variable too

	private void convertParameterTree(ParameterTree pt, List<String> mediaTypes, boolean externalCardinality) throws MDSLException {
		HTTPParameter parameterBinding;
		String treeName = pt.getName();
		
		if(mdslWrapper.treeHasMultiplicity(pt)) {
			externalCardinality = true;
		}
 
		parameterBinding = HTTPBindingConverterHelpers.findParameterBindingFor(mdslOperation.getName(), treeName, this.httpBinding);
		if(parameterBinding!=null) {
			MDSLLogger.reportInformation(mdslOperation.getName() + " in " + endpointType.getName()+  ": binding entire tree as DTO in payload.");
			convertParameterTreeAsSingleBoundParameter(pt, parameterBinding, mediaTypes, externalCardinality);
		}
		else {
			MDSLLogger.reportInformation(mdslOperation.getName() + " in " + endpointType.getName()+  ": binding via level 1 tree nodes in payload.");
			// assuming that convertParameterTreeViaLevel1TreeNodeTraversal is only used here
			convertParameterTreeViaLevel1TreeNodeTraversal(pt, parameterBinding, mediaTypes); 
		}
	}

	private void convertParameterForest(List<String> mediaTypes) throws MDSLException {
		MDSLLogger.reportWarning(httpVerb.name() + ": parameter forest bound to body"); 
		
		if(!HTTPBindingConverterHelpers.verbIsAllowedToHaveRequestBody(httpVerb)) {
			// forests are always mapped to body at present (which is not ok for GET and DELETE; workaround: replace forest with tree)
			throw new MDSLException("Known limitation: Parameter Forests can only be mapped to BODY at present, which is not possible for " + mdslOperation.getName() + " and " + httpVerb);
		}	
		oasOperation.requestBody(createRequestBodyForTopLevelPayload(mdslOperation.getRequestMessage().getPayload(), mediaTypes));
	}

	private void convertParameterTreeViaLevel1TreeNodeTraversal(ParameterTree pt, HTTPParameter parameterBinding, List<String> mediaTypes) {
		
		MDSLLogger.reportDetailedInformation(mdslOperation.getName() + " in convertParameterTreeViaLevel1TreeNodeTraversal");

		List<Parameter> parameterList = new ArrayList<Parameter>();
		List<TreeNode> treeNodes = mdslWrapper.collectTreeNodes(pt);
		
		ParameterTree parameterTreeForBody = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		// merge BODY snippets of all nextNode elements in case multiple of them (AP/PT) map to BODY
		// do not create the body in loop but collect them in a new PT, which is then converted at the end 
		parameterTreeForBody.setName("request body elements for " + mdslOperation.getName());
		
		// iterate over all children, first and next (no recursion, top-level only) and add one parameter each
		for(TreeNode nextNode : treeNodes) {
			if(nextNode.getPn()!=null) { // is AP, GenP, TR
				MDSLLogger.reportDetailedInformation(mdslOperation.getName() + " is SPN");
				parameterBinding = handleInnerSingleParameterNodeInComplexPayload(parameterBinding, parameterList, parameterTreeForBody, nextNode);
			}
			else if (nextNode.getChildren()!=null) { // is Parameter Tree
				// note: caused problems in test case 4 (header vs. path), fixed:
				MDSLLogger.reportDetailedInformation(mdslOperation.getName() + " is PT, calling handleInnerParameterTreeInComplexPayload for " + nextNode.getChildren().getName() + " while processing " + pt.getName());
				// parameterBinding = handleInnerParameterTreeInComplexPayload(pt.getName(), nextNode, parameterList, parameterTreeForBody);
				parameterBinding = handleInnerParameterTreeInComplexPayload(nextNode.getChildren(), parameterList, parameterTreeForBody);
			}
			else if (nextNode.getApl()!=null) { // is Atomic Parameter List
				MDSLLogger.reportWarning("Not supported: mapping of APL in " + mdslOperation.getName());
			}
		}
		
		if(parameterList.size()>0) {
			oasOperation.parameters(parameterList);
		}	
		schemaForCollectedBodyElements(parameterTreeForBody, oasOperation, mediaTypes);
	}

	private void schemaForCollectedBodyElements(
			ParameterTree parameterTreeForBody, io.swagger.v3.oas.models.Operation oasOperation, List<String> mediaTypes) {
		RequestBody ptSchema = this.createRequestBodyForParameterTree(parameterTreeForBody, mediaTypes);
		MDSLLogger.reportDetailedInformation("Entering schemaForCollectedBodyElements");
		if(ptSchema!=null) {
			ptSchema.setDescription("Message payload (content)"); 
			oasOperation.requestBody(ptSchema);
		}
	}

	private HTTPParameter handleInnerParameterTreeInComplexPayload(ParameterTree treeNode, List<Parameter> parameterList, ParameterTree parameterTreeForBody) {
		HTTPParameter parameterBinding;		
		MDSLLogger.reportDetailedInformation("Entering handleInnerParameterTreeInComplexPayload");
		
		boolean treeCardinality = false;
		if(mdslWrapper.treeHasMultiplicity(treeNode)) {
			treeCardinality = true;
		}
		
		parameterBinding = this.bindParameterUseDefaultIfNoExplicitBindingExists(treeNode.getName());
		
		MDSLLogger.reportInformation(mdslOperation.getName() + ": adding to " + parameterBinding + " payload/parameter " + treeNode.getName());
		if(parameterBinding==HTTPParameter.BODY) {
			HTTPBindingConverterHelpers.addToNewParameterTree(parameterTreeForBody, treeNode);
		}
		else {
			parameterList.add(dataType2ParameterConverter.convertParameterTree(treeNode, parameterBinding, treeCardinality));
		}
		
		return parameterBinding;
	}

	private HTTPParameter handleInnerSingleParameterNodeInComplexPayload(
			HTTPParameter parameterBinding, List<Parameter> parameterList, ParameterTree parameterTreeForBody, TreeNode nextNode) {
		MDSLLogger.reportDetailedInformation(mdslOperation.getName() + " in handleInnerSingleParameterNodeInComplexPayload");
		if(nextNode.getPn().getAtomP()!= null) {
			// note: different from genP and PT treatment in that it always creates and returns a parameterBinding (default?)
			parameterBinding = handleInnerAtomicParameterInComplexPayload(parameterList, parameterTreeForBody, nextNode);
		}
		else if(nextNode.getPn().getGenP()!= null) {
			parameterBinding = handleGenericParameterInComplexPayload(parameterList, parameterTreeForBody, nextNode);
		}
		else if(nextNode.getPn().getTr()!= null) {
			String trName = nextNode.getPn().getTr().getName();		
			MDSLLogger.reportDetailedInformation(mdslOperation.getName() + " in handleInnerSingleParameterNodeInComplexPayload, next node is TR " + trName);

			parameterBinding = this.bindParameterUseDefaultIfNoExplicitBindingExists(trName);
			logMapping(mdslOperation, httpVerb, trName, parameterBinding);
		
			// ElementStructure payloadInReferencedType = nextNode.getPn().getTr().getDcref().getStructure();
			
			if(HTTPParameter.BODY.equals(parameterBinding)) {
				MDSLLogger.reportDetailedInformation(mdslOperation.getName() + " in handleInnerSingleParameterNodeInComplexPayload, body mapping");
				HTTPBindingConverterHelpers.addToNewParameterTree(parameterTreeForBody, nextNode.getPn().getTr());
			}
			else {
				parameterList.addAll(dataType2ParameterConverter.convertSingleParameterNodeToOneParameter(nextNode.getPn(), parameterBinding));
			}
		}
		return parameterBinding;
	}

	private HTTPParameter handleGenericParameterInComplexPayload(List<Parameter> parameterList,
			ParameterTree parameterTreeForBody, TreeNode nextNode) {
		HTTPParameter parameterBinding;
		parameterBinding = HTTPBindingConverterHelpers.findParameterBindingFor(mdslOperation.getName(), nextNode.getPn().getGenP().getName(), this.httpBinding);
		if(parameterBinding==null) {
			parameterBinding = HTTPBindingConverterHelpers.defaultBindingFor(httpVerb);
		}
		if(HTTPParameter.BODY.equals(parameterBinding)) {
			HTTPBindingConverterHelpers.addToNewParameterTree(parameterTreeForBody, nextNode.getPn().getAtomP());
			logMapping(mdslOperation, httpVerb, nextNode.getPn().getGenP().getName(), parameterBinding);
		}
		else {
			logMapping(mdslOperation, httpVerb, nextNode.getPn().getAtomP().getRat().getName(), parameterBinding);
			parameterList.addAll(dataType2ParameterConverter.convertSingleParameterNodeToOneParameter(nextNode.getPn(), parameterBinding));
		}
		return parameterBinding;
	}

	private HTTPParameter handleInnerAtomicParameterInComplexPayload(List<Parameter> parameterList, ParameterTree parameterTreeForBody, TreeNode nextNode) {
		HTTPParameter parameterBinding;
		parameterBinding = HTTPBindingConverterHelpers.findParameterBindingFor(mdslOperation.getName(), nextNode.getPn().getAtomP().getRat().getName(), this.httpBinding);
		if(parameterBinding==null) {
			parameterBinding = HTTPBindingConverterHelpers.defaultBindingFor(httpVerb);
		}			
		if(HTTPParameter.BODY.equals(parameterBinding)) {
			logMapping(mdslOperation, httpVerb, nextNode.getPn().getAtomP().getRat().getName(), parameterBinding);
			HTTPBindingConverterHelpers.addToNewParameterTree(parameterTreeForBody, nextNode.getPn().getAtomP());
			// checkAndPerformBodyMappingOfSimpleParameterNode(mdslOperation, httpVerb, nextNode.getPn(), oasOperation, parameterBinding, mediaTypes);
		}
		else {
			logMapping(mdslOperation, httpVerb, nextNode.getPn().getAtomP().getRat().getName(), parameterBinding);
			parameterList.addAll(dataType2ParameterConverter.convertSingleParameterNodeToOneParameter(nextNode.getPn(), parameterBinding));
		}
		return parameterBinding;
	}
	
	private void convertParameterTreeAsSingleBoundParameter(ParameterTree pt, HTTPParameter parameterBinding, List<String> mediaTypes, 
			boolean externalCardinality) throws MDSLException {
		if(parameterBinding==HTTPParameter.BODY) {
			if(HTTPBindingConverterHelpers.verbIsAllowedToHaveRequestBody(httpVerb)) {
				oasOperation.requestBody(createRequestBodyForTopLevelPayload(mdslOperation.getRequestMessage().getPayload(), mediaTypes));
			}
			else {
				String ptName = pt.getName();
				if(ptName==null) {
					ptName = "unnamed";
				}
				throw new MDSLException("Unsupported HTTPVerb-HTTPParameterType combination in " 
						+ mdslOperation.getName() + ", parameter: " + ptName + ": "
						+ httpVerb.name() + " and " + parameterBinding.getLiteral() + " cannot be used together.");
			}
		}
		else {
			// must be query, path, cookie; note: single representation element can explode to multiple parameters!
			Parameter parameter = this.dataType2ParameterConverter.convertParameterTree(pt, parameterBinding, externalCardinality);
			oasOperation.addParametersItem(parameter);
		}
	}
	
	private RequestBody createRequestBodyForSingleParameterNode(SingleParameterNode spn, List<String> mediaTypes) {
		if(spn==null) {
			throw new MDSLException("Invalid operation invocation: expected a non-empty parameter tree node.");
		}

		if(mediaTypes==null) {
			MDSLLogger.reportError("At least one media type must be defined.");
		}

		MDSLLogger.reportDetailedInformation("Entering createRequestBodyForSingleParameterNode");
		
		RequestBody result = new RequestBody();
		Content requestBodyContent = new Content();
		Schema schema = dataType2SchemaConverter.createSchema4SingleParameterNode(spn);
		MediaType item = new MediaType().schema(schema);

		for(int i=0;i<mediaTypes.size();i++) {
			requestBodyContent.addMediaType(mediaTypes.get(i), item);		
		}

		result.setContent(requestBodyContent);
		return result;
	}

	private RequestBody createRequestBodyForTopLevelPayload(ElementStructure messageRepresentation, List<String> mediaTypes) {
		if(messageRepresentation!=null) {	    
			if(mediaTypes==null) {
				MDSLLogger.reportWarning("At least one media type must be defined.");
			}
			else { 
				RequestBody result = new RequestBody();
				// mediaTypes.forEach(mediaType->c.addMediaType(mediaType, new MediaType().schema(getSchema4RequestOrResponseStructure(requestPayload))));
				Schema newSchema = this.dataType2SchemaConverter.getSchema4RequestOrResponseStructure(messageRepresentation);
				Content c = createContentFromSchemaAndMediaTypes(mediaTypes, newSchema);
				result.setContent(c);
				return result;
			}
			return null;
		}
		else {
			return null;
		}
	}
	
	private RequestBody createRequestBodyForParameterTree(ParameterTree parameterTree, List<String> mediaTypes) {
		if(parameterTree!=null) {	    
			if(mediaTypes==null) {
				MDSLLogger.reportWarning("At least one media type must be defined.");
			}
			else { 				
				if(HTTPBindingConverterHelpers.hasAtLeastOneNode(parameterTree)) {
					RequestBody result = new RequestBody();
					// respect cardinality of incoming PT
					Schema newSchema = this.dataType2SchemaConverter.convertAndCreateSchema4ParameterTreeAndItsNodes(parameterTree, mdslWrapper.treeHasMultiplicity(parameterTree));
					newSchema.setDescription(parameterTree.getName()); 
					Content c = createContentFromSchemaAndMediaTypes(mediaTypes, newSchema);
					result.setContent(c);
					return result;
				}
				else {
					return null;
				}
			}
			return null;
		}
		else 
			return null;
	}
	
	// ** response

	private void convertResponseMessages() {
		
		// 'delivering' part of operation signature 
		if (mdslWrapper.operationHasReturnValue(mdslOperation)) {
		    List<String> mediaTypes =  HTTPBindingConverterHelpers.findMediaTypeForResponse(mdslOperation, httpBinding);
			
			ApiResponse apiResponse = createAPIResponse(mdslOperation.getResponseMessage().getPayload(), mdslOperation.getName() + SUCCESSFUL_EXECUTION, mediaTypes);
			ApiResponses responseList = new ApiResponses().addApiResponse(DEFAULT_RESPONSE_NAME, apiResponse);

			HTTPBindingConverterHelpers.handleLinks(mdslOperation, httpBinding, apiResponse); 
				
			// handle 'reporting`
			// input example: `reporting error orderCreated "text":D<string>` (more options available)
			if (mdslWrapper.operationHasReturnValueWithReports(mdslOperation)) {
				StatusReports reports = mdslOperation.getReports();
				EList<StatusReport> rl = reports.getReportList();

				for(int i=0;i<rl.size();i++) {
					ApiResponse reportResponse = null;
					String code = X_999_CODE;
					String reportText = TBD_TEXT;
					StatusReport report = rl.get(i);
					String reportNameInEndpointType = report.getName();
					ElementStructure reportDataForResponse = report.getReportData();
							
					if(reportDataForResponse!=null) {
						reportResponse = createAPIResponse(reportDataForResponse, mdslOperation.getName() + ": " + reportText, mediaTypes);

						code = mdslWrapper.findReportCodeInBinding(mdslOperation.getName(), reportNameInEndpointType, httpBinding);
						reportText = mdslWrapper.findReportTextInBinding(mdslOperation.getName(), reportNameInEndpointType, httpBinding);
						reportResponse.description(reportText);
						
						// TODO (future work) cardinality of report response (top level)? description?
					    // TODO (future work) are there any more links in reports?
					    // TODO (future work) what about media types of error responses?
						
						responseList.addApiResponse(code, reportResponse);
					}
				}
			}
			oasOperation.responses(responseList);
		} else {
			oasOperation.responses(new ApiResponses().addApiResponse(DEFAULT_RESPONSE_NAME, new ApiResponse().description(NO_RETURN_VALUE).content(new Content())));
		}
	}
	
	private ApiResponse createAPIResponse(ElementStructure responsePayload, String description, List<String> mediaTypes) {
		Content c = new Content();
		mediaTypes.forEach(mediaType->c.addMediaType(mediaType, new MediaType().schema(this.dataType2SchemaConverter.getSchema4RequestOrResponseStructure(responsePayload))));
		return new ApiResponse().description(description).content(c);
	}
	
	// ** local helpers
	
	private Content createContentFromSchemaAndMediaTypes(List<String> mediaTypes, Schema<?> newSchema) {
		Content c = new Content();
		MediaType item = new MediaType().schema(newSchema);
		for(int i=0;i<mediaTypes.size();i++) {
			c.addMediaType(mediaTypes.get(i), item);		
		}
		return c;
	}
	
	// TODO (future work) support what WSDL, gRPC another target protocols need
	
	private List<SecurityRequirement> handleSecurity() {
		// https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#securitySchemeObject 
		  
		List<SecurityRequirement> ss = new ArrayList<SecurityRequirement>(); 
		SecurityPolicies securityPolicies = mdslOperation.getPolicies();
		if(securityPolicies == null)
			return null;
		
		List<SecurityPolicy> secPols = securityPolicies.getPolicyList();
		for(int i=0;i<secPols.size();i++){
			SecurityPolicy securityPolicy = secPols.get(i);
			String spName = securityPolicy.getName();
			SecurityBinding boundPolicy = mdslWrapper.findPolicyInBinding(mdslOperation.getName(), spName, httpBinding);
			
			this.mdsl2OpenAPIConverter.convertPolicy2SecurityScheme(securityPolicy, boundPolicy);
						
			if(boundPolicy!=null) {
				MDSLLogger.reportInformation("Found a security policy " + spName + " for " + mdslOperation.getName()); 
				SecurityRequirement sr = new SecurityRequirement(); 
				sr.addList(spName); // does OAS know a "policyDescription"?
				ss.add(sr); 
			}
			else 
				MDSLLogger.reportWarning("Found a security policy (but no binding)" + spName + " for " + mdslOperation.getName() + ". Skipping it so that the generated OpenAPI validates.");
		}
		
		return ss;
	}

	private HTTPParameter bindParameterUseDefaultIfNoExplicitBindingExists(String parameterName) {
		HTTPParameter parameterBinding = HTTPBindingConverterHelpers.findParameterBindingFor(this.mdslOperation.getName(), parameterName, this.httpBinding);
		if(parameterBinding==null) {
			parameterBinding = HTTPBindingConverterHelpers.defaultBindingFor(httpVerb);
		}
		logMapping(this.mdslOperation, this.httpVerb, parameterName, parameterBinding);
		return parameterBinding;
	}

	private void logMapping(Operation mdslOperation, HttpMethod httpVerb, String nextParameterName, HTTPParameter boundParameter) {
		if(mdslOperation==null) {
			MDSLLogger.reportError("MDSL operation not defined yet.");
			return;
		}
		if(httpVerb==null) {
			MDSLLogger.reportWarning(mdslOperation.getName() + " does not map to an HTTP verb yet.");
			return;
		}
		
		String nextParameterNameForLog;
		if(nextParameterName==null) {
			nextParameterNameForLog = "anonymous";
		}
		else {
			nextParameterNameForLog = nextParameterName;
		}
				
		if(boundParameter==null) {
			MDSLLogger.reportInformation(mdslOperation.getName() + " (" + httpVerb.name() + "): " + nextParameterNameForLog + " not bound");
			return;
		}
		else {
			MDSLLogger.reportInformation(mdslOperation.getName() + " (" + httpVerb.name() + "): " + nextParameterNameForLog + " bound to: " + boundParameter.getLiteral());  
		}
	}
}

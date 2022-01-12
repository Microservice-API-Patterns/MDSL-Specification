package io.mdsl.generator.openapi.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.DirectionList;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.EndpointList;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.OASSecurity;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.SecurityBinding;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MAPLinkResolver;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;
import io.mdsl.utils.URITemplateHelper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Converts MDSL ServiceSpecification to an OpenAPI specification
 * 
 * @author ska, socadk
 * 
 */
public class MDSL2OpenAPIConverter {

	private static final String JWT_BEARER_FORMAT = "JWT";
	private static final String API_KEY_SCHEME_NAME = "api_key";
	private static final String BEARER_SCHEME_NAME = "bearer";
	private static final String BASIC_SCHEME_NAME = "basic";
	private static final String DEFAULT_VERSION = "1.0"; // use different value? (preference)
	private static final String ROLE_SUFFIX = " role";
	private static final String CONTRACT_SUFFIX = " contract";
	
	private ServiceSpecificationAdapter mdslSpecification;
	private MDSLSpecificationWrapper mdslWrapper;
	private Map<String, SecurityScheme> securitySchemes;

	private List<Server> servers;

	public MDSL2OpenAPIConverter(ServiceSpecification mdslSpecification) {
		this.mdslSpecification = new ServiceSpecificationAdapter(mdslSpecification);
		this.mdslWrapper = new MDSLSpecificationWrapper(this.mdslSpecification);
		this.securitySchemes = new HashMap<String, SecurityScheme>();
		this.servers = new ArrayList<Server>();
	}

	/**
	 * MDSL to OpenAPI model transformation.
	 * 
	 * @return returns the resulting OpenAPI model
	 */
	public OpenAPI convert() {
		OpenAPI oas = new OpenAPI();
		Info info = new Info().title(mdslSpecification.getName());
		String descriptionText = "";
		if(this.getDescription()!=null && !"".equals(getDescription())) {
			descriptionText += this.getDescription();
		}
		String visibilityAndDirection = this.getVisibilityAndDirectionInformation();
		if(visibilityAndDirection!=null && !visibilityAndDirection.equals("") ) {
			if(!"".equals(descriptionText) ) {
				descriptionText += " ";
			}
			descriptionText += visibilityAndDirection; // TODO turn pattern names into plain lower case text or hyperlink
		}
		if(descriptionText!=null &&!descriptionText.equals("")) {
			info.setDescription(descriptionText);
		}
		String versionText = getAPIVersion();
		if(versionText!=null&&!versionText.equals("") ) {
			info.setVersion(versionText); 
		}

		info.setExtensions(Map.of("x-generated-on", getCurrentLocalDateTimeStamp()));		
		oas.setInfo(info);
		
		oas.setPaths(this.convertEndpoints2Paths());
		
		oas.setComponents(this.createComponents());
		oas.getComponents().setSchemas(this.convertDataTypes2Schemas());
		oas.setTags(this.createTagsViaEndpointInstanceAndItsResources());
		
		// postprocessing:
		oas.servers(this.servers);
		if(!this.securitySchemes.isEmpty())
		    oas.getComponents().securitySchemes(this.securitySchemes);
		
		return oas;
	}
	
	private Components createComponents() {
		return new Components();
	}
	
	private String getDescription() {
		return mdslSpecification.getDescription() != null && !"".equals(mdslSpecification.getDescription()) ? mdslSpecification.getDescription() : null;
	}
	
	private String getAPIVersion() {
		return mdslSpecification.getSvi() != null && !"".equals(mdslSpecification.getSvi()) ? mdslSpecification.getSvi() : DEFAULT_VERSION;
	}
	
	private String getVisibilityAndDirectionInformation() {
		if(mdslSpecification.getReach()==null&&!"".equals(mdslSpecification.getReach())) {
			return null;
		}
		StringBuffer result = new StringBuffer(mdslSpecification.getReach() + " ");
		
		mdslSpecification.getDirection().forEach(direction->{if(directionToString(direction) != null&&!"".equals(directionToString(direction))) result.append(directionToString(direction));});

		return result.toString();
	}
	
	private String directionToString(DirectionList direction) {
		if(direction==null)
			return null;
		
		StringBuffer result = new StringBuffer();
		if(direction.getPrimaryDirection()!=null&&!("").equals(direction.getPrimaryDirection())) {
			result.append(direction.getPrimaryDirection());
		}
		if(direction.getOtherDirection()!=null&&!("").equals(direction.getOtherDirection())){
			result.append(direction.getOtherDirection());
		}
		return result.toString();
	}

	public String getCurrentLocalDateTimeStamp() {
		return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
	
	private List<Tag> createTagsViaEndpointInstanceAndItsResources() {
		List<Tag> tags = new ArrayList<Tag>();
		boolean foundAtLeastOneBinding = false;
		
		for (HTTPResourceBinding resourceBinding : EcoreUtil2.eAllOfType(mdslSpecification, HTTPResourceBinding.class)) {
			Tag tag = createTag(null, resourceBinding, true);
			tags.add(tag);
			foundAtLeastOneBinding=true;
		}
		
		if(!foundAtLeastOneBinding) {
			return createTagsViaEndpointType();
		}
		return tags;
	}

	public Tag createTag(EndpointContract endpointType, HTTPResourceBinding resourceBinding, boolean createExternalDescription) {

		Tag tag = new Tag();
		if(resourceBinding!=null) {
			EndpointInstance ei = getContainingEndpointInstance(resourceBinding);
			EndpointList eil = (EndpointList) ei.eContainer();
			Provider provider = (Provider) eil.eContainer();

			tag.setName(provider.getName() + "-" + resourceBinding.getName());
			// not looking good in some OAS tools
			// String contractName = eil.getContract().getName();
			// tag.setDescription("Offered contract/endpoint type: " + contractName);

			if(createExternalDescription) {
				ExternalDocumentation externalDocs = new ExternalDocumentation();
				externalDocs.setDescription(wrapContractAndPatternName(eil.getContract()));
				externalDocs.setUrl(MAPLinkResolver.provideLinktoMAPWebsite(eil.getContract()));
				tag.setExternalDocs(externalDocs);
			}
		}
		else if (endpointType!=null) {
			tag.setName(endpointType.getName());
			
			if(createExternalDescription) {
				ExternalDocumentation externalDocs = createExternalDescriptionForTag(endpointType);
				tag.setExternalDocs(externalDocs);
			}
		}
		else
			throw new MDSLException("Either a contract or a resource binding must be present.");
		
		return tag;
	}

	private String wrapContractAndPatternName(EndpointContract contract) {
		String result = contract.getName() + CONTRACT_SUFFIX;
		String mapText = MAPLinkResolver.provideMAP(contract);
		if(mapText!=null&&!"".equals(mapText)) {
			result += ", " + mapText + ROLE_SUFFIX;
		}
		return result;
	}

	private ExternalDocumentation createExternalDescriptionForTag(EndpointContract endpointType) {
		ExternalDocumentation externalDocs = new ExternalDocumentation();
		externalDocs.setDescription(wrapContractAndPatternName(endpointType));
		externalDocs.setUrl(MAPLinkResolver.provideLinktoMAPWebsite(endpointType));
		return externalDocs;
	}
	
	public EndpointInstance getContainingEndpointInstance(HTTPResourceBinding resourceBinding) {
		EObject rbc = resourceBinding.eContainer();
		EObject eic = rbc.eContainer().eContainer().eContainer();
		return (EndpointInstance) eic;
	}
	
	private List<Tag> createTagsViaEndpointType() {
		List<Tag> tags = new ArrayList<Tag>();
		for (EndpointContract endpointType : mdslSpecification.getEndpointContracts()) {
			Tag tag = new Tag();
			tag.setName(endpointType.getName());
			// tag.setDescription("Endpoint type:" + endpointType.getName()); // does not look good in some OAS tools
			ExternalDocumentation externalDocs = createExternalDescriptionForTag(endpointType);
			tag.setExternalDocs(externalDocs);
			tags.add(tag);
		}
		return tags;
	}

	public SecurityScheme convertPolicy2SecurityScheme(SecurityPolicy sp, SecurityBinding secBinding) { 
		// expects this structure in MDSL: protected by policy "HTTPBasicAuthentication": MD<string>
		// binding is ID and STRING only
		// see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#securitySchemeObject
		SecurityScheme ss = null;
		
		if(sp==null) {
			throw new MDSLException("Can't bind an empty security policy");
		}
		
		ElementStructure spo = sp.getSecurityObject();
		
		if(secBinding==null) {
			MDSLLogger.reportWarning("Skipping empty binding");
			// could insist on binding and throw an exception
			return null; 
		}
		
		if(secBinding.getHttp()==null) {
			throw new MDSLException("Security binding policy: expected something from getHttp" + sp.getName());
		}
		
		if(secBinding.getHttp().getValue()==OASSecurity.BASIC_AUTHENTICATION_VALUE) {
			ss = new SecurityScheme().type(SecurityScheme.Type.HTTP);
			ss.scheme(BASIC_SCHEME_NAME);
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else if(secBinding.getHttp().getValue()==OASSecurity.JWT_VALUE) {
			ss = new SecurityScheme().type(SecurityScheme.Type.HTTP);
			ss.scheme(BEARER_SCHEME_NAME);
			ss.bearerFormat(JWT_BEARER_FORMAT);
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else if(secBinding.getHttp().getValue()==OASSecurity.API_KEY_VALUE) {
			ss = new SecurityScheme().type(SecurityScheme.Type.APIKEY); 
			ss.in(In.HEADER);
			ss.name(API_KEY_SCHEME_NAME);
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else if(secBinding.getHttp().getValue()==OASSecurity.OAUTH_IMPLICIT_VALUE) {
			// https://swagger.io/specification/#oauth-flows-object
			ss = new SecurityScheme().type(SecurityScheme.Type.OAUTH2); // .scheme("oauth2");
			OAuthFlows flows = new OAuthFlows();
			
			Map<String,String> scopesMap = mdslWrapper.findScopesInPolicyOrBinding(sp, secBinding);
			// could also set password and two more (on object level, e.g. "implicit")
			Scopes scopes = new Scopes();
			scopesMap.forEach((skey, svalue) -> scopes.addString(skey, svalue));
			OAuthFlow implicitOAuthObject = new OAuthFlow();
			implicitOAuthObject.authorizationUrl(mdslWrapper.findAuthorizationUrlInPolicy(secBinding)); // same UR used twice now 
			implicitOAuthObject.scopes(scopes);
			flows.implicit(implicitOAuthObject);
			
			OAuthFlow authorizationCode = new OAuthFlow();
			authorizationCode.authorizationUrl(mdslWrapper.findAuthorizationUrlInPolicy(secBinding));
			authorizationCode.tokenUrl(mdslWrapper.findTokenUrlInPolicy(secBinding));
			authorizationCode.scopes(scopes);		
			flows.authorizationCode(authorizationCode);
	
			ss.flows(flows );
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else if(secBinding.getHttp().getValue()==OASSecurity.OAUTH_FLOW_VALUE) {
			// TODO (future work) could get OpenID decision from input
			ss = new SecurityScheme().type(SecurityScheme.Type.OPENIDCONNECT);
			ss.openIdConnectUrl(mdslWrapper.findOIDUrlInPolicy(secBinding));
			
			// TODO test and document scopes and use them here too (see implicit flow)?
			
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else {
			// TODO (future work): how about the other security policy string enums?
			// see https://swagger.io/specification/#security-scheme-object
			MDSLLogger.reportError("Unknown security type" + secBinding.getHttp().getValue());
		}
		
		if(ss!=null&&spo!=null) {
			String id = mdslWrapper.getElementName(spo);
			if(id!=null)
				ss.description(id); 	
		}
		
		return ss; 
	}

	/**
	 * Convert endpoints and their operations 
	 */
	private Paths convertEndpoints2Paths() {
		Paths paths = new Paths();
		Endpoint2PathConverter pathsConverter = new Endpoint2PathConverter(mdslSpecification, this);
		for (EndpointContract endpointType : mdslSpecification.getEndpointContracts()) {
			List<EndpointInstance> endpointInstanceList = mdslWrapper.findProviderEndpointInstancesFor(endpointType);
			 
			// TODO (future work) possibly also https://swagger.io/specification/#callback-object (SSEs?)
			
			MDSLLogger.reportInformation("Endpoint type " + endpointType.getName() + " has " + endpointInstanceList.size() + " HTTP binding(s).");

			if(endpointInstanceList.size()==0) {
				MDSLLogger.reportInformation("No endpoint instance/provider in " + endpointType.getName());
				String pathURI = "/" + endpointType.getName(); // use type name if no provider endpoint specified
				PathItem mappedEndpoint = pathsConverter.convertMetadataAndOperations(endpointType, null);
				addPathItemIfPossible(paths, pathURI, mappedEndpoint);
			}
			else for(int i=0;i<endpointInstanceList.size();i++) {
				Parameter pp = null;
				String pathURI;
				
				Server server = new Server().url(endpointInstanceList.get(i).getLocation());
				this.servers.add(server);
				
				if(endpointInstanceList.get(i).getLocation().startsWith("/")) {
					MDSLLogger.reportInformation("Next endpoint instance: " + endpointInstanceList.get(i).getLocation());
					pathURI = endpointInstanceList.get(i).getLocation();
					
					// could support templates here too, done on resource level at present
					List<String> templates = URITemplateHelper.findTemplateParameters(pathURI);
					if(templates.size()>0) {
						MDSLLogger.reportWarning("Found one or more URI template parameters on endpoint level, not mapped."); 
					}
				}	 
				else {
					MDSLLogger.reportWarning("Endpoint instance location should start with '/', added.");
					pathURI = "/" + endpointInstanceList.get(i).getLocation(); 
				}
				 
				EList<HTTPResourceBinding> bindings = mdslWrapper.getHTTPResourceBindings(endpointInstanceList.get(i));
				
				if(bindings.size()==0) {
					MDSLLogger.reportWarning("No HTTP binding found for " + endpointType.getName());
					PathItem mappedEndpoint = pathsConverter.convertMetadataAndOperations(endpointType, null);
					addPathItemIfPossible(paths, pathURI, mappedEndpoint);
				}
				else for(int j=0;j<bindings.size();j++) {
					String relURI = "";
					// TODO (future work) check that relative URI is there and makes sense (API Linter?) 
					HTTPResourceBinding binding = bindings.get(j);
					if(binding.getUri() != null && !binding.getUri().equals("")) {
						if(binding.getUri().startsWith("/"))
							relURI = binding.getUri();
						else {
							MDSLLogger.reportWarning("Relative URI should start with '/', adding it.");
							relURI = "/" + binding.getUri();
						}				
					}
					else {
						MDSLLogger.reportWarning("HTTP binding does not have a relative URI, adding resource name " + endpointType.getName());
						relURI = "/" + binding.getName();
					}
									
					PathItem mappedEndpoint = pathsConverter.convertMetadataAndOperations(endpointType, binding);
					List<String> templates = URITemplateHelper.findTemplateParameters(relURI);
					if(templates!=null) {
						for(int k=0;k<templates.size();k++) {
							pp = new Parameter();
							String template = templates.get(k);
							pp.name(template.substring(1,template.length()-1));
							pp.in("path");
							pp.schema(new Schema().type("string"));
							mappedEndpoint.addParametersItem(pp);
						}
					}
					else {
						MDSLLogger.reportInformation("No URI template parameters in resource URI: " + relURI);
					}
		
					addPathItemIfPossible(paths, relURI, mappedEndpoint);
				}
			}
		}
		
		return paths;
	}

	// ** converters 
	
	@SuppressWarnings("rawtypes")
	private Map<String, Schema> convertDataTypes2Schemas() {
		Map<String, Schema> map = new LinkedHashMap<>();
		DataType2SchemaConverter typesConverter = new DataType2SchemaConverter();
		for (DataContract dataType : mdslSpecification.getTypes()) {
			map.put(dataType.getName(), typesConverter.convert(dataType));
		}
		return map;
	}

	// ** PathItem manipulation helpers (TODO move to helper?)
	
	private void addPathItemIfPossible(Paths paths, String pathURI, PathItem mappedEndpoint) {
		if(paths.get(pathURI)==null) { 
			// normal case: each endpoint and binding works with different (relative) URI 
			paths.addPathItem(pathURI, mappedEndpoint);
		}
		else {
			MDSLLogger.reportWarning("Path URI uses multiple times: " + pathURI);
			// TODO check verbs in existing PathItem, merge/report mapping conflicts
			PathItem existingPathItem = paths.get(pathURI);
			mergePathItems(existingPathItem, mappedEndpoint);
		}
	}

	// TODO (M) v55 refactor and improve error message: include value of pathURI; use MDSLLogger.reportWarning and skip duplicate rather than stop
	
	private void mergePathItems(PathItem existingPathItem, PathItem mappedEndpoint) {
		Operation mappedOp = mappedEndpoint.getPost();
		if(mappedOp!=null) {
			if(existingPathItem.getPost()!=null) 
				throw new MDSLException("Cannot add " + mappedEndpoint.getPost().getOperationId() + " to path item: Path URI already defines a POST method, ignoring second one: " + existingPathItem.getPost().getOperationId());
			else {
				MDSLLogger.reportInformation("Merging POST into path URI.");
				existingPathItem.setPost(mappedOp);
			}
		}

		mappedOp = mappedEndpoint.getGet();
		if(mappedOp!=null) {
			if(existingPathItem.getGet()!=null) 
				throw new MDSLException("Cannot add " + mappedEndpoint.getGet().getOperationId() + " to path item: Path URI already defines a GET method: " + existingPathItem.getGet().getOperationId());
			else {
				MDSLLogger.reportInformation("Merging GET into path URI.");
				existingPathItem.setGet(mappedOp);
			}
		}
		
		mappedOp = mappedEndpoint.getPut();
		if(mappedOp!=null) {
			if(existingPathItem.getPut()!=null) 
				throw new MDSLException("Cannot add " + mappedEndpoint.getPut().getOperationId() + " to path item: Path URI already defines a PUT method: " + existingPathItem.getPut().getOperationId());
			else {
				MDSLLogger.reportInformation("Merging PUT into path URI.");
				existingPathItem.setPut(mappedOp);
			}
		}
		
		mappedOp = mappedEndpoint.getPatch();
		if(mappedOp!=null) {
			if(existingPathItem.getPatch()!=null) 
				throw new MDSLException("Cannot add " + mappedEndpoint.getPatch().getOperationId() + " to path item: Path URI already defines a PATCH method: " + existingPathItem.getPatch().getOperationId());
			else {
				MDSLLogger.reportInformation("Merging PATCH into path URI.");
				existingPathItem.setPatch(mappedOp);
			}
		}
		
		mappedOp = mappedEndpoint.getDelete();
		if(mappedOp!=null) {
			if(existingPathItem.getDelete()!=null) 
				throw new MDSLException("Cannot add " + mappedEndpoint.getDelete().getOperationId() + " to path item: Path URI already defines a DELETE method: " + existingPathItem.getDelete().getOperationId());
			else {
				MDSLLogger.reportInformation("Merging DELETE into path URI.");
				existingPathItem.setDelete(mappedOp);
			}
		}
		
		mappedOp = mappedEndpoint.getHead();
		if(mappedOp!=null) {
			if(existingPathItem.getHead()!=null) 
				throw new MDSLException("Cannot add " + mappedEndpoint.getHead().getOperationId() + " to path item: Path URI already defines a HEAD method: " + existingPathItem.getHead().getOperationId());
			else {
				MDSLLogger.reportInformation("Merging HEAD into path URI.");
				existingPathItem.setHead(mappedOp);
			}
		}
		
		mappedOp = mappedEndpoint.getOptions();
		if(mappedOp!=null) {
			if(existingPathItem.getOptions()!=null) 
				throw new MDSLException("Cannot add " + mappedEndpoint.getOptions().getOperationId() + " to path item: Path URI already defines a OPTIONS method: " + existingPathItem.getOptions().getOperationId());
			else {
				MDSLLogger.reportInformation("Merging OPTIONS into path URI.");
				existingPathItem.setOptions(mappedOp);
			}
		}
	}
}

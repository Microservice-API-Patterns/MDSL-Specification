package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.EndpointList;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.HTTPParameterBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.OASSecurity;
import io.mdsl.apiDescription.ProtocolBinding;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.SecurityBinding;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MAPLinkResolver;
import io.mdsl.utils.MDSLSpecificationWrapper;
import io.mdsl.utils.URITemplateHelper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
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
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Converts MDSL ServiceSpecification to an OpenAPI specification
 * 
 * @author ska, socadk
 * 
 */
public class MDSL2OpenAPIConverter {


	private static final String DEFAULT_VERSION = "1.0";

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
		oas.setInfo(new Info().title(mdslSpecification.getName()).version(getAPIVersion()));
		// TODO add foundation MAPs if present: Visibility, Direction
		
		// this start the main action:
		oas.setPaths(convertEndpoints2Paths());
		
		oas.setComponents(createComponents());
		oas.getComponents().setSchemas(convertDataTypes2Schemas());
		// oas.setTags(createTagsViaEndpointType());
		oas.setTags(createTagsViaEndpointInstanceAndItsResources());
		
		// postprocessing:
		oas.servers(this.servers);
		if(!this.securitySchemes.isEmpty())
		    oas.getComponents().securitySchemes(this.securitySchemes);
		
		return oas;
	}
	
	private Components createComponents() {
		return new Components();
	}
	
	private String getAPIVersion() {
		return mdslSpecification.getSvi() != null && !"".equals(mdslSpecification.getSvi()) ? mdslSpecification.getSvi() : DEFAULT_VERSION;
	}
	
	private List<Tag> createTagsViaEndpointInstanceAndItsResources() {
		List<Tag> tags = new ArrayList<Tag>();
		boolean foundAtLeastOneBinding = false;
		
		for (HTTPResourceBinding resourceBinding : EcoreUtil2.eAllOfType(mdslSpecification, HTTPResourceBinding.class)) {
			// mdsl2OpenAPIConverter.log("[TB]: " + " found resource " + resourceBinding.getName());
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
			// mdslWrapper.log("[I-TB]: Found EP: " + provider.getName());

			tag.setName(provider.getName() + "-" + resourceBinding.getName());
			String contractName = eil.getContract().getName();
			// tag.setDescription("Offered contract/endpoint type: " + contractName);

			if(createExternalDescription) {
				ExternalDocumentation externalDocs = new ExternalDocumentation();
				externalDocs.setDescription("The role of this endpoint, offering a " + contractName + " contract, is " + MAPLinkResolver.provideMAP(eil.getContract()));
				externalDocs.setUrl(MAPLinkResolver.provideLinktoMAPWebsite(eil.getContract()));
				tag.setExternalDocs(externalDocs);
			}
		}
		else if (endpointType!=null) {
			tag.setName(endpointType.getName());
			
			if(createExternalDescription) {
				ExternalDocumentation externalDocs = new ExternalDocumentation();
				externalDocs.setDescription("The role of this endpoint, offering a " + endpointType.getName() + " contract, is " + MAPLinkResolver.provideMAP(endpointType));
				externalDocs.setUrl(MAPLinkResolver.provideLinktoMAPWebsite(endpointType));
				tag.setExternalDocs(externalDocs);
			}
		}
		else
			throw new MDSLException("Either a contract name or a resource binding name must be present.");
		
		return tag;
	}
	
	public EndpointInstance getContainingEndpointInstance(HTTPResourceBinding resourceBinding) {
		EObject rbc = resourceBinding.eContainer();
		EObject eic = rbc.eContainer().eContainer().eContainer();
		// mdsl2OpenAPIConverter.log("[TB]: Container is of type: " + eic.getClass().getName());
		return (EndpointInstance) eic;
	}
	
	private List<Tag> createTagsViaEndpointType() {
		List<Tag> tags = new ArrayList<Tag>();
		for (EndpointContract endpointType : mdslSpecification.getEndpointContracts()) {
			Tag tag = new Tag();
			tag.setName(endpointType.getName());
			// tag.setDescription("Offered contract/endpoint type:" + endpoint.getName());
			
			ExternalDocumentation externalDocs = new ExternalDocumentation();
			externalDocs.setDescription("The role of this endpoint is " + MAPLinkResolver.provideMAP(endpointType));
			externalDocs.setUrl(MAPLinkResolver.provideLinktoMAPWebsite(endpointType));
			
			tag.setExternalDocs(externalDocs);
			tags.add(tag);
		}
		return tags;
	}

	public SecurityScheme convertPolicy2SecurityScheme(SecurityPolicy sp, SecurityBinding secBinding) { 
		// expects something like this in MDSL: protected by policy "HTTPBasicAuthentication": MD<string>
		// binding is ID and STRING only
		// see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#securitySchemeObject
		SecurityScheme ss = null;
		
		if(sp==null) {
			throw new MDSLException("Can't bind an empty security policy");
		}
		
		ElementStructure spo = sp.getSecurityObject();
		
		if(secBinding==null) {
			mdslWrapper.logWarning("Skipping empty binding");
			// could insist on binding and throw an exception
			return null; // TODO return a default binding?
		}
		
		if(secBinding.getHttp()==null) {
			throw new MDSLException("Security binding policy: expected something from getHttp" + sp.getName());
		}
		
		if(secBinding.getHttp().getValue()==OASSecurity.BASIC_AUTHENTICATION_VALUE) {
			ss = new SecurityScheme().type(SecurityScheme.Type.HTTP);
			ss.scheme("basic");
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else if(secBinding.getHttp().getValue()==OASSecurity.JWT_VALUE) {
			ss = new SecurityScheme().type(SecurityScheme.Type.HTTP);
			ss.scheme("bearer");
			ss.bearerFormat("JWT");
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else if(secBinding.getHttp().getValue()==OASSecurity.API_KEY_VALUE) {
			ss = new SecurityScheme().type(SecurityScheme.Type.APIKEY); 
			ss.in(In.HEADER);
			ss.name("api_key");
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
			// TODO (tbd) get OpenID decision from input?
			ss = new SecurityScheme().type(SecurityScheme.Type.OPENIDCONNECT);
			ss.openIdConnectUrl(mdslWrapper.findOIDUrlInPolicy(secBinding));
			
			// TODO test and document scopes and use them here too (see implicit flow)?
			
			this.securitySchemes.put(sp.getName(), ss); 
		}
		else {
			// TODO how about the other security policy string enums?
			// see https://swagger.io/specification/#security-scheme-object
			mdslWrapper.logError("Unknown security type" + secBinding.getHttp().getValue());
		}
		
		if(ss!=null) {
			// TODO only use if present in abstract policy (if not null)
			String id = mdslWrapper.getElementName(spo);
			ss.description(id); 	
		}
		
		return ss; 
	}

	/**
	 * Convert endpoints
	 */
	private Paths convertEndpoints2Paths() {
		Paths paths = new Paths();
		Endpoint2PathConverter pathsConverter = new Endpoint2PathConverter(mdslSpecification, this);
		for (EndpointContract endpointType : mdslSpecification.getEndpointContracts()) {
			List<EndpointInstance> endpointInstanceList = mdslWrapper.findProviderEndpointInstancesFor(endpointType);
			 
			// TODO possibly also https://swagger.io/specification/#callback-object (SSEs?)
			
			mdslWrapper.logInformation("Endpoint type " + endpointType.getName() + " has " + endpointInstanceList.size() + " HTTP binding(s).");

			if(endpointInstanceList.size()==0) {
				mdslWrapper.logWarning("No endpoint instance/provider in " + endpointType.getName());
				String pathURI = "/" + endpointType.getName(); // use type name if no provider endpoint specified
				PathItem mappedEndpoint = pathsConverter.convertMetadataAndOperations(endpointType, null);
				paths.addPathItem(pathURI, mappedEndpoint);
			}
			else for(int i=0;i<endpointInstanceList.size();i++) {
				Parameter pp = null;
				String pathURI;
				
				Server server = new Server().url(endpointInstanceList.get(i).getName());
				this.servers.add(server);
				
				if(endpointInstanceList.get(i).getName().startsWith("/")) {
					mdslWrapper.logInformation("Next endpoint instance: " + endpointInstanceList.get(i).getName());
					pathURI = endpointInstanceList.get(i).getName();
					
					// TODO (tbd) support templates here too? (done on resource level at present)
					List<String> templates = URITemplateHelper.findTemplateParameters(pathURI);
					if(templates.size()>0) {
						mdslWrapper.logWarning("Found one or more URI template parameters on endpoint level, not mapped."); 
					}
					// else
						// mdslResolver.log("No URI template parameters");
				}	 
				else {
					mdslWrapper.logWarning("Endpoint instance location should start with '/', added.");
					pathURI = "/" + endpointInstanceList.get(i).getName(); 
				}
				 
				// pathURI = convertPathParameters(pathURI, endpointInstanceList.get(i)); // old code
				EList<HTTPResourceBinding> bindings = getHTTPResourceBindings(endpointInstanceList.get(i));
				
				if(bindings.size()==0) {
					mdslWrapper.logWarning("No HTTP binding found for " + endpointType.getName());
					PathItem mappedEndpoint = pathsConverter.convertMetadataAndOperations(endpointType, null);
					paths.addPathItem(pathURI, mappedEndpoint);
				}
				else for(int j=0;j<bindings.size();j++) {
					String relURI = "";
					HTTPResourceBinding binding = bindings.get(j);
					if(binding.getUri() != null) {
						if(binding.getUri().startsWith("/"))
							relURI = binding.getUri();
						else {
							mdslWrapper.logWarning("Relative URI should start with '/', adding it.");
							relURI = "/" + binding.getUri();
						}				
					}
									
					PathItem mappedEndpoint = pathsConverter.convertMetadataAndOperations(endpointType, binding);
					List<String> templates = URITemplateHelper.findTemplateParameters(relURI);
					if(templates!=null) {
						// mdslWrapper.logInformation("Found one or more URI template parameters in resource URI: " + relURI); 
						for(int k=0;k<templates.size();k++) {
							pp = new Parameter();
							String template = templates.get(k);
							pp.name(template.substring(1,template.length()-1));
							pp.in("path");
							pp.schema(new Schema().type("string"));
							mappedEndpoint.addParametersItem(pp);
						}
					}
					else
						mdslWrapper.logInformation("No URI template parameters in resource URI: " + relURI);
						
					paths.addPathItem(pathURI+relURI, mappedEndpoint);
				}
			}
		}
		
		return paths;
	}


	/**
	 * Convert datatypes
	 */
	@SuppressWarnings("rawtypes")
	private Map<String, Schema> convertDataTypes2Schemas() {
		Map<String, Schema> map = new LinkedHashMap<>();
		DataType2SchemaConverter typesConverter = new DataType2SchemaConverter();
		for (DataContract dataType : mdslSpecification.getTypes()) {
			map.put(dataType.getName(), typesConverter.convert(dataType));
		}
		return map;
	}

	
	/**
	 * Helpers
	 */
	
	// could move and merge with the one in MSDL wrapper/helper (DRY)
	private EList<HTTPResourceBinding> getHTTPResourceBindings(EndpointInstance endpointInstance) {
		EList<TechnologyBinding> protocolBindings = endpointInstance.getPb();
		for(int i=0;i<protocolBindings.size();i++) {
			ProtocolBinding pb = endpointInstance.getPb().get(i).getProtBinding(); 
			HTTPBinding httpb = pb.getHttp();
			if(httpb!=null) {
				EList<HTTPResourceBinding> httprb = httpb.getEb();
				if(httprb==null) { // check needed?
					return null;
				}
				return httprb;
 			}
		}
		return null; 
	}
}

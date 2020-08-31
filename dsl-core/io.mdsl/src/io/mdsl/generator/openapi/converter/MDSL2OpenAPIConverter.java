package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TechnologyBinding;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Converts MDSL ServiceSpecification to an OpenAPI specification
 * 
 * @author ska, zio
 * 
 */
public class MDSL2OpenAPIConverter {

	private static final String DEFAULT_VERSION = "1.0";

	private ServiceSpecification mdslSpecification;

	public MDSL2OpenAPIConverter(ServiceSpecification mdslSpecification) {
		this.mdslSpecification = mdslSpecification;
	}

	/**
	 * MDSL to OpenAPI model transformation.
	 * 
	 * @return returns the resulting OpenAPI model
	 */
	public OpenAPI convert() {
		OpenAPI oas = new OpenAPI();
		oas.setInfo(new Info().title(mdslSpecification.getName()).version(getAPIVersion()));
		oas.setPaths(convertEndpoints2Paths());
		oas.setComponents(createComponents());
		oas.getComponents().setSchemas(convertDataTypes2Schemas());
		oas.setTags(createTags());

		// oas.getComponents().securitySchemes(convertPolicies2SecuritySchemes());
		return oas;
	}

	private List<Tag> createTags() {
		List<Tag> tags = new ArrayList<Tag>();
		for (EndpointContract endpoint : this.mdslSpecification.getContracts()) {
			Tag tag = new Tag();
			tag.setName(endpoint.getName());
			tag.setDescription(Endpoint2PathConverter.mapRolePattern(endpoint));
			ExternalDocumentation externalDocs = new ExternalDocumentation();
			externalDocs.setDescription(endpoint.getPrimaryRole());
			externalDocs.setUrl("https://microservice-api-patterns.org/patterns/responsibility/");
			tag.setExternalDocs(externalDocs);
			tags.add(tag);
		}
		return tags;
	}

	/*
	 * private Map<String, SecurityScheme> convertPolicies2SecuritySchemes() { //
	 * TODO only create (all) the ones that are needed (on operation level), use
	 * binding // needs this in MDSL: protected by policy
	 * "HTTPBasicAuthentication":MD // see
	 * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#
	 * securitySchemeObject
	 * 
	 * Map<String, SecurityScheme> securitySchemes = new HashMap<String,
	 * SecurityScheme>();
	 * 
	 * SecurityScheme ss = new
	 * SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic");
	 * securitySchemes.put("HTTPBasicAuthentication", ss); return securitySchemes; }
	 */

	/**
	 * Convert endpoints
	 */
	private Paths convertEndpoints2Paths() {
		Paths paths = new Paths();
		Endpoint2PathConverter pathsConverter = new Endpoint2PathConverter(mdslSpecification);
		for (EndpointContract endpoint : this.mdslSpecification.getContracts()) {
			EndpointInstance endpointInstance = findHttpBindingIfExisting(endpoint);
			if (endpointInstance != null && endpointInstance.getName() != null
					&& endpointInstance.getName().startsWith("/"))
				paths.addPathItem(endpointInstance.getName(), pathsConverter.convert(endpoint));
			else
				paths.addPathItem("/" + endpoint.getName(), pathsConverter.convert(endpoint));
		}
		return paths;
	}

	private Components createComponents() {
		return new Components();
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

	private String getAPIVersion() {
		return mdslSpecification.getSvi() != null && !"".equals(mdslSpecification.getSvi()) ? mdslSpecification.getSvi()
				: DEFAULT_VERSION;
	}

	private EndpointInstance findHttpBindingIfExisting(EndpointContract endpoint) {
		List<TechnologyBinding> bindings = EcoreUtil2.eAllOfType(mdslSpecification, TechnologyBinding.class);
		List<TechnologyBinding> httpBindings = bindings.stream().filter(b -> b.getProtBinding() != null
				&& b.getProtBinding().getHttp() != null && b.getProtBinding().getHttp() instanceof HTTPBinding)
				.collect(Collectors.toList());

		if (httpBindings.size() == 1) // use HTTP binding, if there is only one
			return (EndpointInstance) httpBindings.get(0).eContainer();

		return null;
	}
}

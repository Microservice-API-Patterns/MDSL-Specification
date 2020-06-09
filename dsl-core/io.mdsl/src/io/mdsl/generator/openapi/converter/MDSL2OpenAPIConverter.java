package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.mdsl.apiDescription.dataContract;
import io.mdsl.apiDescription.endpointContract;
import io.mdsl.apiDescription.serviceSpecification;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Converts MDSL serviceSpecification to an OpenAPI specification
 * 
 * @author ska, zio
 * 
 */
public class MDSL2OpenAPIConverter {

	private static final String DEFAULT_VERSION = "1.0";

	private serviceSpecification mdslSpecification;

	public MDSL2OpenAPIConverter(serviceSpecification mdslSpecification) {
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
		for (endpointContract endpoint : this.mdslSpecification.getContracts()) {
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
	private Map<String, SecurityScheme> convertPolicies2SecuritySchemes() {
		// TODO only create (all) the ones that are needed (on operation level), use binding
		// needs this in MDSL: protected by policy "HTTPBasicAuthentication":MD 
		// see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#securitySchemeObject

		Map<String, SecurityScheme> securitySchemes = new HashMap<String, SecurityScheme>();
		
		SecurityScheme ss = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic");
		securitySchemes.put("HTTPBasicAuthentication", ss);
		return securitySchemes;
	}
	*/

	/**
	 * Convert endpoints
	 */
	private Paths convertEndpoints2Paths() {
		Paths paths = new Paths();
		Endpoint2PathConverter pathsConverter = new Endpoint2PathConverter();
		for (endpointContract endpoint : this.mdslSpecification.getContracts()) {
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
		for (dataContract dataType : mdslSpecification.getTypes()) {
			map.put(dataType.getName(), typesConverter.convert(dataType));
		}
		return map;
	}

	private String getAPIVersion() {
		return mdslSpecification.getSvi() != null && !"".equals(mdslSpecification.getSvi()) ? mdslSpecification.getSvi()
				: DEFAULT_VERSION;
	}

	// TODO work with new, still emerging HTTP binding in provider
}

<#assign endpoint = genModel.endpoints?filter(e -> e.name == endpointName)?first>
package ${resolveJavaPackage(genModel, endpoint)}.services;

import ${resolveJavaPackage(genModel, endpoint)}.types.*;

/**
 * This interface has been generated from the MDSL endpoint called '${endpoint.name}'. 
 * 
 */
public interface ${endpoint.name} {

	<#list endpoint.operations as operation>
	<#if operation.responsibility?has_content>
	/**
	 * MAP decorator: ${operation.responsibility}
	 * 
	 * Find all MAP responsibility patterns here: https://microservice-api-patterns.org/patterns/responsibility/
	 */
	</#if>
	${mapType(operation.response.name, true)} ${resolveOperationName(endpoint, operation)}(${operation.parameters?map(p -> mapType(p.type.name) + " " + p.name)?join(", ")});
	
	</#list>

}

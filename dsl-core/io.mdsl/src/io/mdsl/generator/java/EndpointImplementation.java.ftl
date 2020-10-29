<#assign endpoint = genModel.endpoints?filter(e -> e.name == endpointName)?first>
package ${resolveJavaPackage(genModel, endpoint)}.services.impl;

import ${resolveJavaPackage(genModel, endpoint)}.services.${endpoint.name};
import ${resolveJavaPackage(genModel, endpoint)}.types.*;
import java.util.Arrays;

/**
 * This implementation has been generated from the MDSL endpoint called '${endpoint.name}'.
 * The methods are a starting point to realize the logic behind an endpoint
 * and are not complete. 
 * 
 */
public class ${endpoint.name}Impl implements ${endpoint.name} {

	<#list endpoint.operations as operation>
	<#if operation.responsibility?has_content>
	/**
	 * MAP decorator: ${operation.responsibility}
	 * 
	 * Find all MAP responsibility patterns here: https://microservice-api-patterns.org/patterns/responsibility/
	 */
	</#if>
	public ${mapType(operation.response.name, true)} ${resolveOperationName(endpoint, operation)}(${operation.parameters?map(p -> mapType(p.type.name) + " " + p.name)?join(", ")}) {
		<#list operation.parameters as parameter>
		System.out.println("The received object for parameter '${parameter.name}' is " + (${parameter.name} == null ? "null." : "not null."));
		</#list>
		<#if mapType(operation.response.name, true) != 'void'>
		// TODO: we just return a dummy object here; replace this with your implementation
		${mapType(operation.response.name)} obj = new ${mapType(operation.response.name)}();
		<#assign returnType = genModel.dataTypes?filter(d -> d.name == operation.response.name)?first>
		<#list returnType.fields as field>
		<#if isPrimitiveType(field.type.getName())>
		obj.set${capitalize(field.name)}(<#if field.isList()>Arrays.asList(new ${mapType(field.type.getName())}[] { ${generateRandomValue4PrimitiveType(field.type.getName())} })<#else>${generateRandomValue4PrimitiveType(field.type.getName())}</#if>);
		</#if>
		</#list>
		return obj;
		<#else>
		// TODO: implement operation
		</#if>
	}
	
	</#list>

}

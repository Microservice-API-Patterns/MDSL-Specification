<#assign endpoint = genModel.endpoints?filter(e -> e.name == endpointName)?first>
package ${resolveJavaPackage(genModel, endpoint)}.services.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import ${resolveJavaPackage(genModel, endpoint)}.services.${endpoint.name};
import ${resolveJavaPackage(genModel, endpoint)}.services.impl.${endpoint.name}Impl;
import ${resolveJavaPackage(genModel, endpoint)}.types.*;

/**
 * This test has been generated from the MDSL endpoint called '${endpoint.name}'.
 * The methods are a starting point to implement your tests
 * and are not complete. 
 * 
 */
public class ${endpoint.name}Test {

	<#list endpoint.operations as operation>
	@Test
	public void can${capitalize(resolveOperationName(endpoint, operation))}() {
		// given
		${endpoint.name} service = new ${endpoint.name}Impl();
		<#if operation.parameters?has_content>
		<#list operation.parameters as parameter>
		<#if isPrimitiveType(parameter.type.getName())>
		${mapType(parameter.type.name)} ${parameter.name} = ${generateRandomValue4PrimitiveType(parameter.type.getName())};
		<#else>
		${mapType(parameter.type.name)} ${parameter.name} = new ${mapType(parameter.type.name)}();
		<#list parameter.type.fields as field>
		<#if isPrimitiveType(field.type.getName())>
		${parameter.name}.set${capitalize(field.name)}(<#if field.isList()>Arrays.asList(new ${mapType(field.type.getName())}[] { ${generateRandomValue4PrimitiveType(field.type.getName())} })<#else>${generateRandomValue4PrimitiveType(field.type.getName())}</#if>);
		</#if>
		</#list>
		</#if>
		</#list>
		</#if>
		
		// when
		<#if mapType(operation.response.name, true) != 'void'>
		${mapType(operation.response.name)} result = service.${resolveOperationName(endpoint, operation)}(${operation.parameters?map(p -> p.name)?join(", ")});
		<#else>
		service.${operation.name}(${operation.parameters?map(p -> p.name)?join(", ")});
		</#if>
		
		// then
		<#if mapType(operation.response.name, true) != 'void'>
		assertNotNull(result);
		<#else>
		// TODO: implement your assertions
		</#if>
	}
	
	</#list>

}

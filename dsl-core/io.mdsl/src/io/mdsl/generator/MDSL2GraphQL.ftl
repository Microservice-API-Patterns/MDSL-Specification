<#assign endpoint = genModel.endpoints?filter(e -> e.name == graphQLEndpointName)?first>
<#assign queryOperations = endpoint.operations?filter(o -> o.responsibility == "COMPUTATION_FUNCTION" || o.responsibility == "RETRIEVAL_OPERATION")>
<#assign mutationOperations = endpoint.operations?filter(o -> !(o.responsibility == "COMPUTATION_FUNCTION" || o.responsibility == "RETRIEVAL_OPERATION"))>
# GraphQL generated for endpoint ${endpoint.name} of ${genModel.apiName}

<#list genModel.dataTypes?filter(t -> !t.fields?has_content && !(t.name == "VoidResponse")) as pType>
scalar ${pType.name}Input # abstract, unspecified data type
scalar ${pType.name}Output # abstract, unspecified data type
</#list>

<#list genModel.dataTypes?filter(t -> t.fields?has_content) as type>
input ${type.name}Input {
	<#list type.fields as field>
	${field.name}: <#if field.isList()>[</#if>${mapType(field.type.getName(), "Input")}<#if field.isList() && !field.nullable>!]<#elseif field.isList()>]</#if><#if !field.nullable>!</#if>
	</#list>
}
</#list>
<#list genModel.dataTypes?filter(t -> t.fields?has_content) as type>
type ${type.name}Output {
	<#list type.fields as field>
	${field.name}: <#if field.isList()>[</#if>${mapType(field.type.getName(), "Output")}<#if field.isList() && !field.nullable>!]<#elseif field.isList()>]</#if><#if !field.nullable>!</#if>
	</#list>
}
</#list>

<#if queryOperations?has_content>
type Query {
<@renderOperations queryOperations />
}
</#if>

<#if mutationOperations?has_content>
type Mutation {
<@renderOperations mutationOperations />
}
</#if>

<#if queryOperations?has_content || mutationOperations?has_content>
schema {
	<#if queryOperations?has_content>query: Query</#if>
	<#if mutationOperations?has_content>mutation: Mutation</#if>
}
</#if>

# additional scalars for types in MDSL
scalar Raw
scalar VoidResponse
<#macro renderOperations operations>
<#list operations as operation>
	${operation.name}<#if operation.parameters?has_content>(
		<#list operation.parameters as parameter>
		${parameter.name}: ${mapType(parameter.type.name, "Input")}
		</#list>
	)</#if>: ${mapType(operation.response.name, "Output")}
</#list>
</#macro>
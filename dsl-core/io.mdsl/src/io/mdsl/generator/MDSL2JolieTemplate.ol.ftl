// API name: ${serviceSpecification.name}
<#-- API description (visibility, direction) TODO -->

<#list jolieModel.types as typeName, typeDef>
type ${typeName} {
	${typeDef.definition}
}

</#list>

<#-- TODO make sure that operation names are unique (across endpoints?) -->
<#list jolieModel.operations as operationName, operationModel>
// operation responsibility: ${operationModel.reponsibilityPattern}
type ${operationModel.name}RequestDTO {
	${operationModel.requestType}
}

type ${operationModel.name}ResponseDTO {
	${operationModel.responseType}
}

</#list>

type SOAPFaultMessage {
	code: int
	text: string
	actor: string
	details: string
}

<#-- TODO add REST annotations for Jester, see https://github.com/jolie/jester (URI template might need other MDSL input!) -->

<#list serviceSpecification.contracts as endpoint>
<#--  check that endpoint is core MDSL endpoint and not AsynchMDSL channel: -->
<#if endpoint.class.name == 'io.mdsl.apiDescription.impl.EndpointContractImpl'>
// interface/endpoint role: ${endpoint.primaryRole!"undefined"}
interface ${endpoint.name} {
<#--  TODO also support OneWay (and other MEPs?): -->
RequestResponse:
<#list endpoint.ops as operation>
    <#-- operation responsibility: ${operation.responsibility!"undefined"} TODO FM function needed -->
	${operation.name}( ${operation.name}RequestDTO )( ${operation.name}ResponseDTO ),
    <#-- TODO no comma for last op (Freemarker has a utility for that) -->
    <#-- TODO port error reporting from Python to Java: {% if operation["errors"] %} throws {{ operation["errors"][0] }}( SOAPFaultMessage ) {% endif %} --> 
</#list>
}
<#else>
// Unsuported type of contract:  ${endpoint.name} is a ${endpoint.class.name}
</#if>
</#list>

<#list serviceSpecification.contracts as endpoint>
inputPort ${endpoint.name}Port {
	location: "socket://localhost:8080" <#-- this should come from API Provider binding in MDSL; "socket" or "http"? -->
	protocol: soap
	interfaces: ${endpoint.name}
}

// sample conversion to WSDL/SOAP:
// jolie2wsdl --namespace "http://tbc.org" --portName ${endpoint.name}Port --portAddr "localhost:8080" --outputFile ${jolieModel.specificationFilename}${endpoint.name}.wsdl ${jolieModel.specificationFilename}.ol
// The WSDL could be viewed/analyzed at: https://www.wsdl-analyzer.com/upload

</#list>

main
{
	nullProcess
}

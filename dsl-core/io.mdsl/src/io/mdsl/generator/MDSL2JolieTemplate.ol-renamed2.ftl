// API name: ${serviceSpecification.name}
// TODO API description: {{ description }}, serving in {{ roles }} role(s)
// sample conversion to WSDL/SOAP:
// jolie2wsdl --namespace "http://tbc.org" --portName nnPort --portAddr "localhost:8080" --o nn.wsdl nn.ol
// The WSDL could be viewed/analyzed at: https://www.wsdl-analyzer.com/upload (seems to be broken in 8/2020)

<#list serviceSpecification.contracts as endpoint>
<#list endpoint.ops as operation>
type ${operation.name}RequestDTO {
   // TODO implement full data contract conversion (in Java)
   <#if operationSignatures['operations']??>
   // ${operationSignatures['operations'][operation.name].reponsibilityPattern}
   ${operationSignatures['operations'][operation.name].requestType}
   <#else>
    msg: string // default for all types not yet implemented/mapped properly
   </#if>
}

type ${operation.name}ResponseDTO {
   // TODO implement full data contract conversion (in Java)
   <#if operation["response_declaration_jolie"]??>
   <#else>
    msg: string // default for all types not yet implemented/mapped properly
   </#if>
}
</#list>
</#list>

type SOAPFaultMessage {
    code: int
    text: string
    actor: string
    details: string
}

// TODO add REST annotations for Jester, see https://github.com/jolie/jester (URI template might need other MDSL input!)

<#list serviceSpecification.contracts as endpoint> 
// TODO only capitalize first character (last one is decapitalized?)
// interface/endpoint role: ${endpoint.primaryRole!"undefined"}
interface ${endpoint.name} {
// TODO also support OneWay (and other MEPs?):
RequestResponse:
<#list endpoint.ops as operation>
    // operation responsibility: ${operation.responsibility!"undefined"}
    ${operation.name}( ${operation.name}RequestDTO )( ${operation.name}ResponseDTO ), // TODO no comma for last op
    // TODO port error reporting from Python to Java: {% if operation["errors"] %} throws {{ operation["errors"][0] }}( SOAPFaultMessage ) {% endif %} 
</#list>
</#list>

<#list serviceSpecification.contracts as endpoint>
inputPort ${endpoint.name}Port {
location: "socket://localhost:8080" // this should come from API Provider Info in MDSL; "socket" or "http"?
protocol: soap
interfaces: ${endpoint.name}
}
</#list>

main
{
  nullProcess
}

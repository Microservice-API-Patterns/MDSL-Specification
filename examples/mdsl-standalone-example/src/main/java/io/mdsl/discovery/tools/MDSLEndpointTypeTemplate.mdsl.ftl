API description ${serviceSpecification.apiName}

data type Placeholder D<string> // or D<void>?

<#list serviceSpecification.endpoints as endpoint>
endpoint type ${endpoint.name?keep_after_last(".")} <#if endpoint.role??>serves as ${endpoint.role}</#if>
<#if (endpoint.operations?size > 0)>
exposes 
<#list endpoint.operations as operation>
   operation ${operation.name} <#if operation.responsibility??>with responsibility ${operation.responsibility}</#if>
    expecting payload ${operation.expecting}
    delivering payload ${operation.delivering}
</#list>
<#else>
// does not expose any operations
</#if>
</#list>

<#list serviceSpecification.endpoints as endpoint>
API provider ${endpoint.name?keep_after_last(".")}Provider
  offers ${endpoint.name?keep_after_last(".")}
	at endpoint location "${endpoint.name}" // TODO could cut class/endpoint name off
		via protocol Java // local call, interface or POJO class
		  // binding "passthrough" (no need to map operation names) 
</#list>

<#list serviceSpecification.endpoints as endpoint>
API client ${endpoint.name?keep_after_last(".")}Client
  consumes ${endpoint.name?keep_after_last(".")}
  via protocol Java // interface or POJO class
</#list>


# Report for MDSL specification ${fileName}

## Endpoint Types 

The API description ${genModel.apiName} features the following endpoint types (a.k.a. service contracts): 

<#list genModel.endpoints as endpoint>
* ${endpoint.name} 
</#list>

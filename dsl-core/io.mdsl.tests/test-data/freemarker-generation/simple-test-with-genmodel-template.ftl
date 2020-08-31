${genModel.apiName} specified in ${fileName}

endpoints:
<#list genModel.endpoints as endpoint>
${endpoint.name}
</#list>

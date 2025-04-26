${genModel.apiName} specified in ${fileName}

endpoints:
<#list genModel.endpoints as endpoint>
${endpoint.name}
</#list>

datatypes:
<#list genModel.dataTypes as dtype>
name:${dtype.name} 
<#if dtype.fields?has_content>
	<#list dtype.fields as f>
${f.name}: ${f.typeAsString} (${f.nullable?string('optional','required')}, ${f.list?string('list','single')}) 
description: ${f.description}
	</#list>
</#if>
</#list>

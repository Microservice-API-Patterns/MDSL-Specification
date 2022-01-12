{ 
"l0": { 
 "_sampleJSONForDataTypesIn": "${genModel.apiName}"<#list genModel.dataTypes as dtype>,
 "${dtype.name}": ${dtype.sampleJSON(0)}</#list>
}, 
"l1": { 
  "_sampleJSONForDataTypesIn": "${genModel.apiName}"<#list genModel.dataTypes as dtype>,
  "${dtype.name}": ${dtype.sampleJSON(1)}</#list>
},
"l2": { 
  "_sampleJSONForDataTypesIn": "${genModel.apiName}"<#list genModel.dataTypes as dtype>,
  "${dtype.name}": ${dtype.sampleJSON(2)}</#list>
}
}

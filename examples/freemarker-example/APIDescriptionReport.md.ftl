---
title: Minimal API Description from MDSL specifiation ${fileName}
author: MDSL Freemarker Generator (via genmodel)
date: tba
---

<#-- see https://microservice-api-patterns.github.io/MDSL-Specification/generators/freemarker -->

<#-- TODO create template variants even closer to templates in the literature (e.g., P. Sturgeon, A. Lauret, M. Amundsen, J. Higginbotham)

The current version is inspired by MAP Tutorial 2 and the CEL/REL tables in DPR
-->

<#function getType type>
<#return type.name> 
</#function>

# API Description for ${genModel.apiName}


## Data types

| Name | Fields | 
|-|-|
<#list genModel.dataTypes as dtype>
| ${dtype.name} | <#-- ${dtype.stereotype} TODO Missing in genmodel --> |
<#if dtype.fields?has_content><#list dtype.fields as f>| | ${f.name}: ${f.typeAsString} (${f.nullable?string('optional','required')}, ${f.list?string('list','single')}) | 
</#list></#if></#list>


<#list genModel.endpoints as endpoint>
## Endpoint type ${endpoint.name}

<#-- ${dtype.role} TODO Missing in genmodel -->
  <#if endpoint.operations?has_content>
  Operations:<#lt>
  | Operation | Responsibility | Parameters | Return Type | State Transition |<#lt>
  |-|-|-|-|-|<#lt>
  <#list endpoint.operations as op>
  | ${op.name} | ${op.responsibility!"*n/a*"} | <#if op.parameters?has_content><#list op.parameters as p> ${p.name} of type ${p.type.name} </#list> <#else> none </#if> | <#if op.response?has_content>${getType(op.response)}<#else>none</#if> | not available yet |<#lt>
  </#list>
  </#if>    
</#list>

<#-- TODO split into minimal and full; feature events, commands state transitions in full report; feature errors and security policies -->

## Providers and bindings 

<#list genModel.providers as provider>
<#if provider?has_content>
${provider.name} offers the following endpoint types:<#list provider.offeredEndpoints as contract> ${contract.name}</#list>
<#-- ${provider.bindings} TODO missing in genmodel (!) -->
</#if>
</#list>

## Providers implementations 

<#list genModel.providerImplementations as providerImpl>
<#if providerImpl?has_content>
${providerImpl.name} 
</#if>
</#list>

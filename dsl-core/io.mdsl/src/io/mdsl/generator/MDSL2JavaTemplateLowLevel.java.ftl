<#-- *Note*: Incomplete implementation, not used in MDSL tools at present (v4.0); demo/PoC level quality
<#-- An example showing that entire MSDL grammar AST is available/accessible in Freemarker templates -->
<#-- Not that business logic in templates can be seen an an anti-pattern, usually you will want to create and work with a data model that is more target-oriented. -->
<#-- In fact, the MDSL tools provide such target language-oriented intemediate model since Version 4.0 (TODO URI to documentation/example -->
<#assign apiName=serviceSpecification.name>
package ${findPackageNameInJavaBinding()!apiName};

// this file was generated from a Freemarker templates from API description ${apiName}
<#assign typeTable=""><#assign tIndex=0><#assign cIndex=0><#assign pIndex=0>

import io.mdsl.annotations.ServiceEndpoint;
import io.mdsl.annotations.ServiceOperation;

<#function findPackageNameInJavaBinding>
<#-- TODO warn if there is more than one Java binding (using only first hit now) -->
<#list serviceSpecification.providers as provider>
<#list provider.epl as endpointList>
<#list endpointList.endpoints as endpointInstance>
<#list endpointInstance.techBindings as techBinding>
<#if techBinding.protBinding.java??><#return endpointInstance.name?keep_before_last(".")>
<#elseif techBinding.protBinding.http??><#return "HTTP">
<#elseif techBinding.protBinding.grpc??><#return "GRPC">
<#elseif techBinding.protBinding.other??><#return "other">
<#else><#return apiName>
</#if>
</#list>
</#list>
</#list>
</#list>
</#function>

<#function convertBaseType mdslType>
  <#if mdslType == "string">
    <#return "java.lang.String">
  <#elseif mdslType == "bool">
    <#return "boolean">
  <#elseif mdslType == "void">
    <#return "java.lang.Object"> // not really what is needed
  <#elseif mdslType == "raw">
    <#return "byte"> // what about char-based blobs? byte vs. byte[]
  <#else>
    <#return mdslType>
  </#if>
  <#-- int, long, double do not need any special treatment here; no float in MDSL (yet) -->
</#function>

<#function convertCardinality cardinality>
  <#if !cardinality??>
    <#return "">
  </#if>

  <#if cardinality.zeroOrOne??>
     <#return "[]">
  <#elseif cardinality.zeroOrMore??> 
    <#return "[]">
  <#elseif cardinality.atLeastOne??> 
    <#return "[]">
  <#elseif cardinality.exactlyOne??> 
    <#return "">
  <#else>
    <#return "UnknownCardinality">
  </#if>
</#function>

<#function convertTreeNode treeNode response=false>
  <#assign tnrt="n/a">
  <#if treeNode.pn??> <#-- violates DRY somewhat -->
      <#if treeNode.pn.atomP??>
       <#if treeNode.pn.atomP.rat??>
        <#if treeNode.pn.atomP.rat.name??><#assign nname=treeNode.pn.atomP.rat.name><#else><#assign nname="p" + pIndex><#assign pIndex+=1></#if>
        <#if treeNode.pn.atomP.rat.btype??><#assign btype=treeNode.pn.atomP.rat.btype><#else><#assign btype="string"></#if>
        <#if treeNode.pn.atomP.card??>
          <#assign tnrt=convertBaseType(btype) + convertCardinality(treeNode.pn.atomP.card) + " " + nname + ";"> 
        <#else>
          <#assign tnrt=convertBaseType(btype) + " " + nname + ";"> 
        </#if>
       </#if>
      <#elseif treeNode.pn.tr??>
        <#if treeNode.pn.tr.name??><#assign genpname=treeNode.pn.tr.name><#else><#assign genpname="p"+pIndex><#assign pIndex+=1></#if>
        <#if response>
          <#assign tnrt=treeNode.pn.tr.dcref.name> 
        <#else>
          <#assign tnrt=treeNode.pn.tr.dcref.name + " " + genpname + ";"> 
        </#if>
      <#elseif treeNode.pn.genP??> 
        <#if treeNode.pn.genP.name??><#assign genpname=treeNode.pn.genP.name><#else><#assign genpname="p"+pIndex><#assign pIndex+=1></#if>
        <#assign tnrt="String " + genpname + "; ">
    </#if>
  <#elseif treeNode.apl??> 
      <#assign tnrt=convertAP(treeNode.apl.first) + "; ">
      <#list treeNode.apl.nextap as nextatom>
      <#assign tnrt=tnrt + convertAP(nextatom) + "; ">
      </#list>
  <#elseif treeNode.children??> 
      <#return convertParameterTree(treeNode.children, true, response)>
  <#else>
      <#assign tnrt="void UnknownTreeNode;"> <#-- cannot get here if this template matches grammar exactly -->
  </#if>
  <#return tnrt>
</#function>

<#function payloadToReturnParameter payload>
  <#-- TODO (L) could put role D, MD, L, ID in comment... same for MAP decorator stereotype -->
  <#assign returnValue="void"> <#-- using "void" as default -->
  <#if payload.np??>
    <#if payload.np.atomP??>
      <#if payload.np.atomP.rat??>
        <#if payload.np.atomP.rat.btype??><#assign btype=payload.np.atomP.rat.btype><#else><#assign btype="string"></#if>
        <#if payload.np.atomP.card??>
          <#assign returnValue=convertBaseType(btype) + convertCardinality(payload.np.atomP.card)> 
        <#else>
          <#assign returnValue=convertBaseType(btype)> <#-- using "string" as default -->
        </#if>
      </#if>
    <#elseif payload.np.tr??> 
      <#if payload.np.tr.name??><#assign genpname=payload.np.tr.name><#else><#assign genpname="p"+pIndex><#assign pIndex+=1></#if>
      <#assign returnValue=payload.np.tr.dcref.name> 
    <#elseif payload.np.genP??> 
      <#assign returnValue="String">
    <#else> 
      <#assign returnValue="UnknownReturnType">
    </#if>
  <#elseif payload.apl??>
      <#-- TODO (M) not using (optional) name/identifier of APL yet -->   
      <#assign returnType=convertAP(payload.apl.first) + "; ">
      <#list payload.apl.nextap as nextatom>
      <#assign returnType=returnType + convertAP(nextatom) + "; ">
      </#list>
      <#assign returnValue="ReturnType"+cIndex>
      <#assign cIndex+=1>
      <#assign typeTable=typeTable + "\n  class " + returnValue + " { " + returnType + "}">
  <#elseif payload.pt??> 
      <#return convertParameterTree(payload.pt, false, true)>
  <#elseif payload.pf??> 
    <#assign cIndex+=1><#assign returnValue="ParameterForestClass"+cIndex>
    <#assign tdef=convertParameterTree(payload.pf.ptl.first, false, false) + ";">
    <#list payload.pf.ptl.next as nextPT>
      <#assign tdef=tdef + " " + convertParameterTree(nextPT, false, false) + ";">
    </#list>
    <#assign typeTable=typeTable + "\n  class " + returnValue + " { " + tdef + "}">
  </#if>
  <#return returnValue>
</#function>

<#function payloadToSignature payload>
  <#-- TODO (L) put role D, MD, L, ID in comment? same for MAP decorator stereotype -->
  <#assign signature=""> <#-- empty parameter list (to begin with) -->
  <#if payload.np??>
    <#if payload.np.atomP??>
      <#assign signature=convertAP(payload.np.atomP!"String")>
    <#elseif payload.np.tr??> 
      <#if payload.np.tr.name??><#assign genpname=payload.np.tr.name><#else><#assign genpname="p"+pIndex><#assign pIndex+=1></#if>
      <#assign signature=payload.np.tr.dcref.name + " " + genpname> 
    <#elseif payload.np.genP??> 
      <#if payload.np.genP.name??><#assign genpname=payload.np.genP.name><#else><#assign genpname="p"+pIndex><#assign pIndex+=1></#if>
      <#assign signature="String " + genpname>
    </#if>
  <#elseif payload.apl??> 
    <#assign signature=convertAP(payload.apl.first)>
    <#list payload.apl.nextap as nextatom>
    <#assign signature=signature + ", " + convertAP(nextatom)>
    </#list>
  <#elseif payload.pt??>
    <#assign signature=convertParameterTree(payload.pt, false, false)> 
  <#elseif payload.pf??> 
    <#assign signature=convertParameterTree(payload.pf.ptl.first, false, false)>
    <#list payload.pf.ptl.next as nextPT>
      <#assign signature=signature + ", " + convertParameterTree(nextPT, false, false)>
    </#list>
  </#if>
  <#if !signature??> <#assign signature="UnknownType"></#if>
  <#return signature>
</#function>

<#function convertParameterTree pt recursive=true, response=false> <#-- corresponds to MDSL grammar rule "parameterTree" (line 125 in v3.3) -->
  <#assign treeType=convertTreeNode(pt.first, false)>
  <#list pt.nexttn as nextnode>
    <#assign treeType=treeType + " " + convertTreeNode(nextnode, false)> <#-- inner types are the same in request and response (no need to take "Instance" off) -->
  </#list>
  <#if pt.name??><#assign treeName=pt.name><#else><#assign cIndex+=1><#assign treeName="AnonymousTree"+cIndex></#if>
  <#assign typeTable=typeTable + "\n  class " + treeName?cap_first + "Class { " + treeType + " }">    
  <#if !response==true>
    <#if recursive==true><#assign instanceName=" " + treeName?uncap_first + "Instance;"><#else><#assign instanceName=" " + treeName?uncap_first + "Instance"></#if>
  <#else>
    <#assign instanceName="">
  </#if>
  <#if pt.card??>
   <#return treeName?cap_first + "Class" + convertCardinality(pt.card)  + instanceName>
  <#else>
    <#return treeName?cap_first + "Class" + instanceName>
  </#if> <#-- TODO (H) implement cardinality everywhere: genP, type ref etc. (check grammar, see Jolie gen) -->
  
</#function>

<#function convertAP ap>
  <#if ap.rat??> <#-- rat is mandatory, so check not really needed -->
    <#if ap.rat.btype??>
      <#assign btype=ap.rat.btype><#else><#assign btype="String">
     </#if>
     <#if ap.card??> 
       <#assign rT=convertBaseType(btype!"string") + convertCardinality(ap.card) + " " + ap.rat.name!"p"+pIndex>
       <#if !ap.rat.name??><#assign pIndex+=1></#if>
       <#return rT>
     <#else>
       <#assign rT=convertBaseType(btype!"string") + " " + ap.rat.name!"p"+pIndex>
       <#if !ap.rat.name??><#assign pIndex+=1></#if>
     <#return rT>
    </#if>
  </#if>
</#function>

<#function convertTypeElementStructure name, schema> 
  <#assign tdef=""> <#-- empty type def list (to begin with) -->
  <#if schema.np??>
    <#if schema.np.atomP??>
      <#assign tdef=convertAP(schema.np.atomP!"String")>
    <#elseif schema.np.tr??> 
      <#assign tdef=schema.np.tr.dcref.name> 
    <#elseif schema.np.genP??> 
      <#if schema.np.genP.name??><#assign genpname=schema.np.genP.name><#else><#assign genpname="p"+pIndex><#assign pIndex+=1></#if>
      <#assign schema="String " + genpname>
    </#if>
  <#elseif schema.apl??> 
    <#assign schema=convertAP(schema.apl.first)>
    <#list schema.apl.nextap as nextatom>
    <#assign tdef=tdef + ", " + convertAP(nextatom)>
    </#list>
  <#elseif schema.pt??>
    <#assign tdef=convertParameterTree(schema.pt, false, false)> 
  <#elseif schema.pf??> 
    <#assign tdef="ParameterForestTODO notYet;"> <#-- TODO (H) not yet: create a class for inline type (see signature, response) -->
  </#if>
  <#if !tdef??> <#assign tdef="UnknownType"></#if>
  <#assign typeTable=typeTable + "\n  class " + name + " { " + tdef + ";}">
  <#return true>
</#function>

<#-- map explicit data types -->
<#list serviceSpecification.types as type>
    <#if type.name??><#assign typeName=type.name><#else><#assign tIndex+=1><#assign typeName="AnonymousType"+tIndex></#if>
    <#assign success=convertTypeElementStructure(typeName, type.structure)>
</#list>

<#-- could also generate apiDoc, see https://apidocjs.com/#examples? -->
<#-- TODO include version numbers if present (put in comment?) -->
// ** endpoints:
<#list serviceSpecification.contracts as contract>

@ServiceEndpoint(<#if contract.primaryRole??>role="${contract.primaryRole}"</#if>) // TODO (M) otherRoles not used yet
public interface ${contract.name}Interface {

    // ** operations:

    <#list contract.ops as operation>
    @ServiceOperation() // TODO (H) responsibilities (see endpoint roles)
    ${payloadToReturnParameter(operation.responseMessage.payload)} ${operation.name}(${payloadToSignature(operation.requestMessage.payload)});     
    </#list>

  // ** inner types: 
${typeTable}
}
</#list>

 
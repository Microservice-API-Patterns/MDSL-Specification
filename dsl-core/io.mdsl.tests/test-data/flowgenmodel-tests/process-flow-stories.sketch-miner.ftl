# MDSL to Sketch Miner Converter Version 0.0.1 (Status: Demonstrator/Proof of Concept):

<#list genModel.orchestrationFlows as oflow>
/// Flow ${oflow.name}

/// Events: 
<#list oflow.getEvents() as ename, event>
/// ${ename}
</#list>

/// Commands:
<#list oflow.getCommands() as command>
/// ${command.name}
</#list>


/// Canonical/normalized flow model:
${oflow.toString()}

/// Paths dump:
<#if oflow.processView().getAllPaths().size()==0>
This flow does not seem to have any paths, at least one init event or command are needed to find one.
<#else>
${oflow.processView().dumpAllPathsDirectly()}
</#if>

/// Story (copy-paste to <a href="https://www.bpmn-sketch-miner.ai/index.html#">Sketch Miner Web App</a>)
<#assign story=oflow.processView().exportAsSketchMinerStories()>
<#if story?has_content>
${oflow.name}:
${story}
<#else>
As this flow does not seem to have any paths (at least one init event or init command required), no path stories can be reported here.
</#if>

</#list> 
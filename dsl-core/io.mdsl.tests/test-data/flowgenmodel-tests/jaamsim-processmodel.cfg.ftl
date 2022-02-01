# MDSL to JaamSim Converter Version V2.4.2 (MDSL Tools Version 5.4.6)
#
# Generation metadata: ${fileName} specifying ${apiName} processed at ${timeStamp}
#
# Invocation example: JaamSim2021-05.jar ${fileName?remove_ending(".mdsl")}.cfg
# 
# Known limitations: 
#
# - The input should only contain one flow; the generated file has to be split manually otherwise
# - Combines semantics does not align well with all event storming/MDSL flow semantics (wait strategy)
# - To be added manually: Assign, Entity Logger, Entity conveyor, Entity processor (and other model objects)

<#-- Future work: generate more expressive attribute definition lists (from event/command types) --> 
<#-- Future work: support MDSL subprocesses, create Assign model objects (flow binding?) --> 
<#-- Future work: feature external program invocation (for subprocess?) -->

<#-- Freemarker macros and functions and constants (TODO macros for special queue/server handling) -->

<#function convertInitEventsAndCommandsToEntityGenerators oflow>
<#assign result="">
<#list oflow.initEvents() as ename, event>
<#assign result=result + " " + event.name + "EntityGenerator"> 
</#list> 
<#list oflow.initCommands() as command>
<#assign result=result + " " + command.name + "EntityGenerator">
</#list>
<#return result>
</#function>

<#-- Main template -->
RecordEdits
<#list genModel.orchestrationFlows as oflow>
<#assign queues=oflow.jaamSimHelper().queues>
<#assign servers=oflow.jaamSimHelper().servers>
<#assign branches=oflow.jaamSimHelper().branches>
<#assign duplicates=oflow.jaamSimHelper().duplicates>
<#assign combines=oflow.jaamSimHelper().combines>
<#assign gatewayGuardServers=oflow.jaamSimHelper().gatewayGuardServers>
<#assign splitAndChoiceQueues=oflow.jaamSimHelper().gateQueues>
<#assign aggregationQueues=oflow.jaamSimHelper().aggregationQueues>
<#assign inputGuards=oflow.jaamSimHelper().inputGuardServers>
<#assign guardQueues=oflow.jaamSimHelper().guardInputQueues>
<#assign statistics=oflow.jaamSimHelper().statistics>

# ** Configuration of JaamSim simulation of flow ${oflow.name} **

Define SimEntity { ${oflow.name}SimEntity } 
Define EntityGenerator {${convertInitEventsAndCommandsToEntityGenerators(oflow)} } 
# Define Assign { SampleAssignment }

Define Queue {<#if oflow.processView().initiationCommands?size gt 0>${oflow.name}InitQueue</#if>${oflow.jaamSimHelper().queueNamesAsString} DeadLetterQueue }
<#if guardQueues??&&guardQueues?size gt 0>Define Queue {${oflow.jaamSimHelper().getNamesAsString(guardQueues)} }<#else># No guard queues in this model</#if>
<#if splitAndChoiceQueues??&&splitAndChoiceQueues?size gt 0>Define Queue {${oflow.jaamSimHelper().getNamesAsString(splitAndChoiceQueues)} }<#else># No split/choice queues in this model</#if>
<#if aggregationQueues??&&aggregationQueues?size gt 0>Define Queue {${oflow.jaamSimHelper().getNamesAsString(aggregationQueues)} }<#else># No aggregation queues in this model</#if>
Define Server {${oflow.jaamSimHelper().serverNamesAsString} }
<#if inputGuards??&&inputGuards?size gt 0>Define Server {${oflow.jaamSimHelper().getNamesAsString(inputGuards)} }<#else># No guard servers in this model</#if>
<#if gatewayGuardServers??&&gatewayGuardServers?size gt 0 >Define Server {${oflow.jaamSimHelper().getNamesAsString(gatewayGuardServers)} }<#else># No guard servers in this model</#if>
<#if branches??&&branches?size gt 0>Define Branch {${oflow.jaamSimHelper().getNamesAsString(branches)} }<#else># No branches in this model</#if>
<#if duplicates??&&duplicates?size gt 0>Define Duplicate {${oflow.jaamSimHelper().getNamesAsString(duplicates)} }<#else># No duplicates in this model</#if>
<#if combines??&&combines?size gt 0>Define Combine {${oflow.jaamSimHelper().getNamesAsString(combines)} }<#else># No combines in this model</#if>
Define Statistics {${oflow.jaamSimHelper().getNamesAsString(statistics)} }
Define EntitySink { ${oflow.name}EntitySink }

# ***  Standard definitions to jumpstart simulation 

Define DiscreteDistribution { TwoWayDiscreteDistribution }
Define ExponentialDistribution { DefaultGenerateDistribution }
Define UniformDistribution { DefaultUniformDistribution }

Define DisplayEntity { XY-Grid XYZ-Axis }
# was: Define ColladaModel { Axis  Grid100x100 }
Define ColladaModel { Axis  Grid100x100 }
Define OverlayClock { Clock }
Define OverlayText { Title }
Define View { View1 }

# *** Sample Assign and Other Sample Objects/Entities

# SampleAssignment StateAssignment { SecondState }
# SampleAssignment AttributeAssignmentList { { 'this.obj.SampleCustomVariable=this.obj.SampleCustomVariable+1' } } <#-- TODO (L) use variable in flow (correlation?) -->
# SampleAssignment NextComponent { ${oflow.name}EntitySink }

TwoWayDiscreteDistribution UnitType { DimensionlessUnit }
DefaultGenerateDistribution UnitType { TimeUnit }
DefaultUniformDistribution UnitType { TimeUnit }

# *** Simulation (as some Statistics usage, and two sample states) *** 

Simulation Description { 'Simulate ${genModel.apiName}' }
# Simulation RunDuration { 1 min }
Simulation RunOutputList { { [${oflow.name}Statistics].SampleAverage/1[min] } { '[${oflow.name}Statistics].EntityTimeAverage("ProcessInitiated")/1[min]' } }
# Simulation NumberOfReplications { 10 }

# *** DiscreteDistribution ***

TwoWayDiscreteDistribution RandomSeed { 1 }
TwoWayDiscreteDistribution ValueList { 1  2 } <#-- note: requires the branch that uses this to define two targets -->
TwoWayDiscreteDistribution ProbabilityList { 0.50  0.50 }

# TODO add three way etc. if needed

# *** ExponentialDistribution ***

DefaultGenerateDistribution RandomSeed { 1 }
DefaultGenerateDistribution Mean { 1  min }

# *** UniformDistribution ***

DefaultUniformDistribution RandomSeed { 3 }
DefaultUniformDistribution MinValue { 0.75  min }
DefaultUniformDistribution MaxValue { 0.80  min }


# *** SimEntity ***

${oflow.name}SimEntity InitialState { ProcessInitiated } 
${oflow.name}SimEntity AttributeDefinitionList { SampleCustomVariable 0 }

# *** EntityGenerator ***

<#list oflow.initEvents() as ename, event>
${event.name}EntityGenerator NextComponent { ${event.name} }
${event.name}EntityGenerator PrototypeEntity { ${oflow.name}SimEntity }
${event.name}EntityGenerator InterArrivalTime { DefaultGenerateDistribution }
${event.name}EntityGenerator PrototypeEntity { ${oflow.name}SimEntity }
${event.name}EntityGenerator BaseName { ${oflow.name}SimEntity }

</#list>
<#list oflow.initCommands() as command>
${command.name}EntityGenerator NextComponent { ${oflow.name}InitQueue }
${command.name}EntityGenerator PrototypeEntity { ${oflow.name}SimEntity }

</#list>

# *** Queues ***

<#list queues as queue>
${queue.name} MaxValidLength { ${queue.maxValidLength} }
${queue.name} StateAssignment { ${queue.name}QueuePassed }

</#list>
<#list guardQueues as guardQueue>
${guardQueue.name} MaxValidLength { ${guardQueue.maxValidLength} }
</#list>
<#list splitAndChoiceQueues as splitOrChoiceQueue>
${splitOrChoiceQueue.name} MaxValidLength { ${splitOrChoiceQueue.maxValidLength} }
</#list>
<#list aggregationQueues as aggregationQueue>
${aggregationQueue.name} MaxValidLength { ${aggregationQueue.maxValidLength} }
</#list>

# *** Servers ***

<#list servers as server>
${server.name} NextComponent { ${server.nextComponent} } 
${server.name} WaitQueue { ${server.waitQueue} } 
${server.name} StateAssignment { ${server.name}Finished }
# ${server.name} ServiceTime { 1.0 h }

</#list>
<#list inputGuards as inputGuard>
${inputGuard.name} NextComponent { ${inputGuard.nextComponent} } 
${inputGuard.name} WaitQueue { ${inputGuard.waitQueue} } 

</#list>
<#list gatewayGuardServers as guardServer>
${guardServer.name} NextComponent { ${guardServer.nextComponent} } 
${guardServer.name} WaitQueue { ${guardServer.waitQueue} } 

</#list>

# *** Branches ***

<#list branches as branch>
${branch.name} NextComponentList { <#list branch.nextComponentList as nextComponent>${nextComponent} </#list>}
<#if branch.nextComponentList?size==2>${branch.name} Choice { TwoWayDiscreteDistribution } 
<#else>${branch.name} Choice { 1 } # TODO define selection 
</#if>

</#list>

# *** Duplicates ***

<#list duplicates as duplicate>
${duplicate.name} NextComponent { ${duplicate.nextComponent} }  
${duplicate.name} TargetComponentList { <#list duplicate.targetComponents as target>${target} </#list>}  

</#list>

# *** Combines ***

<#list combines as combine>
${combine.name} WaitQueueList { <#list combine.waitQueueList as source>${source} </#list>}  
${combine.name} NextComponent { ${combine.nextComponent} }    
${combine.name} RetainAll { TRUE }
# ${combine.name} MatchRequired { TRUE }
# ${combine.name} NumberRequired { 1 }

</#list>

# *** Statistics Elements ***

<#list statistics as statisticsItem> 
${statisticsItem.name} UnitType { TimeUnit }
${statisticsItem.name} NextComponent { ${statisticsItem.nextComponent} }

</#list> 

# *** EntitySink ***

# Nothing to be defined for entity sinks here

# *** EntityProcessor, Assign, ... ***

# not mapped 


# *** GRAPHICS INPUTS ***

Simulation RealTime { TRUE }
Simulation SnapToGrid { TRUE }
Simulation ShowLabels { TRUE }
Simulation ShowSubModels { TRUE }
Simulation ShowEntityFlow { TRUE }
Simulation ShowModelBuilder { TRUE }
Simulation ShowObjectSelector { TRUE }
Simulation ShowInputEditor { TRUE }
Simulation ShowOutputViewer { TRUE }
Simulation ShowPropertyViewer { FALSE }
Simulation ShowLogViewer { FALSE }

TwoWayDiscreteDistribution Position { 7 2 0 m }
DefaultGenerateDistribution Position { 11 2 0  m } 
DefaultUniformDistribution Position { 15 2 0 m }

<#-- TODO (L) look for longest event/command names to asign proper values? -->
<#assign increment = 3> 
<#assign specialsincrement = 4> 

${oflow.name}SimEntity Position { -5 2 0  m } 
${oflow.name}SimEntity Alignment { 0 0 0 }

<#assign count = -2>
<#list oflow.initEvents() as ename, event>
${event.name}EntityGenerator Position { ${count} 2 0 m } <#assign count = count + increment>
</#list>
<#list oflow.initCommands() as command> 
${command.name}EntityGenerator Position { ${count} 2 0 m } <#assign count = count + increment>
</#list>

# SampleAssignment Position { -3 5 0 m } 

<#assign hasGuards = false>
<#assign hasGates = false>
<#assign hasSpecialQueues = false>

# position information for servers
<#assign servercount = -1>
<#list servers as server>
${server.name} Position { ${servercount} 0 0 m } <#assign servercount = servercount + increment>
</#list> 

# guard servers (complex MDSL2JaamSim mapping case)
<#assign specialservercount = 0>
<#list inputGuards as inputGuard>
<#assign hasGuards = true>
${inputGuard.name} Position { ${specialservercount} -2 0 m }<#assign specialservercount = specialservercount + specialsincrement + 1>
</#list>
<#list gatewayGuardServers as server>
<#assign hasGuards = true>
${server.name} Position { ${specialservercount} -2 0 m }<#assign specialservercount = specialservercount + specialsincrement + 1>
</#list>

# position information for branches (commands and events)
<#assign gatecount = 1>
<#list branches as branch>
<#assign hasGates = true>
${branch.name} Position { ${gatecount} -4 0 m }<#assign gatecount = gatecount + specialsincrement + 1> 
</#list>  

# position information for duplicates (commands and events)
<#list duplicates as duplicate>
<#assign hasGates = true>
${duplicate.name} Position { ${gatecount} -4 0 m }<#assign gatecount = gatecount + specialsincrement + 1>
</#list>

# position information for combines (events only)
<#list combines as combine>
<#assign hasGates = true>
${combine.name} Position { ${gatecount} -4 0 m }<#assign gatecount = gatecount + specialsincrement + 1>
</#list>

# position information for split/choice queues, aggregation queues, guard queues (complex MDSL2JaamSim mapping cases)
<#assign specialqueuecount = -1>
<#list guardQueues as queue>
<#assign hasSpecialQueues = true>
${queue.name} Position { ${specialqueuecount} -6 0 m }<#assign specialqueuecount = specialqueuecount + specialsincrement + 1>
</#list>

<#list splitAndChoiceQueues as queue>
<#assign hasSpecialQueues = true>
${queue.name} Position { ${specialqueuecount} -6 0 m }<#assign specialqueuecount = specialqueuecount + specialsincrement + 1>
</#list>

<#list aggregationQueues as queue>
<#assign hasSpecialQueues = true>
${queue.name} Position { ${specialqueuecount} -6 0 m }<#assign specialqueuecount = specialqueuecount + specialsincrement + 1>
</#list>

<#assign queueHeight = -2>
<#if hasGuards=true><#assign queueHeight = queueHeight - 2></#if>
<#if hasGates=true><#assign queueHeight = queueHeight - 2></#if>
<#if hasSpecialQueues=true><#assign queueHeight = queueHeight - 2></#if>

# position information for queues
<#assign queuecount = -2>
<#if oflow.processView().initiationCommands?size gt 0>${oflow.name}InitQueue Position { ${queuecount} ${queueHeight} 0 m } <#assign queuecount = queuecount + increment></#if>
<#list queues as queue>
<#assign hasSpecialQueues = true>
${queue.name} Position { ${queuecount} ${queueHeight} 0 m } <#assign queuecount = queuecount + increment>
</#list>
DeadLetterQueue Position { ${queuecount} ${queueHeight} 0 m } # not used (EIP demonstrator)

# position information for statistics (termination command and events)
<#if servercount gt queuecount><#assign endcount = servercount><#else><#assign endcount = queuecount></#if>
<#if gatecount gt endcount><#assign endcount = gatecount></#if>
<#if specialqueuecount gt endcount><#assign endcount = specialqueuecount></#if>
<#if specialservercount gt endcount><#assign endcount = specialservercount></#if>
<#list statistics as statisticsItem>
${statisticsItem.name} Position { ${endcount} -4 0 m } <#assign endcount = endcount + increment> 
</#list>

# position information for entity sink (one per flow)
${oflow.name}EntitySink Position { ${endcount} -4 0 m }

# *** ColladaModel ***

Axis ColladaFile { <res>/shapes/axis_text.dae }
Grid100x100 ColladaFile { <res>/shapes/grid100x100.dae }

# *** DisplayEntity ***

XYZ-Axis Description { 'Unit vectors' }
XYZ-Axis Alignment { -0.4393409  -0.4410096  -0.4394292 }
# XYZ-Axis Size { 1.125000  1.1568242  1.1266404  m }
XYZ-Axis DisplayModel { Axis }
# from Factory example:
XYZ-Axis Show { FALSE }
XYZ-Axis Movable { FALSE }

XY-Grid Description { 'Grid for the X-Y plane (100 m x 100 m)' }
XY-Grid Size { 100 100 m }
XY-Grid DisplayModel { Grid100x100 }
XY-Grid Movable { FALSE }

# *** OverlayClock ***

Clock Description { 'Simulation date and time' }
Clock ScreenPosition { 15  15 }
Clock AlignBottom { TRUE }
Clock TextHeight { 10 }
Clock FontColour { gray20 }
Clock FontStyle { ITALIC }
Clock DateFormat { 'yyyy-MMM-dd HH:mm:ss.SSS' }

# *** OverlayText ***

Title Description { '${genModel.apiName}' }
Title ScreenPosition { 15  15 }
Title Format { 'Simulation process model for ${genModel.apiName}' }
Title TextHeight { 18 }
Title FontColour { 150  23  46 }
Title FontStyle { BOLD }

# *** View ***

View1 Description { 'Default view window for ${genModel.apiName}' }
# from Factory example:
View1 ViewCenter { -0.883807  0.464479  -2.856374  m }
View1 ViewPosition { -0.883807  0.464479  14.464134  m }
# View1 ViewCenter { 5.975215  -2.17958  1.504278  m }
# View1 ViewPosition { 15.975215  -12.17958  11.504278  m }
View1 ShowWindow { TRUE }
# from Factory example:
View1 Lock2D { TRUE }
View1 SkyboxImage { <res>/images/sky_map_2048x1024.jpg }

# note that each .cfg file should only model and simulate one flow

</#list> 
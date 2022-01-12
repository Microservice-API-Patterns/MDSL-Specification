---
title: Microservice Domain Specific Language (MDSL) Orchestration Flows
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2021.  All rights reserved.
---

[Home](./index) &mdash; [Data Types](./datacontract) &mdash; [Provider and Client](./optionalparts) &mdash; [Tutorial](./tutorial) &mdash; [Cheat Sheet](./quickreference) &mdash; [Transformations](./soad) &mdash; [Generator Tools](./tools)

Orchestration Flows
===================

_Note:_ The status of the language concepts specified here is [*Experimental Preview*](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview.html). Concepts and their tool support might still change in future versions of MDSL.

## Use Cases (When to Specify)

* Business workshop result capturing (for instance, event storming sessions)
* Integration with DDD tools such as [Context Mapper](https://contextmapper.org/docs/application-and-process-layer/)
* Rapid prototyping (for instance, yielding event storming results coming in via Context Mapper)
* Chaining of API operations that depend on each other (API call sequencing and branching) 
* Integration flows, as input for middleware such as Apache Camel or event simulation tools

<!-- 
Early "E-SOAD":

* Application flows in inside a service
* Service orchestration, API operation call sequencing
* EIP-style integration flows (asynch., pipes and filters)
* Testing (mocking, staging)
-->

## Simple Flow Steps 

The following basic flow was generated from the [sample story scenario](./scenarios) with one of the quick fix [transformations](./soad.md) in the [MDSL tools](./tools.md):

~~~
flow Scenario1Flow realizes Scenario1

event something_has_happened triggers command startProcess
command startProcess emits event startProcessCompleted
event CRUDSomeBusinessObjectTrigger triggers command CRUDSomeBusinessObject
event startProcessCompleted triggers command startProcessCompletedProcessor
command CRUDSomeBusinessObject emits event CRUDSomeBusinessObjectCompleted
~~~

In addition to the very basic event-command and command-event pairs used above, it is possible to specific event-command-event triples: 

~~~
event Event1 triggers command Command1 emits event Event2
~~~

The above snippet is semantically equivalent to the folllowing two rules:

~~~
event Event1 triggers command Command1 
command Command1 emits event Event2 
~~~

## Branching and Joining 

Events may trigger multiple commands with and, or, xor semantics: 

~~~
event Event1 triggers command Command1 + Command2
event Event2 triggers command Command1 o Command2
event Event2 triggers command Command1 x Command2
~~~

Commands may emit multiple events (and, or, xor): 

~~~
command StartFlowCommand emits event Event1 + Event2
command Command1 emits event Event1 o Event2
command Command2 emits event Event1 x Event2
~~~

Two or more events may be part of a join/aggregate step:

~~~
event Command1Done + Command2Done triggers command JoinCommand
event Event1 + Event2 + FlowTerminated trigger command TerminateCommand + CleanupCommand
~~~

Note that commands cannot be joined in a single rule. To express alternative event emission (by different commands), it is possible to write:

~~~
command Command1 emits event Event1
command Command2 emits event Event1
~~~

Event joining is required to express that something can only happen when two commands have been executed. 

## Versioning 

If they contain a data structure definition (which is optional), both event types and command types can be versioned (just like entire API descriptions and endpoint types and and data types): 

```
event type something_has_happened version "v1" D<string>, CRUDSomeBusinessObjectTrigger 
event type startProcessCompleted version "v1.0" {"abc":D<int> , "def":D<int>}

command type startProcess version "v10"
```

The [Version Identifier](https://microservice-api-patterns.org/patterns/evolution/VersionIdentifier) (such as `"v1"`) is a plain string, which may be structured according to the [Semantic Versioning](https://microservice-api-patterns.org/patterns/evolution/SemanticVersioning) conventions. Note that a data element definition such as `D<string>` has to ber present; there is no point in versioning a placeholder event/command type.


## Grammar Excerpts 

*Note:* This is an optional language concept. The flow grammar is subject to change still.

### Flow and flow steps (with technology binding)

~~~
Orchestration:
	'flow' name=ID 
	  ('realizes' scenario=[IntegrationScenario])? ('type' type=FlowType)? 
	steps+=FlowStep* 
	flowBinding=OrchestrationBinding?
;
~~~

~~~
enum FlowType: 
	APPLICATION_FLOW  
	| INTEGRATION_FLOW 
	| SERVICE_ORCHESTRATION 
	| API_CALL_SEQUENCING  
	| EVENT_SOURCING
;
~~~
 
~~~
OrchestrationBinding:
	'binding' /* 'flow' */ 'to' (eptb+=EndpointTypeBinding|cb+=ChannelBinding)+
	('implemented' 'as' ft=FlowTechnology)?
;
~~~

~~~
FlowTechnology:
	'APACHE_CAMEL_ROUTE' | 'SPRING_INTEGRATION_FLOW' 
	| 'BPMN_PROCESS' | 'BPEL_PROCESS' | 'YET_ANOTHER_FLOW_LANGUAGE' 
	| 'PLAIN_PROGRAMMING' | STRING 
;
~~~

### Event Types and Command Types

Only event types and command types that have been declared can be used in flows. The syntax for these declarations is similar to that of the [data types](./datacontract) used in endpoint contracts:

~~~
EventTypes: 
	'event' 'type' events+=EventType (',' events+=EventType)*
;

EventType:  
	name=ID (content=ElementStructure)? // the domain data reported
	('version' svi=SemanticVersioningIdentifier)?
;

CommandTypes:
	'command' 'type' commands+=CommandType (',' commands+=CommandType)*
;

CommandType:
	name=ID (subject=ElementStructure)? // in/out data or business object/item
	('version' svi=SemanticVersioningIdentifier)?
;
~~~

The order of the rules does not matter; the control flow is specified partially and implicitly. If commands and events appear in multiple rules, this information is aggregated and assembled in gen model so that generator tools can work on canonical, streamlined models.

See section "Processes and Event/Command Flows" in the [CML language reference](https://contextmapper.org/docs/application-and-process-layer/) for more explanations.

<!-- 
flow SampleFlow type APPLICATION_FLOW
event FlowInitiated triggers command StartFlowCommand
command StartFlowCommand emits event Event1 + Event2
command Command1Command emits event Event1 o Event2
command Command2Command emits event Event1 x Event2
event Event1 triggers command Command1Command + Command2Command
event Event2 triggers command Command1Command o Command2Command
event Event2 triggers command Command1Command x Command2Command
event Command1Done + Command2Done triggers command JoinCommand
event Event1 + Event2 + FlowTerminated triggers command TerminateCommand + CleanupCommand
-->

<!-- 
## Advanced/Experimental Concepts

TODO document more advanced features: binding details, flow type usage, ... 

-->

## Tool Support

 A number of the quick fix [transformations](./soad) operate on flows and their steps. The  in-flow transformations are:

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
|Command in flow step|*Add a domain event production step*|Let the new step emit a `[CommandName]Completed` event |Branching/joining not yet supported in these transformations |
|Event in flow step|*Add a command invocation step (triggered by event)*|Let event trigger a new command `[EventName]Processor` in a new flow step | Supports incremental modeling |
|Combined step|*Split step into a command invocation and an event production*|Replaced single combined step with two simple ones |Only works if command is simple |
|Events in DEP step |*Inject a parallel branch (with join)*|Two events, two commands, one aggregator added |Added at end of flow|
|Command in CIS step|*Inject a choice branch (with join)*|Two events, two commands, one aggregator added |Added at end of flow; condition not specified |
|Command in DEP step |*Merge all single event productions of this command*|Consolidates multiple rules into one that emits multiple events| Events are composed with or semantics | 

<!-- TODO what if input has AND in it? or OR/XOR? -->

Flows can be used to generate endpoint types (with operations realizing the steps):

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
| [Flow](./flows.md) |*Derive endpoint type from application flow*|`Endpoint` added to name of new endpoint, which receives a flow start event |Operations can be added later|
|Command in flow step|*Derive an operation to endpoint type that realizes this command*|[MAP](https://microservice-api-patterns.org/) decorator: `STATE_TRANSITION_OPERATION`|`transitions from ... to ...` added to operation |
|Event in flow step|*Derive an event handler operation for flow step*|MAP decorator: `EVENT_PROCESSOR`|`emitting event` added to operation, event added to `receives` part of endpoint contract |

Freemarker templates allow converting MDSL flows into Apache Camel configurations and into the Sketch Miner story format (which in turn yields BPMN).

## Known Limitations 

* It is possible to specify incomplete, even invalid models (from a workflow management middlware point of view); results from workshops often yield such imperfect, intermediate models.
* The MDSL Tools do not fully support all flow modeling concepts of MDSL: 
  * Subprocesses cannot be processed by quick fix transformations and generators.
  * Combined event-command-event rules cannot be processed by all tools completely (quick fixes, code generators). For instance, the productivity quick fixes expect the command part of such rules to contain a single simple command only.
  * The Context Mapper Language (CML) has support for operations that delegate to service methods in Aggregates; mapping such operations is not supported in the CML to MDSL generator in Context Mapper (as the MDSL grammar does not have such a concept).
* While the models are normalized into a canonic format (see flow gen model export), not all duplicate event emissions and command triggers are detected and may cause invalid gen model content and generator output.
* The flow models do not mandate a single control flow that is guaranteed to terminate. 
   * Loops can be detected by the MDSL Tool generators, but not all edge cases are covered.
   * Many validations could be implemented, for instance single entrance, single exist checks. There is a large body of work in the workflow and BPM community on these topics, which could possibly be leveraged.

<!-- The BPMN stories do not yet distinguish between parallel splits and choices, and may not cover all edge cases. -->

# Site Navigation

* Language specification: 
    * [Scenarios and stories](scenarios.md)
    * Service [endpoint contract types](./servicecontract) (this page) and [data contracts (schemas)](./datacontract)
    * [Bindings](./bindings) and [instance-level concepts](./optionalparts)
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* [MDSL homepage](./index)

*Copyright: Olaf Zimmermann, 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
---
title: Microservice Domain Specific Language (MDSL) Orchestration Flows
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2021.  All rights reserved.
---

[Home](./index) &mdash; [Data Types](./datacontract) &mdash; [Bindings](./bindings) &mdash; [Provider and Client](./optionalparts) &mdash; [Tutorial](./tutorial) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools)

Orchestration Flows
===================

_Note:_ The status of these language concept is [*Experimental Preview*](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview.html). The grammar and the tool support might change, in breaking ways, in future versions of MDSL.

## Use Cases (When to Specify)

* Rapid prototyping (for instance, of event storming results coming in from [Context Mapper](https://contextmapper.org/docs/application-and-process-layer/))
* API call sequencing 
* Integration flows, as input for middleware such as Apache Camel 

<!-- 
* Early SOAD
* Application flows in inside a service
* Service orchestration, API operation call sequencing
* EIP-style integration flows (asynch., pipes and filters)
* Testing (mocking, staging)
-->

## Grammar Excerpts 

This is an optional language concept. The flow grammar is subject to change as this is a technology preview.

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

### Event types and command types

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

## Example

This basic flow was actually generated from the [sample story scenario](./scenarios) with one of the quick fix [transformations](./soad.md) in the [MDSL tools](./tools.md).

~~~
flow Scenario1Flow realizes Scenario1
event something_has_happened triggers command startProcess
command startProcess emits event startProcessCompleted
event CRUDSomeBusinessObjectTrigger triggers command CRUDSomeBusinessObject
event startProcessCompleted triggers command startProcessCompletedProcessor
command CRUDSomeBusinessObject emits event CRUDSomeBusinessObjectCompleted
~~~

See section "Processes and Event/Command Flows" in the [CML language reference](https://contextmapper.org/docs/application-and-process-layer/) for explanations.

<!-- TODO document more advanced features: binding, type, ... -->

# Site Navigation

* Language specification: 
    * [Scenarios and stories](scenarios.md)
    * Service [endpoint contract types](./servicecontract) (this page) and [data contracts (schemas)](./datacontract)
    * [Bindings](./bindings) and [instance-level concepts](./optionalparts)
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* [MDSL homepage](./index)

*Copyright: Olaf Zimmermann, 2018-2021. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
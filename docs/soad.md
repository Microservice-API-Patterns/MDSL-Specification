---
title: Microservice Domain Specific Language (MDSL) Service Contracts
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2021.  All rights reserved.
---

[Home](./index) &mdash; [Primer](./primer) &mdash; [Endpoint Types](./servicecontract) &mdash; [Bindings](./bindings) &mdash; [Tutorial](./tutorial) &mdash;  [Tools](./tools)

MDSL Transformations
====================

## Goal- and Service-Oriented Analysis and Design 

### Use Cases for the Transformations

* Goal-oriented API first design (the "purposeful" in [POINT](https://ozimmer.ch/practices/2021/03/05/POINTPrinciplesForAPIDesign.html))
* Stepwise service design as features in [DPR](https://socadk.github.io/design-practice-repository/activities/SDPR-StepwiseServiceDesign.html)
* [API refactoring](https://interface-refactoring.github.io/)

Go [here](./primer) or [here](./tutorial) to gain a basic understanding of the MDSL concepts and language constructs so that you can benefit from the transformations described on this reference page.

A number of transformations are available, most of which are implemented as plugin quick fixes.

*Note:* The modified  MDSL might not always validate after a transformation (for instance, when operation names are not unique). This is deliberate; it is possible to correct such issues with subsequent refactorings (such as *Rename Element*) or basic manual edit operations. 

*Note:* If certain preconditions are not met, some quick fixes will fail. The "Error Log" view in Eclipse will have an entry in that case that reports the problem.

### Analysis and Orchestration Steps

The available analysis transformations (early steps, story level):

|MDSL element|Transformation (Quick Fix)|Resulting MDSL|Comments (Preconditions, Variants)|
|-|-|-|-|
|[Scenario](./scenarios.md)|*Derive application flow from scenario*|Basic flow, single step (one event, one command)|Can be a requirement, an integration scenario, a test case|
|Scenario|*Add endpoint type supporting this scenario*|Endpoint type (with operations for stories)|CRUD stories receive special attention|
|Scenario story|*Add operation to endpoint type supporting scenario*|Operation for action, role stereotyped|Endpoint may or may not exist|

Flows optionally can serve as analysis and design bridge en route to endpoint types and their technology-specific refinements:

|MDSL element|Transformation (Quick Fix)|Resulting MDSL|Comments (Preconditions, Variants)|
|-|-|-|-|
|[Flow](./flows.md) step|*Add a command invocation step (triggered by event)*|Let event trigger a new command in new step | Supports incremental modeling |
|Flow step|*Add a domain event production step (emitting event)*|Let the new step emit a 'CommandDone' event |Branching/joining supported in language but not in transformations |
|Flow|*Derive endpoint type from application flow*|`Endpoint` added to name of new endpoint, which receives a flow start event |Operations can be added later|
|Command (invocation) in a flow step|*Add an operation to endpoint type that realizes this command*|MAP decorator: `STATE_TRANSITION_OPERATION`|`transitions from ... to ...` added to operation |
|Flow step emitting an event|*Derive event processor operation from flow step*|MAP decorator: `EVENT_PROCESSOR`|`emitting event` added to operation|

<!-- |||tba|-| -->

### API Design Steps

This table lists and specifies the endpoint-level design transformations (intermediate steps): 

|MDSL element|Transformation (Quick Fix)|Resulting MDSL|Comments (Preconditions, Variants)|
|-|-|-|-|
|[Endpoint type](./servicecontract)|*Turn into Processing Resource*|CRUD operations added|Turn into [Information Holder Resource](https://microservice-api-patterns.org/patterns/responsibility/endpointRoles/InformationHolderResource) also available|
|Endpoint type|*Add operations common/typical for this role stereotype*|CRUD operations added for Processing Resource|Retrieval operations added for  Information Holder Resource, other patterns also supported (experimental) |
|Operation|*Add error/status report*|Adds basic example of a report|To be refined manually|
|Operation|*Add security policy*|Adds basic example of a policy|To be refined manually|
|Endpoint type|*Add compensating operation*|Business-level undo (saga support)|Must choose an existing operation in same endpoint|
|Event in endpoint type|*Introduce event management*|Adds suited operations to endpoint|Requires a `receives` event reference to be present|
|Endpoint type|*Add HTTP binding*|Adds a provider with location information and a single HTTP resource binding |Only available if endpoint type cannot be mapped to single resource (due to number and nature of operations) |
|Provider|*Split HTTP binding*|Moves all operations that cause OpenAPI mapping conflicts to new resource|Must have an HTTP binding (with default resource), might have to be executed multiple times to resolve all mapping conflicts|

An additional `Move Operation`refactoring is available as a menu entry, not as a quick fix. And [AsyncMDSL](./asynch-mdsl.md) can be generated from core MDSL via a menu entry too.

<!-- TODO only few on operation level ?-->
This table lists and specifies transformations related to data types: 

|MDSL element|Transformation (Quick Fix)|Resulting MDSL|Comments (Preconditions, Variants)|
|-|-|-|-|
|[Data type](./datacontract) placeholder `P` or id only `"anonymous"` |*Replace with atomic string parameter*|`"anonymous":D<string>`|Can serve as input to subsequent transformations|
|Incomplete type `"someType":D` |*Add string as type* |`"someType":D<string>`|Also available for other basic types (bool, int, raw, long, double)|
|Atomic parameter list `("a":D , "b":D<int>)`|*Replace atomic parameter list with parameter tree*|`{"a":D , "b":D<int>}`|Trees are more flexible|
|Atomic parameter `"a":ID<int>`|*Wrap atomic parameter with parameter tree*|`"aWrapper":{"a":ID<int>}`|Can not be applied recursively yet |
|Atomic parameter `"a":MD<int>`|*Include atomic parameter in key-value map*|`"aKeyValueMap":{"key":ID<string> , "a":MD<int>}`|Example of a commonly used data structure|
|Any type in message payload|*Extract data type definition*|`data type` definition and type reference| To support reuse of specification elements <!-- TODO how about events? commands? --> |
|Any parameter tree type|*Classify/decorate as Pagination*|stereotype `<<Pagination>>` added|Also available for Wish List and Request Bundle |
|Parameter tree marked with `<<Pagination>>`|*Introduce offset-based pagination*|Atomic parameters added|Both request and response message must be parameter trees; transformation only works for inlined types|

*Note:* Only few of these refactorings are also supported in the CLI (for instance, `Add Pagination`. 

## Example

Applying the above transformations, you can go from: 

~~~
API description SoadDemo

scenario SampleIntegrationScenario
  story ElaborateStory
   when "something has happened" //precondition
   a "customer and/or integrator" // role
   wants to "doSomething" // business activity 
   yielding "a result" // outcome
   so that "both actors are satisfied and profit is made" // goal 
    
  story ShortStory
    a API client
    wants to CRUD "SomeBusinessObject" 
    // CRUD is short for Create, Read, Update, Delete  
~~~

to:

~~~
API description SoadDemo

data type SomeBusinessObjectDTO {"someBusinessObject":D}

event type something_has_happened
event type CRUDSomeBusinessObjectTrigger
event type SampleIntegrationScenarioFlowInitiated "eventDetails":MD<string>

command type doSomething
command type CRUDSomeBusinessObject

endpoint type SampleIntegrationScenarioRealizationEndpoint 
  supports scenario SampleIntegrationScenario serves as INFORMATION_HOLDER_RESOURCE
exposes
operation doSomething 
  expecting payload {"location":D , "actor":D} 
  delivering payload "doSomethingResponseBody"
operation createSomeBusinessObject with responsibility STATE_CREATION_OPERATION 
  expecting payload SomeBusinessObjectDTO 
  delivering payload "successFlag":D<string>
operation readSomeBusinessObject with responsibility RETRIEVAL_OPERATION 
  expecting payload "queryFilter":D<string> 
  delivering payload "resultSet":SomeBusinessObjectDTO*
operation updateSomeBusinessObject with responsibility STATE_TRANSITION_OPERATION 
  expecting payload "changeRequest":SomeBusinessObjectDTO 
  delivering payload "updateResult":SomeBusinessObjectDTO
operation deleteSomeBusinessObject with responsibility STATE_DELETION_OPERATION 
  expecting payload "resourceId":ID<string> 
  delivering payload "successFlag":D<bool>
operation findAll with responsibility RETRIEVAL_OPERATION 
  expecting payload "queryParameters" 
  delivering payload "queryResults"
operation findById with responsibility RETRIEVAL_OPERATION 
  expecting payload "id" delivering payload "dto"

endpoint type SampleIntegrationScenarioFlowEndpoint 
  supports flow SampleIntegrationScenarioFlow serves as PROCESSING_RESOURCE
exposes
operation doSomething with responsibility STATE_TRANSITION_OPERATION 
  expecting payload <<Wish_List>> "doSomethingRequestBodyWrapper":{
    "doSomethingRequestBody":D<string>, "desiredElements":MD<string>*}
  delivering payload "doSomethingResponseBody" 
  transitions from "doSomethingTriggered" to "doSomethingExecuted"
operation cRUDSomeBusinessObject with responsibility EVENT_PROCESSOR 
  expecting payload "cRUDSomeBusinessObjectRequestBody" 
  delivering payload "cRUDSomeBusinessObjectResponseBody" 
  emitting event CRUDSomeBusinessObjectCompleted
receives event SampleIntegrationScenarioFlowInitiated

API provider SampleIntegrationScenarioRealizationEndpointProvider 
offers SampleIntegrationScenarioRealizationEndpoint 
at endpoint location "http://localhost:8080"
via protocol HTTP binding
resource Home at "/Home"
operation doSomething to POST all elements realized as BODY parameters
operation createSomeBusinessObject to PUT all elements realized as BODY parameters
operation readSomeBusinessObject to GET all elements realized as QUERY parameters
operation deleteSomeBusinessObject to DELETE all elements realized as PATH parameters
resource SplitResource2 at "/SplitResource2"
operation updateSomeBusinessObject to POST all elements realized as BODY parameters
operation findAll to GET all elements realized as QUERY parameters
resource SplitResource3 at "/SplitResource3"
operation findById to GET all elements realized as QUERY parameters

flow SampleIntegrationScenarioFlow realizes SampleIntegrationScenario
event something_has_happened triggers command doSomething
event CRUDSomeBusinessObjectTrigger triggers command CRUDSomeBusinessObject
~~~

without any programming or MDSL editing. Several transformation steps were applied to the input story. 

*Exercise:* Can you identify the involved transformations? Hint: apply the ones listed in the tables above; watch the validation warnings (and information and errors) while doing so. 

You can find the answer in the HTML source of this page.
<!-- 
Answers to exercise question (solution):

1. *Derive application flow from scenario*
2. *Add endpoint type supporting this scenario* (which in turn also calls *Add operation to endpoint type supporting scenario*)
3. *Derive endpoint type from application flow* 
4. *Add an operation to endpoint type that realizes this command* and **Derive event processor operation from flow step*"
5. *Turn into Information Holder Resource* (scenario endpoint; flow endpoint already is a Processing Resource) followed by *Add operations common/typical for this role stereotype*
7. *Add HTTP binding* followed by two applications of *Split HTTP binding*
8. *Replace with atomic string parameter* (on request parameter of `doSomething` in flow endpoint), followed by *Wrap atomic parameter with parameter tree* 
9. *Classify/decorate as Wish List* and then *Introduce Wish List*
10. (MDSL menu) Generate OpenAPI Specification

Not featured in above example: 

* Two flow-to-flow transformations
* *Add error/status report* and other operation-level transformations
* *Add event management*
* Other data type level transformations: 
    * working with pagination, request bundles and extracting types
    * turning atomic parameter list into parameter tree 
    * introducing key-value-store
-->

## Generating Technology-Specific Contracts

Once the MDSL contract is reasonably complete, the [generators](./tools.md)  can be used to turn it into OpenAPI etc. (final steps). The OpenAPI generated from the above MDSL is [here](./media/FromStoryToOASForMDSLWebsite-Target.yaml).

*Question:* How long did the entire journey take you? Compare with code-first or contract first with plain OpenAPI (or other platform IDL) editing (i.e., no transformation and pattern support).

# Site Navigation

* [MDSL Home](./index) and [MDSL Primer](./primer)
* [Quick reference](./quickreference), [Tutorial](./tutorial) and [Tools](./tools)
* Language specification: service [endpoint contract types](./servicecontract) and [data contracts (schemas)](./datacontract), [bindings](./bindings), [instance-level constructs](./optionalparts),  [integration scenarios including stories](scenarios.md), [orchestration flows](flows.md).

*Copyright: Olaf Zimmermann, 2018-2021. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
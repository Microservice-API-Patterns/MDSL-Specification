---
title: Microservice Domain Specific Language (MDSL) Service Contracts
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2021.  All rights reserved.
---

[Home](./index) &mdash; [Primer](./primer) &mdash; [Stories](./scenarios.md)  &mdash; [Flows](./flows.md) &mdash; [Endpoint Types](./servicecontract) &mdash; [Bindings](./bindings) &mdash; [Tutorial](./tutorial) &mdash;  [Tools](./tools)

MDSL Transformations
====================

## Goal- and Service-Oriented Analysis and Design 

More than 30 transformations are available, implemented as quick fixes in the [MDSL Tools](./tools) plugin. Most of these transformations are also available as refactorings in the standalone API and can be called from external applications; selected ones are exposed in the Command Line Interface as well. 

We assume a basic understanding of MDSL on this reference page. [Primer](./primer) and [tutorial](./tutorial) explain the MDSL concepts and language constructs. <!-- TODO blog post -->

### Use Cases for the Transformations

* Goal-oriented design following an API-first approach, corresponding to the "purposeful" principle of [APIs should stick to their POINT](https://ozimmer.ch/practices/2021/03/05/POINTPrinciplesForAPIDesign.html)
* Stepwise service design as featured in [DPR](https://socadk.github.io/design-practice-repository/activities/SDPR-StepwiseServiceDesign.html) and other API design processes <!-- such as ADDR --> 
* Support for [API refactoring](https://interface-refactoring.github.io/)

### Analysis and Orchestration Steps

The available analysis transformations supporting early steps of service/API design (on scenario/story level) are:

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
|[Scenario](./scenarios.md)|*Derive application flow from scenario*|Basic flow, single step (one event, one command)|Input can be API requirement, integration scenario/story set, test case|
|Scenario|*Add endpoint type supporting this scenario*|Endpoint type (with operations for stories)|CRUD stories receive special attention (keyword causes four operations to be created)|
|Scenario story|*Add operation to endpoint type supporting scenario*|Operation for action, role stereotyped|Endpoint exposing the operation may or may not exist already|

Flows optionally can serve as analysis and design bridge en route to endpoint types and their technology-specific refinements:

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
| [Flow](./flows.md) |*Derive endpoint type from application flow*|`Endpoint` added to name of new endpoint, which receives a flow start event |Operations can be added later|
|Command in flow step|*Derive operation(s) that realize(s) command(s)*|[pattern](https://microservice-api-patterns.org/) role decorator: `STATE_TRANSITION_OPERATION`|`transitions from ... to ...` also added to operation |
|Event in flow step|*Derive event handler operation(s) for flow step*|pattern role decorator: `EVENT_PROCESSOR`|`emitting event` added to operation, event added to `receives` part of endpoint contract |

Additional flow-to-flow transformations are available and documented [here](./flows.md).

### API Endpoint/Operation Design Steps

The following table lists and specifies the transformations supporting intermediate design steps that operate on endpoints operations:

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
|[Endpoint type](./servicecontract)|*Turn into [Processing Resource](https://microservice-api-patterns.org/patterns/responsibility/endpointRoles/ProcessingResource)*|CRUD operations added|Other supported patterns: `VALIDATION_RESOURCE`,  `TRANSFORMATION_RESOURCE`, `STATELESS_PROCESSING_RESOURCE` |
| Endpoint type |*Turn into [Information Holder Resource](https://microservice-api-patterns.org/patterns/responsibility/endpointRoles/InformationHolderResource)*| Lookup/finder operations added|Other patterns also supported: `COLLECTION_RESOURCE` (as other responsibility), `DATA_TRANSFER_RESOURCE`, `LINK_LOOKUP_RESOURCE`, `OPERATIONAL_DATA_HOLDER`, `MASTER_DATA_HOLDER`, `REFERENCE_DATA_HOLDER` <!-- `MUTABLE_COLLECTION_RESOURCE` --> |
|Endpoint type|*Add operations common/typical for this role stereotype*|CRUD operations and/or retrieval operations added | Specific operations: state-oriented CRUD ones for Processing Resources, retrieval operations for Information Holder Resources, Data Transfer Resource, Link Lookup Resource, set-oriented dones for Collection Resource |
|Event in endpoint type|*Introduce event management*|Adds suited operations to endpoint|Requires a `receives` event reference to be present|

Operation design is also supported by a number of transformations:

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
|Operation|*Add error/status report*|Adds basic example of a report|To be refined manually|
|Operation|*Add security policy*|Adds basic example of a policy|To be refined manually|
|Operation|*Add compensating operation*|Business-level undo (saga support)|Must choose an existing operation in same endpoint|
|Operation|*Move operation/extract endpoint*|Target endpoint may or may not exist|HTTP bindings also moved (if present)|

The `Move Operation/Extract Endpoint` refactoring is available as a menu entry and as a quick fix.

Once a platform- and technology-independent API design has been reached, it is time to map it to HTTP via [bindings](./bindings):

|Endpoint type|*Add HTTP binding*|Adds a provider with location information and a single HTTP resource binding |Particularly useful if endpoint type cannot be mapped to single resource (due to number and nature of operations) |
|Provider/binding|*Split HTTP binding*|Moves operation(s) that cause(s) OpenAPI mapping conflict(s) to new resource|Might have to be executed multiple times to resolve all mapping conflicts|
|Provider/binding|*Extend resource URI with template for PATH parameter*|See RFC 6570 for `{uri}` path syntax.|Only works if elements are bound individually (and has impact on all operations in this resource)|
|Provider/binding|*Move operation binding to new resource with URI template for PATH parameter*|Other operations can be moved to new resource manually|Only works if elements are bound individually (and resource name is not already taken)|
|Provider/binding|*Bind message elements to HTTP parameters individually*|PATH, QUERY, BODY, etc.|Might have to be improved manually for later OpenAPI generation |

[AsyncMDSL](./asynch-mdsl.md) can be generated from core MDSL via a menu entry as well.

## Transformations Related to Patterns and Refactorings

This table lists and specifies the operation-level design transformations that refactor an existing API design according to certain patterns (aiming at quality improvements):

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
|Operation with parameter tree request |*Introduce Wish List*|Set parameter added, decorated with marked with `<<Wish_List>>`|Transformation only works for parameter trees <!-- optionality of response element not yet checked -->|
|Operation with parameter tree request |*Introduce Wish Template*|Mock DTO added, decorated with marked with `<<Wish_Template>>`| Only works for tree-typed requests; Wish List overwritten if present |
|Operation working with parameter trees |*Introduce page-based pagination*|Metadata parameters added, response decorated with marked with `<<Pagination>>`|Both request and response message must be parameter trees; transformation only works for inlined types|
|Operation working with parameter trees |*Introduce offset-based pagination*|Same as for page-based variant |See [Pagination](https://microservice-api-patterns.org/patterns/structure/compositeRepresentations/Pagination) pattern|
|Operation working with parameter trees |*Introduce cursor-based pagination*|Same as for other two pagination variants |See [Introduce Pagination](https://interface-refactoring.github.io/refactorings/introducepagination) refactoring|
|Operation working with parameter trees |*Bundle Requests*|Parameter tree turned into set and marked with `<<Request_Bundle>>`| Also available for response message (*Bundle Responses*); transformation only works for inlined types, not for type references|
|Operation |*Introduce Context Representation*|Sample DTO added and referenced in request payload `<<Context_Representation>>`|Request message must be parameter tree |
|Operation |*Make Request Conditional*|Metadata parameter added to request payload `<<Request_Condition>>`|Request message must be parameter tree |



|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-|
|Operation|*Split operation*|Replaces operation with one per top-level request element|Requires request to be a parameter tree (used as "CSV" list of operation-specific data); does not modify but copy responses|
|Operation with `<<Embedded_Entity>>` element in request or response |*Extract Information Holder*|Endpoint and DTO data type added; stereotyped link parameter replaces entity|Message payload must be parameter tree (may appear in request or response) |
|Operation with `<<Linked_Information_Holder>>` element in request or response |*Inline Information Holder*|stereotyped type reference replaces link parameter|Message payload must be parameter tree |
|Endpoint and its operations|*Segregate commands from queries*|Apply CQRS pattern by moving retrieval operations to new endpoint|New endpoint may exist already, names of moved operations not checked for uniqueness|

See the online ["Interface Refactoring Catalog"](https://interface-refactoring.github.io/) co-authored by Mirko Stocker and Olaf Zimmermann for more information about these transformations.

## Data Type Completions and Extensions

This table lists and specifies transformations related to data, event, and command types: 

|**MDSL element**|**Transformation (Quick Fix)**|**Resulting MDSL**|**Comments (Preconditions, Variants)**|
|-|-|-|-----|
|[Data type](./datacontract) placeholder `P` or id only `"anonymous"` |*Replace with atomic string parameter*|`"anonymous":D<string>`|Can serve as input to subsequent transformations|
|Incomplete type `"someType":D` |*Add string as type* |`"someType":D<string>`|Also available for other basic types (bool, int, raw, long, double)|
|Atomic parameter `"a":ID<int>`|*Wrap atomic parameter with parameter tree*|`"aWrapper": {"a":ID<int>}`|Also available for type references, but can not be applied on trees yet |
|Atomic parameter `"a":MD<int>`|*Include atomic parameter in key-value map*|`"aKeyValueMap":{"key":ID<string>, "a":MD<int>}`|Example of a commonly used data structure|
|Atomic parameter list `("a":D , "b":D<int>)`|*Replace atomic parameter list with parameter tree*|`{"a":D , "b":D<int>}`|Type references can be wrapped too|
|Any type in message payload|*Extract data type definition*|`data type` definition and type reference| To support reuse of specification elements |

*Note:* If certain preconditions are not met, some quick fixes will fail. If so, the "Error Log" view in Eclipse (which hosts the [MDSL Tools](./tools)) will have an entry that reports the problem.

*Note:* The modified  MDSL might not always validate after a transformation (for instance, when operation names are not unique). This is deliberate; it is possible to correct such issues with subsequent refactorings (such as *Rename Element*) or basic manual edit operations. 

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

to such an MDSL specification without any programming or editing: 

~~~
API description SoadDemo
data type SomeBusinessObjectDTO {"someBusinessObject":D}
data type SampleIntegrationScenarioRealizationEndpointDTO "sampleIntegrationScenarioRealizationEndpoint":D<string>

event type something_has_happened
event type CRUDSomeBusinessObjectTrigger
event type SampleIntegrationScenarioFlowInitiated "eventDetails":MD<string>
event type CRUDSomeBusinessObjectCompleted

command type doSomething
command type CRUDSomeBusinessObject

endpoint type SampleIntegrationScenarioRealizationEndpoint supports scenario SampleIntegrationScenario
serves as INFORMATION_HOLDER_RESOURCE
exposes
  operation doSomething expecting payload {"dataTransferObject":D} delivering payload "doSomethingResponseBody":D<string>
  operation createSomeBusinessObject with responsibility STATE_CREATION_OPERATION expecting payload SomeBusinessObjectDTO delivering payload "resourceId":ID<string>
  operation readSomeBusinessObject with responsibility RETRIEVAL_OPERATION expecting payload "queryFilter":MD<string> delivering payload "resultSet":SomeBusinessObjectDTO*
  operation updateSomeBusinessObject with responsibility STATE_TRANSITION_OPERATION expecting payload "changeRequest":SomeBusinessObjectDTO delivering payload "updateResult":SomeBusinessObjectDTO
  operation deleteSomeBusinessObject with responsibility STATE_DELETION_OPERATION expecting payload "resourceId":ID<string> delivering payload "successFlag":D<bool>
  operation findAll with responsibility RETRIEVAL_OPERATION expecting payload "queryFilter":MD<string> delivering payload "resultSet":SampleIntegrationScenarioRealizationEndpointDTO*
  operation findById with responsibility RETRIEVAL_OPERATION expecting payload "resourceId":ID<string> delivering payload SampleIntegrationScenarioRealizationEndpointDTO

endpoint type SampleIntegrationScenarioFlowEndpoint supports flow SampleIntegrationScenarioFlow serves as PROCESSING_RESOURCE
exposes
operation doSomething with responsibility STATE_TRANSITION_OPERATION 
  expecting payload "doSomethingRequestBodyWrapper":{"doSomethingRequestBody":D<string>, <<Wish_List>> "desiredElements":MD<string>*} 
  delivering payload "doSomethingResponseBody" 
  transitions from "doSomethingTriggered" to "doSomethingExecuted"
operation cRUDSomeBusinessObject with responsibility EVENT_PROCESSOR 
  expecting payload "cRUDSomeBusinessObjectRequestBody" 
  delivering payload "cRUDSomeBusinessObjectResponseBody" 
  emitting event CRUDSomeBusinessObjectCompleted
receives event SampleIntegrationScenarioFlowInitiated

API provider SampleIntegrationScenarioRealizationEndpointProvider offers SampleIntegrationScenarioRealizationEndpoint at endpoint location "http://localhost:8080"
via protocol HTTP binding
resource Home at "/Home"
  operation doSomething to POST all elements realized as BODY parameters
  operation createSomeBusinessObject to PUT all elements realized as BODY parameters
  operation readSomeBusinessObject to GET all elements realized as QUERY parameters
resource Home_deleteSomeBusinessObject at "/{resourceId}"
  operation deleteSomeBusinessObject to DELETE element "resourceId" realized as PATH parameter
resource Resource2 at "/Resource2"
  operation updateSomeBusinessObject to POST all elements realized as BODY parameters
  operation findAll to GET all elements realized as QUERY parameters
resource Resource3 at "/Resource3"
  operation findById to GET all elements realized as QUERY parameters

flow SampleIntegrationScenarioFlow realizes SampleIntegrationScenario
  event something_has_happened triggers command doSomething
  event CRUDSomeBusinessObjectTrigger triggers command CRUDSomeBusinessObject
  command CRUDSomeBusinessObject emits event CRUDSomeBusinessObjectCompleted
~~~

Several transformation steps were applied to the input story. It also is available [here](./media/FromStoryToOASForMDSLWebsite-Target.mdsl). You can generate OpenAPI and other IDL formats now. <!-- TODO redo example, copy (again) from instructions below, to here and to file -->

*Exercise:* Can you identify the involved transformations? Hint: apply the ones listed in the tables above; watch the validation warnings (and information and errors) while doing so. 

You can find the answer in the HTML source of this page.

<!-- 
Answers to exercise question (solution):

1. *Derive application flow from scenario* and then *Add a domain event production step (emitting event)"*
2. *Add endpoint type supporting this scenario* (which in turn also calls *Add operation to endpoint type supporting scenario*)
3. *Derive endpoint type from application flow* 
4. *Add an operation to endpoint type that realizes this command* and *Derive event processor operation from flow step*"
5. *Decorate as Information Holder Resource* (scenario endpoint; flow endpoint already is a Processing Resource) followed by *Add operations common/typical for this role stereotype*
7. *Provide HTTP binding* followed by two applications of *Split HTTP binding* and *Bind message elements to HTTP parameters individually* and *Move operation binding to new resource with URI template for PATH parameter*
8. *Replace with atomic string parameter* (on request parameter of `doSomething` in flow endpoint), followed by *Wrap atomic parameter with parameter tree* 
9. *Introduce Wish List* (also on request parameter of `doSomething` in flow endpoint) 
10. (in MDSL menu) Generate OpenAPI Specification

Not featured in above example (try next?): 

* The other flow-to-flow transformations that create a command invocation
* Other MAP role decorators, COLLECTION_RESOURCE as other role
* *Add error/status report*, *Add security policy*, *Add compensation*
* *Other operation-level IRC transformations (Introduce Pagination, Split Operation etc.)
* *Introduce event management (operations)*, *Segregate commands from queries*
* Other data type-related transformations: 
    * Working with pagination, request bundles and extracting types
    * Turning atomic parameter list into parameter tree 
    * Introducing key-value-store
-->

## Chaining Transformations

You can apply a predefined combination of transformations and refactorings on the scenario and story level. It created an endpoint for it, applies some pattern-related refactorings (targeting the performance quality), and adds an HTTP binding so that an OpenAPI specification (and other contract formats) can be generated straight away.

This demonstrator of a transformation chain (many more can be thought of) is available under *Derive quality-assured, pattern-oriented endpoint type* (when having selected a scenario). Note that at present, this transformation is not configurable, it applies a greedy approach and tries to apply a number of transformations and refactorings. Take you time to inspect its output, and map it back to the atomic ones describes above!

## Generating Technology-Specific Contracts

Once the MDSL contract is reasonably complete, the [MDSL Generators](./tools.md) can be used to turn it into OpenAPI and several other formats. The OpenAPI generated from the above MDSL is [here](./media/FromStoryToOASForMDSLWebsite-Target.yaml); note that the end-point-to-resource and operation-to-verb bindings are required in this case. <!-- to satisfy REST constraint -->

*Question:* How long did the entire journey take you? Compare with code-first or contract first with plain OpenAPI (or other platform IDL) editing (i.e., no transformation and pattern support).

# Site Navigation

* [MDSL Home](./index) and [MDSL Primer](./primer)
* [Quick Reference](./quickreference)
* [Tutorial](./tutorial) 
* Language specification: service [endpoint contract types](./servicecontract) and [data contracts (schemas)](./datacontract), [bindings](./bindings), [instance-level constructs](./optionalparts),  [integration scenarios including stories](scenarios.md), [orchestration flows](flows.md).
* Tools [Overview](./tools) and [Command Line Interface (CLI)](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli), [update site for editor (Eclipse plugin)](./updates/)

*Copyright: Olaf Zimmermann, 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
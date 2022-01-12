---
title: Microservice Domain-Specific Language (MDSL) Primer
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2022. All rights reserved.
---

[Home](./index) &mdash; [Endpoint Types](./servicecontract) &mdash; [Bindings](./bindings) &mdash; [Tutorial](./tutorial) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools) &mdash; [Transformations](./soad.md)

## Primer: Getting Started with MDSL

MDSL supports the [API Description](https://microservice-api-patterns.org/patterns/foundation/APIDescription) pattern from [Microservice API Patterns (MAP)](https://ozimmer.ch/patterns/2020/05/07/MAPMetaPost.html). It picks up MAP concepts such as API *endpoint*, *operation*, *client* and *provider*, and features many API design patterns as decorators (a.k.a. stereotypes) such as ``<<pagination>>``. Its data contracts are inspired — but generalize from — message exchange formats such as JSON and the Jolie type system.

### Example (Service/API Edition)

The following example `CustomerRelationshipManager` extends concepts features in the MDSL ["Hello World"](./index) with compensation, reporting, and state transitions:

~~~
API description SampleCRMScenario
version "1.0"
usage context PUBLIC_API for FRONTEND_INTEGRATION 
overview "Example in MDSL Primer"

data type Customer {"name": D<string>, 
                    "address": D<string>, 
                    "birthday": D<string>}
data type StatusCode "success": MD<bool> default is "true"

endpoint type CustomerRelationshipManager serves as PROCESSING_RESOURCE
exposes 
  operation createCustomer with responsibility STATE_CREATION_OPERATION
    expecting payload "customerRecord": Customer
    delivering payload "customerId": D<int>
    compensated by deleteCustomer
  operation upgradeCustomer with responsibility STATE_TRANSITION_OPERATION
    expecting payload "promotionCode": P // request partially specified
    delivering payload P // response unspecified
  operation deleteCustomer with responsibility STATE_DELETION_OPERATION
    expecting payload "customerId": D<int>
    delivering payload "success": StatusCode
    transitions from "customerIsActive" to "customerIsArchived"
  operation validateCustomerRecord with responsibility COMPUTATION_FUNCTION
    expecting payload "customerRecord": Customer
    delivering payload "isCompleteAndSound": D<bool>
    reporting error ValidationResultsReport 
      "issues": {"code":D<int>, "message":D<string>}+
~~~

`createCustomer` is marked with the pattern [STATE_CREATION_OPERATION](https://microservice-api-patterns.org/patterns/responsibility/operationResponsibilities/StateCreationOperation); the reverse operation is specified (`compensated by deleteCustomer`).  `upgradeCustomer` also has a responsibility decorator, and its parameters are specified incompletely (`P`). `deleteCustomer` discloses the API-internal state transition that takes place (via the strings `"customerIsActive"` and `"customerIsArchived"`). Finally, `validateCustomerRecord` not only has a normal response payload, but also may return an error report.

Two data types `Customer` and `StatusCode` are made explicit, the others are inlined in the request and response message definitions. As a basic type, `StatusCode` has a default value defined.

### Design Goals

As a contract language for (micro-)service API design, MDSL should:

* Facilitate [*agile modeling* practices](http://agilemodeling.com/), for instance in API design workshops:
    * Value and promote readability over parsing efficiency (in language design)
    * Support partial specifications as first-class language concepts to be refined iteratively (see above example `{ID, D}`)
* Promote *platform independence*:
    * Feature [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/) as first-class language elements annotating/decorating endpoint types, operations, and representation elements 
    * Not be limited to HTTP (unlike Swagger and its successor OpenAPI Specification) or any other single protocol or message exchange format
* Support *meet-in-the-middle* service design:
    * *Top-down* from requirements (for instance, user stories for integration scenarios), as for instance proposed as an activity in the [Design Practice Repository (DPR)](https://github.com/socadk/design-practice-repository) 
    * *Bottom up* from existing systems (represented, for instance, as [DDD-style context maps](https://contextmapper.org/))

<!-- TODO: retrofit paper page (Appendix A from https://www.overleaf.com/project/5e384b88f46297000133080d)? -->

### Design Principles

To achieve the above design goals:

* The *abstract* syntax of MDSL is inspired and driven by the domain model and concepts of [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/primer), featuring endpoints, operations, and data representation elements. 
* The *concrete* syntax of service [endpoint contracts](./servicecontract) is elaborate; the concrete syntax of [data contracts](./datacontract) is compact yet simple; it abstracts from data exchange formats such as XML and JSON, as well as service-centric programming languages such as Ballerina and Jolie.[^1]

[^1]: The service and the data contract languages can be used independently of each other; for instance, data contracts for operations in contract types can also be specified in JSON Schema (but might not be supported by MDSL tools in that case).

### Where and When to Use MDSL 

Let us visualize the usage context of MDSL specifications with two [hexagons](https://herbertograca.com/2017/09/14/ports-adapters-architecture/), a notation and style that is quite popular in the microservices community:[^2] 

[^2]: Octogons, as used to denote cells in [Cell-Based Architectures (CBAs)](https://github.com/wso2/reference-architecture), look promising too!

<!-- [Solution Sketch of API Integration](MAP-HexagonionAODLegend.png) -->
<img src="MAP-HexagonionAODLegend.png" alt="Solution Sketch of API Integration" class="inline"/>

If you want to leverage and promote [microservices tenets](http://rdcu.be/mJPz) such as polyglot programming and use multiple integration protocols, e.g., an HTTP resource API and message queuing, then MDSL is for you. The request and response message representations in the diagram can be specified with MDSL data contracts; the provided interface supports an MDSL endpoint type.

### Integration with Microservice API Patterns (MAP)

MDSL supports all [Microservice API Patterns](https://microservice-api-patterns.org/) one way or another:

* The basic representation elements serve as MDSL grammar rules in the data contract part, e.g., [*ParameterTree*](https://microservice-api-patterns.org/patterns/structure/representationElements/ParameterTree) and [*AtomicParameter*](https://microservice-api-patterns.org/patterns/structure/representationElements/AtomicParameter).
* [Foundation patterns](https://microservice-api-patterns.org/patterns/foundation/) may appear as decorators/annotations for the entire API description, for instance, `PUBLIC_API`, `FRONTEND_INTEGRATION` (not shown in the above `HelloWorldAPI` example).
* Some [responsibility patterns](https://microservice-api-patterns.org/patterns/responsibility/) serve as decorators/annotations for endpoint roles and responsibilities, for example `PROCESSING_RESOURCE` and `MASTER_DATA_HOLDER` (enum/label).
* Other responsibility patterns are represented as decorators/annotations for operation responsibilities, for instance, `COMPUTATION_FUNCTION` and `EVENT_PROCESSOR`.
* Finally, the advanced structural representation patterns (for example, ``<<Pagination>>``) and many [quality patterns](https://microservice-api-patterns.org/patterns/quality/) appear as stereotypes annotating representation elements (for example, ``<<Embedded_Entity>>`` and ``<<Wish_List>>``).

The four types of decorators/annotations and stereotypes are optional; if present, they make the API description more expressive and can be processed by tools such as API linters/contract validators, code/configuration generators, MDSL to OpenAPI or WSDL converters (work in progress).

### Tool Support

At present, the following tools are available (see [tools page](./tools) for more information):

* An Eclipse-based [Editor and API Linter](./tools#eclipse-plugin), also offering [transformations](./soad) for rapid, goal-driven API design ("API first") 
* [Command-Line Interface (CLI)](./tools#command-line-interface-cli-tools) tools to validate a specification, to generate platform-specific contracts (OpenAPI, gRPC, Jolie) and reports
* *Work in progress:* [MDSL Web](https://mdsl-web.herokuapp.com/) Tools.

There is an MDSL generator in [Context Mapper](https://contextmapper.org/docs/mdsl/).

### MDSL Documentation (this Website)

* API usage [scenarios and stories](scenarios.md)
* Orchestration [flows](flows.md)
* Service [endpoint contract types](./servicecontract)
* [Data contracts (schemas)](./datacontract)
* General [binding](./bindings) information and [HTTP-/REST-specific binding details](./http-rest)
* Optional [language elements on the instance level (provider, client, gateway)](./optionalparts)
* [Tutorial](./tutorial) and [example](./examples)
* [Quick reference](./quickreference)
<!-- * [Tool information (CLI, editor/linter)](./tools) -->
* [Transformations and refactorings (in tools)](./soad)

### Publications

* Section 5.2 in *From OpenAPI Fragments to API Pattern Primitives and Design Smells*, Proc. of European Conference on Pattern Languages of Programs (EuroPLoP) 2021 by Souhaila Serbout, Cesare Pautasso, Uwe Zdun, and Olaf Zimmermann features MDSL ([PDF](http://design.inf.usi.ch/sites/default/files/biblio/apiace-europlop2021.pdf))
* Zimmermann, Olaf: *Dimensions of Successful Web API Design and Evolution: Context, Contracts, Components*, Keynote, 20th International Conference on Web Engineering (ICWE), June 11, 2020. ([PDF](https://ozimmer.ch/assets/presos/ZIO-ICWEKeynoteWADEC3v10p.pdf), [blog post](https://ozimmer.ch/practices/2020/06/10/ICWEKeynoteAndDemo.html))
* Kapferer, Stefan and Zimmermann, Olaf: *Domain-driven Service Design — Context Modeling, Model Refactoring and Contract Generation*. Proc. of SummerSoC 2020 conference, Springer CCIS Volume 1310 ([PDF](https://contextmapper.org/media/SummerSoC-2020_Domain-driven-Service-Design_Authors-Copy.pdf), [Presentation](https://contextmapper.org/media/Stefan-Kapferer_SummerSoC2020_presentation.pdf))

*Copyright: [Olaf Zimmermann](https://ozimmer.ch/index.html), 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->

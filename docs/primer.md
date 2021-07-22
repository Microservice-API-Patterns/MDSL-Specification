---
title: Microservice Domain-Specific Language (MDSL) Primer
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2021. All rights reserved.
---

[Home](./index) &mdash; [Endpoint Types](./servicecontract) &mdash; [Bindings](./bindings) &mdash; [Tutorial](./tutorial) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools) &mdash; [Transformations](./soad.md)

## Getting Started with MDSL

MDSL supports the [API Description](https://microservice-api-patterns.org/patterns/foundation/APIDescription) pattern from [Microservice API Patterns (MAP)](https://ozimmer.ch/patterns/2020/05/07/MAPMetaPost.html). It picks up MAP concepts such as API *endpoint*, *operation*, *client* and *provider*, and features many API design patterns as decorators (a.k.a. stereotypes) such as ``<<pagination>>``. Its data contracts are inspired — but generalize from — message exchange formats such as JSON and the Jolie type system.

### Hello World (Service API Edition)

The `Hello World` of MDSL and service design specifies a single endpoint `HelloWorldEndpoint` that exposes an operation `sayHello`:

<!-- TODO (M) feature CustomerExample here, with MAP decorators -->

~~~
API description HelloWorldAPI

data type SampleDTO {ID, D<string>}

endpoint type HelloWorldEndpoint
exposes 
  operation sayHello 
    expecting payload "in": D<string>  
    delivering payload SampleDTO

API provider HelloWorldAPIProvider
  offers HelloWorldEndpoint 
  at endpoint location "http://localhost:8000" 
  via protocol HTTP 
    binding resource HomeResource at "/"
      operation sayHello to POST 
  
API client HelloWorldAPIClient
  consumes HelloWorldEndpoint 
  from HelloWorldAPIProvider
  via protocol HTTP
~~~

`sayHello` accepts a single scalar string value `D<string>` as input. This operation returns a Data Transfer Object (DTO) called `SampleDTO` as output, which is modeled explicitly so that its specification can be reused. `SampleDTO` is specified incompletely as an identifier-data pair `{ID, D}`: the two elements in the pair are an identifier `ID` and some data (`D`). The names of these two "parameters" have not been specified yet (unlike `"in"`, the data send in the request message of `sayHello`). The [data element](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement) in the pair is a string; type of the `ID` parameter is yet unspecified. In addition to the endpoint type (a.k.a. service contract) `HelloWorldEndpoint`, an API client and an API provider working with this contract are defined (and [bound](./bindings) to HTTP, a single home resource in this simple case). 

Take a look at Hello World in [Swagger/OpenAPI Specification](https://swagger.io/blog/api-development/getting-started-with-swagger-i-what-is-swagger/) in comparison. You can find such contract specification example [here](./HelloWorldWebsitePrimer.yaml) (note: this OpenAPI specification contains a few more details about the HTTP [binding](./bindings) of the abstract contract). 

<!-- TODO (M) could also link to Protocol Buffers, GraphQL, WSDL/XML schema here; files are in here: HelloWorldWebsitePrimerGeneratedIDLs.zip -->


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

### Tool Support

At present, the following tools are available (see [tools page](./tools) for more information):

* An Eclipse-based [Editor and API Linter](./tools#eclipse-plugin), also offering [transformations](./soad) for rapid, goal-driven API design ("API first") 
* [Command-Line Interface (CLI)](./tools#command-line-interface-cli-tools) tools to validate a specification, to generate platform-specific contracts (OpenAPI, gRPC, Jolie) and reports
* MDSL generator in Context Mapper

## A Closer Look 

### Where and when to use MDSL 
Let us visualize the usage context of MDSL specifications with two [hexagons](https://herbertograca.com/2017/09/14/ports-adapters-architecture/), a notation and style that is quite popular in the microservices community:[^2] 

[^2]: Octogons, as used to denote cells in [Cell-Based Architectures (CBAs)](https://github.com/wso2/reference-architecture), look promising too!

<!-- [Solution Sketch of API Integration](MAP-HexagonionAODLegend.png) -->
<img src="MAP-HexagonionAODLegend.png" alt="Solution Sketch of API Integration" class="inline"/>

If you want to leverage and promote [microservices tenets](http://rdcu.be/mJPz) such as polyglot programming and use multiple integration protocols, e.g., an HTTP resource API and message queuing, then MDSL is for you. The request and response message representations in the diagram can be specified with MDSL data contracts; the provided interface supports an MDSL endpoint type.


### Language specification elements 

1. Service [endpoint contract types](./servicecontract) follow this template: 
   `endpoint type ... exposes operation ... expecting ... delivering ...`
2. [Data contracts (schemas)](./datacontract): `data type SampleDTO {ID, V}` in the above example
3. Optional [instance-level concepts](./optionalparts): 
    * API Provider with protocol [bindings](./bindings) and, optionally, [*Service Level Agreements*](https://microservice-api-patterns.org/patterns/quality/qualityManagementAndGovernance/ServiceLevelAgreement) and evolution strategies such as [*Two in Production*](https://microservice-api-patterns.org/patterns/evolution/TwoInProduction)
    * API Client instances consuming the contracts exposed by providers
    * API Gateways (or intermediaries) acting both in client and in provider role; note that gateways are not featured in the simple example above
4. Integration scenarios including stories
    * See [this page](scenarios.md).
5. Orchestration flows
    * See [this page](flows.md).

See these concepts in action in the [tutorial](./tutorial), the [quick reference](./quickreference) and on the [example](./examples) page.


### Usage of and support for Microservice API Patterns (MAP)

MDSL supports all [Microservice API Patterns](https://microservice-api-patterns.org/) one way or another:

* The basic representation elements serve as MDSL grammar rules in the data contract part, e.g., [*ParameterTree*](https://microservice-api-patterns.org/patterns/structure/representationElements/ParameterTree) and [*AtomicParameter*](https://microservice-api-patterns.org/patterns/structure/representationElements/AtomicParameter).
* [Foundation patterns](https://microservice-api-patterns.org/patterns/foundation/) may appear as decorators/annotations for the entire API description, for instance, `PUBLIC_API`, `FRONTEND_INTEGRATION` (not shown in the above `HelloWorldAPI` example).
* Some [responsibility patterns](https://microservice-api-patterns.org/patterns/responsibility/) serve as decorators/annotations for endpoint roles and responsibilities, for example `PROCESSING_RESOURCE` and `MASTER_DATA_HOLDER` (enum/label).
* Other responsibility patterns are represented as decorators/annotations for operation responsibilities, for instance, `COMPUTATION_FUNCTION` and `EVENT_PROCESSOR`.
* Finally, the advanced structural representation patterns (for example, ``<<Pagination>>``) and many [quality patterns](https://microservice-api-patterns.org/patterns/quality/) appear as stereotypes annotating representation elements (for example, ``<<Embedded_Entity>>`` and ``<<Wish_List>>``).

The four types of decorators/annotations and stereotypes are optional; if present, they make the API description more expressive and can be processed by tools such as API linters/contract validators, code/configuration generators, MDSL to OpenAPI or WSDL converters (work in progress).


## More Information

### MDSL Git Pages (this website)

* API usage [scenarios and stories](scenarios.md) (experimental)
* Orchestration [flows](flows.md) (experimental)
* Service [endpoint contract types](./servicecontract)
* [Data contracts (schemas)](./datacontract)
* [Bindings](./bindings)
* Optional [language elements on the instance level (provider, client, gateway)](./optionalparts)
* [Tutorial](./tutorial), another [example](./examples)
* [Quick reference](./quickreference)
* [Tool information (CLI, editor/linter)](./tools)
* [SOAD transformations](./soad)

*Copyright: [Olaf Zimmermann](https://ozimmer.ch/index.html), 2018-2021. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->

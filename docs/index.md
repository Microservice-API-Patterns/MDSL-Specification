---
title: Microservice Domain-Specific Language (MDSL) Homepage
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2020. All rights reserved.
---


## TL;DR
Microservice Domain-Specific Language (MDSL) abstracts from technology-specific interface description languages such as Swagger, WSDL and <!-- gRPC --> Protocol Buffers. Its current version is 4.0.

Quick links: 

* Documentation pages (GitHub pages): [Tutorial](./tutorial), [quick reference](./quickreference)<!-- providing skeletons-->, [example](./examples) 
* Language and tools repository (GitHub): [Grammar](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext), [examples](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples)
* Tools: [Overview](./tools), [CLI](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli), [update site for editor (Eclipse plugin)](./updates/)<!-- Update site: [https://microservice-api-patterns.github.io/updates/](https://microservice-api-patterns.github.io/updates/) -->

<!--
### Installation in Eclipse

 * Update site: [https://microservice-api-patterns.github.io/updates/](https://microservice-api-patterns.github.io/updates/)
 * The grammar can be found in the `dsl-core` project (more precisely, in the `io.mdsl./src/io.mdsl` folder of this project): [public](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) and [private](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) repository.
-->

## Getting Started with MDSL

MDSL supports the [API Description](https://microservice-api-patterns.org/patterns/foundation/APIDescription) pattern from [Microservice API Patterns (MAP)](https://ozimmer.ch/patterns/2020/05/07/MAPMetaPost.html). It picks up MAP concepts such as API endpoint, operation, client and provider, and features patterns as decorators (a.k.a. stereotypes): ``<<pagination>>``. 

### A First Example

The `Hello World` of MDSL models an API with a single endpoint `HelloWorldEndpoint` that exposes a single operation called `sayHello`:

~~~
API description HelloWorldAPI

data type SampleDTO {ID, D} 

endpoint type HelloWorldEndpoint
exposes 
  operation sayHello 
    expecting payload D<string>  
    delivering payload SampleDTO

API provider HelloWorldAPIProvider
  offers HelloWorldEndpoint

API client HelloWorldAPIClient
  consumes HelloWorldEndpoint
~~~

`sayHello` accepts a single scalar string value `D<string>` as input. This operation returns a Data Transfer Object (DTO) called `SampleDTO` as output, which is modeled explicitly so that its specification can be reused. `SampleDTO` is specified incompletely as an identifier-data pair `{ID, D}`: the names of the two "parameters" and the type of the data value `D` have not been specified yet. In addition to the endpoint type (a.k.a. service contract) `HelloWorldEndpoint`, an API client and an API provider working with this contract are defined (on an abstract level). 

Take a look at Hello World in [Swagger/Open API Specification](https://swagger.io/blog/api-development/getting-started-with-swagger-i-what-is-swagger/) in comparison. You can find such contract specification example [here](./HelloWorld.swagger.json) (admittedly, this Open API specification contains some more details about te HTTP binding of the abstract contract). 
<!-- could also show WSDL/XML schema here -->


### Design Goals

A contract language for (micro-)service API design should:

* Support [*agile modeling* practices](http://agilemodeling.com/), for instance in API design workshops:
    * Value and promote readability over parsing efficiency (in language design)
    * Support partial specifications as first-class language concepts to be refined iteratively (see above example `{ID, D}`)
* Promote *platform independence*:
    * [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/) as first-class language elements annotating/decorating endpoint types, operations, and representation elements 
    * Not bound to HTTP (unlike Swagger and its successor Open API Specification) or other protocols and message exchange formats
* Support *meet-in-the-middle* service design:
    * *Top-down* from requirements (for instance, user stories for integration scenarios), as for instance proposed as activity in the [Design Practice Repository (DPR)](https://github.com/socadk/design-practice-repository) 
    * *Bottom up* from existing systems (represented, for instance, as [DDD-style context maps](https://contextmapper.org/))

<!-- TODO: retrofit paper page (Appendix A from https://www.overleaf.com/project/5e384b88f46297000133080d)? -->


### Design Principles

To achieve the above design goals, we decided that:

* The *abstract* syntax of MDSL is inspired and driven by the domain model and concepts of [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/primer), featuring endpoints, operations, and data representation elements. 
* The *concrete* syntax of service [endpoint contracts](./servicecontract) is elaborate; the concrete syntax of [data contracts](./datacontract) is compact yet simple; it abstracts from data exchange formats such as XML and JSON, as well as service-centric programming languages such as Ballerina and Jolie.[^1]

[^1]: The service and the data contract languages can be used independently of each other; for instance, data contracts for operations in contract types can also be specified in JSON Schema (but might not be supported by MDSL tools in that case).


### MDSL Tools

At present, the following tools are available (see [tools page](./tools) for more information):

* An Eclipse-based [*Eclipse editor and API Linter*](./tools#eclipse-plugin) 
* [*Command-Line Interface (CLI)*](./tools#command-line-interface-cli-tools) tools to validate a specification, to generate platform-specific contracts (OpenAPI, gRPC, Jolie) and reports


## A Closer Look 

### Where and when to use MDSL 
Let us visualize the usage context of MDSL specifications with two [hexagons](https://herbertograca.com/2017/09/14/ports-adapters-architecture/), as suggested by the microservices community:[^2] 

[^2]: Octogons, as used to denote cells in [Cell-Based Architectures (CBAs)](https://github.com/wso2/reference-architecture), look cool too ;-)

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

See these concepts in action in the [tutorial](./tutorial), the [quick reference](./quickreference) and on the [example](./examples) page.


### Usage of and support for Microservice API Patterns (MAP)

MDSL supports all [Microservice API Patterns](https://microservice-api-patterns.org/) one way or another:

* The basic representation elements serve as MDSL grammar rules in the data contract part, e.g., [*ParameterTree*](https://microservice-api-patterns.org/patterns/structure/representationElements/ParameterTree) and [*AtomicParameter*](https://microservice-api-patterns.org/patterns/structure/representationElements/AtomicParameter).
* [Foundation patterns](https://microservice-api-patterns.org/patterns/foundation/) may appear as decorators/annotations for the entire API description, for instance, `PUBLIC_API`, `FRONTEND_INTEGRATION` (not shown in the above `HelloWorldAPI` example).
* Some [responsibility patterns](https://microservice-api-patterns.org/patterns/responsibility/) serve as decorators/annotations for endpoint roles and responsibilities, for example `PROCESSING_RESOURCE` and `MASTER_DATA_HOLDER` (enum/label).
* Other responsibility patterns are represented as decorators/annotations for operation responsibilities, for instance, `COMPUTATION_FUNCTION` and `EVENT_PROCESSOR`.
* Finally, the advanced structural representation patterns (for example, ``<<Pagination>>``) and many [quality patterns](https://microservice-api-patterns.org/patterns/quality/) appear as stereotypes annotating representation elements (for example, ``<<Embedded_Entity>>`` and ``<<Wish_List>>``).

The four types of decorators/annotations and stereotypes are optional; if present, they make the API description more expressive and can be processed by tools such as API linters/contract validators, code/configuration generators, MDSL to Open API or WSDL converters (work in progress).

<!--
### Tools
 At present, the DSL editor generated from the grammar comes as an Eclipse plugin.

* [Command-Line Interface (CLI)](./tools#command-line-interface-cli-tools)
* [Eclipse editor and API Linter](./tools#eclipse-plugin)
-->

## More Information

### MDSL Git Pages (this website)

* Service [endpoint contract types](./servicecontract)
* [Data contracts (schemas)](./datacontract)
* [Bindings](./bindings)
* Optional [language elements on the instance level (provider, client, gateway)](./optionalparts)
* [Quick reference](./quickreference)
* [Tutorial](./tutorial), another [example](./examples)
* [Tool information (CLI, editor/linter)](./tools)

### External links 

* Public [Microservice API Patterns (MAP) website](https://microservice-api-patterns.org/), access to team-internal preview website available upon request (features more patterns than the public one, in intermediate draft form)
* [Lakeside Mutual](https://github.com/Microservice-API-Patterns/LakesideMutual) repository, featuring [Domain-Driven Design (DDD)](https://www.ifs.hsr.ch/index.php?id=15666&L=4) and [microservices](https://www.ifs.hsr.ch/index.php?id=15266&L=4) in an insurance company scenario (JavaScript frontends and Spring Boot backends)
* [Context Mapper](https://contextmapper.org/), a DSL for strategic DDD and rapid OOAD

<!-- TODO point to presentations (JUG, VSS, Modelsward, GI-AK ICWE, point at VSS report https://www.computer.org/csdl/magazine/so/2020/01/08938118/1fUSO0QBDnW -->

*Copyright: [Olaf Zimmermann](https://ozimmer.ch/index.html), 2018-2020. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->

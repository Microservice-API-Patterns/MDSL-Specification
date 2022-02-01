---
title: Microservice Domain-Specific Language (MDSL) Homepage
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2022. All rights reserved.
---

[Primer](./primer) &mdash; [Endpoint Types](./servicecontract) &mdash; [API Provider and Client](./optionalparts) &mdash; [Tutorial](./tutorial) &mdash; [Tools](./tools)  &mdash;[Transformations](./soad.md)

## Overview
Microservice Domain-Specific Language (MDSL) abstracts from technology-specific interface description languages such as OpenAPI/Swagger, WSDL, and <!-- gRPC --> Protocol Buffers. 

<!--
> If the URI of this page is `https://socadk.github.io/MDSL/index`, you are looking at the GitHub pages of the next version (technology preview)! Please refer to [https://microservice-api-patterns.github.io/MDSL-Specification/index](https://microservice-api-patterns.github.io/MDSL-Specification/index) for the latest public open source version.
-->

### A First Example 

An API with a single endpoint `HelloWorldEndpoint` that exposes a single operation called `sayHello` can be modelled as this: <!-- The `Hello World` of MDSL and service design specifies a single endpoint `HelloWorldEndpoint` that exposes an operation `sayHello`: -->

~~~
API description HelloMDSLWorld

data type SampleDTO {ID, D<string>} 

endpoint type HelloWorldEndpoint
exposes 
  operation sayHello 
    expecting payload "in": D<int>  
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

`sayHello` accepts a single scalar string value `D<string>` as input. This operation returns a Data Transfer Object (DTO) called `SampleDTO` as output, which is modeled explicitly so that its specification can be reused. This `SampleDTO` is specified incompletely as an identifier-data pair `{ID, D}`. Its two elements are an identifier `ID` and some data `D`. The names of these two "parameters" have not been specified yet. The [data element](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement) `D` is a string; the type of the `ID` parameter is not unspecified. This is different from `"in": D<int>`,  the data send in the request message of `sayHello`, which gives its single parameter a name, a role, and a type. 

In addition to the endpoint type (a.k.a. service contract) `HelloWorldEndpoint`, an API client and an API provider working with this contract are defined (and [bound](./bindings) to HTTP, a single home resource in this simple case). 

Take a look at Hello World in [Swagger/OpenAPI Specification](https://swagger.io/blog/api-development/getting-started-with-swagger-i-what-is-swagger/) in comparison. You can find such contract specification example [here](./HelloWorldWebsitePrimer.yaml) (note: this OpenAPI specification contains a few more details about the HTTP [binding](./bindings) of the abstract contract). 

<!-- TODO (H) could also link to Protocol Buffers, GraphQL, WSDL/XML schema here; files are in here: HelloWorldWebsitePrimerGeneratedIDLs.zip -->

An advanced example showing more contract feature appears in the [MDSL Primer](./primer).

### Quickstarts

* Extended overview: [MDSL Primer](./primer)
* Language [Tutorial](./tutorial) and [Quick Reference](./quickreference) <!-- providing skeletons -->
* Design [Transformations and Refactorings](./soad.md)

### Language Specification Elements 

1. Service [endpoint contract types](./servicecontract) follow this template: 
  * `endpoint type ... exposes operation ... expecting ... delivering ...`
2. [Data contracts (schemas)](./datacontract)
  *  `data type SampleDTO {ID, V}` in the above example
3. Optional [instance-level concepts](./optionalparts):
  * API Provider with protocol [bindings](./bindings) and, optionally, [*Service Level Agreements*](https://microservice-api-patterns.org/patterns/quality/qualityManagementAndGovernance/ServiceLevelAgreement) and evolution strategies such as [*Two in Production*](https://microservice-api-patterns.org/patterns/evolution/TwoInProduction)
  * API Client instances consuming the contracts exposed by providers
  * API Gateways (or intermediaries) acting both in client and in provider role; note that gateways are not featured in the simple example above
4. Integration [scenarios](scenarios.md) including user/job/test stories 
5. Orchestration [flows](flows.md).

<!-- See these concepts in action in the [tutorial](./tutorial), the [quick reference](./quickreference), the [SOAD transformations](./soad) page, and on the [example](./examples) page. -->

## Next Steps

### MDSL Tools and Their Application

The following tools already support MDSL:

* An Eclipse plugin for MDSL, available from this [update site](./updates/), offers syntax checking highlighting, semantic validation, code completion, transformations, and generators.
* A [Command-Line Interface (CLI)](./tools#command-line-interface-cli-tools) makes contract validation and generators available outside any IDE. <!-- https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli --> *Work in progress:* [MDSL Web](https://mdsl-web.herokuapp.com/) Tools.
* [Context Mapper](https://contextmapper.org/docs/mdsl/) can generate MDSL.

See MDSL [Tools](./tools) page for more information. Two blog posts, ["Story-Driven Service Design: From Feature Story to Minimum Viable API Product"](https://ozimmer.ch/practices/2022/01/20/StoryDrivenServiceDesignDemo.html)
and ["Event-Driven Service Design: Five Steps from Whiteboard to OpenAPI and Camel Flow"](https://ozimmer.ch/practices/2022/01/13/EventDrivenServiceDesignDemo.html) feature MDSL these tools in action.

### MDSL Documentation (this website)

* Language reference: 
  * [Endpoint Type](./servicecontract), [Data Types](./datacontract)
  * [API Provider and Client](./optionalparts), [Technology Bindings](./bindings) with [HTTP/REST specifics](./http-rest) 
  * [Scenarios and stories](./scenarios), [Orchestration flows](./flows), (experimental) [AsyncMDSL](./async-mdsl) 
* Service-oriented [quick fixes and model transformations](./soad.md) supporting "API First" 
* IDL/code generators: 
  * [OpenAPI generator](./generators/open-api), [Protocol buffers generator](./generators/protocol-buffers), [Graphql generator](./generators/graphql), [Jolie generator](./generators/jolie) 
  * [Java "Modulith" generator](./generators/java), [Arbitrary textual generation with Freemarker](./generators/freemarker)
* Language and tools repository (GitHub): [XText grammar](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext), [examples](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples) folder


### External links 

* [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/) and [Interface Refactoring Catalog](https://interface-refactoring.github.io/)
* An [end-to-end service design demo](https://medium.com/olzzio/domain-driven-service-design-with-context-mapper-and-mdsl-d5a0fc6091c2) is available on Medium also featuring [Context Mapper](https://contextmapper.org/), a DSL for strategic DDD and rapid OOAD (Context Mapper can generate MDSL). The demo takes you from a user story to a walking microservice skeleton (Spring Boot, Hipster, Heroku) in seven steps (partially automated in model transformations).
* A [microservices and DevOps conference report](https://www.computer.org/csdl/magazine/so/2020/01/08938118/1fUSO0QBDnW) mentioning Context Mapper and MDSL.
* [Lakeside Mutual](https://github.com/Microservice-API-Patterns/LakesideMutual) repository, featuring [Domain-Driven Design (DDD)](https://www.ifs.hsr.ch/index.php?id=15666&L=4) and [microservices](https://www.ifs.hsr.ch/index.php?id=15266&L=4) in an insurance company scenario (JavaScript frontends and Spring Boot backends)
* Olaf Zimmermann's Blog ["The Concerned Architect"](https://ozimmer.ch/blog/)

*Copyright: [Olaf Zimmermann](https://ozimmer.ch/index.html), 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->

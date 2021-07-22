---
title: Microservice Domain-Specific Language (MDSL) Homepage
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2021. All rights reserved.
---

[Primer](./primer) &mdash; [Endpoint Types](./servicecontract) &mdash; [API Provider and Client](./optionalparts) &mdash; [Tutorial](./tutorial) &mdash; [Tools](./tools)  &mdash;[Transformations](./soad.md)

## TL;DR
Microservice Domain-Specific Language (MDSL) abstracts from technology-specific interface description languages such as OpenAPI/Swagger, WSDL, and <!-- gRPC --> Protocol Buffers. 

> If the URI of this page is `https://socadk.github.io/MDSL/index`, you are looking at the GitHub pages of the next version (technology preview)! Please refer to [https://microservice-api-patterns.github.io/MDSL-Specification/index](https://microservice-api-patterns.github.io/MDSL-Specification/index) for the latest public open source version.

### A First Example 

An API with a single endpoint `HelloWorldEndpoint` that exposes a single operation called `sayHello` can be modelled as this:

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

These language elements and their motivation are explained in the [MDSL Primer](./primer).

## MDSL Tools

The following tools already support MDSL:

* Our Eclipse plugin for MDSL, available from this [update site](./updates/), offers syntax checking highlighting, semantic validation, code completion, transformations, and generators.
* A [Command-Line Interface (CLI)](./tools#command-line-interface-cli-tools) makes contract validation and generators available outside any IDE. <!-- https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli -->
* [Context Mapper](https://contextmapper.org/docs/mdsl/) can generate MDSL.
* *Work in progress:* MDSL Web Application

See MDSL [Tools](./tools) page for more information.

<!--
### Installation in Eclipse

 * Update site: [https://microservice-api-patterns.github.io/MDSL-Specification/updates/](https://microservice-api-patterns.github.io/MDSL-Specification/updates/)
 * The grammar can be found in the `dsl-core` project (more precisely, in the `io.mdsl./src/io.mdsl` folder of this project): [public](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) and [private](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) repository.
-->

## More Information

### MDSL Web Pages (this site)

* Language reference and "getting started" resources:
    * [Primer](./primer), [Tutorial](./tutorial), [Quick Reference](./quickreference) <!-- providing skeletons --> 
    * [Endpoint Type](./servicecontract), [Data Types](./datacontract), [Provider and Client](./optionalparts), [Technology Bindings](./bindings)
    * Technology previews [scenarios and stories](./scenarios), [orchestration flows](./flows), [AsyncMDSL](./async-mdsl), 
* Tools: [Overview](./tools), [CLI](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli), [update site for editor (Eclipse plugin)](./updates/)
    * Service-oriented [quick fixes and model transformations](./soad.md) supporting "API First"
    * Generators: [OpenAPI generator](./generators/open-api), [Protocol buffers generator](./generators/protocol-buffers), [Graphql generator](./generators/graphql), [Jolie generator](./generators/jolie), [Java "Modulith" generator](./generators/java), [Arbitrary textual generation with Freemarker](./generators/freemarker)
* Language and tools repository (GitHub): [XText grammar](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext), [examples](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples) folder


### External links 

* [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/) and [Interface Refactoring Catalog](https://interface-refactoring.github.io/)
* An [end-to-end service design demo](https://medium.com/olzzio/domain-driven-service-design-with-context-mapper-and-mdsl-d5a0fc6091c2) is available on Medium also featuring [Context Mapper](https://contextmapper.org/), a DSL for strategic DDD and rapid OOAD (Context Mapper can generate MDSL). The demo takes you from a user story to a walking microservice skeleton (Spring Boot, Hipster, Heroku) in seven steps (partially automated in model transformations).
* A [microservices and DevOps conference report](https://www.computer.org/csdl/magazine/so/2020/01/08938118/1fUSO0QBDnW) mentioning Context Mapper and MDSL.
* [Lakeside Mutual](https://github.com/Microservice-API-Patterns/LakesideMutual) repository, featuring [Domain-Driven Design (DDD)](https://www.ifs.hsr.ch/index.php?id=15666&L=4) and [microservices](https://www.ifs.hsr.ch/index.php?id=15266&L=4) in an insurance company scenario (JavaScript frontends and Spring Boot backends)
* Olaf Zimmermann's Blog ["The Concerned Architect"](https://ozimmer.ch/blog/)

*Copyright: [Olaf Zimmermann](https://ozimmer.ch/index.html), 2018-2021. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->

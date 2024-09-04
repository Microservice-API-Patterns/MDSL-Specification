Microservice Domain-Specific Language (MDSL)
============================================

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

<!--
[![Build Status](https://travis-ci.com/Microservice-API-Patterns/MDSL-Specification.svg?branch=master)](https://travis-ci.com/Microservice-API-Patterns/MDSL-Specification)
-->

Author: Olaf Zimmermann, (c) 2018-2024. All rights reserved.

## What is MDSL?

MDSL supports the [API Description](https://microservice-api-patterns.org/patterns/foundation/APIDescription) pattern from [Microservice API Patterns (MAP)](https://ozimmer.ch/patterns/2020/05/07/MAPMetaPost.html) ("polyglot service design"). 

This is the "hello world" of cross-platform service contracting, specified in MDSL: 

~~~
API description HelloWorldAPI

data type SampleDTO {ID, "someData": D<string>} 

endpoint type HelloWorldEndpoint
exposes 
  operation sayHello 
    expecting payload D<string>  
    delivering payload SampleDTO

API provider HelloWorldAPIProvider
  offers HelloWorldEndpoint at endpoint location "http://localhost:8080"
  via protocol HTTP binding resource Home at "/hello"
~~~

As the example shows, the MDSL grammar defines two related specification languages:

1. An *API description language*: API `endpoints type`s (a.k.a. service contracts types) can be defined, including their operations; API client and providers of instances of these endpoint types can be specified elaborately, including Service Level Agreements (SLAs).
2. A *data contract language* providing a type system for DTRs in request and response messages (which is very compact): `data type SampleDTO {ID, "someData": D<string>}`. This type definition pairs an ID (without a name) with some string data.

These two languages can be used independently of each other; for instance, data contracts for operations in contract types can also be specified in JSON Schema (or XML Schema). Specifications do not have to be complete to be useful (e.g., in early stages of service design); tools will be expected to check that, use defaults, etc. 


## Getting Started

* The public [GitHub Pages for MDSL](https://microservice-api-patterns.github.io/MDSL-Specification/) provide a tutorial and language reference information.
* There is an [Eclipse update site](https://microservice-api-patterns.github.io/MDSL-Specification/updates/) for the MDSL editor. 
* All generators are available via a Command Line Interface (CLI); see [this readme](./dsl-core/io.mdsl.cli/README.md) and [these examples](./examples/mdsl-standalone-example).
* Presentations featuring Context Mapper, MAP and MDSL can be found [here](https://ozimmer.ch/papers/), and an [end-to-end demo](https://medium.com/olzzio/domain-driven-service-design-with-context-mapper-and-mdsl-d5a0fc6091c2) is available on Medium.
* As a contributor, please consult the [readme file of the dsl-core](./dsl-core/README.md) project for getting started information and prerequisites.


## Language Specification 

The [Primer](https://microservice-api-patterns.github.io/MDSL-Specification/primer) is a good starting point. Detailed are explained on reference pages:

* [Data types](https://microservice-api-patterns.github.io/MDSL-Specification/datacontract) (a.k.a. published language)
* [Endpoint types](https://microservice-api-patterns.github.io/MDSL-Specification/servicecontract) (a.k.a. ports)
* [Protocol bindings](https://microservice-api-patterns.github.io/MDSL-Specification/bindings) (a.k.a. adapters)
* [Instance-level concepts](https://microservice-api-patterns.github.io/MDSL-Specification/optionalparts) (provider, client, gateway)
* Integration [scenarios and stories](https://microservice-api-patterns.github.io/MDSL-Specification/scenarios.html)
* Orchestration [flows](https://microservice-api-patterns.github.io/MDSL-Specification/flows.html)
* A language extension supporting queue-based messaging endpoints is [AsyncMDSL](https://microservice-api-patterns.github.io/MDSL-Specification/async-mdsl).

A [Quick Reference](https://microservice-api-patterns.github.io/MDSL-Specification/quickreference) providing reusable snippets is available as well.


## Repository Structure 

This repository contains:

* [dsl-core](dsl-core), the DSL project, Xtext grammar and everything else needed to build an Eclipse plugin providing a MDSL editor.
* Various [examples](examples).
* The [sources of the GitHub pages](docs) for MDSL.
* [Background information](background): papers and presentations as well as information on relation to other IDLs and on related projects.

If you want to contribute to MDSL, you have to clone this repo and generate the required Xtext files.

*Note:* The project has to be imported as an existing Maven project, and an adjustment of the IDE setup is required. The [readme](dsl-core/README.md) of the main project contains detailed instructions. This setup and build process eases integration with [Context Mapper](https://contextmapper.org/).

## Latest Features and Status

*Important note:* All MDSL tools, including validators, quick fix transformations, Freemarker templates, IDL and Java generators, etc. are the output of research projects with limited budgets; at present, resources to continue development and reduce technical debt are sparse. The MDSL tools should be viewed and positioned as **technology demonstrators** that show how production-ready API-first design tools *could* look like.   

The core MDSL language (data types, endpoint types, protocol bindings) is stable now; a language reference and primer can be found in ["Patterns for API Design"](https://api-patterns.org/book/). Features auch as user stories and flow modeling remain [experimental previews](https://api-patterns.org/patterns/evolution/ExperimentalPreview.html). The [Interface Refactoring Catalog (IRC)](https://interface-refactoring.github.io/) and publications about it feature MDSL snippets.

MDSL Version 6 maintains the Version 5 feature set and is designed to work with the 2024 versions of Eclipse. The standalone Commain Line (CLI) interface and MDSL-Web continue to be available (no changes). 

Version 5 of the MDSL language extended service contracts with support for events, states, compensation as well as integration scenarios/stories and event-command flows. These concepts are featured in a number of model transformations that support rapid "API First" development:

* <https://ozimmer.ch/practices/2022/01/20/StoryDrivenServiceDesignDemo.html>
* <https://ozimmer.ch/practices/2022/01/13/EventDrivenServiceDesignDemo.html>
* <https://ozimmer.ch//practices/2022/02/01/ProcessOrientedServiceDesignDemo.html>
* <https://medium.com/olzzio/domain-driven-service-design-with-context-mapper-and-mdsl-d5a0fc6091c2>

Since Version 5.2, MDSL supports true REST level 3 concepts both on the abstract endpoint type level and in the redesigned bindings and comes with additional Freemarker generators (Markdown reports, ALPS). Many API refactorings from IRC and other API-first [transformations](https://microservice-api-patterns.github.io/MDSL-Specification/soad.html) are supported.

The [Change Log](changelog.md) provides an evolution history; the GitHub [Release Notes](https://github.com/Microservice-API-Patterns/MDSL-Specification/releases) also contain update information.


## Context Information: MAP and Xtext

All [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/) are supported and integrated one way or another:

* As grammar rules
* As enums for roles and responsibilities
* As stereotypes annotating representation elements

See ["MAP Decorators" section of the MDSL tutorial](https://microservice-api-patterns.github.io/MDSL-Specification/tutorial) for more information. 

The MDSL grammar, located in the `src` folder of the `dsl-core/io.mdsl` project, was originally developed with Eclipse Photon (4.8.0) and Xtext (2.14) as provided by the Eclipse Modeling Platform. MDSL makes use of the referencing feature in Xtext ('name' attribute).

Feedback and contributions welcome!

[ZIO (a.k.a. socadk)](https://ozimmer.ch/index.html)


##  Acknowledgements 

Contributors (input, DevOps support, feedback): 

* Giacomo De Liberali (AsyncMDSL language, AsyncAPI generator)
* Stefan Kapferer (also the author of the [MDSL generator in Context Mapper](https://contextmapper.org/docs/mdsl/))
* MAP co-authors: Mirko Stocker, Daniel Lübke, Cesare Pautasso, Uwe Zdun
* Bachelor/master students at HSR/OST  
* Microservices 2019 and [VSS 2019](https://www.computer.org/csdl/magazine/so/2020/01/08938118/1fUSO0QBDnW) conference participants 
* Early adopters and reviewers

The creation and release of MDSL 4 in 2020 was supported by the [Hasler Foundation](https://haslerstiftung.ch/en/welcome-to-the-hasler-foundation/).


## Getting involved 

We are happy to welcome new contributors who want to help improve MDSL language and tools:

* Feel free to create issues in GitHub.
* Submit pull requests. If you do so, we assume that you comply with this [Developer Certificate of Origin](https://developercertificate.org/).
* Contact us to discuss collaboration and integration opportunities.

<!-- 
Please review our contribution rules/code of conduct upfront. Thank you! 
https://github.com/TODO/blob/master/CONTRIBUTING.md
-->

-- [Olaf Zimmermann (ZIO)](https://ozimmer.ch)

*Copyright: The author, 2019-2022. All rights reserved. See [license information](/LICENSE).*

Microservice Domain-Specific Language (MDSL) 
============================================

## What is MDSL?

MDSL supports the [API Description](https://microservice-api-patterns.org/patterns/foundation/APIDescription) pattern from [Microservice API Patterns (MAP)](https://ozimmer.ch/patterns/2020/05/07/MAPMetaPost.html). 

This is the "hello world" of service contracting, specified in MDSL: 

~~~
API description HelloWorldAPI

data type SampleDTO {ID, D} 

endpoint type HelloWorldEndpoint
exposes 
  operation sayHello 
    expecting payload D<string>  
    delivering payload SampleDTO

API provider HelloWorldAPIProvider1
  offers HelloWorldEndpoint

API client HelloWorldAPIClient1
  consumes HelloWorldEndpoint
~~~

As the example shows, the MDSL grammar defines two related specification languages:

1. An *API description language*: API `endpoints type`s (a.k.a. service contracts types) can be defined, including their operations; API client and providers of instances of these endpoint types can be specified elaborately, including Service Level Agreements (SLAs).
2. A *data contract language* providing a type system for DTRs in request and response messages (which is very compact): `data type SampleDTO {ID, D}`.

These two languages can be used independently of each other; for instance, data contracts for operations in contract types can also be specified in JSON Schema (or XML Schema). Specifications do not have to be complete to be useful (e.g., in early stages of service design); tools will be expected to check that, use defaults, etc. 


## Getting Started

* Presentations featuring Context Mapper, MAP and MDSL can be found [here](https://ozimmer.ch/papers/).
* The [GitHub Pages for MDSL](https://microservice-api-patterns.github.io/MDSL-Specification/) provide a tutorial and language reference information.
* There is an Eclipse update site [https://microservice-api-patterns.github.io/MDSL-Specification/updates/](https://microservice-api-patterns.github.io/MDSL-Specification/updates/) for the MDSL editor. 
* As a contributor, please consult the [readme file of the dsl-core](./dsl-core/README.md) project for getting started information and prerequisites.


## Change Log

The current version of MDSL is 3.1.0. See release notes for more information.


## Repository Structure 

This repository contains:

* [dsl-core](dsl-core), the DSL project, Xtext grammar and everything else needed to build an Eclipse plugin providing a MDSL editor.
<!-- * An antlr4 version of the grammar in [this folder](antlr4). -->
* Various [examples](examples).
* The [sources of the GitHub pages](docs) for MDSL.
<!-- * Some [background information](background) on other IDLs and related projects. -->

If you want to contribute to MDSL, you have to clone this repo and generate the required Xtext files.

*Note:* Setup and build process have been improved recently to ease integration with [Context Mapper](https://contextmapper.org/). As a consequence, the project has to be imported as an existing Maven project, and an adjustment of the IDE setup is required. The [readme](dsl-core/README.md) of the main project contains detailed instructions.


## Context Information: MAP and Xtext

All [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/) are supported and integrated one way or another:

* As grammar rules
* As enums for roles and responsibilities
* As stereotypes annotating representation elements

See ["MAP Decorators" section of the MDSL tutorial](https://microservice-api-patterns.github.io/MDSL-Specification/tutorial) for more information. <!-- TODO copy one-pager in SummerSoC paper to GitHub pages or elsewhere in repo -->

The [MDSL grammar](dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) was originally developed with Eclipse Photon (4.8.0) and Xtext (2.14) as provided by the Eclipse Modeling Platform. MDSL makes use of the referencing feature in Xtext ('name' attribute). Future work is required to also support  this technology in other IDEs (such as Visual Studio Code).

<!-- A derived (but not yet fully equivalent) version of the grammar for antlr4 can be found [in this folder](https://microservice-api-patterns.github.io/MDSL-Specification/blob/master/antlr4/). -->


##  Acknowledgements 

The creation and open source release of MDSL 3.0 was supported by the [Hasler Foundation](https://haslerstiftung.ch/en/welcome-to-the-hasler-foundation/).

Contributors (input, DevOps support, feedback): 

* [Olaf Zimmermann (ZIO)](https://ozimmer.ch/index.html)
* [MAP](https://microservice-api-patterns.org/) co-authors: Mirko Stocker, Daniel Lübke, Cesare Pautasso, Uwe Zdun
* Stefan Kapferer (author of MDSL generator in Context Mapper)
* Bachelor students at HSR FHO 
* MS 2019 and VSS 2019 participants 


## Getting Involved 

We are open to welcome new contributors who want to help improve MDSL language specification and tools:

* Feel free to create issues in GitHub.
* Submit pull requests. If you do so, we assume that you comply with this [Developer Certificate of Origin](https://developercertificate.org/).
* Contact us to discuss collaboration and integration opportunities.

*Copyright: Olaf Zimmermann, 2019-2020. All rights reserved. See [license information](/LICENSE).*

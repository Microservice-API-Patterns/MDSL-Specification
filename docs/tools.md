---
title: Microservice Domain Specific Language (MDSL) Tools
author: Olaf Zimmermann, Stefan Kapferer
copyright: Olaf Zimmermann, 2019-2022. All rights reserved.
---


Quick Links: [MDSL Home](./index), [OpenAPI](./generators/open-api), [Protocol Buffers](./generators/protocol-buffers), [GraphQL schema](./generators/graphql), [Jolie](./generators/jolie), [Java POJOs](./generators/java), [Freemarker](./generators/freemarker), [AsyncAPI](./generators/async-api) Generators, [Design Transformations and Refactorings](./soad)

MDSL Tools: Users Guide
=======================

At present, the following types of MDSL Tools are available: 

* *Command Line Interface (CLI)* tools: API Linter (validation), technology-specific contract generation 
* *MDSL Web Application*, try it [here](https://mdsl-web.herokuapp.com/)  (ork in progress).
* *Eclipse Plugin*: editor, API Linter (validation), technology-specific contract generation

[Context Mapper](https://contextmapper.org/) has its own website.

<!-- Web app under construction -->

## Command Line Interface (CLI) Tools

The CLI tools can  validate MDSL files and call our generators from the command line. When calling `./mdsl` (or `mdsl.bat` in Windows), the CLI shows you the available parameters:

```text
usage: mdsl
 -f,--outputFile <arg>   The name of the file that shall be generated.
                         This parameter is only used if you pass 'text' to
                         the 'generator' (-g) parameter because the
                         Freemarker generator does not guess any file name
                         extension).
 -g,--generator <arg>    The generator you want to call. Use one of the
                         following values: oas (OpenAPI Specification),
                         proto (Protocol Buffers), jolie (Jolie), graphql
                         (GraphQL Schemas), java (Java Modulith), text
                         (arbitrary text file by using a Freemarker
                         template), soad (transformation chain to generate
                         bound endpoint type from user story), storyoas
                         (transformation chain to generate OpenAPI from
                         scenario/story), gen-model-json (Generator model
                         as JSON (exporter)), gen-model-yaml (Generator
                         model as YAML (exporter))
 -h,--help               Prints this message.
 -i,--input <arg>        Path to the MDSL file for which you want to
                         generate output.
 -o,--outputDir <arg>    The output directory into which the generated
                         files shall be written. By default files are
                         generated into the execution directory.
 -s,--standalone         Create output in main memory and write it to
                         standard output console.
 -t,--template <arg>     Path to the Freemarker template you want to use.
                         This parameter is only used if you pass 'text' to
                         the 'generator' (-g) parameter. 
```

Please refer to the readme of the [DSL core project](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/README.md) and of the [CLI package](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli) for  more information.


## Eclipse Plugin

### MDSL Editor
The MDSL Eclipse plugin provides editing support (syntax highlighting, auto completion, etc.) for our DSL. You can install the released snapshot versions of the plugin in your Eclipse from the following update site:

[https://microservice-api-patterns.github.io/MDSL-Specification/updates/](https://microservice-api-patterns.github.io/MDSL-Specification/updates/)

Once you have installed the plugin successfully, the MDSL editor should open for any file that ends with `.mdsl`. You can create one and copy-paste the above hello world example, or find additional examples [in this folder](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples).

If you want to check whether the plugin has installed successfully, you can go to the Eclipse "Help" menu, select "About Eclipse IDE" and then "Installation Details". Two MDSL entries should be there.


### API Linter 

The API Linter comes with the MDSL Editor as well as the CLI (see above); every time a file is saved or parsed, a number of validation rules and some simple metrics are evaluated and the results displayed in the Problems view of Eclipse.

In addition to the usual editor features such as syntax highlighting, completion and syntax checking, it implements a few simple semantic validators (as a basic *API Linter* demonstrator):

* The modeler is warned about incomplete and unsuited data types such as `P` and `ID<bool>`.
* The constraints of the message exchange patterns such as REQUEST_REPLY, ONE_WAY and NOTIFICATION are validated (if specified). For instance, a REQUEST_REPLY must define a request and a response message by definition.
* Some MAP pattern decorator combinations are checked and warned about (for instance, a COMPUTATION_FUNCTION in an INFORMATION_HOLDER_RESOURCE is a modeling smell).
* The number of operations per endpoint is reported; if is likely that the endpoint cannot be mapped to OpenAPI due to a large amount of operations, the user is warned.
* Existing HTTP resource bindings are checked for completeness (of operation bindings) and soundness (of element/parameter bindings).

An example that features all validators in action can be found in the examples folder of the repository: [`APILinterTestAndDemo.mdsl`](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/examples/APILinterTestAndDemo.mdsl).
The validation of this MDSL file yields the following errors, warnings and information messages (among others):

<a target="_blank" href="/media/api-linter-example.png">![API Linter example](/media/api-linter-example.png)</a>

*Update (12/2021):* Several more validators are available now, many of which are able to trigger continuos specification refinements via quick fix [transformations](soad).

### Transformations 

Quick fixes and menu entries support rapid service-oriented analysis and design and an "API first" approach. For instance, the plugin now comes with quick fixes to complete data type specifications: 

* Generic parameters `P`, `nameOnly`, and `name:P` can be completed with `D<string>`.
* Incomplete atomic parameters `D`, `ID`, `L`, `MD` can be completed with `D<string>` or `D<int>`.
* Atomic parameters can be wrapped in parameter trees.
* Key-value pairs can also be introduced.
* Type definitions can be extracted from requets and response messages (for reuse).
* Refactoring to patterns such as [Pagination](https://microservice-api-patterns.org/patterns/structure/compositeRepresentations/Pagination), Wish List, and Request Bundle (in a simple form) is supported. [Extract Information Holder](https://interface-refactoring.github.io/refactorings/extractinformationholder) is also supported.
* HTTP bindings can be generated.

Many endpoint-level transformations are available now too, including one to create AsyncMDSL from core MDSL. A `Move Operation`refactoring is available as a menu entry as well.

See [this page](soad.md) for more information on the quick fixes and transformations.

### Generators

In the MDSL Editor, you can invoke the following generators from the "MDSL" entry in the context menu:

* [Generate OpenAPI Specification](./generators/open-api)
* [Generate Protocol Buffers Specification](./generators/protocol-buffers)
* [Generate GraphQL Schema](./generators/graphql)
* [Generate Jolie Lang(uage) Specification](./generators/jolie)
* [Generate Java "Modulith" Code](./generators/java)
* Generate [ALPS](https://datatracker.ietf.org/doc/html/draft-amundsen-richardson-foster-alps-07) specification (status: [technology preview](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview))
* Generate AsyncMDSL specification (this actually is an in-model [transformation](soad), it does not generate a new output file)
* [Generate Text File with Freemarker Template](./generators/freemarker)
* Generate AsyncAPI (from AsyncMDSL). See page [AsyncAPI Specification Generator](./generators/async-api) and readme in [this examples folder](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/asyncMDSL) for further information.

These generator features are also available in the CLI (see above).

#### Generator Model for Freemarker Templating and Model Exports
To ease code generation with the [template-based generator](./generators/freemarker), an intermediate model is available. The following UML class diagram illustrates it (*note:* not all model elements are contained in the figure; for instance, flows and HTTP bindings are also accessible via the intermediate model):

<a href="./media/mdsl-generator-model.png" target="_blank">![MDSL Generator Model](./media/mdsl-generator-model.png)</a>

This model can also be exported for offline processing (for instance, to feed other tools): 

* Export Generator Model as JSON 
* Export Generator Model as YAML

*Note*: This feature is subject to change. We use it internally in the [GraphQL schema](./graphql) and [Java](./java) generators, so it has reached a certain level of maturity and test coverage. That said, it also has some known limitations and much room for improvement: 

* For instance, the output can be rather verbose and partially redundant (input depending, of course). 
* Recursive/cyclic data definitions cause the serializers not to terminate.

### MDSL Web Tools 

You may want to try this experimental [MDSL Web](https://mdsl-web.herokuapp.com/) version of the tools. 

# Site Navigation

[Home](./index) &mdash; [Endpoint Type](./servicecontract) &mdash; [Data Type](./datacontract) &mdash; [Provider and Client](./optionalparts) &mdash; [Bindings](./bindings) &mdash; [Tutorial](./tutorial) &mdash; [Tools](./tools)

*Copyright: Olaf Zimmermann, 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
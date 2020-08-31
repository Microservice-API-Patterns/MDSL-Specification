---
title: Microservice Domain Specific Language (MDSL) Tools
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2020. All rights reserved.
---

MDSL Tools: Users Guide
=======================

<!-- from top-level index page/readme:
### Installation in Eclipse

 * Update site: [https://microservice-api-patterns.github.io//MDSL-Specification/updates/](https://microservice-api-patterns.github.io//MDSL-Specification/updates/)
 * The grammar can be found in the `dsl-core` project (more precisely, in the `io.mdsl./src/io.mdsl` folder of this project): [public](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) and [private](https://github.com/socadk/MDSL/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) repository.

### Direct links into repository

* [Full grammar](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext)
* [Examples](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/tree/master/examples)
-->

At present, two types of MDSL tools are available (more to come): 

* Command Line Interface (CLI) tools: validation, technology-specific contract generation 
* Eclipse Plugin: editor, API Linter (validation) , technology-specific contract generation

## Command Line Interface (CLI) Tools
The CLI tools can  validate MDSL files and call our generators from the command line. When calling `./mdsl` (or `mdsl.bat` in Windows), the CLI shows you the available parameters:

```text
usage: mdsl
 -f,--outputFile <arg>   The name of the file that shall be generated
                         (only used by Freemarker generator, as we cannot
                         know the file extension).
 -g,--generator <arg>    The generator you want to call. Use one of the
                         following values: oas (Open API Specification),
                         jolie (Jolie), text (arbitraty text file by using
                         a Freemarker template), proto (Protocol Buffers),
                         gen-model-json (Generator model as JSON
                         (exporter)), gen-model-yaml (Generator model as
                         YAML (exporter))
 -h,--help               Prints this message.
 -i,--input <arg>        Path to the MDSL file for which you want to
                         generate output.
 -o,--outputDir <arg>    The output directory into which the generated
                         files shall be written. By default files are
                         generated into the execution directory.
 -t,--template <arg>     Path to the Freemarker template you want to use.
                         This parameter is only used if you pass 'text' to
                         the 'generator' (-g) parameter.
```

Please refer to the [readme of the DSL core project](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/README.md) and the [CLI package readme](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli) for  more information.


## Eclipse Plugin

### MDSL Editor
The MDSL Eclipse plugin provides editing support (syntax highlighting, auto completion, etc.) for our DSL. You can install the plugin in your Eclipse from the following update site:

[https://microservice-api-patterns.github.io//MDSL-Specification/updates/](https://microservice-api-patterns.github.io//MDSL-Specification/updates/)

Once you have installed the plugin successfully, the MDSL editor should open for any file that ends with `.mdsl`. You can create one and copy-paste the above hello world example, or find additional examples [in this folder](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples).

If you want to check whether the plugin has installed successfully, you can go to the Eclipse "Help" menu, select "About Eclipse IDE" and then "Installation Details". Two MDSL entries should be there.


### API Linter 
The API Linter comes with the MDSL Editor as well as the CLI (see above); every time a file is saved or parsed, a number of validation rules and some simple metrics are evaluated and the results displayed.

<!-- TODO 4.0 add screen shot -->

An example that features all validators in action can be found in the examples folder of the repository [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/examples/APILinterTestAndDemo.mdsl).

### Generators

In the MDSL Editor, you can invoke four generators from the "MDSL" entry in the context menu:

* Generate Text File with Freemarker Template 
* Generate OpenAPI Specification 
* Generate Protocol Buffers Specification
* Generate Jolie Lan Specification 

These generator features are also available in the CLI (see above).

#### IDL Generators: Open API, Proticol Buffers, Jolie 

See [this demo](https://ozimmer.ch/practices/2020/06/10/ICWEKeynoteAndDemo.html) for the time being (only Open API featured so far). 

#### Freemarker Templating

The entire grammar is available as a data model to the Freemarker templating feature.  

Examples of such reports can be found [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/generator-templates)

#### Generator Model
<!-- TODO document new generator model here -->

The intermediate model that is available to the template-based generator can be exported: 

* Export Generator Model as JSON 
* Export Generator Model as YAML


## Site Navigation
<!-- TODO update for V4.0 consistently; does this Jekyll template support a menu on the right? -->

* [Quick reference](./quickreference) and [tutorial](./tutorial). 
* Language specification: service [endpoint contract types](./servicecontract) and [data contracts (schemas)](./datacontract). [Bindings](./bindings) and [instance-level concepts](./optionalparts). 
* Back to [MDSL homepage](./index).

*Copyright: Olaf Zimmermann, 2018-2020. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
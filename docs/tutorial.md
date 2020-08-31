---
title: Microservice Domain Specific Language (MDSL) Tutorial
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019. All rights reserved.
---

MDSL Tutorial
=============

<!-- TODO (H) feature HTTP binding, error reporting, security policy, default value (V3/V4 enhancements) in this tutorial -->

## Getting Started

Let us assume we want to create an [HTTP resource API](https://restful-api-design.readthedocs.io/en/latest/) that supports upload and download of spreadsheets. The structure of these sheets resembles that of the popular [CSV format](https://en.wikipedia.org/wiki/Comma-separated_values) and supporting tools. We choose a *contract-first* approach to design this API.[^1]

[^1]: There are two ways to approach API design, *code first* and *contract first*. With code first, one writes an API implementation, annotates the API with the required routing and (de)serialization information and lets tools and middleware generate a contract. In contract first, one authors an interface description, for instance in Swagger/Open API Specification and then (optionally) lets tools generate client and server stubs. MDSL supports contract-first service design at present.

Unlike Swagger/Open API Specification, Microservice Domain-Specific Language (MDSL) is a technology-neutral notation; see [MDSL home page](./index) for more positioning information. 

## Modeling Representation Elements 
Let's start with data modeling. A spreadsheet may contain several sheet tabs (sometime also called worksheets). This multiplicity can be indicated by an asterisk `*`:
<!-- say that we simplify on purpose here? -->

~~~
data type CSVSpreadsheet CSVSheetTab*
~~~

The content of such sheet tabs is usually structured into rows, and the tabs usually also have unique names. Hence, we model them as *Parameter Trees*, a pattern from the [MAP language](https://microservice-api-patterns.org/patterns/structure/representationElements/ParameterTree.html). In MDSL, parameter trees are represented by curly braces `{}`:

~~~
data type CSVSheetTab {"name": D<string>, "content": Rows*}
~~~

`"name": D<string>`is a fully specified [*Atomic Parameter*](https://microservice-api-patterns.org/patterns/structure/representationElements/AtomicParameter) (scalar). `"content": Rows*` is the second parameter, referencing yet another data type definition. Each sheet may have an arbitrary number of rows (indicated by an asterisk `*`). 

<!-- TODO talk about incomplete param spec., as "fully specified" is mentioned above? -->

Each row is identified by a `line` number and features data in at least one `column` (which is indicated by a plus `+`):
 
~~~
data type Rows {"line": ID<int>, "columns":Column+} 
~~~

This atomic parameter is not characterized as a plain value `D` as the name in `CSVSheetTab` above, but as a unique identifier `ID`. [*Id(entifier) Element*](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/IdElement) is another MAP leveraged by and integrated into MDSL. [^2]


[^2]: The other two element stereotypes in MAP, are Metadata `MD` and Link `L`. 

Columns are often identified by characters `position` and, optionally, a more expressive text `header` (optionality is indicated by the `?` modifier in MDSL):

<!-- TODO position and header now appearing in all rows (at runtime); this modeling cannot guarantee that all rows use same column structure (just an example, no need to match CSV 100%) -->

~~~
data type Column {"position": ID<string>, 
                  "header": D<string>?, 
                  "cell": Cell}
~~~

The combination of columns and rows gives us the powerful matrix/cell structure that we would expect from spreadsheets. <!-- greatly/grossly simplified! -->
We have now arrived at the cell level. Cells either contain text, numbers or formulas (in this example):[^4]

[^4]: ignoring special types of cells such as pivot tables and data visualizations, as supported for instance in Microsoft Excel, in/for this simplified example.

~~~
data type Cell { "text":D<string> 
               | "integerValue": D<int> 
               | "longValue": D<long> 
               | "formula": D<string> }
~~~


## Modeling Operations and Endpoints <!-- Service Contract -->
We are now ready to send instances of the complex (nested, repetitive) `CSVSpreadsheet` over the wire; it can serve as a [Data Transfer Object (DTO)](https://martinfowler.com/eaaCatalog/dataTransferObject.html).[^3] We need a service contract to do that.

[^3]: In our integration context, Data Transfer Representation (DTR) is a better name, actually.

Operation specifications in MDSL are quite talkative (unlike the rather compact data type definitions we have worked with so far):

~~~
  operation downloadSpreadsheet
    expecting payload ID
    delivering payload CSVSpreadsheet
      reporting error "SheetNotFound" 
~~~

The request and response messages of `downloadSpreadsheet` are defined via their data type structures. Here, the expected request message contains a simple `ID`(entifier) that is not specified any further at this point; the delivered response is the `CSVSpreadsheet` DTO from above. 

Optionally, error reporting information can be added (which comes in the form of a parameter specification; see "Reporting" section on the [quick reference page](./quickreference) for an example). In the above example, one error message is defined, reporting that the received ID does not match any existing spreadsheet.

To complete the example, we should also model an upload operation:
~~~
  operation uploadSpreadsheet
    expecting payload CSVSpreadsheet
    delivering payload {"successFlag":D<bool>, ID}
~~~ 

The two operation specifications appear inside an endpoint specification (which corresponds to a resource definition in REST):

~~~
endpoint type SpreadSheetExchangeEndpoint
exposes 
  [operations go here]
~~~

## Wrapping it all up (Full Specification)

We are still missing an API description wrapper and, optionally, can add a sample client and provider:

~~~
API description SpreadSheetExchangeAPI

data type CSVSpreadsheet CSVSheetTab*
data type CSVSheetTab {"name": D<string>, 
                       "content": Rows*}
data type Rows {"line": ID<int>, 
                "columns":Column+}
data type Column {"position": ID<string>, 
                  "header": D<string>?, 
                  <<Entity>> "cell": Cell}
data type Cell {"formula":D<string> 
               | "intValue": D<int> 
               | "longValue": D<long> 
               | "text": D<string>}

endpoint type SpreadSheetExchangeEndpoint
exposes 
  operation uploadSpreadsheet
    expecting payload CSVSpreadsheet
    delivering payload {"successFlag":D<bool>, ID}
    
  operation downloadSpreadsheet
    expecting payload ID
    delivering payload CSVSpreadsheet
      reporting error "SheetNotFound" 

API provider SpreadSheetExchangeAPIProvider
offers SpreadSheetExchangeEndpoint

API client SpreadSheetExchangeAPIClient
consumes SpreadSheetExchangeEndpoint
~~~

We are done modeling and now would be ready to implement the contract and deploy a provider supporting it. An intermediate step probably would be to create a platform- and technology-specific contract such as a OpenAPI/Swagger specification; see [MDSL tools](./tools).
A protocol [binding](./bindings) might be needed for that step. <!-- TODO show it here -->

## Outlook: MAP Decorators
MDSL is aware of the [Microservice API Patterns](https://microservice-api-patterns.org/); these patterns can be used to annotate endpoints, operations, and representation elements. This makes the machine-readable specification more expressive (in comparison to formated comments or free-form texts accompanying the formal specification):

~~~
API description SpreadSheetExchangeAPI

data type CSVSpreadsheet CSVSheetTab*
data type CSVSheetTab {"name": D<string>, 
                       "content": Rows*}
data type Rows {"line": ID<int>, 
                "columns":Column+}
data type Column {"position": ID<string>, 
                  "header": D<string>?, 
                  <<Entity>> "cell": Cell}
data type Cell {"formula":D<string> 
               | "intValue": D<int> 
               | "longValue": D<long> 
               | "text": D<string>}

endpoint type SpreadSheetExchangeEndpoint serves as TRANSFER_RESOURCE
exposes 
  operation uploadSpreadheet with responsibility NOTIFICATION_OPERATION
    expecting payload CSVSpreadsheet
    delivering payload {"successFlag":D<bool>, ID}
    
  operation downloadSpreadsheet with responsibility RETRIEVAL_OPERATION
    expecting payload ID 
    delivering payload CSVSpreadsheet
      reporting error "SheetNotFound" 

API provider SpreadSheetExchangeAPIProvider
offers SpreadSheetExchangeEndpoint

API client SpreadSheetExchangeAPIClient
consumes SpreadSheetExchangeEndpoint
~~~

This enhanced specification states that `SpreadSheetExchangeEndpoint` serves as a `TRANSFER_RESOURCE`; `upSpreadsheet` is an `NOTIFICATION_OPERATION` and `downloadSpreadsheet` is a `RETRIEVAL_OPERATION`. The cell content in the `Column` data type is an `<<Entity>>`. All these decorators are defined as patterns in the MAP language.

In addition to MAP patterns, any string can decorate endpoints and operations; if this capability is used, it is undefined how tools will process them (as these tools cannot know the meaning of free-form decorators).


## Links

* [Quick reference skeleton](./quickreference) and [tools](./tools)
* Service [endpoint contract types](./servicecontract), [data contracts (schemas)](./datacontract), [bindings](./bindings) and other [runtime language concepts](./optionalparts).
* Back to [MDSL homepage](./index). 
* [Microservice API Patterns](https://microservice-api-patterns.org/).

*Copyright: Olaf Zimmermann, 2018-2020. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
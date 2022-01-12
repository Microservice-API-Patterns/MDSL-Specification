---
title: Microservice Domain Specific Language (MDSL) to OpenAPI Specifications
author: Olaf Zimmermann, Stefan Kapferer
copyright: Olaf Zimmermann, 2020-2022. All rights reserved.
---

[Tools Overview](./../tools), [Protocol Buffers](./protocol-buffers), [GraphQL](./graphql), [Jolie](./jolie), [Java](./java), [Freemarker templating](./freemarker), [AsyncAPI](./async-api)

OpenAPI Specification Generator
===============================

The MDSL Eclipse plugin and the CLI allow API designers to generate [OpenAPI specifications](https://www.openapis.org/) out of MDSL service contracts. 

## Usage
You can generate the OpenAPI specifications from MDSL models via the [Eclipse plugin](./../tools#eclipse-plugin) or the [CLI](./../tools#command-line-interface-cli-tools). 

In Eclipse, you find the generator in the MDSL context menu:

<a href="./../media/eclipse-oas-generator-context-menu.png">![OpenAPI Specification Generator Context Menu in Eclipse](./../media/eclipse-oas-generator-context-menu.png)</a>

The following command generates a specification in case you work with the CLI:

```bash
./mdsl -i model.mdsl -g oas
```

_Hint:_ Both tools generate the output into the `src-gen` folder which is located in the projects root directory (Eclipse) or the directory from which the `mdsl` command has been called (CLI). Both tools create the directory automatically in case it does not already exist. Look for `.yaml` files corresponding to the `.mdsl` input.

## Generator Output / Mapping
The OpenAPI Specification (OAS) generator maps endpoint types to HTTP resource paths, and operations to HTTP methods/verbs like this:

* If a MAP decorator is used, it is mapped as this:
    * `STATE_CREATION_OPERATION` is transformed to `PUT` (yes, `POST` also would make sense)
    * `RETRIEVAL_OPERATION` is transformed to `GET` (which causes problems if the request message has a complex structure)
    * `STATE_TRANSITION_OPERATION` is transformed to `PATCH`
    * `COMPUTATION_FUNCTION` is transformed to `POST`
* If an HTTP verb is used instead of a MAP decorator (`"GET"`, `"POST"` etc.), it is passed through.
* If the operation name suggests CRUDish semantics (or starts with HTTP verb names), it is mapped as this:
    1. createNN and addToNN are transformed into `POST` methods
    2. readNN, getNN, retrieveNN, searchNN all are transformed into `GET`ters
    3. putNN and replaceNN are transformed into `PUT`s
    4. updateX and patchX are transformed into `PATCH` methods
    5. deleteNN and removeNN transformed into `DELETE` methods

If an HTTP verb appears more than once, for instance if more than five operations are defined, the information in the `expecting` part of the endpoint type is not sufficient to define a single HTTP resource API. An HTTP [binding](./../bindings#http-protocol-binding) with multiple resources has to be defined in that case (see the bindings page for an example of a resource definition). 

The input data in MDSL are mapped differently, depending on the HTTP method/verb (which is mapped as explained above). The following table explains the mapping:

|  [HTTP Methods](https://tools.ietf.org/html/rfc7231) | [MDSL Data Type](./datacontract) | [OAS/HTTP Parameter Type](https://swagger.io/docs/specification/describing-parameters/) | Mapping |
|-|-|-|-|
| GET | Atomic Parameter `"pname":D<string>` | query, cookie, header | supported, simple |
|  | Atomic Parameter List `("p1":D<string>, "p2":D<string>)`| query, cookie, header <!-- TODO test cookie and header --> | each element becomes separate parameter |
|  | Flat Parameter Tree `{"p1":D<string>, "p2":D<string>}`| query, cookie , header <!-- TODO test cookie and header --> | yes, also flattened |
|  | Nested Parameter Tree `{"p1":D<string>, {"t2":D<string>}}`| query | yes (Deep Object) |
|  | any parameter | path | basic support (validation incomplete) |
|  | any parameter | body | not supported (by HTTP/OAS) |
| DELETE | same as GET |  |  |
| POST | Atomic Parameter `"pname":D<string>` | query, cookie, header | supported, simple |
| | Atomic Parameter List `("p1":D<string>, "p":D<string>)`| query, cookie, header | supported, simple |
| | Flat Parameter Tree `{"p1":D<string>, "p":D<string>}`| query, cookie, header | supported, simple |
| | Nested Parameter Tree `{"p1":D<string>, {"anotherTree":D<string>}}`| query (others possible too)) |  |
|  | any parameter | path | not recommended (TODO)  |
|  | any parameter | body | default (multiple mime/media types) |
| PUT, PATCH | same as POST |  |  |

<!-- 
| | Atomic Parameter `"pname":D<string>` |  |  |
| | Atomic Parameter List `("p1":D<string>, "p":D<string>)`|  |  |
| | Flat Parameter Tree `{"p1":D<string>, "p":D<string>}`| |  |
| | Nested Parameter Tree `{"p1":D<string>, {"anotherTree":D<string>}}`| |  |
-->

<!-- TODO talk about, P, void, "idOnly"; talk about cardinalities (turn flat into nested structures) -->

Response messages `delivering` are always mapped to the response body. Additional status and error report responses can be defined; multiple media types, including custom ones, are respected if present in the endpoint type (`replies`).

_Note:_ The above mappings works with MDSL HTTP mappings in the providers. Providers that do not contain an HTTP mapping are ignored.

<!--  TODO feature binding: global and individual; reference MDSL language page; list defaults -->

## Example
The following example illustrates what the generator produces for an exemplary MDSL contract.

You find the complete sources (incl. the generated OAS specification) of this example [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/open-api-example).

We use the following MDSL model which was an outcome of this [blogpost](https://ozimmer.ch/practices/2020/06/10/ICWEKeynoteAndDemo.html) to illustrate our generator outputs:

```
API description ReferenceManagementServiceAPI

data type PaperItemDTO { "title":D<string>, "authors":D<string>, "venue":D<string>, "paperItemId":PaperItemKey }
data type PaperItemKey { "doi":D<string> }
data type createPaperItemParameter { "who":D<string>, "what":D<string>, "where":D<string> }

endpoint type PaperArchiveFacade
  serves as INFORMATION_HOLDER_RESOURCE
  exposes
    operation createPaperItem
      with responsibility STATE_CREATION_OPERATION
      expecting
        payload createPaperItemParameter
      delivering
        payload PaperItemDTO
    operation lookupPapersFromAuthor
      with responsibility RETRIEVAL_OPERATION
      expecting
        payload "author": D<string>
      delivering
        payload PaperItemDTO*
    operation convertToMarkdownForWebsite
      expecting
        payload PaperItemKey
      delivering
        payload D<string>
```

For the MDSL contract above, the generator produces the following OAS specification:

```yaml
openapi: 3.0.1
info:
  title: ReferenceManagementServiceAPI
  version: "1.0"
  x-generated-on: 2022-01-11T07:09:55.4937813
servers: []
tags:
- name: PaperArchiveFacade
  externalDocs:
    description: PaperArchiveFacade contract, Information Holder Resource role
    url: https://microservice-api-patterns.org/patterns/responsibility/endpointRoles/InformationHolderResource.html
paths:
  /PaperArchiveFacade:
    summary: general data-oriented endpoint
    get:
      tags:
      - PaperArchiveFacade
      summary: lookupPapersFromAuthor (read only method)
      description: '[Retrieval Operation](https://microservice-api-patterns.org/patterns/responsibility/operationResponsibilities/RetrievalOperation.html).'
      operationId: lookupPapersFromAuthor
      parameters:
      - name: author
        in: query
        description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
          target="_blank">Data Element</a>
        required: true
        schema:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
      responses:
        "200":
          description: lookupPapersFromAuthor successful execution
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/PaperItemDTO'
    put:
      tags:
      - PaperArchiveFacade
      summary: createPaperItem (write only method)
      description: '[State Creation Operation](https://microservice-api-patterns.org/patterns/responsibility/operationResponsibilities/StateCreationOperation.html).'
      operationId: createPaperItem
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/createPaperItemParameter'
      responses:
        "200":
          description: createPaperItem successful execution
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PaperItemDTO'
    post:
      tags:
      - PaperArchiveFacade
      summary: convertToMarkdownForWebsite
      description: ""
      operationId: convertToMarkdownForWebsite
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PaperItemKey'
      responses:
        "200":
          description: convertToMarkdownForWebsite successful execution
          content:
            application/json:
              schema:
                type: object
                properties:
                  anonymous3:
                    type: string
                    description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
                      target="_blank">Data Element</a>
components:
  schemas:
    PaperItemDTO:
      type: object
      properties:
        title:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
        authors:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
        venue:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
        paperItemId:
          $ref: '#/components/schemas/PaperItemKey'
    PaperItemKey:
      type: object
      properties:
        doi:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
    createPaperItemParameter:
      type: object
      properties:
        who:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
        what:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
        where:
          type: string
          description: <a href="https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement"
            target="_blank">Data Element</a>
```

You find the complete sources (incl. the generated OAS specification) of this example [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/open-api-example).


## Editor and Validation
You can use the [Swagger Editor](https://editor.swagger.io/) to validate generated OAS specifications and/or generate an HTML documentation. You can also generate client and server code from there.

This and other code generation options are featured in [this blog post](https://nordicapis.com/3-tools-to-auto-generate-client-libraries-from-oas/). 

## Known Limitations

* If a representation element such as `"id"` is mapped to a `PATH` parameter but the resource URI does not contain a corresponding URI template `{id}`, the generated OpenAPI does not validate.
  * Many of these case are reported by the MDSL validator a.k.a. API Linter; quick fixes hae been introduced in Version 5.4 to help fix such situations. There still are cases that require attention, for instance when mixing binding styles across operations (paths are an endpoint/resource property!)
* The HTTP binding information is not validated completely; for instance, it is not checked whether bound operations actually are exposed in the endpoint type.
* The existing binding validators do not catch all edge cases; for instance incomplete parameters `"idNoType"` are reported as not mappable.
* The parameter mappings (MDSL to path, query, body/form parameters) do not cover all edge cases yet:
  * Externally referenced generic parameters without a name (for example, `P`) are ignored during response body/schema generation.
  * Standalone identifiers `"someName"`are mapped but cannot be used as one might expect. Unnamed parameters cannot be mapped explicitly and individually (workaround: name them or use a global operation-level binding).
  * The `*` and `+` multiplicities of Atomic Parameter Lists are not always taken into account.
  * It is possible to generate nested/deep cookie parameters, which may not make much sense.
  * Void parameters `D<void>` are mapped to strings, as VoidSchema (which exists in OpenAPI) may cause the generated OpenAPI not to validate in some tools (or cause unexpected behavior of tools that use OPenAPI as input).
* The security binding supports all [security types and schemes in OAS](https://swagger.io/specification/#security-scheme-object), but might not respect all configuration options and input correctly yet, for instance in OAuth flows. If a security policy is defined in the abstract endpoint type but not bound (because no API provider instances with a suited HTTP binding can be found), the policy ignored and not mapped to an OAS security requirement.
 

# Other Generators

The other generators are:

* [Protocol Buffers generator](./protocol-buffers)
* [GraphQL generator](./graphql)
* [Jolie generator](./jolie)
* [Java generator](./java)
* [Arbitrary textual generation with Freemarker](./freemarker)


# Site Navigation

* [MDSL homepage](./../index), [Tools page](./../tools), [Quick reference](./../quickreference), [Tutorial](./../tutorial
* Language specification: 
    * Service [endpoint contract types](./../servicecontract)
    * [Data contracts (schemas)](./../datacontract). 
    * [Bindings](./../bindings) and [instance-level concepts](./../optionalparts).

*Copyright: Stefan Kapferer and Olaf Zimmermann, 2020-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

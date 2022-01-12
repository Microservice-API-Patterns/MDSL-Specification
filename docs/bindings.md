---
title: Microservice Domain Specific Language (MDSL) Bindings
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2022. All rights reserved.
---

[Home](./index) &mdash; [Endpoint Type](./servicecontract) &mdash; [Data Types](./datacontract) &mdash; [Provider and Client](./optionalparts) &mdash; [Tutorial](./tutorial) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools)


Protocol Bindings for HTTP, gRPC, Jolie, Java
=============================================

MDSL by design abstracts from and generalizes concepts in other API contract languages. This is rather straightforward for most of them (and has been done before). For HTTP resource APIs, additional concepts and an intermediate step are required because MDSL endpoints do not map to resources and their URIs one-to-one (what about dynamic addressing as promoted by URI templates/path parameters? how to map complex request payloads of retrieval operations, HTTP GET and request bodies do not go well together?). The HTTP Protocol Binding of MDSL realizes this intermediate step in a flexible way.

## Overview 

Let us start with the following endpoint type (and then show all available bindings in this example):

~~~
API description ProductManagement version "v0.0.1"

data type ProductDescription P /* defined incompletely */
data type Money {"currency":D, "amount":D<int>} /* defined incompletely, but a bit more precisely */
data type ErrorReport {"rootCause":D<string>, "correctiveAction":D<string>}

endpoint type ProductManagementService
exposes 
  operation define
    expecting payload "productDescription": ProductDescription
    delivering payload "successAck": D<bool>
      // technology-neutral error reporting (to be bound to OAS/HTTP, gRPC, Java, Jolie):
      reporting error DuplicateEntry D<string>
    // technology-neutral security policy (to be bound to protocols and platforms):
    protected by policy BasicAuthentication "UserIdPassword": {"userId":ID<string>, "password":MD<string>}
  operation updatePrice /* optional: "in REQUEST_REPLY conversation" */
    expecting payload "price": Money  
    // delivering payload D<void>
    // not delivering any payload, just a transport-level status code
~~~


## HTTP Protocol Binding

HTTP handles addressing, request and response parameters, errors, and security concerns in certain ways (for good reasons). The protocol design deviates from than of most interface definition languages and "RPC-ish" communication protocols. To generate OpenAPI and, later on, server-side stubs and client-side proxies from MDSL specifications, some of the required information can therefore always be derived from the abstract endpoint types. A primary  example is the mapping of MDSL operations to HTTP verbs/methods such as GET, POST, PUT etc. 

The additional information can be specified in a provider-level *HTTP binding*: 

~~~
API provider ProductManagementWebServiceProvider
  offers ProductManagementService
  at endpoint location "http://www.tbc.io:80/path/subpath"
  via protocol HTTP  
    binding 
     resource PMSResource at "/products/{productId}" // PATH parameter (implicit)
      operation define to POST 
        all elements realized as BODY parameters
        report DuplicateEntry realized as 412 with "DuplicateEntry"
        policy BasicAuthentication realized as BASIC_AUTHENTICATION
        accepts "application/json"// defined at https://www.iana.org/assignments/media-types/media-types.xhtml
        replies "application/vnd.custom-mediatype-for-productDTO-v1" // custom media type 
      
      operation updatePrice to PATCH at "/products/{productId}/price"
        element "currency" realized as QUERY parameter
        element "amount" realized as QUERY parameter
~~~

The information in the binding refers to and refines the operation- and message level specification (abstract endpoint type level:)

* At least one resource must/can be defined (`PMSResource`); their names and URIs must differ. 
* The resource URIs may contain URI name templates /`{productId}`).
* The operations from the references endpoint type can be bound in multiple resources (once each). The operation-to-verb assignment (`POST`, `PATCH`) must be unique in each resource.  
* The representation elements from request payloads of operations can be mapped to HTTP parameter types jointly (`all elements realized as BODY parameters`) or individually (`element "currency" realized as QUERY parameter`). Default are in place (see [here](./tools/generators/open-api)). There are some limitations of flat/nested tree usage; cardinalities (`?`, `*`, `+`) are respected, though.
* The abstract error/status reporting is mapped to HTTP codes (`report DuplicateEntry realized as 412 with "DuplicateEntry"`). In our MDSL tools, a validator checks that only existing reports are bound. 
* Security policies are bound and mapped in the same way (`policy BasicAuthentication realized as BASIC_AUTHENTICATION`).
* One or more MIME types of request and response messages can be defined (`accepts`, `replies`). Both standard and custom media types can be specified.
* Not shown in the above example, but explained [here](./http-rest), links are mapped to OpenAPI [link objects](https://swagger.io/docs/specification/links/) and, in turn hypermedia links in response messages. 
* Server instances are created for the endpoint addresses.

*Note:* The protocol bindings ("adapters") stand at an intermediate level of elaboration and validation. Grammar, linter, and generator support are rather stable, but might still change in future versions of MDSL and [MDSL Tools](./tools).

<!-- *Status*: This is the first complete version of the binding. It is implemented in the current [MDSL Tools](./tools), but has not been fully validated yet. The tool implementation has some known limitations. -->

<!--  TODO 
feature 'with' clause of individual bindings (grammar rule 'HTTPParameterBinding'):
```
operation op1 to POST element "properties" realized as BODY parameter with SomeCommand // TODO
```
-->

## gRPC Protocol Buffers Binding

gRPC is easier to map than HTTP. At present, the grammar only foresees one additional construct: 

~~~
enum StreamingOption:
	client | server | bidirectional // if not present, "unary" is assumed
;
~~~

See the [gRPC documentation](https://grpc.io/docs/what-is-grpc/core-concepts/) for explanations. 

~~~
API provider ProductManagementGRPCServiceProvider
  offers ProductManagementService
  at endpoint location "tbd"
  via protocol gRPC
    // no need for bindings here, but still demoing it: 
    binding
     operation define to "define" as server stream
     operation updatePrice as bidirectional stream
~~~


## Jolie Binding

There is no such binding at present; in the future we might support concepts such as namespace (and pass this information on to the `jolie2wsdl` tool that comes with Jolie).

<!-- TODO also tak about non-HTTP transports in Jolie? -->


## Local Java Binding

A binding is defined that maps operations to methods and representation elements to parameters (both optional), and allows contract and binding designers to specify a  Java package: 

~~~
API provider ProductManagementJavaServiceProvider
  offers ProductManagementService
  at endpoint location "n/a" 
  via protocol Java
    // no need for operation bindings here, but still demoing them: 
    binding 
     package "co.something.model.ProductActor"
     operation define to "define"
        element productDescription realized as int type
     operation updatePrice to "updatePrice"
        element money realized as boolean type
~~~

In the current release, only the package name is used. <!-- there is an unfinished Freemarker template; /* [Q]: "extends"? */ -->


## Other Bindings
An enum in the grammar defines some more commonly used protocols (no detailed bindings for these technologies have been defined yet):

~~~
OtherBinding:
    soap='SOAP_HTTP' | avro='Avro_RPC' | thrift='Thrift' | amqp='AMQP' | jms='JMS_ActiveMQ' | stomp='STOMP' | kafka='Kafka' | mqtt='MQTT' | other=STRING 
;
~~~

The "other" part of this grammar rule makes it possible to define `"AnyOtherProtocol"` (without expecting the existing tools to be able to do anything specific with this information).


# Site Navigation

<!--
Language specification pages:

* Service [endpoint contract types](./servicecontract)
* [Data contracts (schemas)](./datacontract)
* Other [runtime concepts](./optionalparts)
-->

* Language specification:
  * Service [endpoint contract types](./servicecontract) and [data contracts (schemas)](./datacontract).
  * Advanced [REST binding concepts](./http-rest)
  * [Providers, clients, gateways](./optionalparts) and [instance-level concepts](./optionalparts) (this page).
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* Back to [MDSL homepage](./index).

*Copyright: Olaf Zimmermann, 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->

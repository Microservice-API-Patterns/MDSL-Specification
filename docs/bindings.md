---
title: Microservice Domain Specific Language (MDSL) Bindings
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2020. All rights reserved.
---

Protocol Bindings for HTTP, gRPC, Jolie, Java
=============================================

*Note:* The protocol bindings ("adapter") specifications are work in progress still, and therefore subject to change in future versions (grammar, linter, generator support).

<!--
TODO update (from other): 
The language concepts described here can be used for context mapping, deployment modeling, and code generation (e.g., walking provider skeletons, test clients). Unlike endpoint types and data contracts, they play on the "instance" rather than the "class" level (just like ports in WSDL are instances of port types).
-->

## Overview 

Let us start with the following endpoint type (and then show all available bindings in this example):

~~~
API description ProductManagement version "v0.0.1"

data type ProductDescription P /* defined incompletely */
data type Money {"currency", "amount":D<int>} /* defined incompletely, but a bit more precise */
data type ErrorReport {"rootCause":D<string>, "correctiveAction":D<string>}

endpoint type ProductManagementService
exposes 
  operation define
    expecting payload "productDescription": ProductDescription
    delivering payload "successAck": D<bool>
      // technology-neutral error reporting (to be bound to OAS/HTTP, gRPC, Java, Jolie):
      reporting error "DuplicateEntry": D<string>
    // technology-neutral security policy (to be bound to protocols and platforms):
    protected by policy "UserIdPassword":{"userId":ID<string>, "password":MD<string>}
  operation updatePrice /* optional: "in REQUEST_REPLY conversation" */
    expecting payload "price": Money  
    delivering payload D<void>
    // not delivering any payload, just a transport-level status code (?)
~~~


## HTTP Protocol Binding

HTTP handles addressing, request and response parameters, errors, and security concerns in certain ways (for good reasons). The protocol design deviates from than of most interface definition languages and "RPC-ish" communication protocols. To generate OpenAPI and, later on, server-side stubs and client-side proxies from MDSL specifications, some of the required information can therefore always be derived from the abstract endpoint types. A primary  example is the mapping of MDSL operations to HTTP verbs/methods such as GET, POST, PUT etc. 

The additional information can be specified in a provider-level *binding*: 

~~~
API provider ProductManagementWebServiceProvider
  offers ProductManagementService
  at endpoint location "http://www.tbc.io:80/path/subpath"
  via protocol HTTP 
    binding 
      operation define to POST at "/products/{productId}" // PATH parameter (implicit)
        element productDescription realized as BODY parameter
        element successAck realized as BODY parameter // only possibility for response payload element 
        report DuplicateEntry realized as 412 with ErrorReport
        policy UserIdPassword realized as BASIC_AUTHENTICATION
        // media types (defined in several RFCs, see https://en.wikipedia.org/wiki/Media_type)
        accepts application/json // defined at https://www.iana.org/assignments/media-types/media-types.xhtml
        replies "application/custom-mediatype-for-productDTO-v1" // custom media type (recommended REST practice) 
      operation updatePrice to PATCH at "/products/{productId}/price"
        element money realized as BODY parameter
~~~

*Important note*: This binding is work in progress and yet has to be completed and fully validated. For instance, the parameter mappings to path, query, form/body and cookie parameters is not fully implemented in the current MDSL tools yet. 


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

In the current release, only the package name is used. 
<!-- there is an unfinished Freemarker template; /* [Q]: "extends"? */ -->


## Other Bindings
An enum in the grammar defines some more commonly used protocols (no detailed bindings for these technologies have been defined yet):

~~~
OtherBinding:
    soap='SOAP_HTTP' | avro='Avro_RPC' | thrift='Thrift' | amqp='AMQP' | jms='JMS_ActiveMQ' | stomp='STOMP' | kafka='Kafka' | mqtt='MQTT' | other=STRING 
;
~~~

The "other" part of this grammar rule makes it possible to define `"AnyOtherProtocol"` (without expecting the existing tools to be able to do anything specific with this information).


# Site Navigation

Language specification pages:

* Service [endpoint contract types](./servicecontract)
* [Data contracts (schemas)](./datacontract)
* Other [runtime concepts](./optionalparts)

[Quick reference](./quickreference). [Tutorial](./tutorial). [Tools](./tools). Back to [MDSL homepage](./index).

*Copyright: Olaf Zimmermann, 2018-2020. All rights reserved. See [license information](https://github.com/socadk/MDSL/blob/master/LICENSE).*

<!-- *EOF* -->
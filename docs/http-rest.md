---
title: Microservice Domain Specific Language (MDSL) Advanced HTTP Binding Concepts
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2020-2021. All rights reserved.
---

[Home](./index) &mdash; [Endpoint Type](./servicecontract) &mdash; [Data Types](./datacontract) &mdash; [Provider and Client](./optionalparts) &mdash; [Bindings](./bindings) &mdash; [OpenAPI generator](./generators/open-api) &mdash; [Tools Overview](./tools)


Advanced Support for HTTP Resource APIs
========================================

*Note:* This is an [technology preview](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview) subject to change at any time. Its documentation is work in progress.

## Context 

Web API come on different levels of support for REST; hence, it is not clear what is meant by the term "REST API":

* [Hypertext as the engine of application state](https://en.wikipedia.org/wiki/HATEOAS), sometimes abbreviated HATEOAS, is one of the defining principles of the REST style.
* There are four [REST Maturity Levels](https://martinfowler.com/articles/richardsonMaturityModel.html); HATEOAS support is required to get to the highest level 3. Only "RESTful HTTP APIs" on maturity level 3 should be  REST APIs; APIs on other levels can be called "HTTP APIs, "HTTP resource APIs", or "Web APIs".  
* The book ["REST in Practice"](https://en.wikipedia.org/wiki/HATEOAS) provides an example of a REST level 3 hypermedia API called "RESTBucks", inspired by the IEEE Software article ["Your Coffee Shop Doesn’t Use Two-Phase Commit"](https://ieeexplore.ieee.org/document/1407829).
* Subbu Allamaraju called for and outlined contract language support in ["Describing RESTful Applications"](https://www.infoq.com/articles/subbu-allamaraju-rest/), and provided another example.
* OpenAPI supports [Link Objects](https://swagger.io/specification/#link-object), which have sinilar design goals.

MDSL and OpenAPI support maturity levels 1 and 2; this preview describes emerging REST level 3 support in MDSL:

* A `link type` concept is introduces on the endpoint type level.
* HTTP bindings can wrk with multiple resources and bind them in the ways required for level 3: hyperlinks bind the abstract link concept and add media type information (among other things).

This page explains the two concepts, for the time being by way of example only.

<!--
Let us start with the following domain model:

~~~
This baseline will be provided in a later version.
~~~
-->

## Links in Abstract Contract

MDSL operations can return abstract `links` in their `delivering` payload:

~~~
API description SimpleHypermediaAPI 

data type HALInstance1 {"someMoreILinkInformation":D<string>}
data type HALInstance2 {"evenMoreILinkInformation":D<string>}

relation type InternalStateTransferLink1 targets HypermediaDrivenEndpoint action continueProcessing input HALInstance1 // DAP (RiP)
relation type InternalStateTransferLink2 targets HypermediaDrivenEndpoint action terminate input HALInstance2 // action: operation or free-form
relation type ExternalStateTransferLink targets external resource at "http://map.mdsl.hateoas/elsewehere"

endpoint type HypermediaDrivenEndpoint
  serves as PROCESSING_RESOURCE
  exposes
    operation initiateProcess with responsibility STATE_CREATION_OPERATION
      expecting
        payload "in":D<string>
      delivering
        payload "out":D<int>
        links 
         "transferToStep2": InternalStateTransferLink1 
      
    operation continueProcessing with responsibility STATE_TRANSITION_OPERATION
      expecting
        payload "id2" 
      delivering
        payload {"statusCode":D<int>}
        links 
          "self": InternalStateTransferLink1
          "finishProcessing": InternalStateTransferLink2 
          "goElsewhere": ExternalStateTransferLink               
~~~

<!-- TODO talk reader through code -->

## HTTP Binding Extensions

On the tecnology binding level, the abstract links become "DAPs", i.e., typed links that define taregt URI, HTTP verb and media type (there are several options for providing this information): 

~~~
API provider RESTLevel3Provider
  offers HypermediaDrivenEndpoint
  at endpoint location "http://map.mdsl.hateoas"
  via protocol HTTP
   static binding
    resource Home at "/home"
     
      media type CustomMediaTypeRepresentationJSON as "application/vnd.step2cmt+json"
      relation type InternalStateTransferLink1 to {ResourceForStep2, PUT, CustomMediaTypeRepresentationJSON} 
                
      operation initiateProcess to POST
        // all elements realized as QUERY parameters
        // element "in" realized as PATH parameter // OAS/Swagger editor warns if not in URI
        element "in" realized as COOKIE parameter // 2nd one and all others ignored (ok)
        // element "in" realized as HEADER parameter 
        // element "in" realized as BODY parameter
        // element "in" realized as QUERY parameter // default
    
        accepts CustomMediaTypeRepresentationJSON 
        replies "application/vnd.restbucks.order-payment+json" 
    
    resource ResourceForStep2 at "/home/{id}"
      
      media type ProcessTerminationInformation as "application/vnd.myHALinstance+json" 
      relation type InternalStateTransferLink2 to {Home, PUT} // CMT not defined here for GET on "/home/{id} 
      relation type ExternalStateTransferLink to {"SomeExternalResource", POST, ProcessTerminationInformation} 
      
      operation continueProcessing to PUT
        element "id2" realized as PATH parameter // OAS/Swagger editor warn if not in URI
        // no accepts/replies here (link is enough)
~~~

Note that this endpoint provider instance defines tw resources to bind a single endpoint type.

<!-- TODO talk reader through code -->

## Status and Limitations

As a technology preview, this feature might change at any time, both on the language and on the tool level. And it still specifies static contracts, whereas the original vision of REST promotes dynamic contracts in support of flexibility and evolvability. <!-- TODO bring MDSL to runtime via annotation processing -->

## More information

Several versions of the RESTBucks example are modelled as examples and can be found [here](TODO).

Since version 5.0.3, the OpenAPI generator converts the above information into Link Objects.

# Site Navigation

Language specification pages:

* Service [endpoint contract types](./servicecontract)
* [Data contracts (schemas)](./datacontract)
* Other [runtime concepts](./optionalparts)
* Other [binding concepts](./bindings)
* [OpenAPI generator]([OpenAPI generator](./generators/open-api))

*Copyright: Olaf Zimmermann, 2020-2021. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->

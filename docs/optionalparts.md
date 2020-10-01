---
title: Microservice Domain Specific Language (MDSL) Other Concepts (Provider, Client, Gateway)
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2020. All rights reserved.
---

Runtime Language Concepts: API Provider, API Client, API Gateway
================================================================

*Note:* Work in progress still (the protocol bindings in particular)! 

The language concepts described here can be used for context mapping, deployment modeling, and code generation (e.g., walking provider skeletons, test clients). Unlike endpoint types and data contracts, they play on the "instance" rather than the "class" level (just like ports in WSDL are instances of port types).

## API Provider

An API provider exposes one or more endpoint contracts at an address that understands a particular platform-specific protocol:

~~~
API provider sampleProvider
offers SomeDemoContract
at endpoint location "http://www.tbc.io:80/path/subpath"
via protocol HTTP // or other supported protocol
under conditions "See http://www.tbc.io/terms-and-conditions.html"
provider governance AGGRESSIVE_OBSOLESCENCE
~~~

At present, the following protocols are predefined:

> HTTP | SOAP_HTTP | gRPC | Java 

<!-- | Avro_RPC | Thrift | AMQP | JMS_ActiveMQ | 'STOMP' | 'Kafka'  'MQTT' -->

It is also possible to define a custom protocol by including its name in double quotes: `"Some other protocol"`.[^1]

[^1]: In this case, future MDSL tools cannot be expected to be able to process the specification fully (unless a suited plugin is available).

Optionally, providers can disclose terms and conditions for API usage or a [Service Level Agreement](https://microservice-api-patterns.org/patterns/quality/qualityManagementAndGovernance/ServiceLevelAgreement.html) (SLA) as well as their approach to [versioning and evolution](https://microservice-api-patterns.org/patterns/evolution/).

A more complete example, also featuring an SLA and evolution governance information, looks like this:

~~~
API provider SampleAPIProvider1 
  offers SomeDemoContract 
    at endpoint location "http://www.testdomain.io:80/path/subpath"
    via protocol HTTP   
  	with endpoint SLA // provider1Endpoint1SLA
      type QUANTITATIVE // optional now
    	objective performanceSLO1 "responseTimeUnder" 5 seconds
    	penalty "If the SLA is not met, the penalty is ..."
    	notification "To report SLA violations, you have to ..."
    	rate plan USAGE_BASED
    	rate limit MAX_CALLS 5 within 24 hours
    endpoint governance AGGRESSIVE_OBSOLESCENCE
  with provider SLA // provider1SLA
  type QUALITATIVE
  objective performanceSLO2 "availability" 
     100 "every commercially reasonable effort, but not guaranteed"
  provider governance TWO_IN_PRODUCTION
~~~

The language elements in the endpoint and provider SLA sections model the elements an SLA is supposed to contain according to the [SLA pattern page on the MAP website](https://microservice-api-patterns.org/patterns/quality/qualityManagementAndGovernance/ServiceLevelAgreement.html).

### Protocol Bindings

See [bindings](./bindings) page.


## API Client

The consumers of endpoint contracts (API clients) are modeled according to the following template:

~~~
API client SampleAPIClient
    consumes SomeDemoContract
    from sampleProvider
    via protocol HTTP
~~~

Clients merely have to decide which APIs to consume and which protocol to use to do so.


## API Gateway

API gateways are hybrid providers (in upstream role) and clients (downstream role):

~~~
API gateway SampleAPIGatweway
  offers SomeDemoContract
  at endpoint location "ExternalURI"
  via protocol SOAP_HTTP

  consumes SomeDemoContract 
  from SampleAPIProvider1
  via protocol gRPC
~~~

## API Provider Implementation 
An API provider has an upstream interface (see [bindings](./bindings)), but also a downstream implementation (which is not visible to its external client, but still worth specifying internally): 

~~~
ProviderImplementation:
	'API' 'provider' 'implementation'
	name=ID
	'realizes' upstreamBinding=[Provider]
	'in' platform=ImplementationTechnology
	('as' class=STRING ('extending' superclass=STRING)? // default assigned if not specified
;

enum ImplementationTechnology: PlainJava | SpringMVC | STRING // more to be added
;	
~~~

<!-- removed | 'with' 'binding' downstreamBinding=[Provider])? // reference to Java protocol binding (optional) TODO 4.1 adopt example -->

An example of an instance of such provider binding is:

~~~

API provider implementation ProductManagementJavaServiceProviderImpl 
  realizes ProductManagementWebServiceProvider
  in PlainJava
  as "co.something.model.ProductActor" extending "Entity"
  // or: with binding ProductManagementJavaServiceProvider
~~~


# Site Navigation

* Language specification:
    * Service [endpoint contract types](./servicecontract) and [data contracts (schemas)](./datacontract).
    * [Bindings](./bindings) and [instance-level concepts](./optionalparts) (this page).
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* Back to [MDSL homepage](./index).

*Copyright: Olaf Zimmermann, 2018-2020. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
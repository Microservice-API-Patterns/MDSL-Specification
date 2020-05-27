---
title: Microservices Domain Specific Language (MDSL) Service Contracts
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2020.  All rights reserved.
---

Service Endpoint Contracts in MDSL
==================================


## Use Cases for MDSL Specifications 

* Creation, review and evolution of [Elaborate API Descriptions](https://microservice-api-patterns.org/patterns/foundation/APIDescription) as featured in Microservice API Patterns (MAP), in forward engineering:
  * A context-sensitive, Eclipse-based editor for MDSL, developed with [Xtext](https://www.eclipse.org/Xtext/), is available [here](./updates/)
  * Generators for platform-specific technologies such as Open API (f.k.a. Swagger), WSDL/SOAP, and gRPC  <!-- plus GQL; early prototypes/demonstrators available -->
* Reverse engineering (future work):
  * Discover contracts, clients, providers in existing systems 
  * Analysis support (metrics)

<!--
Requirements/principles (wouldn't it be nice?): 

* Be quick, be human readable w/o tools
* Abstract from WSDL, OAS, JSON, XSD w/o loosing detail/PIM specifics
-->

## Concepts 
The contract syntax (grammar) of MDSL is inspired by the API domain model from Microservice API Patterns (MAP).[^99] An API description features one or more *endpoints*, which expose *operations* that expect and deliver *messages*. These messages consist of headers and payloads, whose content is modelled with MDSL [data transfer representations](./datacontract):

[^99]: This domain model is published in Section 3 of the [EuroPLoP 2019 paper on Patterns for API Evolution from the MAP team](http://eprints.cs.univie.ac.at/6082/1/WADE-EuroPlop2019Paper.pdf):

~~~
serviceSpecification: 
	'API' 'description' name=ID
	('usage' 'context' visibility=visibility 
	    'for' direction+=directionList)?
	types+=dataContract*
	contracts+=endpointContract+
	providers+=provider*
	clients+=client*
	gateways+=gateway*
	('IPA')?;

endpointContract:
	'endpoint' 'type' name=ID 
	('version' svi=semanticVersioningIdentifier)? 
	('serves' 'as' primaryRole=ResourceRole 
	    ('and' otherRoles+=ResourceRole)* 'role'?)? 
	('identified' 'by' pathParameters=elementStructure)? 
	('exposes' ops+=operation+)?;

operation:
	'operation' name=ID
	('version' svi=semanticVersioningIdentifier)?
	('with' 'responsibility' respos=operationResponsibility)?
	('in'  mep=messageExchangePattern 'conversation')?  
	'expecting' requestMessage=dataTransferRepresentation
	('delivering' responseMessage=dataTransferRepresentation
		('reporting' reportData=statusReport)? // optional
	)?;

dataTransferRepresentation:
	('headers' headers=elementStructure)? 
	'payload' payload=elementStructure
	('structured' 'as' ts=typeSystem)?;
~~~


The notation used above is the [grammar language of Xtext](https://www.eclipse.org/Xtext/documentation/301_grammarlanguage.html) (which close to that of antlr4). The full MSDL grammar can be found [here](https://github.com/socadk/MDSL/blob/master/dsl-core/org.mdsl/src/org/mdsl/APIDescription.xtext) (*note:* still a private repo). <!-- TODO 2020 update when released -->


## Example

The following exemplary API specification compiles against the [MDSL grammar](https://github.com/socadk/MDSL/blob/master/dsl-core/org.mdsl/src/org/mdsl/APIDescription.xtext) sketched above: 

<!-- TODO feature new role keyword (if we keep it)? -->

~~~
API description SampleCustomerManagementAPI version "1.0.0"
usage context PUBLIC_API for FRONTEND_INTEGRATION 

endpoint type CustomerManagementContract 
  version "1.0.0" 
  serves as INFORMATION_HOLDER_RESOURCE
  exposes 
  	operation lookupSingleCustomer version "1.0.1"
	  with responsibility RETRIEVAL_OPERATION in REQUEST_REPLY conversation
	  expecting 
		payload ID<string> 
	  delivering  
		payload {"customerId":ID<int>,
		          "name":D,
		          "address"} 
				
  	operation lookupCustomerDirectory // no version information
	  with responsibility RETRIEVAL_OPERATION in REQUEST_REPLY conversation
	  expecting 
		payload <<Request_Bundle>> "customerId":ID<int>+ // at least one
	  delivering
		payload
		  "customerRecord": { 
			"cid":ID!, // ! mandatory, exactly one
			"nameTuple":("firstname":D, "lastname":D), 
			"addressTuple":(
			  "street":D<string>, 
			  "poBox":D?, // optional
			  "zipCode":D, 
			  "city":D)+,
			"segment":("REGULAR":D|"VIP":D) // choice 
		}* // zero or more
~~~

<!-- TODO convert this to Swagger etc. -->

The described API `SampleCustomerManagementAPI`supports one endpoint type `CustomerManagementContract` that exposes two operations `lookupSingleCustomer`, `lookupCustomerDirectory` to retrieve some customer data in a Customer Relationship Management (CRM) scenario. While request messages (`expecting`) are mandatory, response messages (`delivering`) are optional.

The specificaiton of the message exchange pattern is optional; permitted values are `REQUEST_REPLY`, `ONE_WAY` and `NOTIFICATION`. Request-reply operations must have a request and a response message; one way operations only have a requests message.

Several [Microservice API Patterns (MAPs)](https://microservice-api-patterns.org/) are used to annotate endpoint, operation, and one representation element (`INFORMATION_HOLDER_RESOURCE`, `RETRIEVAL_OPERATION`).[^2] Moreover, foundation patterns from MAP may comment on the usage scenario for the described API; this is optional (here: `PUBLIC_API` for `FRONTEND_INTEGRATION`). <!-- TODO MAP decorators are available as enum but you can also use any "STRING" (rationale: CML2MDSL) -->

[^2]: The notion of API roles and responsibilities as well as the term *Information Holder* have their roots in [Responsibility-Driven Design (RDD)](http://www.wirfs-brock.com/Design.html).

The first operation `lookupSingleCustomer` requires the API client to send an ID in the request message to be able to return the corresponding customer record. Its response message format lists three atomic parameters (`customerId`, `name`, `address`). 

The second operation `lookupCustomerDirectory` expects at least one customer id (`+`) so that multiple records can be returned in one response (`*`), which implements the [Request Bundle](https://microservice-api-patterns.org/patterns/quality/dataTransferParsimony/RequestBundle) pattern in MAP. This is indicated with the `<<Request_Bundle>>` stereotype. These pattern stereotypes merely serve as markers; tools such as code generators can use them to fine-tune their output.  

The data contract of the `lookupSingleCustomer` operation is fully specified, whereas part of the message elements in the response of  `lookupCustomerDirectory` are still incomplete (for instance, `"zipCode":D`).[^3]

[^3]: If an operation does not expect or deliver any payload, this can be expressed with the help of the empty/null type `D<void>`. This type should only be used for this purpose. <!-- it can also be used to model enums (tbd) -->

For more explanations of the message structures, see [data contracts (schemas)](./datacontract).


### Security and Error Reporting

Response messages can indicate which type of error information might replace/accompany it, and operations can define a security policy: 

~~~
endpoint type HelloWorldPlusEndpoint
exposes 
  operation sayHello in REQUEST_REPLY conversation
    expecting payload D<string>  
    delivering payload SampleDTO
      // message level status report:
      reporting error "code": D<int> 
    // operation level:
	protected by policy {"userId":D<string>, "password":D<raw>} 
~~~

In this example, the error report is a simple numeric `code`; more elaborate [error reports](https://microservice-api-patterns.org/patterns/quality/qualityManagementAndGovernance/ErrorReport) can be modeled as well; any MDSL [data type](/datacontract) can be used.

The security `policy` also is modelled as an MDSL data contract; it can be used to define the various security assertions and protocol headers that exist. This MDSL feature is still under design construction; future versions of the MDSL documentation pages will provide more exmaples.

<!-- TODO 2020 analysis report not featured yet -->


## Technology Mappings

### RESTful HTTP (a.k.a. HTTP resource APIs)
Endpoints correspond to resources (with the mapping not being straightforward due to concepts such as URI templates and path parameters in HTTP). Operations correspond to HTTP verbs or methods (with additional constraints being imposed by the architectural style and nest practices for REST).

<!-- link to [FM's paper](https://www.fabriziomontesi.com/files/m16.pdf), to Subbu A.'s Cookbook? -->

### Web Services Description Language (WSDL)
MDSL endpoints map to port types in [WSDL](https://www.w3.org/TR/2001/NOTE-wsdl-20010315); operations (not surprisingly) to operations. API providers are pendants to ports in WSDL, API clients are service consumers. 

<!-- online WSDL test tool: https://www.wsdl-analyzer.com/ -->

### Jolie
The service contract grammar can easily be mapped to the [glossary of Jolie terms](https://github.com/jolie/docs/blob/master/glossary.md). For instance, endpoint types in MDSL correspond to interfaces in Jolie: 


| Jolie | MDSL/MAP | Comments |
|-------|-----|----------|
| Operation | Operation | n/a  |
| Interface | Endpoint type | n/a  |
| tbd | API (multiple endpoint types) | n/a  |
| Port (inbound) | API provider | n/a |
| Port (outbound) | API client | n/a  |
| (Micro-)Service | (Micro-)Service | exposes one or more APIs with one or more operations |
| Conversation | Conversation | to be checked |
| Service definition | (service implementation) | n/a  | 
| Service network | tbd | n/a  | 
| Cell and related concepts | not in scope | n/a  | 

<!-- not mapped (yet): Connection, Behavior, Process, Service Dependency, Network boundary, Cell boundary, Cell overlay -->

### Other integration technologies 

MSDL service contracts can also be mapped to gRPC, GraphQL, and Avro in a straightforward manner. Stay tuned! 

## Links

[Data contracts (schemas)](./datacontract) and optional [runtime language concepts](./optionalparts).

[Quick reference](./quickreference).

Back to [MDSL homepage](./index).

*Copyright: Olaf Zimmermann, 2018-2020. All rights reserved. See [license information](https://github.com/socadk/MDSL/blob/master/LICENSE).*

<!-- *EOF* -->
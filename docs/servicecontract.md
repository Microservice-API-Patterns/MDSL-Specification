---
title: Microservice Domain Specific Language (MDSL) Service Contracts
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2021.  All rights reserved.
---

[Home](./index) &mdash; [Data Types](./datacontract) &mdash; [Bindings](./bindings) &mdash; [Provider and Client](./optionalparts) &mdash; [Tutorial](./tutorial) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools)

Service Endpoint Contracts in MDSL
==================================


## Use Cases for MDSL Specifications 

* Creation, review and evolution of elaborate [API Descriptions](https://microservice-api-patterns.org/patterns/foundation/APIDescription) as featured in Microservice API Patterns (MAP), in forward engineering:
  * A context-sensitive, Eclipse-based editor for MDSL, developed with [Xtext](https://www.eclipse.org/Xtext/), is available [here](./updates/)
  * Generators for platform-specific technologies such as OpenAPI (f.k.a. Swagger), WSDL/SOAP (via Jolie), gRPC, see [here](./tools/)  <!-- GQL: prototype (not fully tested yet) -->
  * Command Line Interface (CLI) for the generators, see [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/dsl-core/io.mdsl.cli) <!-- emerging: Web application -->
* Reverse engineering:
  * Discover contracts, clients, providers in existing systems 
  * Analysis support (metrics)

<!--
Requirements/principles (wouldn't it be nice?): 

* Be quick, be human readable w/o tools
* Abstract from WSDL, OAS, JSON, XSD w/o loosing detail/PIM specifics
-->

## Concepts 
The contract syntax (grammar) of MDSL is inspired by the API domain model from Microservice API Patterns (MAP): An API description features one or more *endpoints*, which expose *operations* that expect and deliver *messages*.[^99]: 

[^99]: This domain model is published in Section 3 of the [EuroPLoP 2019 paper on Patterns for API Evolution from the MAP team](http://eprints.cs.univie.ac.at/6082/1/WADE-EuroPlop2019Paper.pdf).

~~~
ServiceSpecification: 
    'API' 'description' name=ID
    ('version' svi=SemanticVersioningIdentifier)? // plain STRING
    ('usage' 'context' reach=Visibility ('for' direction+=DirectionList)?)?
	('overview' description=STRING)?
	types+=DataContract*
	contracts+=EndpointContract+
	providers+=Provider*
	clients+=Client*
	gateways+=Gateway*
	orchestrations+=Orchestration* 
	scenarios+=IntegrationScenario* 
	('IPA')?;

EndpointContract:
	'endpoint' 'type' name=ID 
	('version' svi=SemanticVersioningIdentifier)? // plain STRING
	('serves' 'as' primaryRole=ResourceRole 
	    ('and' otherRoles+=ResourceRole)* 'role'?)? // pattern decorator
	('identified' 'by' pathParameters=ElementStructure)? 
	('exposes' ops+=operation+)?;
	('receives' events+=Event+)?

Operation:
	'operation' 
	name=ID 
	('version' svi=SemanticVersioningIdentifier)?
	('with' 'responsibility' responsibility=OperationResponsibility)? // pattern decorator
	('in'  mep=MessageExchangePattern 'conversation')? 
	('expecting' requestMessage=DataTransferRepresentation)? 
	('delivering' responseMessage=DataTransferRepresentation 
	  ('links' relations+=RelationshipLink+)? // optional within 'delivering'
	  ('reporting' reports=StatusReports)? // optional within 'delivering'
	)? // response is optional 
	('transitions' st=StateTransition)? 
	('emitting' events+=Event+)?  
	('compensated' 'by' undo=[Operation])?  
	('protected' 'by' policies=SecurityPolicies)?  

DataTransferRepresentation:
	('headers' headers=ElementStructure)? 
	'payload' payload=ElementStructure
	('structured' 'as' ts=TypeSystem)?;
~~~

The notation used above is the [grammar language of Xtext](https://www.eclipse.org/Xtext/documentation/301_grammarlanguage.html) (which is close to that of antlr4). The full MSDL grammar can be found [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext).

Endpoint types correspond to ports in the Hexagonal Architecture terminology; adapters are represented by technology [bindings](./bindings) <!-- [O] generate hexagon drawing --> The request and response messages consist of headers and payloads, whose content is modelled with MDSL [data transfer representations](./datacontract).

*Note:* The above MDSL grammar also has language concepts to model compensating operations, states and their transitions, event emission and reception, and security policies. These features are still under design and development. The [MDSL Primer](./primer) shows them, and sample specifications featuring these advanced concepts can also be found [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/examples-advanced/AdvancedServiceContractConcepts1.mdsl) and [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/examples-advanced/AdvancedServiceContractConcepts2.mdsl). Please view these grammar extensions as [technology previews](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview) subject to change in future versions.

## Example

The following exemplary API specification compiles against the [MDSL grammar](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext) sketched above: 

<!-- TODO feature optional "role" keyword (?) -->

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

<!-- TODO convert this to OAS/Swagger etc. -->

The described API `SampleCustomerManagementAPI`supports one endpoint type `CustomerManagementContract` that exposes two operations `lookupSingleCustomer`, `lookupCustomerDirectory` to retrieve some customer data in a Customer Relationship Management (CRM) scenario. While request messages (`expecting`) are mandatory, response messages (`delivering`) are optional.

The specification of the message exchange pattern is optional; permitted values are `REQUEST_REPLY`, `ONE_WAY` and `NOTIFICATION`. Request-reply operations must have a request and a response message; one way operations only have a requests message.

Several [Microservice API Patterns (MAPs)](https://microservice-api-patterns.org/) are used to annotate endpoint, operation, and one representation element (`INFORMATION_HOLDER_RESOURCE`, `RETRIEVAL_OPERATION`).[^2] Moreover, foundation patterns from MAP may comment on the usage scenario for the described API; this is optional (here: `PUBLIC_API` for `FRONTEND_INTEGRATION`). The MAP decorators are supported as explicit enum values in the grammar; you can also use any "STRING". <!-- (rationale: CML2MDSL).-->

[^2]: The notion of API roles and responsibilities as well as the term *Information Holder* have their roots in [Responsibility-Driven Design (RDD)](http://www.wirfs-brock.com/Design.html).

The first operation `lookupSingleCustomer` requires the API client to send an ID in the request message to be able to return the corresponding customer record. Its response message format lists three atomic parameters (`customerId`, `name`, `address`). 

The second operation `lookupCustomerDirectory` expects at least one customer id (`+`) so that multiple records can be returned in one response (`*`), which implements the [Request Bundle](https://microservice-api-patterns.org/patterns/quality/dataTransferParsimony/RequestBundle) pattern in MAP. This is indicated with the `<<Request_Bundle>>` stereotype. These pattern stereotypes merely serve as markers; tools such as code generators can use them to fine-tune their output.  

The data contract of the `lookupSingleCustomer` operation is fully specified, whereas part of the message elements in the response of  `lookupCustomerDirectory` are still incomplete (for instance, `"zipCode":D`).[^3]

[^3]: If an operation does not expect or deliver any payload, this can be expressed with the help of the empty/null type `D<void>`. This type should only be used for this purpose. <!-- TODO tbd: it can also be used to model enums -->

For more explanations of the message structures, see [data contracts (schemas)](./datacontract).


### Security and Error Reporting

Response messages can indicate which type of error information might replace/accompany it, and operations can define a security policy: 

~~~
endpoint type HelloWorldPlusEndpoint
exposes 
  operation sayHello in REQUEST_REPLY conversation
    expecting payload D<string>  
    delivering payload SampleDTO
      // message-level status report:
      reporting error "400": D<int> // bad request (HTTP error code) 
  // operation-level security policy:
  protected by policy "HTTPBasicAuthentication":MD
~~~

In this example, the error report is a simple numeric code (`400`); elaborate [error reports](https://microservice-api-patterns.org/patterns/quality/qualityManagementAndGovernance/ErrorReport) can be modeled as well, as any MDSL [data type](./datacontract) can be used. 

<!-- TODO 'analysis' keyword for reporting not featured yet (only 'error') -->

The security `policy` also is modelled as an MDSL data contract; it can be used to define the various security assertions and protocol headers that exist (for instance, basic authentication in HTTP, as explained in the [OpenAPI specification](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#securitySchemeObject)). *Important note*: This MDSL feature also still is under design and construction (view it as an incomplete [technology preview](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview)); future versions of the MDSL documentation pages will provide more examples.

*Note*: These two MDSL features and tool support for them still are under development; future versions of the MDSL documentation pages will provide more examples.

## Technology Mappings

See [this page](technology-mappings) for information on how MDSL maps to OpenAPI and REST, WSDL/SOAP, and gRPC.


# Site Navigation

* Language specification: 
    * Service [endpoint contract types](./servicecontract) (this page) and [data contracts (schemas)](./datacontract)
    * [Bindings](./bindings) and [instance-level concepts](./optionalparts)
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* Back to [MDSL homepage](./index)

*Copyright: Olaf Zimmermann, 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
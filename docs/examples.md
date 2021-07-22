---
title: Microservice Domain Specific Language (MDSL) Examples
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019. All rights reserved.
---

[Home](./index) &mdash; [Endpoint Type](./servicecontract) &mdash; [Data Type](./datacontract) &mdash; [Provider and Client](./optionalparts) &mdash; [Bindings](./bindings) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools)

<!-- TODO (H) update for 5.3 and merge/align with primer? -->

MDSL Examples
=============

## Service Contract (Grammar Reference)
The following exemplary API specification compiles against the [MDSL grammar](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext): 

<!-- TODO feature error reporting and versioning of data types -->

~~~
API description SampleCustomerManagementAPI
usage context PUBLIC_API for FRONTEND_INTEGRATION 

endpoint type CustomerManagementContract
  version "1.0.0"
  serves as INFORMATION_HOLDER_RESOURCE
  exposes 
  	operation lookupSingleCustomer 
	  with responsibility RETRIEVAL_OPERATION 
	  expecting 
		payload ID<string> 
	  delivering  
		payload {"customerId":ID<int>,
		          "name":D,
		          "address"} 
				
  	operation lookupCustomerDirectory
	  with responsibility RETRIEVAL_OPERATION
	  expecting 
		payload <<Request_Bundle>> "customerId":ID<int>+ 
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

<!-- some text from service contract page could be copied or moved here -->

## Data Contract Examples

The following simple examples feature the structural language primitives and give instantiation examples (in the comments):

~~~
data type SomeAtomicParameter D 
// yields any string or numeric literal: "A", 1, true

data type SomeNumber D<int> 
// yields 1

data type SomeStructuredRecord {SomeFlatRecord, SomeNumber} 
// yields { ("A", 1, true), 42 }

data type TwoNestingLevels {SomeStructuredRecord, SomeAtomicParameter} 
// yields { {("A", 1, true), 42}, "A" }

data type SomeFlatRecord (D<string>, D<int>, D<bool>) 
// yields ("A", 1, true)

data type ChoiceDemo {"optionA":D | "optionB":D} // not implemented in tools yet
~~~

More examples can be found in in the [public](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples) <!-- and in the [private](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples) --> MDSL repository.

<!-- TODO (M) feature RESTBucks here -->

## Links


* API usage [scenarios and stories](scenarios.md) (experimental)
* Orchestration [flows](flows.md) (experimental)
* Service [endpoint contract types](./servicecontract)
* [Data contracts (schemas)](./datacontract)
* [Bindings](./bindings)
* Optional [language elements on the instance level (provider, client, gateway)](./optionalparts)
* [Tutorial](./tutorial), another [example](./examples)
* [Quick reference](./quickreference)
* [Tool information (CLI, editor/linter)](./tools)
* [SOAD transformations](./soad)

*Copyright: Olaf Zimmermann, 2018-2021. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
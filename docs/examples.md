---
title: Microservice Domain Specific Language (MDSL) Examples
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2022. All rights reserved.
---

[Home](./index) &mdash; [Endpoint Type](./servicecontract) &mdash; [Data Type](./datacontract) &mdash; [Provider and Client](./optionalparts) &mdash; [Bindings](./bindings) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools)


MDSL Examples
=============

## Service Contract (Grammar Reference)
The following exemplary API specification compiles against the [MDSL grammar](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl/src/io/mdsl/APIDescription.xtext): 

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
	  reporting error NotFound
	  protected by policy RequiresReadRights
~~~

<!-- TODO (future work) feature state transitions and compensation (requires two SCO/STO) -->

## Data Contract Examples

The following simple examples feature the structural language primitives and give instantiation examples (in the comments):

~~~
data type SomeAtomicParameter D 
// specifies any string or numeric literal: "A", 1, true

data type SomeNumber D<int> default is "7"
// specifies numeric literal

data type SomeStructuredRecord {SomeFlatRecord, SomeNumber} 
// specifies { ("A", 1, true), 42 }

data type TwoNestingLevels {SomeStructuredRecord, SomeAtomicParameter} 
// specifies { {("A", 1, true), 42}, "A" }

data type SomeFlatRecord version "1.0.0" {D<string>, D<int>, D<bool>}
// specifies {"A", 1, true}

data type SomeFlatRecordAsAPL (D<string>, D<int>, D<bool>) 
// specifies ("A", 1, true)

// not implemented specifically in tools yet (treated as Parameter Tree):
data type ChoiceDemo {"optionA":D<int> | "optionB":D<string>} 
// specifies { 42 } or { "someText" }
~~~


More examples can be found in in the [public](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples) <!-- and in the [private](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples) --> MDSL repository.


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

*Copyright: Olaf Zimmermann, 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
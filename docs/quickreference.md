---
title: Microservice Domain Specific Language (MDSL) Quick Reference
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2020. All rights reserved.
---

MDSL Grammar Quick Reference (Skeletons)
========================================


## Service Contract Skeleton
One can start from this, e.g., by copy-pasting into a text editor or [the Eclipse editor](./updates) that Xtext generates from the MDSL grammar. `[...]` are placeholders to be replaced with correct MDSL:

<!-- could feature "role" keyword introduced in V3 (optional) -->

~~~
API description [name]
usage context [visibility] for [direction] // MAP pattern tags (optional)

data type [name] [...] // reusable data contract elements (optional) 

endpoint type [name]  
  version x.y.z // semantic versioning information (optional) 
  serves as [role_enum] // MAP tag(s) (optional)
  exposes 
  	operation [name]
	  with responsibility [resp_enum] // MAP tag (optional)
	  expecting
	    headers [...] // optional 
		payload [...] // mandatory, e.g., {V}
	  delivering  
	    headers [...] // optional
		payload [...] // mandatory in request-response exchanges
	    reporting 
	  	[...] // see bottom of page for explanation (optional)
	protected by [...] // see bottom of page for explanation (optional)
~~~

### Usage Context 
The valid values for API *visibility* are: 
> PUBLIC_API | COMMUNITY_API | SOLUTION_INTERNAL_API

The API type or *direction* can be: 
> FRONTEND_INTEGRATION | BACKEND_INTEGRATION 

### Roles and Responsibilities 
MAP defines the following *roles* (of endpoint types): 
> PROCESSING_RESOURCE | INFORMATION_HOLDER_RESOURCE | DATA_TRANSFER_RESOURCE | LINK_LOOKUP_RESOURCE 
> OPERATIONAL_DATA_HOLDER | MASTER_DATA_HOLDER | REFERENCE_DATA_HOLDER

And the *responsibilities* (of operations) are: 
> COMPUTATION_FUNCTION | STATE_CREATION_OPERATION | RETRIEVAL_OPERATION |  STATE_TRANSITION_OPERATION

All these enum values correspond to Microservice API Patterns (MAP); go to the [pattern index](https://microservice-api-patterns.org/patterns/index) for quick access.


## Data Contract Skeletons (for Operation Signatures)
Data types can be modelled under `data type` and then referenced in headers, payloads, and reports within operations (`expecting`, `delivering`, `reporting`) in the above skeleton. They can also be inlined directly in the operation definitions.

<!--
Type systems that can be listed: 
> MAP_TYPE | JSON_SCHEMA | XML_SCHEMA | PROTOCOL_BUFFER | AVRO_SCHEMA | THRIFT_TYPE | OTHER 
-->

Skeleton data contracts for `headers` and `payload` elements and `data type` definitions are:

* [Parameter Tree](https://microservice-api-patterns.org/patterns/structure/representationElements/ParameterTree.html) (ptree): `{ {subtree1}, {subtree2}, ap1, ap2, ...}`, can appear in pforest
* [Atomic Parameter](https://microservice-api-patterns.org/patterns/structure/representationElements/AtomicParameter.html) (ap), can appear in ptree and apl: `<<pattern>>"name": Role<Type>` (see below for explanations) 
* [Atomic Parameter List](https://microservice-api-patterns.org/patterns/structure/representationElements/AtomicParameterList.html) (apl): `(ap1, ap2, ...)`, can appear in ptree (note: can and should be modeled as a flat ptree)
* [Parameter Forest](https://microservice-api-patterns.org/patterns/structure/representationElements/ParameterForest.html) (pforest): `[ {ptree1}; {ptree2}; ... ]`, can only appear on top level of operation signature


### Atomic Parameter syntax

The `ap` from the above contract skeleton resolves to `<<pattern>>"name": Role<Type>`. A first example featuring all parts hence is `<<API_Key>>"accessToken1":D<string>`. 

Pattern stereotype and type information are optional; either the role or the name of the representation element have to be specified. Theefore, more compact examples are `ID`, `D<bool>`, `L<string>`, and `"resultCounter":MD<int>`.

Furthermore, an abstract, unspecified element can be represented as `P` (for parameter or payload part). It must not contain a stereotype or  role and type information (but can have an identifier). 

### `<<pattern>>` Stereotype (optional)
The `<<pattern>>` stereotype can take one of the following values from the [Microservice API Patterns (MAP)](https://microservice-api-patterns.org/introduction) pattern language: 

> API_Key | Context_Representation | Request_Bundle | Request_Condition 
> Wish_List | Wish_Template | Pagination | Error_Report 
> Embedded_Entity | Linked_Information_Holder | Annotated_Parameter_Collection |
> Data_Element | Metadata_Element | Identifier_Element | Link_ELement
> Control_Metadata | Aggregated_Metadata | Provenance_Metadata 

`Data_Element` is the default; `L` is a shorthand for `<<Link_ELement>> D`, `ID` is short for `<<Identifier_ELement>> D`, and `MD` is short for `<<Metadata_ELement>> D`. 

A pattern stereotype can also be assigned to other tree nodes (apls and ptrees). This is optional.

### `"name"` Identifier
An identifier can, but does not have to be defined (if the role information is present). It appears in double quotes. At present, names must start with a character and must not contain blanks. Special characters such as `-` are not permitted (yet).

### `Role` Element role stereotypes
The roles match the four [element stereotype patterns in MAP](https://microservice-api-patterns.org/patterns/structure/):

> D(ata) | MD (Metadata) | ID(entifier) | L(ink) 

Data corresponds to the [Data Element](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement) pattern; the other mappings are straightforward as well.  

### `<Type>` Base types 
Finally, the base types are:

> bool | int | long | double | string | raw | void

<!-- You can also use any `STRING`, but in that case MDSL tools cannot do much with the specification (this might, for instance, be useful in early conceptualization work). -->
 

### Cardinalities (Multiplicity)

MDSL does not know an array construct as known from the type system in most programming languages. Cardinalities are marked with single-byte flags instead (as also used to define [regular expressions](https://en.wikipedia.org/wiki/Regular_expression)):

* `?` means that an element is optional (i.e., none or one instance may appear)
* `*` represents "any" (i.e., zero or more)
* `+` indicates that at least one instance of the specified element must appear (i.e., one or more)

The cardinality markers can be applied to Atomic Parameters, Atomic Parameter Lists,[^1] and Parameter Trees. Two examples are `"listOfIntegers":D<int>*` and `"optionalInformation": {"part1":D, "part2":D}?`. If there is no suffix, the default is `!` (exactly one, mandatory).

[^1]: Note that the Atomic Parameter List, introduced above, actually is closer to a sequence or a map in programming languages.

*Note:* The marker might be a bit hard to notice, especially when deeply nested structured are modeled. You can increase readability by introducing external data type definitions (see [data contracts](./datacontract) page). 

### Variability (Choice)

In the definition of a Parameter Tree `{...|...}` and an Atomic Parameter List `(...|...)`, you can express optionality: 

* `|` choice 

An example is `data type AnIntegerOrAString {D<int> | D<string>}`.


## Reporting 
MDSL has an specific construct for error handling such as fault elements or response codes (still *experimental*):


Add the following snippet to the specification of response messages (behind `delivering`):

~~~
reporting 
    error [...]
~~~

The placeholder `[...]` resolves to a data contract (see above). The simplest one is `P`. Some more advances examples are: 

* `error "BadRequest": {D<string>}`, 
* `error <<Error_Report>>"resourceNotFound": {"errorCode":D<int>, "errorMessage":D<string>}+`, and 
* `error <<Error_Report>>{("code402":D<int>, "notAuthorized":D<string>) | ("code403":D<int>, "anotherMessage":D<string>)}`.

<!-- TODO tbd: feature `analytics`? move examples to service contract page? -->

The report elements can be modeled as data types as described under [data contracts (schemas)](./datacontract). Examples are: 

* `error "soapFault": SOAPFaultElement`, with a previous definition:
* `data type SOAPFaultElement {"code":D<int>, "string": D<string>, "actor":D<string>, "detail":D<string>}`


## Security Policy
Finally, a security policy can be specified for each operation: 

* `protected by policy "UserIdPassword":{"userId":ID<string>, "password":MD<string>}`

The report elements can be modeled as data types as described under [data contracts (schemas)](./datacontract) as well.


## Links

More on service [endpoint contract types](./servicecontract), [data contracts (schemas)](./datacontract), [bindings](./bindings) and [instance-level constructs](./optionalparts).
[Tutorial](./tutorial) and [tools](./tools). Back to [MDSL homepage](./index). 

*Copyright: Olaf Zimmermann, 2018-2020. All rights reserved. See [license information](https://github.com/socadk/MDSL/blob/master/LICENSE).*

<!-- *EOF* -->
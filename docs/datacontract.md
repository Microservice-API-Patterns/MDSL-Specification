---
title: Microservice Domain Specific Language (MDSL) Data Contracts
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2020. All rights reserved.
---

Data Contracts and Schemas in MDSL
==================================

## Use Cases for MDSL Data Type Models

MDSL aims at supporting agile modeling. Any service API exposes a published language of some kind This language contains data elements in several places:

* Payload and headers of operations specified in an MDSL service [endpoint contract](./servicecontract)
* Service Level Objectives (SLOs) and/or Service Level Indicators (SLIs) that are part of [Service Level Agreements (SLAs)](https://microservice-api-patterns.org/patterns/quality/qualityManagementAndGovernance/ServiceLevelAgreement)
* Data Transfer Objects (DTOs) or, more precisely, Data Transfer Representations (DTRs) 


## Concepts

The structure patterns from MAP form the base of the type system that is used in MDSL. *Identifier-Role-Type (IRT)* triples `"aName":D<String>`  for [Atomic Parameters](https://microservice-api-patterns.org/patterns/structure/representationElements/AtomicParameter) (only the role is mandatory):
    
* The optional *identifier* `"aName"` corresponds to the variable names in programming languages and data representation languages such as JSON.
* The *role* can be any [element stereotype](https://microservice-api-patterns.org/patterns/structure/) from MAP: `D` (data), `MD` (metadata), `ID` (identifier), `L` (link).
* The optional *type* `<String>` is either basic (see below) or nested/structured; it also corresponds to a concept known from type systems in programming and data representation languages.

Simple, yet powerful nesting is supported (as known from data representation languages such as JSON):

* The nesting is expressed in an object- or block-like syntax: `{...{...}}`.
* This nesting realizes the Microservice API Pattern (MAP) [Parameter Tree](https://microservice-api-patterns.org/patterns/structure/representationElements/ParameterTree).

Already existing metamodels and schema languages can be used alternatively to `MAP_TYPES`. Examples are: `JSON_SCHEMA`, `XML_SCHEMA`, and `PROTOCOL_BUFFER`. 

<!-- TODO (L) grammar also has `AVRO_SCHEMA` | `THRIFT_TYPE` | 'GRAPHQL_SDL' | 'OTHER' -->

MDSL specifications do not have to be complete to be useful (e.g., in early stages of service design); tools are expected to check completeness, use defaults for missing parts, etc.

### The I in IRT: Identifiers

Identifiers must be embedded in double quotes. They may contain blanks or underscores. 

### The R in IRT: Role stereotypes for representation elements/data types
The role within a message payload that is played/taken by a particular part of a header or payload (a representation element in MAP terminology) is the primary specification element; identifiers and data type are optional. This three-part specification (with only one mandatory part) is quite different from the identifier-type pairs typically used in programming languages. It makes it possible to create rather compact (but still incomplete) specifications during agile API modeling. <!-- talk about rationale for this modeling decision even more? --> 

An abstract, unspecified element is represented as `P` (for parameter or payload placeholder). `P`  takes the place of the Role-Type elements in the IRT triple introduced above. 

Concrete atomic type refinements of `P`, matching the [element stereotypes](https://microservice-api-patterns.org/patterns/structure/) in MAP, are: 

* `Data` or `D`, representing a plain/basic data/value role. `D` corresponds to [Data Element](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement) in MAP.
* `Identifier` or `ID` for identifiers, MAP: [Id Element](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/IdElement).
* `Link` or `L` for link identifiers (which are network-accessible, e.g. URI, or URN), MAP: [Link Element](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/LinkElement).
* `Metadata` or `MD` representing metadata, MAP: [Metadata Element](https://microservice-api-patterns.org/patterns/structure/elementStereotypes/MetadataElement).

`D<void>` may represent a generic, unspecified parameter (just like `P`).

### The T in IRT: Types

#### Base Types

The role stereotypes can be combined with the following base types to yield precise specifications of atomic parameters: `bool`, `int`, `long`, `double`, `string`, `raw`, and `void`. So `D<int>` is an integer data value and `D<void>` is an empty/non-existing payload part/parameter. 

#### Complex Types

See explanations above (under "simple, yet powerful nesting is supported"). 

<!-- TODO (M): the "quick reference" cheat sheet that used to be in grammar/examples in V2 would fit here -->

### Collections and optionality 

`*`, `?`, and `+` turn a type definition into a collection (`*`: zero or more, `?`: one or none, `+` : at least one). The default is `!` (exactly one); it does not have to be modeled. 

Parameter trees and atomic parameter lists can be used to express optionality if `|` (is used rather than `,`).

### Reuse of data type definitions (in multiple representation elements)

~~~
dataContract:
    'data' 'type' name=ID 
    ('version' svi=semanticVersioningIdentifier)? 
    structure=elementStructure;
    default=defaultValue?

elementStructure: 
    pf=parameterForest | pt=parameterTree 
  | apl=atomicParameterList  | np=singleParameterNode;

[...]

TreeNode:
    spn=singleParameterNode | apl=atomicParameterList | pt=parameterTree;

SingleParameterNode: 
    genP=genericParameter | atomP=atomicParameter | tr=typeReference;
~~~

The semantic versioning identifier `svi` is a simple `STRING`; at present, the entire API as well as data types, endpoints and operations can be versioned. 

### Default values

Only reusable, explicitly defined data types can have default values (still experimental): 

~~~
data type SampleDTO {ID, D} default is "{42, 'TODO'}"
~~~

## Examples

The following example features a partial specification of nested customer information as the roles/types of all "three plus three" representation elements yet unknown:

~~~
data type MoveHistory {"from", "to", "when"}  
data type CustomerWithAddressAndMoveHistory { 
    "CustomerCoreData", 
    "AddressRecords", 
    MoveHistory* // type reference
} 
~~~

Alternatively, one can start with [element stereotypes](https://microservice-api-patterns.org/patterns/structure/) and pure structure instead of element names: 

~~~
data type MoveHistory {D, D, D}  // record, modeled as Parameter Tree
data type CustomerWithAddressAndMoveHistory { 
    D, 
    D, 
    MoveHistory* // type reference
} 
~~~

Once some more analysis work has been done, the specification can be refined, but still remain incomplete (as `"CustomerCoreData":D` does not say anything about the inner structure of the entity value):

~~~
data type AddressRecord (
    "street":D<string>, 
    "zipCode":D<int>, 
    "city":D<string>) // Atomic Parameter List in '()' syntax

data type MoveHistory 
    {"from":AddressRecord, "to":AddressRecord, "when":D<string>} 

data type CustomerWithAddressAndMoveHistory { 
    <<Entity>>"CustomerCoreData":D, 
    "AddressRecords":AddressRecord+, // one or more
    "MoveHistory": MoveHistory* // type reference, collection
} 
~~~

Note that a parameter tree that only contains atomic parameters `{D<int>, D<string>}` can also be modeled as an Atomic Parameter List `(D<int>, D<string>)`. It is recommended to prefer the Parameter Tree syntax over that of the Atomic Parameter List (to promote information hiding and defer detailed modeling decisions until the last/most responsible moment). `<<Entity>>` is a patttern stereotype (see section "Outlook: MAP Decorators" of the [CSV tutorial](./tutorial) for explanations).

<!-- TODO (H): * feature choice '|' -->

<!-- TODO feature CSV in new Tutorial "sheets": ["rows":{"columnCells":{Data|Formula}*}*] // title row is different? -->

<!-- TODO model this in JSON Schema and XML Schema (and/or Avro?) -->


## Technology Mappings

### JSON/JSON Schema

| JSON | MDSL | Comments |
|------|------|----------|
| Basic JSON data types | Atomic Parameter | Base types do not match 100% |
| Object (flat) | Parameter Tree (flat) or Atomic Parameter List | Parameter Tree preferred |
| Object (structured) | Parameter Tree (nested) | Straightforward |
| Array | Cardinality of `*` or `+` | Is homogeneous in MDSL/MAP |

### XML Schema

| XML Schema | MDSL/MAP | Comments |
|-----|-----|----------|
| Built-in data types | Atomic Parameter | Not the same expressiveness |
| Sequence element (referencing built-in types) | Parameter Tree (flat) or Atomic Parameter List | Parameter Tree preferred |
| Complex type | Parameter Tree | MDSL syntax more compact  |
| Sequence with `maxoccurs` > 1 | Cardinality of `*` or `+` | n/a | 

### gRPC and Protocol Buffers
The MAP base types can be mapped in a straightforward manner. `AnyType` is used as default.

<!--
The base types in MDSL map to gRPC and Protocol Buffers like this: 

| Protocol Buffers | MDSL | 
|-------|------|
| int32 | int |
| int64 | long |
| double | double |
| bool | bool |
| bytes | raw |

TODO tbc (complete mapping?)
-->

Parameter forests and parameter trees translate into nested *messages*.

<!-- An example can be downloaded [here](./Test0APIGrpcPb.proto). -->

<!--
### GraphQL 

To be continued (tbc).
-->

<!--
~~~
type Query {
    ping(in_dtr: String): String

    sayhelloAgain(in_dtr: sayhelloAgainRequestType): sayhelloAgainResponseType 
}

type Mutation {

    sayhello(in_dtr: sayhelloRequestType): sayhelloResponseType 

}

input sayhelloRequestType {aString: String!}
type sayhelloResponseType {identifier1: String!}

input sayhelloAgainRequestType {anInt: Int!}
type sayhelloAgainResponseType {stringList: [String]}
~~~
-->

<!--
### Avro

| Avro | MAP | Comments |
|-----|-----|----------|
| Basic data types | Atomic Parameter | n/a |
| tbc | Atomic Parameter List | n/a  |
| tbc | Parameter Tree | n/a  |
| tbc | Cardinality of `*` or `+` | n/a  | 
-->


### Jolie 
The MAP base types map to [simple data in Jolie](https://jolielang.gitbook.io/docs/basics/handling_simple_data) in a straightforward manner. 

The same holds for the mapping of parameter trees to [Jolie data structures](https://jolielang.gitbook.io/docs/basics/data_structures). 

An example can be downloaded [here](./HelloWorldAPIJolieInterfaceAndPort.ol). 


## Known Limitations 

Note that some combinations are syntactically possible at present (to simplify the grammar), but do not make much sense (or create ambiguity):

* Types such as `ID<bool>` can be modeled, but do not make much sense.
* Parameter trees may contain undesired cycles.
* External data types can also define an identifier, but do not have to: In `data type X "X":D<int>`, the second `X` is somewhat redundant and can be removed; `data type X D<int>` will do.

[Tools](./tools) such as API linters and model validators can report inappropriate specifications. 


## Links

* Language specification:
    * Service [endpoint contract types](./servicecontract) and [data contracts (schemas)](./datacontract) (this page).
    * [Bindings](./bindings) and [instance-level concepts](./optionalparts).
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* Back to [MDSL homepage](./index).

*Copyright: Olaf Zimmermann, 2018-2020. All rights reserved. See [license information](https://github.com/socadk/MDSL/blob/master/LICENSE).*

<!-- *EOF* -->
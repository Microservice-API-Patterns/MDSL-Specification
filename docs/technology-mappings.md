---
title: Microservice Domain Specific Language (MDSL) Technology Mappings
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2019-2021.  All rights reserved.
--- 

## Technology Mappings (Endpoint Level)

*Note:* This page contains incomplete, informal mappings. It is not part of the MDSL language specification, but intended to serves as background information for instance when working with the generator [tools]./tools).

### RESTful HTTP (a.k.a. HTTP resource APIs)
Endpoints correspond to resources (with the mapping not being straightforward due to concepts such as URI templates and path parameters in HTTP). Operations correspond to HTTP verbs or methods (with additional constraints being imposed by the architectural style and nest practices for REST).

<!-- link to [FM's paper](https://www.fabriziomontesi.com/files/m16.pdf), to Subbu Allamaraju's Cookbook? -->

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


### gRPC and Protocol Buffers
An endpoint type in MDSL corresponds to a gRPC service; MDSL operations correspond to gRPC messages.


### Other integration technologies 
MSDL service contracts can also be mapped to GraphQL, and Avro in a straightforward manner. Stay tuned! 


## Technology Mappings (Data Contract Level)


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


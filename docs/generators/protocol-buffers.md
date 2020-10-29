---
title: Microservice Domain Specific Language (MDSL) to Protocol Buffer Specifications
author: Stefan Kapferer
copyright: Stefan Kapferer and Olaf Zimmermann, 2020. All rights reserved.
---

Protocol Buffers Generator
==========================

The MDSL Eclipse plugin and the CLI allow API designers to generate [Protocol Buffer specifications](https://developers.google.com/protocol-buffers/) out of MDSL. 

## Usage
You can generate the specifications out of an MDSL model by using the [Eclipse plugin](./../tools#eclipse-plugin) or our [CLI](./../tools#command-line-interface-cli-tools).

In Eclipse you find the generator in the MDSL context menu:

<a href="./../media/eclipse-protocol-buffers-generator-context-menu.png">![Protocol Buffers Specification Generator Context Menu in Eclipse](./../media/eclipse-protocol-buffers-generator-context-menu.png)</a>

The following command generates a specification in case you work with the CLI:

```bash
./mdsl -i model.mdsl -g proto
```

_Hint:_ Both tools generate the output into the `src-gen` folder which is located in the projects root directory (Eclipse) or the directory from which the `mdsl` command has been called (CLI). Both tools create the directory automatically in case it does not already exist.

## Generator Output / Mapping
The generator maps the MDSL concepts to `*.proto` files as follows:

 * One message for each MDSL data type.
 * One service for each MDSL endpoint.
 * One RPC call inside the service for each endpoint operation.

## Example
The following example illustrates what the generator produces for an exemplary MDSL contract.

You find the complete sources (incl. generated `*.proto` file) of this example [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/protocol-buffers-example).

We use the following MDSL model which was an outcome of this [blogpost](https://ozimmer.ch/practices/2020/06/10/ICWEKeynoteAndDemo.html) to illustrate our generator outputs:

```
API description ReferenceManagementServiceAPI

data type PaperItemDTO { "title":D<string>, "authors":D<string>, "venue":D<string>, "paperItemId":PaperItemKey }
data type PaperItemKey { "doi":D<string> }
data type createPaperItemParameter { "who":D<string>, "what":D<string>, "where":D<string> }

endpoint type PaperArchiveFacade
  serves as INFORMATION_HOLDER_RESOURCE
  exposes
    operation createPaperItem
      with responsibility STATE_CREATION_OPERATION
      expecting
        payload createPaperItemParameter
      delivering
        payload PaperItemDTO
    operation lookupPapersFromAuthor
      with responsibility RETRIEVAL_OPERATION
      expecting
        payload D<string>
      delivering
        payload PaperItemDTO*
    operation convertToMarkdownForWebsite
      expecting
        payload PaperItemKey
      delivering
        payload D<string>
```

For the MDSL contract above the generator produces the following `*.proto` file:

```proto
syntax = "proto3";

package ReferenceManagementServiceAPI;

message PaperItemDTO {
  string title = 1;
  string authors = 2;
  string venue = 3;
  PaperItemKey paperItemId = 4;
}

message PaperItemKey {
  string doi = 1;
}

message createPaperItemParameter {
  string who = 1;
  string what = 2;
  string where = 3;
}

message lookupPapersFromAuthorRequestMessage {
  string anonymous1 = 1;
}

message PaperItemDTOList {
  repeated PaperItemDTO entries = 1;
}

message ConvertToMarkdownForWebsiteResponseMessage {
  string anonymous2 = 1;
}

service PaperArchiveFacade {
  rpc lookupPapersFromAuthor(lookupPapersFromAuthorRequestMessage) returns (PaperItemDTOList);
  rpc createPaperItem(createPaperItemParameter) returns (PaperItemDTO);
  rpc convertToMarkdownForWebsite(PaperItemKey) returns (ConvertToMarkdownForWebsiteResponseMessage);
}
```

You can use the generated `*.proto` files to implement a [gRPC](https://grpc.io/) interface.

You find the complete sources (incl. generated `*.proto` file) of this example [here](https://github.com/Microservice-API-Patterns/MDSL-Specification/tree/master/examples/protocol-buffers-example).

# Other Generators
Also checkout our other generators:
* [Open API generator](./open-api)
* [GraphQL generator](./graphql)
* [Jolie generator](./jolie)
* [Java generator](./java)
* [Arbitrary textual generation with Freemarker](./freemarker)

# Site Navigation
* Back to [tools page](./../tools).
* [Quick reference](./../quickreference) and [tutorial](./../tutorial). 
* Language specification: 
    * Service [endpoint contract types](./../servicecontract) and [data contracts (schemas)](./../datacontract). 
    * [Bindings](./../bindings) and [instance-level concepts](./../optionalparts). 
* Back to [MDSL homepage](./../index).

*Copyright: Stefan Kapferer and Olaf Zimmermann, 2020. All rights reserved. See [license information](https://github.com/socadk/MDSL/blob/master/LICENSE).*

---
title: Asynchronous Microservice Domain-Specific Language (AsynchMDSL)
author: Giacomo Di Liberali, Olaf Zimmermann
copyright: The authors, 2020-2021.  All rights reserved.
---

[Home (Language Reference)](./index) &mdash; [MDSL Data Types](./datacontract) &mdash; [MDSL Tools](./tools) &mdash; [AsyncAPI Generator](./generators/async-api)

AsyncMDSL
=========

_Note:_ The status of the Asynchronous Microservice Domain Specific Language (AsynchMDSL), created by Giacomo Di Liberali, is [*Technology Preview*](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview.html), standing at Version 1.1 at present.

AsyncMDSL aims at modeling asynchronous, messaging APIs while exploiting the [design goals of core MDSL](./index). Extending core MDSL, AsyncMDSL derives its abstract syntax from the state-of-the-art patterns and concepts described in the [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/) book. The language is fully specified in Giacomo Di Liberali's  [master thesis](UNIPI-AsyncMDSL-MasterThesis-2020-GiacomoDeLiberali-v1.3.pdf).

## Use Cases 

AsyncMDSL can be used to model scenarios where communication between systems is accomplished by the use of queue-based *messaging*, i.e., systems are not communicating through synchronous calls. In this asynchronous context, indeed, a request might not expect a reply, and message producers might not know the message consumers because *channels* and *message brokers* decouple them from each other. 

## Concepts 

An AsyncMDSL document uses the same structure as core MDSL, including its [data transfer representations](./datacontract):

~~~
API description HelloWorldAPI

data type SampleDTO {ID, D} 

channel SayHello
of type PUBLISH_SUBSCRIBE
on path "/public/sayHello" 
produces message HelloMessage
	delivering payload SampleDTO

message broker HelloWorldAmqpProvider 
exposes SayHello
at location "amqp.example.com"
via protocol AMQP

message endpoint HelloWorldAmqpClient
uses from HelloWorldAmqpProvider:
	SayHello
~~~

The example defines an API which has a single channel, `SayHello`, to which many [Message Endpoints](https://www.enterpriseintegrationpatterns.com/patterns/messaging/MessageEndpoint.html) can consume from. The channel is a [Publish-Subscribe Channel](https://www.enterpriseintegrationpatterns.com/patterns/messaging/PublishSubscribeChannel.html) and delivers a `SampleDTO` as output to subscribers. A Message Broker exposes the `SayHello` channel via AMQP protocol, and the Message Endpoint `HelloWorldAmqpClient` uses the channel from this broker (i.e., consumes messages from it). <!-- where do the messages come from? -->

### Message Channels

~~~
channel [name]
of type [type]
delivery guarantee [deliveryGuarantee]  // optional, default UNKNOWN
description [description]               // optional
on path [path]
    with                                // optional path parameters
        [pathParamName]: [pathParamType], [pathParamDescription],
        [...] 
produces message [messageName]          // or accepts [and produce]
    description [messageDescription]
	delivering                     // or expecting
		headers [...]          // optional
		payload [...]          // mandatory, e.g., {V}
                    as [messageIntent] // optional 
        where                          // optional message constraints
            [expression],
            [...]
bindings for [protocol] {
    // JSON object
}
~~~

#### Root

- 
    A channel type can assume one (or a combination of) the following values:
    > POINT_TO_POINT | PUBLISH_SUBSCRIBE | DATA_TYPE 
    > INVALID_MESSAGE | DEAD_LETTER | GUARANTEED_DELIVERY

    Invalid combinations will be notified by the API Linter that comes with the [editor and generator plugin](./generators/async-api) for ASyncMDSL.

- 
    A channel deliveryGuarantee can be any of:
    > UNKNOWN | AT_LEAST_ONCE | AT_MOST_ONCE | EXACTLY_ONCE

#### Channel path

- 
    A path param type can assume any of teh basic data types of MDSL:
    > bool | int | long | double | string

#### Message

- 
    A message intent can assume one of the following three [EIP patterns](https://www.enterpriseintegrationpatterns.com/patterns/messaging/MessageConstructionIntro.html):
    > COMMAND_MESSAGE | EVENT_MESSAGE | DOCUMENT_MESSAGE

- A `WHERE` expression can specify: 
    > MESSAGE_EXPIRES in [number] `s | m` (where `s` stands for _seconds_ and `m` for _minutes_)

	> CORRELATION_ID is [payloadExpression]
	
    > SEQUENCE_ID is [payloadExpression]

    `payloadExpression` has the format `$message.(payload | header)#/path/to/property` (from AsyncAPI). More examples are [here](https://www.asyncapi.com/docs/specifications/2.0.0#runtimeExpression).


#### Bindings

- Protocol can assume one of the [supported protocols](./bindings). As the list of properties is protocol-specific, a single flat JSON object is expected as configuration.

An example of a _binding_ definition can be found in [examples/AsyncMDSL/bindings.mdsl](../examples/AsyncMDSL/bindings.mdsl).

#### Channel definition example

[Message Channels](https://www.enterpriseintegrationpatterns.com/patterns/messaging/MessagingChannelsIntro.html) are defined as this: 

~~~
channel PatientMeasurements
of type PUBLISH_SUBSCRIBE, DATA_TYPE
delivery guarantee AT_LEAST_ONCE
description "Notifies whenever a new measurement comes"
on path "/patients/${patientId}/measurement"
	with 
        patientId: int, "The patient identifier" 
accepts message Measurement
	description "Contains the value and the type of the measurement"
	expecting 
		headers HeadersDTO
		payload MeasurementDTO as EVENT_MESSAGE
	where 
		MESSAGE_EXPIRES in 60m
bindings for MQTT {
    "qos": 1
}
~~~

In the above example we define a `PUBLISH_SUBSCRIBE` channel called _PatientMeasurements_; this type of channel can have multiple subscribers at the same time, and the message will be delivered to each of them as a copy. As a `DATA_TYPE` channel, this channel will only carry messages with the same schema.

The quality of service of the messages that will be sent to this channel is declared as the delivery guarantee `AT_LEAST_ONCE`: every message will be received by each subscriber at least one time.

We then inform subscribers that the path on which this channel will be exposed by a broker contains a parameter (_patientId_), which is determined at runtime by publishers subscribers. <!-- check edit -->

Since we stated that the channel is a `DATA_TYPE` channel, we specify the schema of the message that will be sent to subscribers _MeasurementDTO_ as well as the intent of such message (for example `EVENT_MESSAGE` or `DOCUMENT_MESSAGE`). We may also include other relevant information such as the message expiration time or a correlation identifier (if present).

We can finalize the channel definition by specifying optional _bindings_ for concrete protocols (here: MQTT).

### Request-Reply Channels

[Request-Reply messages](https://www.enterpriseintegrationpatterns.com/patterns/messaging/RequestReply.html) require distinct logical channels to communicate: one channel for the request and one channel for the reply. A Request-Reply Channel allows the definition of both logical channels, where each of them contains the payload they expect/deliver. Also, in this type of communication, it might be useful to specify further information, such as the Correlation Identifier of a message: 

~~~
channel [requestReplyChannelName]
request message [requestChannelName]
	description [description]
	on path [requestPath]
	expecting 
		headers [...]
		payload [...]
reply message [replyChannelName]
    description [description]
    on path [replyPath]
    delivering 
        headers [...]
        payload [...]
    where 
        CORRELATION_ID is [expression]
~~~

`expression` has the format `$message.(payload | header)#/path/to/property`.

### Message Brokers

Message brokers expose previously defined message channels and make them available to consumption under a concrete protocol:

~~~
message broker [name]
description [description]
exposes 
	[channelName],
	[otherChannelName]
            at location [url]
            via protocol [protocol],
            bindings {
                // JSON object with protocol-specific options
            }
            policy [policyName] // optional security policy
                realized using [policyType] in [payloadExpression],
	[...] // expose other channels under different protocols and policies
~~~

#### Parameters

- 
    Protocol can assume one of the [supported protocols](./bindings). Examples are:
    > AMPQ | MQTT | Kafka

- 
    Bindings is single flat JSON object: 
    > Notice that no protocol-specific validation is applied.

- 
    Security policy type. MDSL security policies are not yet fully supported. The available values are:
    > JWT | API_KEY 
    
    <!-- TODO OAS binding does a lot more since core MDSL 5.0, could bring some of that support here -->

- 
    Security policy expression specifies where to find the value and has the format:
    >  `$message.(payload | header)#/path/to/property`

### Message Endpoints

Message Endpoints are clients that connect to Message Brokers. Message Endpoints can use one or more Message Brokers, and for each one of them, they can specify which channels they are going to use to produce and/or consume messages:

~~~
message endpoint [name]
of type [type]          // EIP pattern (optional)
serves as [role_enum]   // MAP tag(s) (optional)
description [description]
uses 
    from [broker]:
        [channel],
        [channel1],

    [...] // declare dependency from other brokers/channels
~~~

## Generator Tool: AsyncMDSL to AsyncAPI

Valid AsyncMDSL specifications can be mapped to [AsyncAPI](https://www.asyncapi.com/).  See [this page](./generators/async-api) for instructions.

# Site Navigation

* Language specification: 
    * Service [endpoint contract types](./servicecontract) (this page) and [data contracts (schemas)](./datacontract). 
    * [Bindings](./bindings) and [instance-level concepts](./optionalparts).
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* Back to [MDSL homepage](./index).

*See [license information](https://github.com/socadk/MDSL/blob/master/LICENSE).*

<!-- *EOF* -->
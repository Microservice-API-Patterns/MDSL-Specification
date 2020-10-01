## MDSL change log (and release notes)

Also see GitHub [release notes](https://github.com/Microservice-API-Patterns/MDSL-Specification/releases).

V4.1, September/October 2020

* GraphQL schema generator
* *Technology preview:* language extension AsyncMDSL, with [AsyncAPI](https://www.asyncapi.com/) generator (Xtend-based)

V4.0, August 2020

* Improved HTTP and Java bindings
* Added API provider implementation concept (assuming Java use for now)

V3.4, July 2020 

* Cleanup
* Additional generators: gRPC protocol buffers, Jolie (and, indirectly, WSDL/XML Schema)

<!--V 3.2, V3.3? -->

V3.1.1, June 2, 2020

* Added three EIPs as stereotypes on message level: `'Command_Message' | 'Document_Message' | 'Event_Message'`

V3.1.0, May 26, 2020 

* Resolved two more TODOs in grammar 
* MAP decorators now match current pattern naming (operation level, element level) 
* Removed experimental MAP decorators (endpoint level)

V3.0.0, May 22, 2020 

* Removed deprecated support for Value `V` as AP (`D` should be used instead) 
* Corrected MAP decorator rule `MapPattern`: now has `'Control_Metadata' | 'Aggregated_Metadata' | 'Provenance_Metadata'`
* Semantic Versioning reverted to `String` (usage of semantic versioning to be validated, not forced by grammar)
* Moved `default is "..."` value to data type definition (from cardinality); this is an experimental feature still!
* Further TODO resolution
* Documented optionality operations/messages in grammar and new example (`expecting`, `delivering`)
* Misc documentation updates
* The Eclipse plugin tool now supports a generic Freemarker generator and a few simple validators (demo)

V2.1.0, May 15, 2020 

* Cleanup of grammar (TODOs, working comments, older rules)
* Semantic version identifier: optional name removed  

V2.0, May 5, 2020

* Experimental parts removed from v1.2.0 (e.g., composition support)

V1.2.0, March 17, 2020

* Extended proposal for asynch. messaging support on binding level (provider, client, gateway -> sender, receiver, broker)
* March 25: project [wiki](https://github.com/Microservice-API-Patterns/MDSL-Specification/wiki) updated (presentations, getting started page)

V1.1.0, February 8, 2020

* Updated endpoint role names from MAP: `CONNECTOR_RESOURCE` -> `TRANSFER_RESOURCE`, `STATIC_DATA_HOLDER` -> `REFERENCE_DATA_HOLDER` (so incompatible change). `TRANSFER_RESOURCE` and `LINK_LOOKUP_RESOURCE` also available as aliases (experimental).
* Updated operation responsibility names from MAP: `EVENT_PROCESSOR` -> `NOTIFICATION_OPERATION`, `BUSINESS_ACTIVITY_PROCESSOR` -> `STATE_TRANSITION_OPERATION` (*note:* kept old names as variants, so syntactically compatible)
* Some grammar cleanup (comments, TODOs)

V1.0.3, January 9, 2020

* Deprecated `V`, brought back `D` for Data Element/Entity Element from MAP. Rationale: user feedback
* Brought back long forms for parameter (stereo-)types: `Data`, `Identifier`, `Link`, `Metadata` (short forms `D`, `ID`, `L`, `MD`still work). Rationale: user feedback
* Fixed doc pages bug (`blob` -> `raw`)

V1.0.2, October 18, 2019

* MAP decorators can now be "text" STRINGs (to ease/improve MDSL generation Context Mapper):
    * Example: `operation createAddress with responsibility "BUSINESS_ACTIVITY_PROCESSOR" in "REQUEST_REPLY" conversation`

V1.0.1, August 31, 2019

* Demo/proposal how to model pub/sub and event sourcing
* Demo/proposal for specifying authentication (inspired by OAS 3.0)
* Other feedback from VSS19 captured as TODOs (e.g., oneOf/allOf,anyOf from OAS 3.0)

V1.0, July 19, 2019

* Entire API description can now also be versioned semantically
* Removed unnecessary () in treeNode rule (line 130)
* Grammar rules edited for better antlr4 compatibility (no changes to language)
* Endpoint parameters were experimental so far, and are now deprecated (but not featured in examples anyway)
* Repository/examples cleanup (TODOs)

V0.99, June 26, 2019 (backward compatible except for `blob` -> `raw` and typo fix: `AGGRESSIVE_OBSOLESCENCE`)

* Housekeeping
* Mappy demo support (examples)
* Renamed byte array base type from `blob` to `raw`
* Type in EvolutionStrategy rule fixed: from `AGGRESSIVE_OBSOLESCENSE` to `AGGRESSIVE_OBSOLESCENCE`

V0.98, May 22, 2019 (backward compatible)

* Data types and operations can now be versioned as well: 
    * `data type VersionedDataType version 0.0.1 {ID, V}`
    * `operation sampleOperation version 0.0.1 with responsibility EVENT_PROCESSOR` 

V0.97, May 15, 2019

* Removed '|' from Cardinality rule, no longer needed and not featured in examples anyway
* Some new comments and TODOs

V0.95 and v.096, May 13, 2019

* `V<"SomeUnknownType">` is now illegal, same effect can be achieved with plain identifier or `data type SomeUnknownType P`
* Refactored data type grammar to make it more precise and to make code completion more useful (to be tested)
* External SLAs now supported
* Many other tweaks in API provider part (e.g. SimpleMeasurement)

V0.94, May 13, 2019

* Added `V<void>` due to user feedback and test results 
* Changed base types to include 'long' and 'double' (instead of 'float')
* Enabled support for choice modeling on PT level 	data type ChoiceDemo `{"optionA":V|"optionB":V}`
* Updated pattern stereotype enum to reflect latest MAP status

V0.93a, May 10, 2019:

* Removed "contract" from "endpoint contact type" for brevity
* Removed "instance" from "endpoint instance location" for brevity
* `<<Value>>` no longer available as pattern stereotype (undone later)
* Entity and E no longer available as parameter roles (but D and Data)

V0.93b, May 10, 2019 (experimental):

* Only `V<int>` supported as parameter role/type pair, `Value<int>` removed to avoid confusion with `<<stereotypes>>`
* `<<Identifier>>` and `<<Link>>` and `<<Metadata>>` now available as pattern stereotypes (`<<Entity>>` was supported already)
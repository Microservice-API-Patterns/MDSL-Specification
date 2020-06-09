grammar AntlrMDSLGrammar;

serviceSpecification: 
	'API' 'description' name=ID
	('version' svi=semanticVersioningIdentifier)? // new July 19, 2019
	('usage' 'context' vis=visibility 'for' dirList+=directionList)?
	types+=dataContract*
	contracts+=endpointContract+
	// slas+=SLATemplate* // experimental 13 05 TODO bring back
	providers+=provider*
	clients+=client*
	// gateways+=Gateway* // TODO bring back (needs lower case rule names)
	// compositions+=Composition*
	// conversations+=Conversation* 
	('IPA')? // IPA is the inversion of API, now optional because multiple API providers and API clients can appear (May 13, 19)
;

visibility: 'PUBLIC_API' | 'COMMUNITY_API' | 'SOLUTION_INTERNAL_API';

directionList:
	primaryDirection=direction ('and' otherDirection=direction)? // simplified 13 05
;

direction: 'FRONTEND_INTEGRATION' | 'BACKEND_INTEGRATION';

// Editorial note: 
// Remainder of grammar defines several languages (orthogonal to each other): A) schema (data),  B) service endpoints (operations/ports), C) instance-level concepts (provider, client)


// *** Part A: data contract/types 

dataContract:
	'data' 'type' name=ID 
	('version' svi=semanticVersioningIdentifier)? // new in v.97 (May 22, 2019) 
	structure=elementStructure
;


// *** Part B.1: service endpoint contracts (with operations)

endpointContract:
	'endpoint' 'type' name=ID // was 'service contract', but service = entire API (also tried other keywords)
	('version' svi=semanticVersioningIdentifier)? // new in v.97 (May 22, 2019)
	('serves' 'as' primaryRole=resourceRole ('and' otherRoles+=resourceRole)* 'role'?)? // experimental 17-05-19: added role for RDD compliance
	('identified' 'by' pathParameters=elementStructure)? // experimental June 12, 2019; needed for RESTful HTTP URI templates (SOAP mapping???)
	('exposes' ops+=operation+)? // early incomplete specs do not have to expose any operations 
;

semanticVersioningIdentifier: 
    number ('.' number ('.' number)?)? // patched
;

resourceRole: 'INFORMATION_HOLDER_RESOURCE' | 'PROCESSING_RESOURCE' | 'CONNECTOR_RESOURCE' | 'LOOKUP_RESOURCE' | 'MASTER_DATA_HOLDER' | 'TRANSACTIONAL_DATA_HOLDER' | 'STATIC_DATA_HOLDER' | 'GATEWAY_RESOURCE' | 'GUARD_RESOURCE' | 'GROUND_RESOURCE'; // roles constrain operations ([O] lint tool)!

operation:
	'operation' name=ID
	('version' svi=semanticVersioningIdentifier)?
	('with' 'responsibility' responsibilities=operationResponsibility)? // say something about consistency, transactionality (and other coupling dimensions/criteria)?
	('in'  mep=MessageExchangePattern 'conversation')?
	'expecting' requestMessage=dataTransferRepresentation // must be present
	// ('delivering' responseMessage=dataTransferRepresentation)? // optional 
	('delivering' responseMessage=dataTransferRepresentation ('reporting'  reportData=statusReport)?)? // experimental June 12, 2019 
	// TODO use reporting info to generate https://flask-restplus.readthedocs.io/en/stable/errors.html or @api.doc(responses={403: 'Not Authorized'}) and throw a Jolie exception
;

statusReport:
	(('error' | 'analytics') reportMessage+=elementStructure)+ // | dtr=dataTransferRepresentation // changed July 19, 2019 tbd can second part (variant b) go?
;

operationResponsibility: 'COMPUTATION_FUNCTION' | 'RETRIEVAL_OPERATION' | 'EVENT_PROCESSOR' | 'BUSINESS_ACTIVITY_PROCESSOR'; 


MessageExchangePattern: 'ONE_WAY' | 'REQUEST_REPLY' | 'NOTIFICATION' | 'OTHER_PATTERN'; // only REQUEST_REPLY has both request and response message
	
dataTransferRepresentation:
	('headers' headers=elementStructure)? // tbd: "header" or "headers"?  note: a Map or other Java/ECore type might be good here
	// May 13, 19: (STX) payload should be optional (but either header or payload must be there)? => added 'P<null>' for now
	'payload' payload=elementStructure // note: this could be an embedded XSD or JSON Schema (or an external one referenced via URI)
	('structured' 'as' TypeSystem)?
;

elementStructure: // renamed from BodyStructure because now also used for header (repr. element structure is the name in MAP) 
	pf=parameterForest | pt=parameterTree | apl=atomicParameterList  | np=singleParameterNode // experimental 13 05
;

parameterForest:
	classifier=patternStereotype?
	'[' ptl=parameterTreeList ']' 
;

parameterTreeList:
    parameterTree | ';'  parameterTreeList
;

// convention: add a two-blank "  " indent per nesting level (not checked by compiler/generators), something for a pretty printer?


parameterTree:
	classifier=patternStereotype? 
	(name=identifier ':')?  
	'{' 
		  first=treeNode ((','|'|') nexttn+=treeNode)* // May 13, 19: was 'first=TreeNode (',' next+=TreeNode)*' // May 22: next->nexttn for antlr4
	'}' 
	card=cardinality? 
;

Separator:
	','  // not sure why I can't have an or here: | '|' (the mid '|' is not the problem)
;

// TODO move classifier=PatternStereotype? and card=Cardinality? to own/other rule to avoid "redundancy"? 

treeNode:
	pn=singleParameterNode | apl=atomicParameterList | pt=parameterTree // changed July 19, 2019: removed extra (), single child is enough here!
;

singleParameterNode: 
	genP=genericParameter | atomP=atomicParameter | tr=typeReference
;

genericParameter:
	 name=identifier // this makes it possible to only have a name/id, but no role (yet)
	| p='P' // default, unspecified 'Parameter' or 'PayloadPart' (could be Value/Entity, Metadata, Identifier, Link or something composed/external)
	| name=identifier ':' 'P'
;

typeReference: 
	classifier=patternStereotype? 
	(name=identifier':')? 
	ID // was dcref=[DataContract] 
	card=cardinality?
;

atomicParameterList:
	classifier=patternStereotype? 
	(name=identifier ':')? 
	'(' first=atomicParameter ((','|'|') nextap+=atomicParameter)* ')' // note: empty APL? not allowed at present, would require: |'()'; May 13, 19: can now be expressed via P<null>
	card=cardinality? 	
;

atomicParameter: 
	classifier=patternStereotype? 
	(name=identifier ':')? 
	rat=roleAndType
	card=cardinality?
;

roleAndType:
 	// name used to be here
	 nn=ParameterRole ('<' mm=basicDataType '>')?  // PATCHED
;

patternStereotype:
    '<<' (pattern=MapPattern | name=ID) '>>' // TODO tbd allow more than one pattern per representation element? remove name=ID here?
;

MapPattern:
	'API_Key' | 'Context_Representation' | 'Error_Report' | 'Request_Bundle' | 'Request_Condition' | 'Wish_List' | 'Wish_Template' | 'Embedded_Entity' | 'Linked_Information_Holder' | 'Annotated_Parameter_Collection' | 'Pagination' | 'ControlMetadata' | 'AggegratedMetadata' | 'ProvenanceMetadata' | 'Metadata' | 'Entity' | 'Identifier' | 'Link' 
;

ParameterRole: 
	'V' // (Atomic) Value, Entity or Value Object from DDD (experimental), short for <<value>>(Data,Data,...) and <<entity>>{ID,V}
	| 'Value' 
	| 'ID' // Identifier
	| 'L' // e.g.  Link/URI. Linked Information Holder: {(ID,V1,..,VN,L} 
	| 'MD' // // e.g. in Annotated Parameter Collection: {MD,(MD,E),(MD,E),...}
;

// TODO catch modeling errors such as ID<bool> (grammar or linter?)

basicDataType:
 	'bool' | 'int' | 'long' | 'double' | 'string' | 'raw' | 'void' 
;

cardinality:
	'?' | '*' | '+' // from (E)BNF, optionality expressed by '?', more powerful and shorter than 'required' flag
	| '!' // "!" means required (default) 
;

TypeSystem: 
	'MAP_TYPE' | 'JOLIE_TYPE' | 'JSON_SCHEMA' | 'XML_SCHEMA' |'PROTOCOL_BUFFER' | 'AVRO_SCHEMA' | 'THRIFT_TYPE' | 'GRAPHQL_SDL' | 'OTHER' /*  | 'YAML_RX' */ // how about MWE2 (Eclipse Xtext)? YAML Kwalify? 
;


// *** C.1: provider/endpoint part 

provider:
	'API' 'provider' name=ID
	epl+=endpointList+
	('provider' 'governance'  evolStrat=EvolutionStrategy)? 
;

EvolutionStrategy: 
	'ETERNAL_LIFETIME' | 'LIMITED_GUARANTEED_LIFETIME' | 'TWO_IN_PRODUCTION' | 'AGGRESSIVE_OBSOLESCENCE' | 'EXPERIMENTAL_PREVIEW' // MAPs
	| 'OTHER_STRATEGY' | 'UNDISCLOSED'
;


endpointList:	
	'offers' contract=ID // [EndpointContract] // one endpoint can only offer a single contract (but provider can have multiple endpoints)
	endpoints+=endpointInstance* // optional; several endpoints per contract mean redundant deployment (or different SLAs for same functionality) 
;

endpointInstance: 
	'at' 'endpoint' 'location' name=identifier
	pb=protocolBinding?
	('endpoint' 'governance'  evolStrat=EvolutionStrategy)?
;

protocolBinding:
	'via' 'protocol' bindings+=transportProtocol ('mapping' ms+=parameterBinding)*
;

parameterBinding:
	('path' 'parameters' pp=identifierList)
	| ('query' 'parameters' qp=identifierList)
	| ('form' 'parameters' fp=identifierList)
	| 'default' 'body'
;

identifierList:
	// TODO could add "" here to make symmetric with AP definition in APL and PT (does name=STRING do the trick)?
	'(' name1=ID (',' moreNames+=ID )* ')'// linter needs to check that ID actually is id of an existing AP in top-level APL in elementStructure of request message
;

transportProtocol:
	'RESTful_HTTP'| 'SOAP_HTTP' | 'gRPC' |  'Avro_RPC' | 'Thrift' | 'AMQP' | identifier // TODO enhance REST binding (for path param support)
;

client:
	'API' 'client' name=ID
     cons+=consumption+
;

consumption: 
	'consumes' contract=ID // [EndpointContract]
	// if present, the provider reference creates a static binding between client and server:
	('from' providerReference=ID)? // could also go down to endpoint level
	('via' 'protocol'  tpbinding+=transportProtocol)? // is this needed (also given in provider endpoint)?
;

gateway:
	'API' 'gateway' name=ID
	gateList+=gate+
	('evolution' 'governance'  evolStrat=EvolutionStrategy)?
;

gate:
	'exposes' upstreamContract+=ID // [endpointContract]+
	endpoints+=endpointInstance+ 

	// [O] model more API Gateway capabilities, on type and instance level
	// e.g. hybrid provider and client (with internal processing resource, computation function?)
	// e.g. security, format mediation? (see FL's comments) 
     cons+=consumption
     ('mediates' dts+=dataTransformation)*
;

dataTransformation:
	'from' indc=ID 'to' outdc=ID // [dataContract]
;

quotedURI:
	QuotedURI
;

identifier: 
    QuotedID
;

number:
	IntNumber
;

comment:
	Comment
;

// ** End of MDSL grammar (parser part), lexer rules next (note: they have to start with upper case, unlike parser rules!) 

// https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md

fragment DIGIT: 
    '0' .. '9'
;

fragment CHAR:
	'a' .. 'z' | 'A' .. 'Z' 
;

fragment STRING:
    (CHAR | DIGIT | '_' | '/' | '.'  | ':' )+ // TODO tbc
;


ID:
    ('a' .. 'z'|'A' .. 'Z') STRING? // starts with char followed by other stuff (that should not be in id)
;

QuotedID:
    '"' WS? ID WS? '"' 
;

IntNumber:
    DIGIT+
;

PORT:
    ':' IntNumber 
;

URI: 
	STRING '://' STRING PORT? '/' STRING? // just a first draft, does not check do path and domain dot yet (and does not work either)
;

QuotedURI: 
    '"' URI '"'
;

Comment:
   '//' WS? STRING
;

// the blank in the WS array does matter! and it does have to be used in other lexer rules (e.g. Comment)
WS: 
	[ \t\r\n]+ -> skip 
;

// End of lexer part 
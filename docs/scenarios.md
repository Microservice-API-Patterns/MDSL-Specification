---
title: Microservice Domain Specific Language (MDSL) Integration Scenarios and Stories
author: Olaf Zimmermann
copyright: Olaf Zimmermann, 2021.  All rights reserved.
---

[Home](./index) &mdash; [Endpoint Types](./servicecontract) &mdash; [Bindings](./bindings) &mdash; [Provider and Client](./optionalparts) &mdash; [Tutorial](./tutorial) &mdash; [Cheat Sheet](./quickreference) &mdash; [Tools](./tools)

Integration Scenarios and User/Job Stories
==========================================

_Note:_ <!--- The status of this language concept is [*Experimental Preview*](https://microservice-api-patterns.org/patterns/evolution/ExperimentalPreview.html). --> The grammar and the tool support for stories might change in future versions of MDSL.

## Use Cases (When to Specify)

The MDSL grammar foresees three scenario types for this optional MDSL concept:

* Scenarios and stories can define requirements for APIs and services from a client perspective.
* They can also express frontend and backend integration needs.
* They can be used for API mocking and testing.

## Example

A scenario contains one or more stories. A story has up to five parts, which combine the elements found in [user stories](https://socadk.github.io/design-practice-repository/artifact-templates/DPR-UserStory.html) and BDD-style [examples as specifications](https://martinfowler.com/bliki/GivenWhenThen.html) and tests. Only actor role and action are mandatory:

~~~
scenario Scenario1
  story Story1
   when "something has happened" // trigger
   a "customer and/or integrator" // actor role (can be system)
   wants to "startProcess" in "location"// action, business activity 
   yielding "a result" // outcome
   so that "both actors are satisfied and profit is made" // goal 
    
  story Story2
    a API client wants to CRUD "SomeBusinessObject"
~~~

The keyword `CRUD` is short for create, read, update, and delete. <!-- The keyword `CQRS` is available as well. -->

## Language Concepts (Overview)

The grammar combines elements from a common [user story template](https://www.agilealliance.org/glossary/user-story-template/) with the given-when-then structure in [Behavior-Driven Development (BDD)](http://dannorth.net/introducing-bdd/) and [Acceptance Test Driven Development (ATDD)](https://www.agilealliance.org/glossary/atdd/):

~~~

IntegrationScenario:
	'scenario' name=ID ('type' type=ScenarioType)? stories+=IntegrationStory*
 ;

IntegrationStory:
	('story' name=ID ('type' type=StoryType)? related+=RelatedStories*)?
	('when' condition=STRING)? // precondition 
	('a'|'an'|'the') (client=STRING | 'API' 'client') // actor, persona
	'wants' 'to' action=Action on+=StoryObject*  // the responsibility/feature
	('yielding' outcome=STRING)? // postcondition 
	('so' 'that' goal=STRING)? // business impact
;

Action:
	plainAction=STRING | keyword=ActionKeyword ('a'|'an'|'the')? target=STRING
;

enum ActionKeyword:
	CRUD | CRUDSF | CQRS 
;

StoryObject:
	('with'|'against'|'for'|'at'|'in'|'to'|'on'|'from'|'and'|'into'|'within'|'via')? ('a'|'an'|'the'|'its')? object=STRING
;

enum StoryType:
	USER_STORY | JOB_STORY | TEST_CASE | API_MOCK
;

enum ScenarioType:
	BUSINESS_API | FRONTEND_INTEGRATION_SCENARIO | BACKEND_INTEGRATION_SCENARIO 
;
~~~

## Related Transformations

Scenarios and their stories can be turned into [flows](./flows.md) and [endpoint types](./servicecontract). See [this page](soad.md) for an overview of the available transformations.

# Site Navigation

* Language specification:
    * Service [endpoint contract types](./servicecontract) and [data contracts (schemas)](./datacontract)
    * [Bindings](./bindings) and [instance-level concepts](./optionalparts)
    * [Orchestration flows](flows.md)
* [Quick reference](./quickreference), [tutorial](./tutorial) and [tools](./tools)
* [MDSL homepage](./index)

*Copyright: Olaf Zimmermann, 2018-2022. All rights reserved. See [license information](https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/LICENSE).*

<!-- *EOF* -->
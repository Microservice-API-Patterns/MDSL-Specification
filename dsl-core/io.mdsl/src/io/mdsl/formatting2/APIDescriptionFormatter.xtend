package io.mdsl.formatting2

import org.eclipse.xtext.formatting2.AbstractFormatter2
import org.eclipse.xtext.formatting2.IFormattableDocument
import io.mdsl.apiDescription.ServiceSpecification
import io.mdsl.apiDescription.EndpointContract
import io.mdsl.apiDescription.Provider
import io.mdsl.apiDescription.Client
import io.mdsl.apiDescription.Operation
import io.mdsl.apiDescription.ChannelContract
import io.mdsl.apiDescription.DataContract
import org.eclipse.emf.ecore.EObject
import io.mdsl.apiDescription.SingleParameterNode
import io.mdsl.apiDescription.ElementStructure
import io.mdsl.apiDescription.AtomicParameter
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.TypeReference
import io.mdsl.apiDescription.ParameterTree
import io.mdsl.apiDescription.TreeNode
import io.mdsl.apiDescription.Cardinality
import io.mdsl.apiDescription.AtomicParameterList
import io.mdsl.apiDescription.RequestReplyChannel
import io.mdsl.apiDescription.RequestChannel
import io.mdsl.apiDescription.ReplyChannel
import io.mdsl.apiDescription.Payload
import io.mdsl.apiDescription.DataTransferRepresentation
import io.mdsl.apiDescription.IntegrationScenario
import io.mdsl.apiDescription.Orchestration
import io.mdsl.apiDescription.Gateway
import io.mdsl.apiDescription.FlowStep
import io.mdsl.apiDescription.EventTypes
import io.mdsl.apiDescription.EventType
import io.mdsl.apiDescription.CommandTypes
import io.mdsl.apiDescription.CommandType
import io.mdsl.apiDescription.CommandInvokationStep
import io.mdsl.apiDescription.EndpointList
import io.mdsl.apiDescription.EndpointInstance
import io.mdsl.apiDescription.ProtocolBinding
import io.mdsl.apiDescription.TechnologyBinding
import io.mdsl.apiDescription.HTTPBinding
import io.mdsl.apiDescription.HTTPResourceBinding
import io.mdsl.apiDescription.HTTPOperationBinding
import io.mdsl.apiDescription.GenericParameter
import io.mdsl.apiDescription.PatternStereotype
import io.mdsl.apiDescription.SecurityPolicies
import io.mdsl.apiDescription.SecurityPolicy
import io.mdsl.apiDescription.StatusReports
import io.mdsl.apiDescription.StatusReport
import io.mdsl.apiDescription.IntegrationStory

class APIDescriptionFormatter extends AbstractFormatter2  {
	
	// TODO (generated, partially done): implement for CommandTypes, CommandType, ParameterForest, BindingParameter, BindingParams, ChannelPathWithParams, RequestReplyChannel, RequestChannel, ReplyChannel, Payload, OneWayChannel, Message, ParameterTreeList, ParameterTree, TreeNode, SingleParameterNode, TypeReference, AtomicParameterList, AtomicParameter, 
	// EndpointContract, Operation, StatusReports, StatusReport, SecurityPolicies, SecurityPolicy, DataTransferRepresentation, Provider, EndpointList, EndpointInstance, MessageBroker, AsyncEndpoint, TechnologyBinding, ProtocolBinding, HTTPBinding, HTTPResourceBinding, HTTPTypeBinding, HTTPOperationBinding, MediaTypeList, HTTPParameterBinding, JavaBinding, JavaOperationBinding, GRPCBinding, 
	// SLA, SLATemplate, InternalSLA, RateLimit, SLO, Measurement, LandingZone, Client, Consumption, MessageEndpoint, AsyncConsumptionFromBroker, AsyncConsumptionNoProtocolBinding, AsyncConsumptionWithProtocolBinding, WhereConstruct, Gateway, Gate, 
	// IntegrationScenario, Orchestration, FlowStep, CombinedInvocationStep, DomainEventProductionStep, CommandInvokationStep, EitherCommandOrOperation, EitherCommandOrOperationInvokation, CommandInvokation, OperationInvokation, SingleOperationInvokation, ConcurrentOperationInvokation, ExclusiveAlternativeOperationInvokation, InclusiveAlternativeOperationInvokation, EventProduction
	// TODO (generated): format HiddenRegions around keywords, attributes, cross references, etc. 
	
	def dispatch void format(ServiceSpecification serviceSpecification, extension IFormattableDocument document) {
		
		for (DataContract dataContract : serviceSpecification.getTypes()) {
			dataContract.format
			dataContract.prepend[newLine] // changed from "append" to "prepend" on June 13
		}
		
		for (EventTypes events : serviceSpecification.getEvents()) {
			// iterate through them (commas?)
			events.format
			events.prepend[newLine]
		}
		
		for (CommandTypes commands : serviceSpecification.getCommands()) {
			// iterate (commas?)
			commands.format
			commands.prepend[newLine]
		}
		
		for (EObject eObject : serviceSpecification.getContracts()) {
			// both channel and endpoint contract called
			format(eObject);
		}
		
		// TODO SLAs missing (see generated APIDescriptionFormatter.java)
		
		for (EObject eObject : serviceSpecification.getProviders()) {
			/*
			serviceSpecification.regionFor.keywords('API').forEach [
				prepend[newLines = 1]
				format(eObject);
			]
			*/
			eObject.format
		}
		
		for (EObject eObject : serviceSpecification.getClients()) {
			// both messaging endpoints and API clients called?
			format(eObject);
		}
		
		for (Gateway gateway : serviceSpecification.getGateways()) {
			format(gateway);
		}
		
		// TODO provider realizations (implementation) missing
		
		for (Orchestration flows : serviceSpecification.getOrchestrations()) {
			format(flows);
		}
		
		for (IntegrationScenario scenario : serviceSpecification.getScenarios()) {
			format(scenario);
		}
    }
    
    def dispatch void format(DataContract dc, extension IFormattableDocument document) {
    	dc.prepend[newLine] 
    	dc.structure.format
	}
	
	def dispatch void format(EventTypes events, extension IFormattableDocument document) {
		events.prepend[newLine]
		for (EventType event : events.getEvents()) {
			format(event);
		}
	}
	
	def dispatch void format(EventType event, extension IFormattableDocument document) {
		event.content.format
	}
	
	def dispatch void format(CommandTypes commands, extension IFormattableDocument document) {
		commands.prepend[newLine]
		for (CommandType command : commands.getCommands()) {
			format(command);
		}
	}
	
	def dispatch void format(CommandType command, extension IFormattableDocument document) {
		command.subject.format
	}
	
	def dispatch void format(DataTransferRepresentation dtr, extension IFormattableDocument document) {
		dtr.prepend[indent] 
		dtr.payload.format
	}
	
	def dispatch void format(ElementStructure dStruct, extension IFormattableDocument document) {
    	dStruct.pf.format 
    	dStruct.pt.format 
    	dStruct.apl.format 
    	dStruct.np.format
	}
	 
	def dispatch void format(ParameterTree pt, extension IFormattableDocument document) {
		
		pt.classifier.format
		pt.regionFor.keyword(':').surround[noSpace]
    	pt.regionFor.keyword('{').append[noSpace] 
    	pt.first.format
    	for (TreeNode tn : pt.nexttn) {
			format(tn);
		}
		pt.card.format
		pt.regionFor.keyword('}').prepend[noSpace]
    }
    
    def dispatch void format(TreeNode tn, extension IFormattableDocument document) {
    	tn.pn.format
    	tn.apl.format
    	tn.children.format
    }
    
	def dispatch void format(SingleParameterNode spn, extension IFormattableDocument document) {
    	spn.genP.format
    	spn.atomP.format 
    	spn.tr.format
	}
	
	def dispatch void format(GenericParameter gp, extension IFormattableDocument document) {
		gp.regionFor.keyword(':').append[noSpace]
	}
	
	def dispatch void format(TypeReference tr, extension IFormattableDocument document) {
		tr.classifier.format
		tr.regionFor.keyword(':').surround[noSpace]
		tr.card.format
		
		// no effect
		// tr.regionFor.keyword('*').surround[noSpace]
		// tr.regionFor.keyword('*').prepend[noSpace]
	}
	
	def dispatch void format(AtomicParameterList apl, extension IFormattableDocument document) {		
    	apl.first.format
    	for (AtomicParameter ap : apl.nextap) {
			format(ap);
		}
 		apl.card.format
	}
	
	def dispatch void format(AtomicParameter ap, extension IFormattableDocument document) {		
		ap.classifier.format
    	ap.rat.format // name is in rat
 		ap.card.format
	}
	
	def dispatch void format(RoleAndType rat, extension IFormattableDocument document) {
		rat.regionFor.keyword(':').surround[noSpace]
		rat.regionFor.keyword('<').surround[noSpace]
		rat.regionFor.keyword('>').prepend[noSpace]
		// rat.btype.format
	}
	
	def dispatch void format(Cardinality c, extension IFormattableDocument document) {
		c.regionFor.keyword('!').prepend[noSpace] // changed from surround->prepend 
		c.regionFor.keyword('+').prepend[noSpace]
		c.regionFor.keyword('?').prepend[noSpace]
		c.regionFor.keyword('*').prepend[noSpace]
	}
	
	def dispatch void format(PatternStereotype classifier, extension IFormattableDocument document) {
		classifier.regionFor.keyword('<<').append[noSpace]
		classifier.regionFor.keyword('>>').prepend[noSpace]
	}
	
	/*
	// grammar does not cause the BasicDataType interface/class to be generated yet:
	def dispatch void format(BasicDataType btype, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting basic type"); // not called yet; is rule the right one?
		btype.regionFor.keyword('<').surround[noSpace]
		btype.regionFor.keyword('>').prepend[noSpace]
	}
	*/ 

    def dispatch void format(EndpointContract ec, extension IFormattableDocument document) {
		
		ec.prepend[newLines=2] 
				
    	ec.regionFor.keyword("exposes").prepend[newLines=1] // no effect?
    	
    	for (Operation operations : ec.ops) {
			format(operations);
		}
		
		// TODO more contract parts: receives (event)
		
		ec.regionFor.keyword("receives").prepend[newLines=1]
		

	}

	def dispatch void format(ChannelContract cc, extension IFormattableDocument document) {
    	// cc.regionFor.keyword("channel").prepend[newLines=1] // "2" here yields conflicting channels exception
    	// p.append[newLines=1] // "2" here yields conflicting channels exception
    	
    	cc.conversationType.format
    	
    	cc.prepend[newLines=2] 
	}
	
	// TODO indentation not working (MDSL does not have block closing keywords, unlike CML)
	
	def dispatch void format(RequestReplyChannel channels, extension IFormattableDocument document) {
    	channels.prepend[newLine]
		interior(
			channels.regionFor.keyword('request'),
			channels.regionFor.keyword('reply')
		)[indent]
    	
    	channels.request.format
    	channels.reply.format
	}
	
	def dispatch void format(RequestChannel channel, extension IFormattableDocument document) {
		channel.prepend[newLine]
		// channel.prepend[indent] // no effect/exception
		interior(
			channel.regionFor.keyword('request'),
			null
		)[indent]

		channel.payload.format
	}

	def dispatch void format(ReplyChannel channel, extension IFormattableDocument document) {
		channel.prepend[newLine] 
		// channel.prepend[space="2"] // no effect
		// no effect
		interior(
			channel.regionFor.keyword('request'),
			channel.regionFor.keyword('message')
		)[indent]
		
		channel.payload.format
	}
	
	def dispatch void format(Payload body, extension IFormattableDocument document) {
		body.schema.format
	}
	
	def dispatch void format(Operation operation, extension IFormattableDocument document) {
    	// op.regionFor.keyword("operation").prepend[newLine] // causes pbs
    	    	
    	operation.prepend[newLine];   	
    	operation.requestMessage.format // TODO causes extra line break (sometimes)
    	operation.responseMessage.format // no extra line break here

    	operation.reports.format   
    	operation.policies.format
    	operation.undo.format // NYI
	}
	
	def dispatch void format(StatusReports reports, extension IFormattableDocument document) {
		for (StatusReport report : reports.getReportList()){
			format(report);
		}
	}
	
	def dispatch void format(StatusReport report, extension IFormattableDocument document) {		
		report.reportData.format;
	}
		
	def dispatch void format(SecurityPolicies policies, extension IFormattableDocument document) {
		for (SecurityPolicy policy : policies.getPolicyList()){
			format(policy);
		}
	}
	
	def dispatch void format(SecurityPolicy policy, extension IFormattableDocument document) {
		policy.securityObject.format;
	}

	def dispatch void format(Provider p, extension IFormattableDocument document) {
		p.prepend[newLines=2]
    	
    	for (EndpointList el : p.epl) {
			el.format;
		}
	}
	
	def dispatch void format(EndpointList el, extension IFormattableDocument document) {
		for (EndpointInstance ei : el.endpoints) {
			ei.format;
		}
	}
	
	def dispatch void format(EndpointInstance ei, extension IFormattableDocument document) {
		for (TechnologyBinding tpb : ei.pb) {
			tpb.format;
		}
	}
	
	def dispatch void format(TechnologyBinding tp, extension IFormattableDocument document) {
		tp.prepend[newLine];
		tp.protBinding.format;
	}
	
	def dispatch void format(ProtocolBinding pb, extension IFormattableDocument document) {
		pb.http.format;
	}
	
	def dispatch void format(HTTPBinding httpb, extension IFormattableDocument document) {
		for (HTTPResourceBinding rb : httpb.eb) {
			rb.format;
		}
	}

	def dispatch void format(HTTPResourceBinding httprb, extension IFormattableDocument document) {
		httprb.prepend[newLine];
		// TODO handle tB+=HTTPTypeBinding too? 
		for (HTTPOperationBinding ob : httprb.opsB) {
			ob.format;
		}
		httprb.opsB.format
	}	
	
	def dispatch void format(HTTPOperationBinding hopb, extension IFormattableDocument document) {
		hopb.prepend[newLine];
	}
	
	def dispatch void format(Client c, extension IFormattableDocument document) {
		c.prepend[newLines=2]
	}
	
	def dispatch void format(Orchestration flow, extension IFormattableDocument document) {
		flow.prepend[newLines=2]
		for (FlowStep flowStep : flow.getSteps()) {
			flowStep.format;
		}
	}
	
	def dispatch void format(FlowStep flowStep, extension IFormattableDocument document) {
		flowStep.prepend[newLine]
		flowStep.cisStep.format
	}
	
	def dispatch void format(CommandInvokationStep cisStep, extension IFormattableDocument document) {
		// no effect:
		interior(
			cisStep.regionFor.keyword('event'),
			cisStep.regionFor.keyword('triggers')
		)[indent]
	}
	
	def dispatch void format(IntegrationScenario scenario, extension IFormattableDocument document) {
		scenario.prepend[newLines=2] 
		for (IntegrationStory story : scenario.getStories()) {
			story.format;
		}
	}
		
	def dispatch void format(IntegrationStory story, extension IFormattableDocument document) {
		story.prepend[newLine]
	}
}
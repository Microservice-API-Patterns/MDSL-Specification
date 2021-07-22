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

// import io.mdsl.apiDescription.BasicDataType;

class APIDescriptionFormatter extends AbstractFormatter2  {
	
	// TODO (generated, updated partially): implement for CommandTypes, CommandType, ParameterForest, BindingParameter, BindingParams, ChannelPathWithParams, RequestReplyChannel, RequestChannel, ReplyChannel, Payload, OneWayChannel, Message, ParameterTreeList, ParameterTree, TreeNode, SingleParameterNode, TypeReference, AtomicParameterList, AtomicParameter, 
	// EndpointContract, Operation, StatusReports, StatusReport, SecurityPolicies, SecurityPolicy, DataTransferRepresentation, Provider, EndpointList, EndpointInstance, MessageBroker, AsyncEndpoint, TechnologyBinding, ProtocolBinding, HTTPBinding, HTTPResourceBinding, HTTPTypeBinding, HTTPOperationBinding, MediaTypeList, HTTPParameterBinding, JavaBinding, JavaOperationBinding, GRPCBinding, 
	// SLA, SLATemplate, InternalSLA, RateLimit, SLO, Measurement, LandingZone, Client, Consumption, MessageEndpoint, AsyncConsumptionFromBroker, AsyncConsumptionNoProtocolBinding, AsyncConsumptionWithProtocolBinding, WhereConstruct, Gateway, Gate, 
	// IntegrationScenario, Orchestration, FlowStep, CombinedInvocationStep, DomainEventProductionStep, CommandInvokationStep, EitherCommandOrOperation, EitherCommandOrOperationInvokation, CommandInvokation, OperationInvokation, SingleOperationInvokation, ConcurrentOperationInvokation, ExclusiveAlternativeOperationInvokation, InclusiveAlternativeOperationInvokation, EventProduction
	
	// TODO (generated): format HiddenRegions around keywords, attributes, cross references, etc. 
	
	def dispatch void format(ServiceSpecification serviceSpecification, extension IFormattableDocument document) {
		
		for (DataContract dataContract : serviceSpecification.getTypes()) {
			dataContract.format
			dataContract.prepend[newLine] // changed from append to prepend on June 13
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
			// taken out June 13 
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
		
		// TODO provider realizations (implementation)  missing
		
		for (Orchestration flows : serviceSpecification.getOrchestrations()) {
			format(flows);
		}
		
		for (IntegrationScenario scenario : serviceSpecification.getScenarios()) {
			format(scenario);
		}
    }
    
    def dispatch void format(DataContract dc, extension IFormattableDocument document) {
    	// System.out.println("[INFO] Formatting data contract " + dc.name);
    	// dc.regionFor.keyword("data").prepend[newLines=1] // needed? replace with dc.prepend[newLine]?
    	dc.prepend[newLine] // changed June 13
    	dc.structure.format
    	// dc.append[newLines=1] // brought back June 9, removed June 13
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
		dtr.prepend[indent] // new  June 25
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
		pt.regionFor.keyword(':').surround[noSpace] // new June 13
    	pt.regionFor.keyword('{').append[noSpace] // June 27
    	pt.first.format
    	// TODO how about APL?
    	for (TreeNode tn : pt.nexttn) {
			format(tn);
		}
		pt.card.format
		pt.regionFor.keyword('}').prepend[noSpace] // June 27
    }
    
    def dispatch void format(TreeNode tn, extension IFormattableDocument document) {
    	tn.pn.format
    	tn.apl.format
    	tn.children.format
    }
    
	def dispatch void format(SingleParameterNode spn, extension IFormattableDocument document) {
    	// System.out.println("[INFO] Formatting spn in ES");
    	spn.genP.format
    	spn.atomP.format 
    	spn.tr.format
	}
	
	def dispatch void format(GenericParameter gp, extension IFormattableDocument document) {
		gp.regionFor.keyword(':').append[noSpace] // new June 13, changed June 25
	}
	
	def dispatch void format(TypeReference tr, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting type reference in AP");
		
		// note: classifier not formatted yet (here and elsewhere)
		
		// tr.dcref.format// taken out June 13  	
		
		tr.regionFor.keyword(':').surround[noSpace] // new June 13
		// tr.dcref.append[noSpace] // removed June 13
		tr.card.format
		
		// no effect, trying to remove space from "delivering payload SampleDTO *"
		// tr.regionFor.keyword('*').surround[noSpace]
		// tr.regionFor.keyword('*').prepend[noSpace]
	}
	
	def dispatch void format(AtomicParameterList apl, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting APL in DC");
		
		// note: classifier not formatted yet (here and elsewhere)
    	apl.first.format
    	// updated July 4
    	for (AtomicParameter ap : apl.nextap) {
			format(ap);
		}
 		apl.card.format
	}
	
	def dispatch void format(AtomicParameter ap, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting AP in DC ");
		
		ap.classifier.format
    	ap.rat.format // name is in rat
 		ap.card.format
	}
	
	def dispatch void format(RoleAndType rat, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting role and type in RAT " + rat.name); // not called yet?
		rat.regionFor.keyword(':').surround[noSpace]
		rat.regionFor.keyword('<').surround[noSpace]
		rat.regionFor.keyword('>').prepend[noSpace] // changed July 4
		// rat.btype.format // needed? removed June 2
	}
	
	def dispatch void format(Cardinality c, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting cardinality");
		// not working yet:
		// c.prepend[noSpace] // removed June 2
		c.regionFor.keyword('!').prepend[noSpace] // changed from surround->prepend June 15
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
		System.out.println("[INFO] Formatting basic type"); // not called yet; is rule the right one?
		btype.regionFor.keyword('<').surround[noSpace]
		btype.regionFor.keyword('>').prepend[noSpace]
	}
	*/ 

    def dispatch void format(EndpointContract ec, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting endpoint contract");
		
		ec.prepend[newLines=2] // added June 9, moved here June 13
				
		// this might cause two tests to fail (that call "save" on resource explicitly):
    	ec.regionFor.keyword("exposes").prepend[newLines=1] // no effect (June 9)?
    	
    	for (Operation operations : ec.ops) {
			format(operations);
		}
		
		// TODO more contract parts: receives, compensatedBy?
		ec.regionFor.keyword("receives").prepend[newLines=1] // seems to work (June 9)
		

	}

	def dispatch void format(ChannelContract cc, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting AsyncMDSL channel contract");
		
    	// cc.regionFor.keyword("channel").prepend[newLines=1] // "2" here yields conflicting channels exception
    	// p.append[newLines=1] // "2" here yields conflicting channels exception
    	
    	cc.conversationType.format
    	
    	cc.prepend[newLines=2] // added June 9 (and taken out first one)
	}
	
	// TODO indentation *not* working yet (MDSL does not have block closing keywords, unlike CML)
	def dispatch void format(RequestReplyChannel channels, extension IFormattableDocument document) {
    	channels.prepend[newLine]
		// channel.prepend[indent]
		interior(
			channels.regionFor.keyword('request'),
			channels.regionFor.keyword('reply')
		)[indent]
    	
    	channels.request.format
    	channels.reply.format
	}
	
	def dispatch void format(RequestChannel channel, extension IFormattableDocument document) {
		// TODO fix/complete
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
    	// op.regionFor.keyword("operation").prepend[newLine] // caused pbs
    	    	
    	operation.prepend[newLine]; // added June 9, moved here June 13

    	// operation.regionFor.keyword('expecting').surround[autowrap].surround[indent] // new July 4
    	// operation.regionFor.keyword('delivering').surround[autowrap] // new July 4
/*
    	operation.regionFor.keyword('expecting').surround[indent] // new June 25, no effect in CF QF (?) 	
    	operation.regionFor.keyword('delivering').surround[indent] // new June 25, no effect in CF QF (?)
 */   	
    	operation.requestMessage.format // TODO bug: causes extra line break (sometimes)
    	operation.responseMessage.format // no extra line break here (?)

    	operation.reports.format   
    	operation.policies.format
    	
    	// TODO other spec elements: compensating etc.
	}
	
	def dispatch void format(StatusReports reports, extension IFormattableDocument document) {
		for (StatusReport report : reports.getReportList()){
			// report.surround[autowrap]
			format(report);
		}
	}
	
	def dispatch void format(StatusReport report, extension IFormattableDocument document) {		
		report.reportData.format;
	}
		
	def dispatch void format(SecurityPolicies policies, extension IFormattableDocument document) {
		for (SecurityPolicy policy : policies.getPolicyList()){
			// policy.surround[autowrap]
			format(policy);
		}
	}
	
	def dispatch void format(SecurityPolicy policy, extension IFormattableDocument document) {
		policy.securityObject.format;
	}

	def dispatch void format(Provider p, extension IFormattableDocument document) {
		p.prepend[newLines=2]
    	// p.regionFor.keyword("API").prepend[newLines=1] // no effect? removed June 9
    	// p.regionFor.keyword("API").append[newLines=1] // no effect? removed June 9
    	// p.append[newLine] // not sure about effect
    	
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
			// pb is a poor/wrong name here (in grammar)
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
		// eb is a poor name (in grammar)
		for (HTTPResourceBinding rb : httpb.eb) {
			rb.format;
		}
	}

	def dispatch void format(HTTPResourceBinding httprb, extension IFormattableDocument document) {
		httprb.prepend[newLine];
		// handle tB+=HTTPTypeBinding too? 
		for (HTTPOperationBinding ob : httprb.opsB) {
			ob.format;
		}
		httprb.opsB.format
	}	
	
	def dispatch void format(HTTPOperationBinding hopb, extension IFormattableDocument document) {
		hopb.prepend[newLine];
	}
	
	def dispatch void format(Client c, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting API client");
		
		c.prepend[newLines=2]
    	// c.regionFor.keyword("API").prepend[newLines=1]
    	// c.regionFor.keyword("API").append[newLines=1]
    	// c.append[newLine] // not sure about effect
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
		scenario.prepend[newLines=2] // not sure this is needed, not added in any QF
	}
}
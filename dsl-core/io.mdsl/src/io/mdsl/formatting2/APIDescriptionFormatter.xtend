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

// import io.mdsl.apiDescription.BasicDataType;

class APIDescriptionFormatter extends AbstractFormatter2  {
	
	// TODO (generated): implement for DomainEvents, DomainEvent, CommandTypes, CommandType, ElementStructure, ParameterForest, ChannelContract, BindingParameter, BindingParams, ChannelPathWithParams, RequestReplyChannel, RequestChannel, ReplyChannel, Payload, OneWayChannel, Message, ParameterTreeList, ParameterTree, TreeNode, SingleParameterNode, TypeReference, AtomicParameterList, AtomicParameter, EndpointContract, Operation, StatusReports, StatusReport, SecurityPolicies, SecurityPolicy, DataTransferRepresentation, Provider, EndpointList, EndpointInstance, MessageBroker, AsyncEndpoint, TechnologyBinding, ProtocolBinding, HTTPBinding, HTTPResourceBinding, HTTPTypeBinding, HTTPOperationBinding, MediaTypeList, HTTPParameterBinding, JavaBinding, JavaOperationBinding, GRPCBinding, SLA, SLATemplate, InternalSLA, RateLimit, SLO, Measurement, LandingZone, Client, Consumption, MessageEndpoint, AsyncConsumptionFromBroker, AsyncConsumptionNoProtocolBinding, AsyncConsumptionWithProtocolBinding, WhereConstruct, Gateway, Gate, IntegrationScenario, Orchestration, FlowStep, CombinedInvocationStep, DomainEventProductionStep, CommandInvokationStep, EitherCommandOrOperation, EitherCommandOrOperationInvokation, CommandInvokation, OperationInvokation, SingleOperationInvokation, ConcurrentOperationInvokation, ExclusiveAlternativeOperationInvokation, InclusiveAlternativeOperationInvokation, EventProduction
	// TODO (generated): format HiddenRegions around keywords, attributes, cross references, etc. 
	
	def dispatch void format(ServiceSpecification serviceSpecification, extension IFormattableDocument document) {
		
		for (DataContract dataContract : serviceSpecification.getTypes()) {
			dataContract.format
			dataContract.append[newLine]
		}
		for (EObject eObject : serviceSpecification.getContracts()) {
			// both channel and endpoint contract called
			format(eObject);
		}
		
		// TODO a few missing (see generated APIDescriptionFormatter.java)
		
		for (EObject eObject : serviceSpecification.getProviders()) {
			serviceSpecification.regionFor.keywords('API').forEach [
				prepend[newLines = 1]
				format(eObject);
			]
		}
		for (EObject eObject : serviceSpecification.getClients()) {
			format(eObject);
		}
    }
    
    def dispatch void format(DataContract dc, extension IFormattableDocument document) {
    	// System.out.println("[INFO] Formatting data contract " + dc.name);
    	dc.regionFor.keyword("data").prepend[newLines=1]
    	dc.structure.format
    	// dc.append[newLines=1]
	}
	
	def dispatch void format(DataTransferRepresentation dtr, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting DTR"); // not tested
		dtr.payload.format
	}
	
	def dispatch void format(ElementStructure dStruct, extension IFormattableDocument document) {
    	// System.out.println("[INFO] Formatting element structure (in DTR)");
    	if(dStruct.pf!==null) dStruct.pf.format // if needed?
    	if(dStruct.pt!==null) dStruct.pt.format 
    	if(dStruct.apl!==null) dStruct.apl.format 
    	if(dStruct.np!==null) dStruct.np.format
	}
	 
	def dispatch void format(ParameterTree pt, extension IFormattableDocument document) {
    	pt.regionFor.keyword('}').surround[noSpace] // not sure about this one
    	pt.first.format
    	for (EObject eObject : pt.nexttn) {
			format(eObject);
		}
		pt.card.format
		pt.regionFor.keyword('}').surround[noSpace]
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
	
	def dispatch void format(TypeReference tr, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting type reference in AP"); // TODO not called yet; is rule the right one?
		// classifier not formatted yet (here and elsewhere)
		tr.dcref.format		
		tr.dcref.append[noSpace]
		tr.card.format
		// no effect, trying to remove space from "delivering payload SampleDTO *"
		// tr.regionFor.keyword('*').surround[noSpace]
		// tr.regionFor.keyword('*').prepend[noSpace]
	}
	
	def dispatch void format(AtomicParameterList apl, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting APL in DC");
		// classifier not formatted yet (here and elsewhere)
    	apl.first.format
    	for (EObject eObject : apl.nextap) {
			format(eObject);
		}
 		apl.card.format
	}
	
	def dispatch void format(AtomicParameter ap, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting AP in DC ");
		// classifier not formatted yet (here and elsewhere)
    	ap.rat.format // TODO name?
 		ap.card.format
	}
	
	def dispatch void format(RoleAndType rat, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting role and type in RAT " + rat.name); // not called yet?
		rat.regionFor.keyword(':').surround[noSpace]
		rat.regionFor.keyword('<').surround[noSpace]
		rat.regionFor.keyword('>').surround[noSpace]
		// rat.btype.format // needed? removed June 2
	}
	
	def dispatch void format(Cardinality c, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting cardinality");
		// not working yet:
		// c.prepend[noSpace] // removed June 2
		c.regionFor.keyword('!').surround[noSpace]
		c.regionFor.keyword('+').surround[noSpace]
		c.regionFor.keyword('?').surround[noSpace]
		c.regionFor.keyword('*').prepend[noSpace]
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
		
		// this might cause two tests to fail (that call "save" on resource explicitly):
    	// ec.regionFor.keyword("endpoint").prepend[newLines=1]
    	for (EObject eObject : ec.ops) {
			format(eObject);
		}
		// TODO more contract parts
	}

	def dispatch void format(ChannelContract p, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting AsyncMDSL channel contract");
    	p.regionFor.keyword("channel").prepend[newLines=1] // "2" here yields conflicting channels exception
    	// p.append[newLines=1] // "2" here yields conflicting channels exception
    	
    	p.conversationType.format
	}
	
	// TODO indentation working yet (MDSL does not have block closing keywords, unlike CML)
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
		// channel.prepend[autowrap] // removed June 2
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
    	// op.regionFor.keyword("operation").prepend[newLine]
    	// op.regionFor.keyword("delivering").surround[newLine]
    	// not tried yet: operation.prepend[indent]
    	// System.out.println("[INFO] Formatting operation " + operation.name); // not tested
    	
    	operation.requestMessage.format // TODO bug: causes extra line break
    	operation.responseMessage.format // no extra line break here (?)
    	
    	// TODO other spec elements: reporting, compensating etc.
	}
		
	def dispatch void format(Provider p, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting API provider");
    	p.regionFor.keyword("API").prepend[newLines=1]
    	p.prepend[newLine]
    	p.regionFor.keyword("API").append[newLines=1]
    	p.append[newLine]
	}
	
	// TODO bindings (HTTP in particular)
	
	def dispatch void format(Client c, extension IFormattableDocument document) {
		// System.out.println("[INFO] Formatting API client");
		c.prepend[newLine]
    	c.regionFor.keyword("API").prepend[newLines=1]
    	c.append[newLine]
    	c.regionFor.keyword("API").append[newLines=1]
	}
	
	// TODO flows and scenarios (not all format methods behaving as expected yet)
}

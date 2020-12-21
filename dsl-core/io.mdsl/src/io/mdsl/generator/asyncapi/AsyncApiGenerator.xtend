package io.mdsl.generator.asyncapi

import com.google.common.base.CaseFormat
import com.google.inject.Inject
import io.mdsl.apiDescription.BindingParameter
import io.mdsl.apiDescription.BindingParams
import io.mdsl.apiDescription.BindingValue
import io.mdsl.apiDescription.ChannelContract
import io.mdsl.apiDescription.ChannelPathWithParams
import io.mdsl.apiDescription.DataContract
import io.mdsl.apiDescription.ElementStructure
import io.mdsl.apiDescription.Message
import io.mdsl.apiDescription.MessageBroker
import io.mdsl.apiDescription.OneWayChannel
import io.mdsl.apiDescription.ProtocolBinding
import io.mdsl.apiDescription.RequestReplyChannel
import io.mdsl.apiDescription.ServiceSpecification
import io.mdsl.apiDescription.WhereClauses
import io.mdsl.generator.AbstractMDSLGenerator
import io.mdsl.generator.asyncapi.helpers.AsyncApiGeneratorHelper
import java.util.ArrayList
import java.util.List
import org.apache.commons.lang3.StringUtils
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import java.util.Map
import java.util.HashMap
import java.util.LinkedList

/*
	Test generation using Docker running the following command
	(assuming a "asyncapi.yaml" file exists in the current directory):

	docker run --rm -it \
		-v ${PWD}/asyncapi.yaml:/app/asyncapi.yml \
		-v ${PWD}/output:/app/output \
		asyncapi/generator -o ./output asyncapi.yml @asyncapi/html-template --force-write

	Once complete a "output" folder will be created 
	in the current directory containing the generated skeleton.

	Alternative templates:
		- @asyncapi/java-spring-template
		- @asyncapi/html-template
		- @asyncapi/nodejs-template
*/
class AsyncApiGenerator extends AbstractMDSLGenerator {

	@Inject extension IQualifiedNameProvider
	@Inject AsyncApiDataTypeGenerator dataTypeGenerator;

	override protected generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
	
		if(mdslSpecification.contracts.filter(ChannelContract).length == 0){
			// no messaging contracts found
			return;
		}
		
		val brokers = mdslSpecification.providers.filter(MessageBroker).clone();
		if (brokers.length > 0) {
			// Generate a separate yaml file as they might expose different ChannelContract.
			// Brokers will appear in the same AsyncAPI file iff they share exactly the same ChannelContracts,
			// otherwise they will be splitted across multiple files (which will contain only the channels supported by the supplied brokers).
			// If no broker is specified, a single file will be produced, containing all ChannelContracts.
			
			val brokersChannels = new HashMap<MessageBroker,List<ChannelContract>>();
			brokers.forEach [ broker |
				// create a map of brokers and their supported channels
				brokersChannels.put(broker, broker.epl.flatMap[ep | ep.contracts].toList().sortBy[channel | channel.name]);	
			];
			
			// keep track of the brokers that have already be written in a file
			val alreadyUsedBrokers = new LinkedList<MessageBroker>();
			brokersChannels.forEach[broker, channels, index | 
				
				if(!alreadyUsedBrokers.contains(broker)){
					val brokersThatExposeSameChannels = getBrokersThatExposeSameChannels(broker, brokersChannels);
					
					alreadyUsedBrokers.add(broker);
					alreadyUsedBrokers.addAll(brokersThatExposeSameChannels);
					
					// create a MDSL Specification object with only the needed information
					mdslSpecification.providers.clear();
					mdslSpecification.providers.addAll(brokersThatExposeSameChannels);	
					
					mdslSpecification.contracts.clear();
					mdslSpecification.contracts.addAll(channels);
					
					// output the resulting AsyncAPI specification
					val yamlWithNoTabs = mdslSpecification.compile.toString().replaceAll("\t", "  ");
					val fileName = inputFileURI.trimFileExtension().lastSegment() + "-group-" + index + "-asyncapi.yaml";
					fsa.generateFile(fileName, yamlWithNoTabs);
				
				}
			];
			
		} else {
			// output just one file
			val yamlWithNoTabs = mdslSpecification.compile.toString().replaceAll("\t", "  ");
			val fileName = inputFileURI.trimFileExtension().lastSegment() + "-asyncapi.yaml";
			fsa.generateFile(fileName, yamlWithNoTabs);
		}
	}
	
	/*
	 * Returns all the MessageBrokers that expose the same ChannelContracts as the source broker
	 */
	private def getBrokersThatExposeSameChannels(MessageBroker source, Map<MessageBroker,List<ChannelContract>> context){
		
		val sourceChannels = source.epl.flatMap[ep | ep.contracts].toList().sortBy[channel | channel.name];
		val brokersThatExposeSameChannelsOfSource = new LinkedList<MessageBroker>();
		
		context.forEach[broker, channelsThatBrokerExposes |
			if(sourceChannels.equals(channelsThatBrokerExposes)){
				brokersThatExposeSameChannelsOfSource.add(broker);
			}	
		];
		
		return brokersThatExposeSameChannelsOfSource;
	}

	private def compile(ServiceSpecification serviceSpecificationInstance) '''
		asyncapi: '2.0.0'
		info:
			title: «serviceSpecificationInstance.fullyQualifiedName»
			version: «getValueOrDefault(serviceSpecificationInstance.svi, "Not defined")»
			description: |
				«getValueOrDefault(serviceSpecificationInstance.description, "No description specified")»
		«IF(serviceSpecificationInstance.providers?.filter(MessageBroker).length > 0)»
		servers:
			«FOR broker : AsyncApiGeneratorHelper.getBrokers(serviceSpecificationInstance)»
				«broker.name»:
					url: «broker.url»
					protocol: «broker.protocol»
					description: «broker.description»
					«insertBinding(broker.bindings, broker.protocol)»
			«ENDFOR»
		«ENDIF»
		channels:
			«FOR contract : serviceSpecificationInstance.contracts.filter(ChannelContract)»
				«contract.compile»
			«ENDFOR»    
		components:
			messages:
				«FOR message : serviceSpecificationInstance.eAllContents.filter(Message).toIterable()»
					«message.name»:
						name: «message.name»
						title: «toSpaceBreak(message.name)»
						description: |
							
							«getValueOrDefault(message.description, "No description specified")»
							
							«getRootCardinalityInfo(message.payload.schema.payload)»
							
						«getCorrelationId((message.eContainer() as OneWayChannel).whereClauses)»
						payload:
							«message.payload.schema.payload.compileElementStructureTypeReferenceNoName»
						«getHeaders(message.payload.schema.headers)»
				«ENDFOR»  
				«FOR reqReplyChannel : serviceSpecificationInstance.eAllContents.filter(RequestReplyChannel).toIterable()»
					«reqReplyChannel.request.name»:
						name: «reqReplyChannel.request.name»
						title: «toSpaceBreak(reqReplyChannel.request.name)»
						description: |
							
							«getValueOrDefault(reqReplyChannel.request.description, "No description specified")»

							«getRootCardinalityInfo(reqReplyChannel.request.payload.schema.payload)»
							
							Request message. Reply message is *«reqReplyChannel.reply.name»*. 
						
						«getCorrelationId(reqReplyChannel.request.whereClauses)»
						payload:
							«reqReplyChannel.request.payload.schema.payload.compileElementStructureTypeReferenceNoName»
						«getHeaders(reqReplyChannel.request.payload.schema.headers)»
					«reqReplyChannel.reply.name»:
						name: «reqReplyChannel.reply.name»
						title: «toSpaceBreak(reqReplyChannel.reply.name)»
						description: |

							«getValueOrDefault(reqReplyChannel.reply.description, "No description specified")»
							
							«getRootCardinalityInfo(reqReplyChannel.reply.payload.schema.payload)»
							
							Reply message. Request message is *«reqReplyChannel.request.name»*. 
							
						«getCorrelationId(reqReplyChannel.reply.whereClauses)»
						payload:
							«reqReplyChannel.reply.payload.schema.payload.compileElementStructureTypeReferenceNoName»
						«getHeaders(reqReplyChannel.reply.payload.schema.headers)»
				«ENDFOR»   
			«IF serviceSpecificationInstance.eAllContents.filter(DataContract).size() > 0»
			schemas:
				«FOR dc : serviceSpecificationInstance.eAllContents.filter(DataContract).toIterable()»
					«IF dc.structure !== null»
						«dc.name»:
							«dc.structure.compileElementStructureTypeReferenceNoName»
					«ENDIF»
				«ENDFOR»
			«ENDIF»
	'''
	
	private def getCorrelationId(List<WhereClauses> whereClauses)'''
		«IF AsyncApiGeneratorHelper.getCorrelationId(whereClauses) !== null»
			correlationId:
				location: '«AsyncApiGeneratorHelper.getCorrelationId(whereClauses).source»'
		«ENDIF»
	'''
	
	private def getHeaders(ElementStructure headers) '''
		«IF headers !== null»
			headers:
				 «headers.compile»
		«ENDIF»
	'''
	
	private def getValueOrDefault(String str, String defaultValue){
		if(str !== null && str.length > 0)
			return str.trim();
		return defaultValue;
	}
	
	private def toSpaceBreak(String str){
		val title = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str);
		
		val capitalizedTitle = new ArrayList<String>();
		
		for(String s : title.split("_")){
			capitalizedTitle.add(StringUtils.capitalize(s));
		}
		
		return capitalizedTitle.join(" ");
		
//		return Stream
//			.of(title.split("_"))
//			.map(StringUtils::capitalize) ??
//			.collect(Collectors::joining(" "));
		
	}
	
	private def getClausoles(List<WhereClauses> whereClausoles)'''
		«IF whereClausoles !== null && whereClausoles.length > 0»
			Where:
			«FOR c : whereClausoles»
				- «NodeModelUtils.findActualNodeFor(c).text.trim»
	    	«ENDFOR»
		«ENDIF»
	'''
	
	private def getRootCardinalityInfo(ElementStructure root) '''
		«IF dataTypeGenerator.getRootPayloadCardinality(root).isArray»
			Payload cardinality: 
				- contains always at least one element: «dataTypeGenerator.getRootPayloadCardinality(root).atLeastOne»
		«ENDIF»
	'''

	private def compile(ChannelContract contract) '''
		«IF contract.conversationType instanceof OneWayChannel»
			«(contract.conversationType as OneWayChannel).compile»
		«ENDIF»
		«IF contract.conversationType instanceof RequestReplyChannel»
			«(contract.conversationType as RequestReplyChannel).compile»
		«ENDIF»
	'''
	
	private def insertBinding(BindingParams binding, ProtocolBinding protocol){
		if(binding !== null && protocol !== null){
			return insertBinding(
		    	binding,
		    	AsyncApiGeneratorHelper.getProtocolBindingPropertyName(protocol)
		    );	
		}
	}
	
	private def insertBinding(BindingParams binding, String protocol)'''
		«IF binding !== null && protocol !== null»
		    bindings:
		    	«AsyncApiGeneratorHelper.getProtocolBindingPropertyName(protocol)»:
		    		«FOR p: binding.params»
		    			«p.compile»
		    		«ENDFOR»
	    «ENDIF»
	'''
	
	private def compile(BindingParameter param) '''
		«IF param.value instanceof BindingParams»
			«param.name»:
				«FOR p: (param.value as BindingParams).params»
					«p.compile»
	    		«ENDFOR»
		«ELSE»
		«param.name»:
			«IF (param.value as BindingValue)?.str !== null»
				«(param.value as BindingValue)?.str»
			«ELSEIF (param.value as BindingValue)?.bool !== null»
				«(param.value as BindingValue)?.bool»
			«ELSEIF (param.value as BindingValue).number != 0»
				«(param.value as BindingValue).number»
			«ENDIF»
		«ENDIF»
	'''
	
	private def insertChannelQuality(ChannelContract contract) '''
		Delivering guarantee: «contract.quality».
	'''

	private def compile(OneWayChannel channel) '''
		«channel.path.compile»
			«IF channel.publish»
				publish:
			«ELSEIF channel.subscribe»
				subscribe:
			«ELSE»
				publish:
			«ENDIF»
		    description: | 

		    	«getValueOrDefault(channel.description, "No description specified")»
		    	
		    	«insertChannelQuality(channel.eContainer as ChannelContract)»
		    	«getClausoles(channel.whereClauses)»
		    	
		    	One way channel (does not expect reply).
		    	
		    	«IF channel.acceptsAndProduces !== null»
		    		This channel both produces **and** consumes messages.
		    	«ENDIF»
		    	
		    operationId: «toCamelCase(channel.message.name)»
		    message: 
		    	$ref: '#/components/messages/«channel.message.name»'
		    «insertBinding(channel.bindings, channel.protocol)»
	'''

	private def compile(RequestReplyChannel channel) '''
		«channel.request.path.compile»
			subscribe:
				description: |
					
					«getValueOrDefault(channel.request.description, "No description specified")»
					
					«insertChannelQuality(channel.eContainer as ChannelContract)»
			    	«getClausoles(channel.request.whereClauses)»
					
					Request channel. Reply channel is [«channel.reply.name»](#operation-publish-«channel.reply.path.path»)
			    	
				operationId: «toCamelCase(channel.request.name)»
				message: 
					$ref: '#/components/messages/«channel.request.name»'
				«insertBinding(channel.request.bindings, channel.request.protocol)»
		«channel.reply.path.compile»
			publish:
				description: |
					
					«getValueOrDefault(channel.reply.description, "No description specified")»
					
			    	«getClausoles(channel.reply.whereClauses)»
			    	
					Reply channel. Request channel is [«channel.request.name»](#operation-subscribe-«channel.request.path.path»)
					
				operationId: «toCamelCase(channel.reply.name)»
				message: 
					$ref: '#/components/messages/«channel.reply.name»'
				«insertBinding(channel.reply.bindings, channel.reply.protocol)»
	'''
	
	
	
	private def compile(ChannelPathWithParams channelPath) '''
		«channelPath.path»:
			«IF channelPath.params !== null && channelPath.params.length > 0»
				parameters:
					«FOR p : channelPath.params»
						«p.paramName»:
							description: «p.description»
							schema:
								type: «dataTypeGenerator.getType(p.type)»
					«ENDFOR»
			«ENDIF»
	'''

	private def compile(ElementStructure dto) '''
		«dataTypeGenerator.compile(dto)»
	'''
	
	private def compileElementStructureTypeReferenceNoName(ElementStructure elem) '''
		«IF AsyncApiGeneratorHelper.isRootTypeReference(elem)»
			«dataTypeGenerator.compile(elem.np.tr, false)»
		«ELSE»
			«dataTypeGenerator.compile(elem)»
		«ENDIF»
	'''
	
	private def toCamelCase(String text){
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, text);
	}
	

}

package io.mdsl.generator;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
// import org.eclipse.core.resources.IResource;
// import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
// import org.eclipse.jface.text.ITextSelection;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGeneratorContext;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.ChannelContract;
import io.mdsl.apiDescription.ChannelPathWithParams;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Message;
import io.mdsl.apiDescription.OneWayChannel;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.Payload;
import io.mdsl.apiDescription.ReplyChannel;
import io.mdsl.apiDescription.RequestChannel;
import io.mdsl.apiDescription.RequestReplyChannel;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;

public class AsyncMDSLGenerator extends AbstractMDSLGenerator {
	@Override
	public void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		try {
			MDSLResource sourceSpec = new MDSLResource(resource);
			addChannels(sourceSpec);

			// formatting is required, defaults looks poor after "save"; this requires additional class/Xtend code 
			// https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#formatting
			sourceSpec.save(null);
			
			// this caused entire spec. to go to single line before formatter was introduced:
			// SaveOptions options = SaveOptions.newBuilder().format().getOptions();
			// targetSpec.save(options.toOptionsMap());

		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new MDSLException("Could not save transformed MDSL " + resource.getURI(), e);
		}
	}
	
	private void addChannels(MDSLResource sourceSpec) {
		ServiceSpecification mdslSpecification = sourceSpec.getServiceSpecification();
		for (EndpointContract endpointType : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			transformEndpointTypeIntoAsyncMDSLChannel(mdslSpecification, endpointType);
		}
		
		// TODO (same or separate transformation): also generate broker and endpoints from providers and clients; tbd: gateway? 
	}

	private void transformEndpointTypeIntoAsyncMDSLChannel(ServiceSpecification mdslSpecification,
			EndpointContract endpointType) {
		// TODO change grammar so that channel can have multiple messages and adjust mapping here
		for (Operation operation : endpointType.getOps()) {
			
			DataTransferRepresentation inDtr = operation.getRequestMessage();
			DataTransferRepresentation outDtr = operation.getResponseMessage();
			// TODO check that both messages are there, generate OneWayChannel if not
			
			// TODO also work with headers (not just payload); security and error reporting tbd
			
			String suggestedName = endpointType.getName() + "_" + operation.getName() /* + "Channel" */;
			
			ChannelContract channelContractType = null;
			if(inDtr!=null && outDtr!=null) {
				// we have to copy the two DTRs as the original ones stay in operation:
				DataTransferRepresentation inDtrClone = EcoreUtil.copy(inDtr);
				DataTransferRepresentation outDtrClone = EcoreUtil.copy(outDtr);
				channelContractType = createRequestReplyChannel(suggestedName, operation, inDtrClone, outDtrClone);
			}
			else if(inDtr!=null){
				// we have to copy the two DTRs as the original ones stay in operation:
				DataTransferRepresentation inDtrClone = EcoreUtil.copy(inDtr);
				channelContractType = createOneWayChannel(suggestedName, operation, inDtrClone);
			}
			else { 
				throw new IllegalArgumentException("Operation " + operation.getName() + "in endpoint type " + endpointType.getName() + " does not have a request message.");
			}
			
			// TODO check that no channel/operation of same name exists already
			mdslSpecification.getContracts().add(channelContractType);
		}
	}

	private ChannelContract createOneWayChannel(String suggestedName, Operation operation, DataTransferRepresentation inDtrClone) {
		ChannelContract channelContractType = ApiDescriptionFactory.eINSTANCE.createChannelContract();
		channelContractType.setName(suggestedName);
		
		OneWayChannel owChannel = ApiDescriptionFactory.eINSTANCE.createOneWayChannel();
		
		owChannel.setDescription("One way channel for " + suggestedName);
		owChannel.setPath(createPath(suggestedName + "OneWayChannel"));
		String accAndProd = "'accepts' 'and' 'produces'"; // ignored (grammar bug?)
		owChannel.setAcceptsAndProduces(accAndProd);
		owChannel.setMessage(createMessage(suggestedName + "Message", inDtrClone));
		
		channelContractType.setConversationType(owChannel);
		
		return channelContractType;
	}

	private ChannelContract createRequestReplyChannel(String suggestedName, Operation operation, DataTransferRepresentation inDtrClone, DataTransferRepresentation outDtrClone) {
		ChannelContract channelContractType = ApiDescriptionFactory.eINSTANCE.createChannelContract();
		channelContractType.setName(suggestedName);
		
		RequestReplyChannel rrChannel = ApiDescriptionFactory.eINSTANCE.createRequestReplyChannel();
		setRequestChannel(rrChannel, operation.getName(), inDtrClone);
		setReplyChannel(rrChannel, operation.getName(), outDtrClone);
		channelContractType.setConversationType(rrChannel);
		
		return channelContractType;
	}

	private void setRequestChannel(RequestReplyChannel rrChannel, String name, DataTransferRepresentation requestMessage) {
		if(requestMessage!=null) {
			RequestChannel reqCh = ApiDescriptionFactory.eINSTANCE.createRequestChannel();
			// TODO check that names are unique (entire specification?)
			reqCh.setName(name + "RequestChannel");
			reqCh.setPath(createPath(name + "RequestChannel"));
			reqCh.setPayload(createPayload(requestMessage)); 
			rrChannel.setRequest(reqCh);
		}
		else
			throw new MDSLException(name + " seems to have an empty request payload.");
	}
		
	private void setReplyChannel(RequestReplyChannel rrChannel, String name, DataTransferRepresentation responseMessage) {
		if(responseMessage!=null) {
			ReplyChannel replCh = ApiDescriptionFactory.eINSTANCE.createReplyChannel();
			// TODO check that names are unique (entire specification?)
			replCh.setName(name + "ReplyChannel");
			replCh.setPath(createPath(name + "ReplyChannel"));
			replCh.setPayload(createPayload(responseMessage)); 
			rrChannel.setReply(replCh);
		}
		else
			throw new MDSLException(name + " seems to have an empty response payload.");

	}
	
	private Message createMessage(String name, DataTransferRepresentation messagePayload) {
		Message result = ApiDescriptionFactory.eINSTANCE.createMessage(); 
		result.setName(name);
		result.setPayload(createPayload(messagePayload));
		result.setDeliveringPayload(true);
		return result;
	}
	
	private Payload createPayload(DataTransferRepresentation payload) {
		Payload result = ApiDescriptionFactory.eINSTANCE.createPayload(); 
		result.setSchema(payload);
		return result;
	}
	
	private ChannelPathWithParams createPath(String address) {
		ChannelPathWithParams path = ApiDescriptionFactory.eINSTANCE.createChannelPathWithParams();
		path.setPath("/" + address + "Path"); // could use URN notation instead, or a.b.c
		return path;
	}

	// unused (but must be present):
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
		org.eclipse.emf.common.util.URI inputFileURI) {
		for (EndpointContract endpointType : new ServiceSpecificationAdapter(mdslSpecification).getEndpointContracts()) {
			; 
		}
	}
}

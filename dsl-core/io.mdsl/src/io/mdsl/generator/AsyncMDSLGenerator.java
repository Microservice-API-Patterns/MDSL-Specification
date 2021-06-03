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
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.Payload;
import io.mdsl.apiDescription.ReplyChannel;
import io.mdsl.apiDescription.RequestChannel;
import io.mdsl.apiDescription.RequestReplyChannel;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;

// TODO rename and move operation refactoring in stake branch have utility code from CM
// (to find out about selected EObject etc.)

public class AsyncMDSLGenerator extends AbstractMDSLGenerator {
	@Override
	public void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		try {
			// EObject selectedObject = getSelectedElement();
			// check that selected object is an operation, cast, get its name  
			// if(selectedObject!=null && selectedObject.getClass() == io.mdsl.apiDescription.impl.OperationImpl.class)
			//	opName = ((io.mdsl.apiDescription.impl.OperationImpl) selectedObject).getName();
			// else
			//	throw new MDSLException("Can't refactor: no operation selected.");

			MDSLResource sourceSpec = new MDSLResource(resource);
			addChannels(sourceSpec);

			// TODO (H) format properly, looks poor after "save"
			// requires additional class/Xtend code 
			// https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#formatting
			// https://github.com/ContextMapper/context-mapper-dsl/blob/master/org.contextmapper.dsl/src/org/contextmapper/tactic/dsl/formatting2/TacticDDDLanguageFormatter.xtend
			// this causes entire spec to go to single line:
			// SaveOptions options = SaveOptions.newBuilder().format().getOptions();
			// targetSpec.save(options.toOptionsMap());
			sourceSpec.save(null);
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
		
		// TODO (same or separate transformation): also generate broker and clients
	}

	private void transformEndpointTypeIntoAsyncMDSLChannel(ServiceSpecification mdslSpecification,
			EndpointContract endpointType) {
		// TODO change grammar so that channel can have multiple messages and adjust mapping here
		for (Operation operation : endpointType.getOps()) {
			
			DataTransferRepresentation inDtr = operation.getRequestMessage();
			DataTransferRepresentation outDtr = operation.getResponseMessage();
			// TODO check that both messages are there, generate OneWayChannel if not
			
			// have to copy these (best EMF/XText way chosen?)
			DataTransferRepresentation inDtrClone = EcoreUtil.copy(inDtr);
			DataTransferRepresentation outDtrClone = EcoreUtil.copy(outDtr);
			
			// TODO also work with headers (not just payload); what about security and error reporting?
			ChannelContract channelContractType = ApiDescriptionFactory.eINSTANCE.createChannelContract();
			channelContractType.setName(endpointType.getName() + "_" + operation.getName() /* + "Channel" */);
			RequestReplyChannel rrChannel = ApiDescriptionFactory.eINSTANCE.createRequestReplyChannel();
			setRequestChannel(rrChannel, operation.getName(), inDtrClone);
			setReplyChannel(rrChannel, operation.getName(), outDtrClone);
			channelContractType.setConversationType(rrChannel);
			
			// TODO check that no channel/operation of same name exists already
			mdslSpecification.getContracts().add(channelContractType);
		}
	}

	private void setRequestChannel(RequestReplyChannel rrChannel, String name, DataTransferRepresentation payload) {
		RequestChannel reqCh = ApiDescriptionFactory.eINSTANCE.createRequestChannel();
		reqCh.setName(name + "RequestChannel");
		reqCh.setPath(createPath(name + "RequestChannel"));
		if(payload!=null) 
			reqCh.setPayload(createPayload(payload)); 
		else
			throw new MDSLException(name + " seems to have an empty request payload, which is not yet supported.");
		rrChannel.setRequest(reqCh);
	}
	
	// TODO merge these two methods 
	
	private void setReplyChannel(RequestReplyChannel rrChannel, String name, DataTransferRepresentation payload) {
		ReplyChannel replCh = ApiDescriptionFactory.eINSTANCE.createReplyChannel();
		replCh.setName(name + "ReplyChannel");
		replCh.setPath(createPath(name + "ReplyChannel"));
		if(payload!=null) 
			replCh.setPayload(createPayload(payload)); 
		else
			throw new MDSLException(name + " seems to have an empty response payload, which is not yet supported.");
		rrChannel.setReply(replCh);
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
			System.out.println("About to add a p2p channel definition for endpoint type (in doGenerate)" + endpointType.getName());
		}
	}
}

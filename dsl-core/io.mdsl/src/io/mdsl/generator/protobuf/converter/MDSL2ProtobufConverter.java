package io.mdsl.generator.protobuf.converter;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.microserviceapipatterns.protobufgen.model.Message;
import io.github.microserviceapipatterns.protobufgen.model.ProtoSpec;
import io.github.microserviceapipatterns.protobufgen.model.Service;
import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;

/**
 * Converts MDSL ServiceSpecification to Protocol Buffers (*.proto)
 * 
 */
public class MDSL2ProtobufConverter {

	private ServiceSpecificationAdapter mdslSpecification;
	private ProtoSpec.Builder proto;

	private DataType2MessageConverter dataContractConverter;
	private Endpoint2ServiceConverter endpointConverter;

	public MDSL2ProtobufConverter(ServiceSpecification mdslSpecification) {
		this.mdslSpecification = new ServiceSpecificationAdapter(mdslSpecification);
		this.proto = new ProtoSpec.Builder();
		this.dataContractConverter = new DataType2MessageConverter(proto);
		this.endpointConverter = new Endpoint2ServiceConverter(proto, dataContractConverter);
	}

	public ProtoSpec convert() {
		// messages
		for (Message message : convertDataTypesToMessages()) {
			proto.withMessage(message);
		}

		// TODO: can we identify enums in MDSL?

		// services
		for (Service service : convertEndpointsToServices(proto)) {
			proto.withService(service);
		}

		return proto.withPackage(mdslSpecification.getName()).build();
	}

	/**
	 * convert all data types to messages
	 */
	public List<Message> convertDataTypesToMessages() {
		List<Message> messages = Lists.newLinkedList();
		for (DataContract contract : mdslSpecification.getTypes()) {
			messages.add(dataContractConverter.convert(contract));
		}
		return messages;
	}

	/**
	 * convert all endpoints to services
	 */
	public List<Service> convertEndpointsToServices(ProtoSpec.Builder protoSpec) {
		List<Service> services = Lists.newLinkedList();
		for (EndpointContract endpoint : mdslSpecification.getEndpointContracts())
			services.add(endpointConverter.convert(endpoint));
		return services;
	}

}

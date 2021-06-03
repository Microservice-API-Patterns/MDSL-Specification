package io.mdsl.generator.protobuf.converter;

// import java.util.List;
import java.util.Optional;

import io.github.microserviceapipatterns.protobufgen.model.Message;
import io.github.microserviceapipatterns.protobufgen.model.MessageField;
import io.github.microserviceapipatterns.protobufgen.model.ProtoSpec;
import io.github.microserviceapipatterns.protobufgen.model.RemoteProcedureCall;
import io.github.microserviceapipatterns.protobufgen.model.Service;
import io.github.microserviceapipatterns.protobufgen.model.SimpleFieldType;
import io.mdsl.apiDescription.Cardinality;
// import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.generator.CardinalityHelper;

/**
 * Converts MDSL endpoints to Protocol Buffer services.
 */
public class Endpoint2ServiceConverter {

	private ProtoSpec.Builder protoSpec;
	private DataType2MessageConverter dataTypeConverter;

	public Endpoint2ServiceConverter(ProtoSpec.Builder protoSpec, DataType2MessageConverter dataTypeConverter) {
		this.protoSpec = protoSpec;
		this.dataTypeConverter = dataTypeConverter;
	}

	public Service convert(EndpointContract endpoint) {
		Service.Builder service = new Service.Builder(endpoint.getName());
		for (Operation operation : endpoint.getOps()) {
			service.withRPC(convertOperation(operation));
		}
		return service.build();
	}

	private RemoteProcedureCall convertOperation(Operation operation) {
		Message input = null;
		Message output = null;
		if (operation.getRequestMessage() != null) {
			// handle references specially: in this case we can assume the message as
			// already been created
			if (operation.getRequestMessage().getPayload().getNp() != null
					&& operation.getRequestMessage().getPayload().getNp().getTr() != null) {
				TypeReference ref = operation.getRequestMessage().getPayload().getNp().getTr();
				input = wrapMessageIntoListTypeIfNecessary(getExistingMessageOrCreateEmpty(ref.getDcref().getName()),
						ref.getCard());
			} else {
				ElementStructure payload = operation.getRequestMessage().getPayload();
				input = wrapMessageIntoListTypeIfNecessary(
						createNewMessage(operation.getName() + "RequestMessage", payload),
						getCardinality4ElementStructure(payload));
			}
		}
		if (operation.getResponseMessage() != null) {
			// handle references specially: in this case we can assume the message as
			// already been created
			if (operation.getResponseMessage().getPayload().getNp() != null
					&& operation.getResponseMessage().getPayload().getNp().getTr() != null) {
				TypeReference ref = operation.getResponseMessage().getPayload().getNp().getTr();
				output = wrapMessageIntoListTypeIfNecessary(getExistingMessageOrCreateEmpty(ref.getDcref().getName()),
						ref.getCard());
			} else {
				ElementStructure payload = operation.getResponseMessage().getPayload();
				output = wrapMessageIntoListTypeIfNecessary(
						createNewMessage(operation.getName().substring(0, 1).toUpperCase()
								+ operation.getName().substring(1) + "ResponseMessage", payload),
						getCardinality4ElementStructure(payload));
			}
		}

		if (input == null)
			input = getExistingMessageOrCreateEmpty(operation.getName().substring(0, 1).toUpperCase()
					+ operation.getName().substring(1) + "RequestMessage");
		if (output == null) {
			output = getExistingMessageOrCreateEmpty("VoidResponseMessage");
		}

		return new RemoteProcedureCall.Builder(operation.getName(), input, output)
				// .withInputAsStream() TODO Olaf: here you set the "stream" flag for the input
				// message (heuristic needed here)
				// .withOutputAsStream() TODO Olaf: here you set the "stream" flag for the
				// output message (heuristic needed here)
				.build();
	}

	private Message getExistingMessageOrCreateEmpty(String name) {
		Optional<Message> optMessage = this.protoSpec.build().getMessages().stream()
				.filter(m -> m.getName().equals(name)).findFirst();
		if (optMessage.isPresent()) {
			return optMessage.get();
		} else {
			Message.Builder message = new Message.Builder(name);
			this.protoSpec.withMessage(message);
			return message.build();
		}
	}

	private Message createNewMessage(String name, ElementStructure elementStructure) {
		Message.Builder message = new Message.Builder(name);
		this.dataTypeConverter.mapElementStructure(elementStructure, message);
		this.protoSpec.withMessage(message);
		return message.build();
	}

	private Message wrapMessageIntoListTypeIfNecessary(Message message, Cardinality card) {
		if (CardinalityHelper.isList(card)) {
			Optional<Message> alreadyExistingList = getMessageIfAlreadyExists(message.getSimpleName() + "List");
			if (alreadyExistingList.isPresent())
				return alreadyExistingList.get();

			Message.Builder wrapper = new Message.Builder(message.getSimpleName() + "List");
			wrapper.withField(new MessageField.Builder(message, "entries", 1).repeated().build());
			this.protoSpec.withMessage(wrapper);
			return wrapper.build();
		} else if (CardinalityHelper.isOptional(card)) {
			Optional<Message> alreadyExistingOptionalType = getMessageIfAlreadyExists(
					message.getSimpleName() + "Optional");
			if (alreadyExistingOptionalType.isPresent())
				return alreadyExistingOptionalType.get();

			Message.Builder wrapper = new Message.Builder(message.getSimpleName() + "Optional");
			wrapper.withField(new MessageField.Builder(message, "value", 2).build());
			wrapper.withField(new MessageField.Builder(SimpleFieldType.BOOL, "has_value", 1).build());
			this.protoSpec.withMessage(wrapper);
			return wrapper.build();
		}

		return message;
	}

	private Optional<Message> getMessageIfAlreadyExists(String name) {
		return protoSpec.build().getMessages().stream().filter(m -> m.getName().equals(name)).findFirst();
	}

	private Cardinality getCardinality4ElementStructure(ElementStructure elementStructure) {
		if (elementStructure.getPt() != null) {
			return elementStructure.getPt().getCard();
		} else if (elementStructure.getApl() != null) {
			return elementStructure.getApl().getCard();
		} else if (elementStructure.getNp() != null) {
			if (elementStructure.getNp().getAtomP() != null) {
				return elementStructure.getNp().getAtomP().getCard();
			} else if (elementStructure.getNp().getTr() != null) {
				return elementStructure.getNp().getTr().getCard();
			}
		}
		return null;
	}

}

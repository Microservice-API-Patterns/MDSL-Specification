package io.mdsl.validation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.AsyncConsumptionFromBroker;
import io.mdsl.apiDescription.AsyncConsumptionWithProtocolBinding;
import io.mdsl.apiDescription.MessageEndpoint;

public class MessageEndpointValidator extends AbstractMDSLValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	@Check
	public void checkEndpointHasAtLeastOneChannel(MessageEndpoint messageEndpoint) {

		EList<AsyncConsumptionFromBroker> channelsFromBrokers = messageEndpoint.getChannels();
		EList<AsyncConsumptionWithProtocolBinding> channelsWithoutBrokers = messageEndpoint.getChannelsNoBroker();

		if ((channelsFromBrokers == null || channelsFromBrokers.size() == 0)
				&& (channelsWithoutBrokers == null || channelsWithoutBrokers.size() == 0)) {
			error("Message Endpoint '" + messageEndpoint.getName()
					+ "' does not contain any channels. Consider adding some.", messageEndpoint,
					ApiDescriptionPackage.eINSTANCE.getMessageEndpoint_Name()); // ()Literals.MESSAGE_ENDPOINT__NAME);
		}

	}

}

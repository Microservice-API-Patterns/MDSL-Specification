package io.mdsl.validation;

import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.Message;
import io.mdsl.apiDescription.OneWayChannel;

public class OneWayChannelValidator extends AbstractMDSLValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	@Check
	public void checkOneWay(OneWayChannel channel) {

		Message msg = channel.getMessage();

		if (channel.isPublish() && msg.isExpectingPayload()) {
			error("Message '" + msg.getName() + "' is defined as a PUBLISH channel. Change 'expecting' to 'delivering'.",
					msg, ApiDescriptionPackage.Literals.MESSAGE__EXPECTING_PAYLOAD);
		}

		if (channel.isSubscribe() && msg.isDeliveringPayload()) {
			error("Message '" + msg.getName()
					+ "' is defined as a SUBSCRIBE channel. Change 'delivering' to 'expecting'.", msg,
					ApiDescriptionPackage.Literals.MESSAGE__DELIVERING_PAYLOAD);
		}
		
		if (channel.isPublish() && msg.isExpectingPayload()) {
			error("Message '" + msg.getName() + "' is defined as a PUBLISH channel. Change 'expecting' to 'delivering'.",
					msg, ApiDescriptionPackage.Literals.MESSAGE__EXPECTING_PAYLOAD);
		}

		if (channel.isSubscribe() && msg.isDeliveringPayload()) {
			error("Message '" + msg.getName()
					+ "' is defined as a SUBSCRIBE channel. Change 'delivering' to 'expecting'.", msg,
					ApiDescriptionPackage.Literals.MESSAGE__DELIVERING_PAYLOAD);
		}

	}

}

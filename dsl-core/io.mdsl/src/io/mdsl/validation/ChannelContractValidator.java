package io.mdsl.validation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.Channel;
import io.mdsl.apiDescription.ChannelContract;
import io.mdsl.apiDescription.ChannelPathWithParams;
import io.mdsl.apiDescription.ChannelType;
import io.mdsl.apiDescription.DeliveryGuarantee;
import io.mdsl.apiDescription.OneWayChannel;
import io.mdsl.apiDescription.RequestReplyChannel;
import io.mdsl.apiDescription.ServiceSpecification;

public class ChannelContractValidator extends AbstractMDSLValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	@Check
	public void checkChannelsPathAreUnique(ServiceSpecification root) {

		HashSet<String> channelPaths = new HashSet<String>();

		List<ChannelPathWithParams> channels = EcoreUtil2.getAllContentsOfType(root, ChannelPathWithParams.class);
		for (ChannelPathWithParams channel : channels) {
			if (!channelPaths.add(channel.getPath())) {
				error("Duplicate channel path '" + channel.getPath() + "'", channel,
						ApiDescriptionPackage.eINSTANCE.getChannelPathWithParams_Path()); // Literals.CHANNEL_PATH_WITH_PARAMS__PATH);
			}
		}
	}

	@Check
	public void checkMessageNamesAreUnique(ServiceSpecification root) {

		HashSet<String> messageNames = new HashSet<String>();

		List<Channel> channels = EcoreUtil2.getAllContentsOfType(root, Channel.class);
		for (Channel channel : channels) {
			if (!messageNames.add(channel.getName())) {
				error("Duplicate channel name '" + channel.getName() + "'", channel,
						ApiDescriptionPackage.eINSTANCE.getChannel_Name()); // Literals.CHANNEL__NAME);
			}
		}
	}

	@Check
	public void checkRequestReplyChannelTypes(ChannelContract channel) {

		if (channel.getConversationType() instanceof RequestReplyChannel) {

			List<String> notAllowedTypesInRequestReply = new LinkedList<String>();
			notAllowedTypesInRequestReply.add(ChannelType.PUBLISH_SUBSCRIBE.getName());
			notAllowedTypesInRequestReply.add(ChannelType.INVALID_MESSAGE.getName());
			notAllowedTypesInRequestReply.add(ChannelType.DEAD_LETTER.getName());

			this.setValidationErrorForChannelWithType(channel, notAllowedTypesInRequestReply, "REQUEST_REPLY");
		}
	}

	@Check
	public void checkChannelTypes(ChannelContract channel) {

		List<String> channelTypes = channel.getTypes().stream().map(t -> t.getName()).collect(Collectors.toList());

		if (channelTypes.contains(ChannelType.POINT_TO_POINT.getName())) {

			List<String> notAllowedTypesWithPointToPoint = new LinkedList<String>();
			notAllowedTypesWithPointToPoint.add(ChannelType.PUBLISH_SUBSCRIBE.getName());
			notAllowedTypesWithPointToPoint.add(ChannelType.INVALID_MESSAGE.getName());
			notAllowedTypesWithPointToPoint.add(ChannelType.DEAD_LETTER.getName());

			this.setValidationErrorForChannelWithType(channel, notAllowedTypesWithPointToPoint,
					ChannelType.POINT_TO_POINT.getName());
		} else if (channelTypes.contains(ChannelType.PUBLISH_SUBSCRIBE.getName())) {

			List<String> notAllowedTypesWithPublishSubscribe = new LinkedList<String>();
			notAllowedTypesWithPublishSubscribe.add(ChannelType.POINT_TO_POINT.getName());
			notAllowedTypesWithPublishSubscribe.add(ChannelType.INVALID_MESSAGE.getName());
			notAllowedTypesWithPublishSubscribe.add(ChannelType.DEAD_LETTER.getName());

			this.setValidationErrorForChannelWithType(channel, notAllowedTypesWithPublishSubscribe,
					ChannelType.PUBLISH_SUBSCRIBE.getName());

		} else if (channelTypes.contains(ChannelType.DEAD_LETTER.getName())) {

			List<String> notAllowedTypesWithDeadLetter = new LinkedList<String>();
			notAllowedTypesWithDeadLetter.add(ChannelType.POINT_TO_POINT.getName());
			notAllowedTypesWithDeadLetter.add(ChannelType.INVALID_MESSAGE.getName());
			notAllowedTypesWithDeadLetter.add(ChannelType.PUBLISH_SUBSCRIBE.getName());
			notAllowedTypesWithDeadLetter.add(ChannelType.DATA_TYPE.getName());
			notAllowedTypesWithDeadLetter.add(ChannelType.GUARANTEED_DELIVERY.getName());

			this.setValidationErrorForChannelWithType(channel, notAllowedTypesWithDeadLetter,
					ChannelType.DEAD_LETTER.getName());

		} else if (channelTypes.contains(ChannelType.INVALID_MESSAGE.getName())) {

			List<String> notAllowedTypesWithInvalidMessage = new LinkedList<String>();
			notAllowedTypesWithInvalidMessage.add(ChannelType.POINT_TO_POINT.getName());
			notAllowedTypesWithInvalidMessage.add(ChannelType.DEAD_LETTER.getName());
			notAllowedTypesWithInvalidMessage.add(ChannelType.PUBLISH_SUBSCRIBE.getName());
			notAllowedTypesWithInvalidMessage.add(ChannelType.DATA_TYPE.getName());
			notAllowedTypesWithInvalidMessage.add(ChannelType.GUARANTEED_DELIVERY.getName());

			this.setValidationErrorForChannelWithType(channel, notAllowedTypesWithInvalidMessage,
					ChannelType.INVALID_MESSAGE.getName());
		}
	}

	private void setValidationErrorForChannelWithType(ChannelContract channel, List<String> notAllowedTypes,
			String mainType) {
		List<String> channelTypes = channel.getTypes().stream().map(t -> t.getName()).collect(Collectors.toList());
		for (String notAllowedType : notAllowedTypes) {
			if (channelTypes.contains(notAllowedType)) {
				error("The '" + mainType + "' Channel '" + channel.getName() + "' can not be also of type '"
						+ notAllowedType + "'. Consider removing this type.", channel,
						ApiDescriptionPackage.eINSTANCE.getChannelContract_Types()); // Literals.CHANNEL_CONTRACT__TYPES, channelTypes.indexOf(notAllowedType));
			}
		}
	}

	@Check
	public void checkDuplicateChannelTypes(ChannelContract channel) {

		List<String> channelTypes = channel.getTypes().stream().map(t -> t.getName()).collect(Collectors.toList());
		HashSet<String> channelTypesSet = new HashSet<String>();

		for (String channelType : channelTypes) {
			if (!channelTypesSet.add(channelType)) {
				error("The Channel '" + channel.getName() + "' already has type '" + channelType
						+ "'. Consider removing this duplicate type.", channel,
						ApiDescriptionPackage.eINSTANCE.getChannelContract_Types()); // Literals.CHANNEL_CONTRACT__TYPES, channelTypes.lastIndexOf(channelType));
			}
		}

	}

	@Check
	public void checkMissingParamsInOneWayChannelPath(ChannelContract channel) {

		String channelName;
		ChannelPathWithParams path;

		if (channel.getConversationType() instanceof OneWayChannel) {
			channelName = channel.getName();
			path = ((OneWayChannel) channel.getConversationType()).getPath();
			
			this.checkMissingParameterInChannelPathError(channelName, path);
		}
	}

	@Check
	public void checkMissingParamsInRequestReplyChannelPath(RequestReplyChannel channel) {
		this.checkMissingParameterInChannelPathError(channel.getRequest().getName(), channel.getRequest().getPath());
		this.checkMissingParameterInChannelPathError(channel.getReply().getName(), channel.getReply().getPath());
	}
	
	private void checkMissingParameterInChannelPathError(String channelName, ChannelPathWithParams src) {
		//if(src.getPath().matches("\\$\\{\\w*\\}")) { // [GDL] TODO: why this does not work?
		if (src.getPath().contains("${")) {
			if (src.getParams() == null || src.getParams().size() == 0) {
				error("The Channel '" + channelName
						+ "' contains parameters in the path. Consider adding the description of those using 'with paramName: type, \"Parameter description\"'",
						src, ApiDescriptionPackage.eINSTANCE.getChannelPathWithParams_Path()); // Literals.CHANNEL_PATH_WITH_PARAMS__PATH);
			}
		}

	}
	
	@Check
	public void checkDeliveryGuaranteeConsistency(ChannelContract channel){
		
		// https://github.com/Microservice-API-Patterns/MDSL-Specification/issues/65
		// GUARANTEED_DELIVERY is only used in combination with the delivery guarantee set to AT_LEAST_ONCE or EXACTLY_ONCE
		
		List<ChannelType> channelTypes = channel.getTypes();
		
		if (channelTypes.stream().anyMatch(t -> t.getValue() == ChannelType.GUARANTEED_DELIVERY_VALUE)) {
			
			int deliveryGuarantee = channel.getQuality().getValue();
			
			// @gdl: added this warning (instead of error)
			if(deliveryGuarantee == DeliveryGuarantee.UNKNOWN_VALUE) {
				warning("GUARANTEED_DELIVERY channel '" + channel.getName() + "' should have a delivery guarantee of type AT_LEAST_ONCE or EXACTLY_ONCE.",
						channel,
						ApiDescriptionPackage.eINSTANCE.getChannelContract_Types(),
						channelTypes.indexOf(channelTypes.stream().filter(t -> t.getValue() == ChannelType.GUARANTEED_DELIVERY_VALUE).findFirst().get()));
			}
			else {
				boolean hasInconsistency  = 
						// deliveryGuarantee != DeliveryGuarantee.UNKNOWN_VALUE && 
						deliveryGuarantee != DeliveryGuarantee.AT_LEAST_ONCE_VALUE &&
						deliveryGuarantee != DeliveryGuarantee.EXACTLY_ONCE_VALUE;

				if(hasInconsistency) {
					error("GUARANTEED_DELIVERY channel '" + channel.getName() + "' must have a delivery guarantee of type AT_LEAST_ONCE or EXACTLY_ONCE.",
							channel,
							ApiDescriptionPackage.eINSTANCE.getChannelContract_Types(),
							channelTypes.indexOf(channelTypes.stream().filter(t -> t.getValue() == ChannelType.GUARANTEED_DELIVERY_VALUE).findFirst().get())); 

					/* @gdl: tbc: is one message enough?
					error("'" + channel.getQuality().getName() +  "' can not be assigned to a channel of type GUARANTEED_DELIVERY.",
						channel,
						ApiDescriptionPackage.eINSTANCE.getChannelContract_Quality());
					 */
				}
			}
		}
	}
}

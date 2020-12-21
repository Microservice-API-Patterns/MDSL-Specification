package io.mdsl.generator.asyncapi.helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.mdsl.apiDescription.AsyncEndpoint;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.CorrelationIdWhereClause;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.MessageBroker;
import io.mdsl.apiDescription.ParameterForest;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.ProtocolBinding;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.apiDescription.WhereClauses;
import io.mdsl.generator.asyncapi.models.MessageBrokerDto;

public class AsyncApiGeneratorHelper {

	/**
	 * Returns the list of brokers, one for each offered location
	 * 
	 * @param sp The serviceSpecification root object
	 * @return The list of brokers, one for each offered location
	 */
	public static List<MessageBrokerDto> getBrokers(ServiceSpecification sp) {

		List<MessageBrokerDto> map = new LinkedList<MessageBrokerDto>();

		sp.getProviders().stream().filter(item -> item instanceof MessageBroker).map(p -> (MessageBroker) p)
				.forEach(msgprovider -> {
					// for each MessageBroker
					int brokerLocationsCount = 0; // avoid broker names clashes
					for (AsyncEndpoint l : msgprovider.getEpl()) {
						// a broker may offer multiple channels under multiple location.
						// for each different location, create a separate broker (as needed in AsyncAPI)
						brokerLocationsCount++;

						MessageBrokerDto b = new MessageBrokerDto(
								brokerLocationsCount > 1 ? msgprovider.getName() + brokerLocationsCount
										: msgprovider.getName(),
								l.getLocation(), getProtocolAsString(l.getPb().getProtBinding()),
								msgprovider.getDescription() != null ? msgprovider.getDescription()
										: "No description specified",
								l.getBindings());

						map.add(b);
					}
				});

		return map;

	}

	/**
	 * Return the first non-null protocol as a string
	 * 
	 * @param pb The protocol container
	 * @return The first non-null protocol as a string
	 */
	public static String getProtocolAsString(ProtocolBinding pb) {

		if (pb.getGrpc() != null)
			return pb.getGrpc().getBinding();
		if (pb.getHttp() != null)
			return pb.getHttp().getHttp();
		if (pb.getJava() != null)
			return pb.getJava().getBinding();

		if (pb.getOther() != null) {
			if (pb.getOther().getAmqp() != null)
				return pb.getOther().getAmqp();
			
			if (pb.getOther().getAvro() != null)
				return pb.getOther().getAvro();
			
			if (pb.getOther().getJms() != null)
				return pb.getOther().getJms();
			
			if (pb.getOther().getKafka() != null)
				return pb.getOther().getKafka();
			
			if (pb.getOther().getMqtt() != null)
				return pb.getOther().getMqtt();
			
			if (pb.getOther().getOther() != null)
				return pb.getOther().getOther();
			
			if (pb.getOther().getSoap() != null)
				return pb.getOther().getSoap();
			
			if (pb.getOther().getStomp() != null)
				return pb.getOther().getStomp();
			
			if (pb.getOther().getThrift() != null)
				return pb.getOther().getThrift();

			// TODO JSON-RPC, Web_Sockets (grammar extended!)
		}

		return "Unknown protocol";

	}

	/**
	 * Maps our grammar enum values of the transport protocols to the ones defined
	 * in AsyncAPI.
	 * 
	 * @param transportProtocol The protocol
	 * @return The corresponding protocol name as defined in AsyncAPI
	 */
	public static String getProtocolBindingPropertyName(ProtocolBinding transportProtocol) {
		return getProtocolBindingPropertyName(getProtocolAsString(transportProtocol));
	}

	/**
	 * Maps our grammar enum values of the transport protocols to the ones defined
	 * in AsyncAPI.
	 * 
	 * @param protocol The string protocol
	 * @return The corresponding protocol name as defined in AsyncAPI
	 */
	public static String getProtocolBindingPropertyName(String protocol) {

		switch (protocol) {
		case "JMS_ActiveMQ":
			return "jms";
		default:
			return protocol.toLowerCase();
		}
	}

	/**
	 * Return the first occurrence of a CorrelationIdWhereClausole or null.
	 * 
	 * @param whereClauses The clauses to search in
	 * @return The first occurrence of a CorrelationIdWhereClausole or null.
	 */
	public static CorrelationIdWhereClause getCorrelationId(List<WhereClauses> whereClauses) {

		if (whereClauses != null) {
			for (WhereClauses wc : whereClauses) {
				if (wc instanceof CorrelationIdWhereClause) {
					return (CorrelationIdWhereClause) wc;
				}
			}
		}

		return null;

	}

	/**
	 * Returns true if the given cardinality contains isAtLeastOne (+) OR zeroOrMore
	 * (*)
	 * 
	 * @param card The cardinality to check
	 * @return True if card contains '+' OR '*'
	 */
	public static boolean isArray(Cardinality card) {
		if (card == null)
			return false;

		return card.getAtLeastOne() != null || card.getZeroOrMore() != null;
	}

	/**
	 * Returns the list of root trees contained in a parameterForest.
	 * 
	 * @param pf The parameterForest
	 * @return The list of root trees
	 */
	public static List<ParameterTree> getRootTrees(ParameterForest pf) {
		List<ParameterTree> trees = new ArrayList<ParameterTree>();
		trees.add(pf.getPtl().getFirst());
		trees.addAll(pf.getPtl().getNext());
		return trees;
	}

	/**
	 * Return true if the given elementStructure is composed by a single type
	 * reference at the root level.
	 * 
	 * @param elemStr The elementStructure to check
	 * @return true if the elementStructure is only a typeReference, false otherwise
	 */
	public static boolean isRootTypeReference(ElementStructure elemStr) {
		return elemStr != null && elemStr.getNp() != null && elemStr.getNp().getTr() != null;
	}

}

package io.mdsl.generator;

import io.mdsl.apiDescription.Cardinality;

public class CardinalityHelper {

	public static boolean isList(Cardinality card) {
		if (card == null)
			return false;
		return (card.getZeroOrMore() != null && "*".equals(card.getZeroOrMore()))
				|| (card.getAtLeastOne() != null && "+".equals(card.getAtLeastOne()));
	}

	public static boolean isOptional(Cardinality card) {
		if (card == null)
			return false;
		return card.getZeroOrOne() != null && "?".equals(card.getZeroOrOne());
	}

}

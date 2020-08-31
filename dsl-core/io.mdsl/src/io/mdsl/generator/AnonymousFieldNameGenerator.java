package io.mdsl.generator;

/**
 * Generates field names by using a counter.
 */
public class AnonymousFieldNameGenerator {

	private int counter = 1;

	private boolean isStringDefined(String name) {
		return name != null && !"".equals(name);
	}

	public String getUniqueName(String baseName) {
		// in case there is a name, just take it
		if (isStringDefined(baseName))
			return baseName;

		// in case there is no name, generate a unique "anonymous" name
		String genName = "anonymous" + counter;
		counter++;
		return genName;
	}

}

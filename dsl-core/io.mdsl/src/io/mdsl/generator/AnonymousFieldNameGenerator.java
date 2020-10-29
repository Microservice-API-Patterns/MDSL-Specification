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
		String name = baseName != null ? baseName.trim() : baseName;

		// in case there is a name, just take it (formatted and without special characters)
		if (isStringDefined(name))
			return format(baseName);

		// in case there is no name, generate a unique "anonymous" name
		String genName = "anonymous" + counter;
		counter++;
		return genName;
	}

	private String format(String baseName) {
		String name = baseName;
		if (name.substring(0, 1).matches("^[0-9]"))
			name = "_" + name;
		return name.replaceAll("[^A-Za-z0-9_]", "");
	}

}

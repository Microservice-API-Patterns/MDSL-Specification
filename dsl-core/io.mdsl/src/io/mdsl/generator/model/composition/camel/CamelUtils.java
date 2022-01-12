package io.mdsl.generator.model.composition.camel;

public class CamelUtils {
	public String headerPrefix() {
		// easier to do in here because $ and { are used both by Camel and by Freemarker:
		return "${header.";
	}
}

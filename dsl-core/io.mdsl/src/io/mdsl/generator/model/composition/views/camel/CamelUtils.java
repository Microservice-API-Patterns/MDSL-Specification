package io.mdsl.generator.model.composition.views.camel;

public class CamelUtils {
	public String headerPrefix() {
		// done here because $ and { are used both by Camel and by Freemarker:
		return "${header.";
	}
}

/*
 * generated by Xtext 2.20.0
 */
package io.mdsl;

/**
 * Initialization support for running Xtext languages without Equinox extension
 * registry.
 */
public class APIDescriptionStandaloneSetup extends APIDescriptionStandaloneSetupGenerated {

	public static void doSetup() {
		new APIDescriptionStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

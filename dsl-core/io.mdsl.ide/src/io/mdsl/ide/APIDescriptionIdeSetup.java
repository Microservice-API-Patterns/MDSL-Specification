/*
 * generated by Xtext 2.35.0
 */
package io.mdsl.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.mdsl.APIDescriptionRuntimeModule;
import io.mdsl.APIDescriptionStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class APIDescriptionIdeSetup extends APIDescriptionStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new APIDescriptionRuntimeModule(), new APIDescriptionIdeModule()));
	}
	
}

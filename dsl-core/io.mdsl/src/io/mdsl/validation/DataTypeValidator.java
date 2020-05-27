/*
 * generated by Xtext 2.20.0
 */
package io.mdsl.validation;

import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.genericParameter;
import io.mdsl.apiDescription.roleAndType;
import io.mdsl.apiDescription.serviceSpecification;

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class DataTypeValidator extends AbstractAPIDescriptionValidator {
	
	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	@Check
	public void dataContractValidator(final serviceSpecification specRoot) {
		warning("FYI: not checking every semantic rule in " + specRoot.getName() +  " yet", specRoot, ApiDescriptionPackage.Literals.SERVICE_SPECIFICATION__NAME);
	}
	
	@Check
	public void checkRoleAndType(final roleAndType rat) {
		String role = rat.getRole();
		String basicType = rat.getBtype();
		if(role.equals("ID") && basicType.equals("bool")) {
			warning("The role-type combination ID<bool> does not make much sense. Try ID<String> or ID<int>.", rat, ApiDescriptionPackage.Literals.ROLE_AND_TYPE__BTYPE);
	    }
	}
	
	// TODO should also warn about incomplete APs such as D (?), quick fix: turn into D<string>
	
	@Check
	public void checkIncompleteTypeInformation(final genericParameter gp) {
		if(gp.getName() != null) {
			warning(gp.getName() + " is a generic parameter. You might want to provide a full identfier-role-type triple.", gp, ApiDescriptionPackage.Literals.GENERIC_PARAMETER__P);
		}
		else {
			warning("This is a generic parameter. You might want to provide a full identfier-role-type triple. See <a href=\"https://microservice-api-patterns.github.io/MDSL-Specification/datacontract\">MDSL documentation</a>.", gp, ApiDescriptionPackage.Literals.GENERIC_PARAMETER__P);
		}
	}

	/* PoC (no longer works as grammar has changed):
	@Check
	public void checkVersionNameStartsWithCapital(serviceSpecification specRoot) {

		 * if (specRoot.getSvi() != null && specRoot.getSvi().getName() != null &&
		 * !specRoot.getSvi().getName().startsWith("v")) {
		 * warning("The version name (if present) should start with a lower case 'v')",
		 * specRoot, ApiDescriptionPackage.Literals.SERVICE_SPECIFICATION__SVI); }
	}
	*/
}
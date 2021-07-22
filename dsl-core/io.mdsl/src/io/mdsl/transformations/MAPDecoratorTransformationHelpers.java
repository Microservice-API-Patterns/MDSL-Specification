package io.mdsl.transformations;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;

public class MAPDecoratorTransformationHelpers {
	public static void setRoleToProcessingResource(EndpointContract etype) {
		etype.setPrimaryRole("PROCESSING_RESOURCE");
	}
	
	public static void deriveResponsibilityFromName(Operation operation, String name) {
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		if(name.startsWith("read"))
			or.setRo("RO");
		else if(name.startsWith("search"))
			or.setRo("RO");
		else if(name.startsWith("lookup"))
			or.setRo("RO");
		else if(name.startsWith("create"))
			or.setSco("SCO");
		// TODO add "setup"?
		else if(name.startsWith("update"))
			or.setSto("STO");
		else if(name.startsWith("modify"))
			or.setSto("STO");
		else if(name.startsWith("replace"))
			or.setSro("SRO");
		else if(name.startsWith("delete"))
			or.setSdo("SDO");
		else if(name.startsWith("add"))
			or.setSto("Collection_Operation");
		else if(name.startsWith("remove"))
			or.setSto("Collection_Operation");
		// TODO tbd how to indicate stateless COMPUTATION_FUNCTION?
		else
			or.setOther("UnspecifiedResponsibility"); // ignore, just a flag
		
		if(or.getOther()==null)
			operation.setResponsibility(or);
	}
}

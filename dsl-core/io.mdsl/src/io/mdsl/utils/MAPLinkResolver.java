package io.mdsl.utils;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.RoleAndType;

public class MAPLinkResolver {
	public static String unquoteString(String string) {
		if (string == null || "".equals(string))
			return "";
		return string.replace("\"", "");
	}
	
	public static String explainRolePattern(EndpointContract mdslEndpoint) {
		String role1 = mdslEndpoint.getPrimaryRole();
		
		// TODO (L) could also work with additional roles ("and" in grammar)	
		if (role1 == null)
			return null;

		if (role1.equals("PROCESSING_RESOURCE"))
			return "activity-oriented endpoint";
		else if (role1.equals("INFORMATION_HOLDER_RESOURCE"))
			return "general data-oriented endpoint";
		else if (role1.equals("OPERATIONAL_DATA_HOLDER"))
			return "data-oriented endpoint, short-lived";
		else if (role1.equals("MASTER_DATA_HOLDER"))
			return "data-oriented endpoint, long-lived";
		else if (role1.equals("REFERENCE_DATA_HOLDER"))
			return "data-oriented endpoint, immutable";
		else if (role1.equals("DATA_TRANSFER_RESOURCE"))
			return "data-oriented endpoint, temporary storage";
		else if (role1.equals("LINK_LOOKUP_RESOURCE"))
			return "data-oriented endpoint: resource directory";

		return "main repsonsibility: " + role1; // free form responsibility (?)
	}

	public static String provideLinktoMAPWebsite(EndpointContract mdslEndpointType) {
		String role1 = mdslEndpointType.getPrimaryRole();
		String uri = "https://microservice-api-patterns.org/patterns/responsibility/";

		if (role1 == null)
			return "";
		
		if(role1.equals("PROCESSING_RESOURCE")) {
			return uri + "endpointRoles/ProcessingResource.html";
		}
		else if(role1.equals("INFORMATION_HOLDER_RESOURCE")) {
			return uri + "endpointRoles/InformationHolderResource.html";
		}
		else if(role1.equals("OPERATIONAL_DATA_HOLDER")) {
			return uri + "informationHolderEndpointTypes/OperationalDataHolder.html";
		}
		else if(role1.equals("MASTER_DATA_HOLDER")) {
			return uri + "informationHolderEndpointTypes/MasterDataHolder.html";
		}
		else if(role1.equals("REFERENCE_DATA_HOLDER")) {
			return uri + "informationHolderEndpointTypes/ReferenceDataHolder.html";
		}
		else if(role1.equals("DATA_TRANSFER_RESOURCE")) {
			return uri + "informationHolderEndpointTypes/DataTransferResource.html";
		}
		else if(role1.equals("LINK_LOOKUP_RESOURCE")) {
			return uri + "informationHolderEndpointTypes/LinkLookupResource.html";
		}
		else // no pattern found, no website link 
			return "";
	}
	
	public static String provideMAP(EndpointContract mdslEndpointType) {
		String role1 = mdslEndpointType.getPrimaryRole();

		if (role1 == null)
			return "not specified.";
		
		if(role1.equals("PROCESSING_RESOURCE")) {
			return "Processing Resource pattern";
		}
		else if(role1.equals("INFORMATION_HOLDER_RESOURCE")) {
			return "Information Holder Resource pattern";
		}
		else if(role1.equals("OPERATIONAL_DATA_HOLDER")) {
			return "Operational Data Holder pattern";
		}
		else if(role1.equals("MASTER_DATA_HOLDER")) {
			return "Master Data Holder pattern";
		}
		else if(role1.equals("REFERENCE_DATA_HOLDER")) {
			return "Reference Data Holder pattern";
		}
		else if(role1.equals("DATA_TRANSFER_RESOURCE")) {
			return "Data Transfer Resource pattern";
		}
		else if(role1.equals("LINK_LOOKUP_RESOURCE")) {
			return "Link Lookup Resource pattern";
		}
		else
			return role1 + "(non-pattern role)";
	}
	
	public static String provideLinktoMAPWebsite(Operation mdslOperation) {
		OperationResponsibility responsibility = mdslOperation.getResponsibility();
		String uri = "https://microservice-api-patterns.org/patterns/responsibility/";
		String patternName;

		if (responsibility == null)
			return "unspecified operation responsibility";

		if (responsibility.getOther() != null) {
			return unquoteString(responsibility.getOther());
		}

		if (responsibility.getCf() != null) {
			patternName = "Computation Function";
			uri += "operationResponsibilities/ComputationFunction.html";
		} else if (responsibility.getSco() != null) {
			patternName = "State Creation Operation";
			uri += "operationResponsibilities/StateCreationOperation.html";
		} else if (responsibility.getRo() != null) {
			patternName = "Retrieval Operation";
			uri += "operationResponsibilities/RetrievalOperation.html";
		} else if (responsibility.getSto() != null) {
			patternName = "State Transition Operation";
			uri += "operationResponsibilities/StateTransitionOperation.html";
		} else
			return "unspecified operation responsibility";
		;
		return "This operation realizes the [" + patternName + "](" + uri + ") pattern.";
	}
	
	public static String explainResponsibilityPattern(Operation mdslOperation) {
		OperationResponsibility responsibility = mdslOperation.getResponsibility();
		if (responsibility == null)
			return null;

		if (responsibility.getCf() != null)
			return "no read, no write"; // return responsibility.getCf();
		if (responsibility.getSco() != null)
			return "write only"; // return responsibility.getSco();
		if (responsibility.getRo() != null)
			return "read only"; // return responsibility.getRo();
		if (responsibility.getSto() != null)
			return "read and write"; // return responsibility.getSto();

		if (responsibility.getOther() != null) {
			return unquoteString(responsibility.getOther());
		} else
			return ""; // unknown or not yet supported responsibility
	}
	
	public static String mapParameterRoleAndType(RoleAndType roleAndType) {
		if (roleAndType.getRole().equals("D") || roleAndType.getRole().equals("Data")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement\" target=\"_blank\">Data Element</a>";
		} else if (roleAndType.getRole().equals("MD") || roleAndType.getRole().equals("Metadata")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/MetadataElement\" target=\"_blank\">Metadata Element</a>";
		} else if (roleAndType.getRole().equals("ID") || roleAndType.getRole().equals("Identifier")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/IdElement\" target=\"_blank\">Id Element</a>";
		} else if (roleAndType.getRole().equals("L") || roleAndType.getRole().equals("Link")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/LinkElement\" target=\"_blank\">Link Element</a>";
		}
		// TODO would have to add R<...> if it stays (and is on this level in grammar)
		return "Unknown role and type.";
	}
}

package io.mdsl.utils;

import org.eclipse.emf.common.util.EList;

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
	
	// TODO add support for API-level foundation patterns
	
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
		else if (!role1.equals(""))
			return "main responsibility: " + role1; // free form responsibility
		
		return "";
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
		
		// TODO what about secondary roles (if present)?
		EList<String> otherRoles = mdslEndpointType.getOtherRoles();

		return mapRolePattern(role1);
	}

	private static String mapRolePattern(String role) {
		if (role == null || role.equals("")) {
			return null; 
		}
		if(role.equals("PROCESSING_RESOURCE")) {
			return "Processing Resource";
		}
		else if(role.equals("INFORMATION_HOLDER_RESOURCE")) {
			return "Information Holder Resource";
		}
		else if(role.equals("OPERATIONAL_DATA_HOLDER")) {
			return "Operational Data Holder";
		}
		else if(role.equals("MASTER_DATA_HOLDER")) {
			return "Master Data Holder";
		}
		else if(role.equals("REFERENCE_DATA_HOLDER")) {
			return "Reference Data Holder";
		}
		else if(role.equals("DATA_TRANSFER_RESOURCE")) {
			return "Data Transfer Resource";
		}
		else if(role.equals("LINK_LOOKUP_RESOURCE")) {
			return "Link Lookup Resource";
		}
		else
			return role + " (non-pattern role)";
	}
	
	public static String specifyResponsibilityWithMAPLinkIfPossible(Operation mdslOperation) {
		OperationResponsibility responsibility = mdslOperation.getResponsibility();
		String uri = "https://microservice-api-patterns.org/patterns/responsibility/";
		String patternName;

		if (responsibility == null)
			return ""; 

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
		} 
		// TODO handle variants (delete, replace, collection) 
		else
			return "other operation responsibility pattern";
		;
		
		return "[" + patternName + "](" + uri + ").";
	}
	
	public static String explainResponsibilityPattern(Operation mdslOperation) {
		OperationResponsibility responsibility = mdslOperation.getResponsibility();
		if (responsibility == null)
			return null;

		if (responsibility.getCf() != null)
			return "no read, no write"; 
		if (responsibility.getSco() != null)
			return "write only"; 
		if (responsibility.getRo() != null)
			return "read only"; 
		if (responsibility.getSto() != null)
			return "read and write"; 

		if (responsibility.getOther() != null) {
			return unquoteString(responsibility.getOther());
		} else
			return ""; // unknown or not yet supported responsibility
	}
	
	public static String mapParameterRoleAndType(RoleAndType roleAndType) {
		if(roleAndType==null) {
			return "";
		}
			
		if (roleAndType.getRole().equals("D") || roleAndType.getRole().equals("Data")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/DataElement\" target=\"_blank\">Data Element</a>";
		} else if (roleAndType.getRole().equals("MD") || roleAndType.getRole().equals("Metadata")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/MetadataElement\" target=\"_blank\">Metadata Element</a>";
		} else if (roleAndType.getRole().equals("ID") || roleAndType.getRole().equals("Identifier")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/IdElement\" target=\"_blank\">Id Element</a>";
		} else if (roleAndType.getRole().equals("L") || roleAndType.getRole().equals("Link")) {
			return "<a href=\"https://microservice-api-patterns.org/patterns/structure/elementStereotypes/LinkElement\" target=\"_blank\">Link Element</a>";
		}
		else {
			return " Unknown role and type.";
		}
	}
}

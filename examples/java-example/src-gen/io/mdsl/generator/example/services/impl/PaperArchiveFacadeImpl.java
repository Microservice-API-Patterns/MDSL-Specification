package io.mdsl.generator.example.services.impl;

import io.mdsl.generator.example.services.PaperArchiveFacade;
import io.mdsl.generator.example.types.*;
import java.util.Arrays;

/**
 * This implementation has been generated from the MDSL endpoint called 'PaperArchiveFacade'.
 * The methods are a starting point to realize the logic behind an endpoint
 * and are not complete. 
 * 
 */
public class PaperArchiveFacadeImpl implements PaperArchiveFacade {

	/**
	 * MAP decorator: STATE_CREATION_OPERATION
	 * 
	 * Find all MAP responsibility patterns here: https://microservice-api-patterns.org/patterns/responsibility/
	 */
	public PaperItemDTO createPaperItem(CreatePaperItemParameter anonymousInput) {
		System.out.println("The received object for parameter 'anonymousInput' is " + (anonymousInput == null ? "null." : "not null."));
		// TODO: we just return a dummy object here; replace this with your implementation
		PaperItemDTO obj = new PaperItemDTO();
		obj.setTitle("8KmDBMtnbr");
		obj.setAuthors("VwMgP7Y5Hl");
		obj.setVenue("VBGae4PUuB");
		return obj;
	}
	
	/**
	 * MAP decorator: RETRIEVAL_OPERATION
	 * 
	 * Find all MAP responsibility patterns here: https://microservice-api-patterns.org/patterns/responsibility/
	 */
	public PaperItemDTOList lookupPapersFromAuthor(LookupPapersFromAuthorRequestDataType anonymousInput) {
		System.out.println("The received object for parameter 'anonymousInput' is " + (anonymousInput == null ? "null." : "not null."));
		// TODO: we just return a dummy object here; replace this with your implementation
		PaperItemDTOList obj = new PaperItemDTOList();
		return obj;
	}
	
	public ConvertToMarkdownForWebsiteResponseDataType convertToMarkdownForWebsite(PaperItemKey anonymousInput) {
		System.out.println("The received object for parameter 'anonymousInput' is " + (anonymousInput == null ? "null." : "not null."));
		// TODO: we just return a dummy object here; replace this with your implementation
		ConvertToMarkdownForWebsiteResponseDataType obj = new ConvertToMarkdownForWebsiteResponseDataType();
		obj.setAnonymous2("P2qGt44VJE");
		return obj;
	}
	

}

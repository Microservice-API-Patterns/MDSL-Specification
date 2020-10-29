package io.mdsl.generator.example.services;

import io.mdsl.generator.example.types.*;

/**
 * This interface has been generated from the MDSL endpoint called 'PaperArchiveFacade'. 
 * 
 */
public interface PaperArchiveFacade {

	/**
	 * MAP decorator: STATE_CREATION_OPERATION
	 * 
	 * Find all MAP responsibility patterns here: https://microservice-api-patterns.org/patterns/responsibility/
	 */
	PaperItemDTO createPaperItem(CreatePaperItemParameter anonymousInput);
	
	/**
	 * MAP decorator: RETRIEVAL_OPERATION
	 * 
	 * Find all MAP responsibility patterns here: https://microservice-api-patterns.org/patterns/responsibility/
	 */
	PaperItemDTOList lookupPapersFromAuthor(LookupPapersFromAuthorRequestDataType anonymousInput);
	
	ConvertToMarkdownForWebsiteResponseDataType convertToMarkdownForWebsite(PaperItemKey anonymousInput);
	

}

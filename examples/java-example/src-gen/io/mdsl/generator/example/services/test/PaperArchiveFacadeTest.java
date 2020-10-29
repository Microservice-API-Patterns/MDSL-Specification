package io.mdsl.generator.example.services.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import io.mdsl.generator.example.services.PaperArchiveFacade;
import io.mdsl.generator.example.services.impl.PaperArchiveFacadeImpl;
import io.mdsl.generator.example.types.*;

/**
 * This test has been generated from the MDSL endpoint called 'PaperArchiveFacade'.
 * The methods are a starting point to implement your tests
 * and are not complete. 
 * 
 */
public class PaperArchiveFacadeTest {

	@Test
	public void canCreatePaperItem() {
		// given
		PaperArchiveFacade service = new PaperArchiveFacadeImpl();
		CreatePaperItemParameter anonymousInput = new CreatePaperItemParameter();
		anonymousInput.setWho("UJ7L4TcJQH");
		anonymousInput.setWhat("0smET1o7Us");
		anonymousInput.setWhere("f1AqeKUaNg");
		
		// when
		PaperItemDTO result = service.createPaperItem(anonymousInput);
		
		// then
		assertNotNull(result);
	}
	
	@Test
	public void canLookupPapersFromAuthor() {
		// given
		PaperArchiveFacade service = new PaperArchiveFacadeImpl();
		LookupPapersFromAuthorRequestDataType anonymousInput = new LookupPapersFromAuthorRequestDataType();
		anonymousInput.setAnonymous1("5hbpNILyJe");
		
		// when
		PaperItemDTOList result = service.lookupPapersFromAuthor(anonymousInput);
		
		// then
		assertNotNull(result);
	}
	
	@Test
	public void canConvertToMarkdownForWebsite() {
		// given
		PaperArchiveFacade service = new PaperArchiveFacadeImpl();
		PaperItemKey anonymousInput = new PaperItemKey();
		anonymousInput.setDoi("1rsV2rzIAe");
		
		// when
		ConvertToMarkdownForWebsiteResponseDataType result = service.convertToMarkdownForWebsite(anonymousInput);
		
		// then
		assertNotNull(result);
	}
	

}

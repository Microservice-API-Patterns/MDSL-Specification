// API name: ReferenceManagementServiceAPI

type PaperItemDTO {
	anonymous7: void { title: string /* data type role: D */ authors: string /* data type role: D */ venue: string /* data type role: D */ paperItemId: PaperItemKey } 
}

type PaperItemKey {
	anonymous8: void { doi: string /* data type role: D */ } 
}

type createPaperItemParameter {
	anonymous9: void { who: string /* data type role: D */ what: string /* data type role: D */ where: string /* data type role: D */ } 
}

// operation responsibility: RETRIEVAL_OPERATION
type lookupPapersFromAuthorRequestDTO {
	anonymous3: string /* data type role: D */ 
}

type lookupPapersFromAuthorResponseDTO {
	anonymous4[0,*]: PaperItemDTO 
}

// operation responsibility: STATE_CREATION_OPERATION
type createPaperItemRequestDTO {
	anonymous1: createPaperItemParameter 
}

type createPaperItemResponseDTO {
	anonymous2: PaperItemDTO 
}

// operation responsibility: undefined
type convertToMarkdownForWebsiteRequestDTO {
	anonymous5: PaperItemKey 
}

type convertToMarkdownForWebsiteResponseDTO {
	anonymous6: string /* data type role: D */ 
}

type SOAPFaultMessage {
	code: int
	text: string
	actor: string
	details: string
}

// interface/endpoint role: INFORMATION_HOLDER_RESOURCE
interface PaperArchiveFacade {
RequestResponse:
	createPaperItem( createPaperItemRequestDTO )( createPaperItemResponseDTO ),
	lookupPapersFromAuthor( lookupPapersFromAuthorRequestDTO )( lookupPapersFromAuthorResponseDTO ),
	convertToMarkdownForWebsite( convertToMarkdownForWebsiteRequestDTO )( convertToMarkdownForWebsiteResponseDTO ),
}

inputPort PaperArchiveFacadePort {
	location: "socket://localhost:8080" 
	protocol: soap
	interfaces: PaperArchiveFacade
}

main
{
	nullProcess
}

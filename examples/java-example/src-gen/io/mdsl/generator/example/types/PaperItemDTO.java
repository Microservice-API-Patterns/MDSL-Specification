package io.mdsl.generator.example.types;


/**
 * This class has been generated from the MDSL data type 'PaperItemDTO'. 
 * 
 */
public class PaperItemDTO {

	private String title; 
	private String authors; 
	private String venue; 
	private PaperItemKey paperItemId; 
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAuthors() {
		return authors;
	}
	
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	
	public String getVenue() {
		return venue;
	}
	
	public void setVenue(String venue) {
		this.venue = venue;
	}
	
	public PaperItemKey getPaperItemId() {
		return paperItemId;
	}
	
	public void setPaperItemId(PaperItemKey paperItemId) {
		this.paperItemId = paperItemId;
	}
	

}

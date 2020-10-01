package io.mdsl.discovery.example;

import io.mdsl.annotations.ServiceParameter;

@ServiceParameter // could also mark individual fields/attributes
public class PaperItemDTO extends AbstractDTO {
	String who;
	String where;
	VenueDTO what;
	public String getWho() {
		return who;
	}
	public void setWho(String who) {
		this.who = who;
	}
	public String getWhere() {
		return where;
	}
	public void setWhere(String where) {
		this.where = where;
	}
	public VenueDTO getWhat() {
		return what;
	}
	public void setWhat(VenueDTO what) {
		this.what = what;
	}
}

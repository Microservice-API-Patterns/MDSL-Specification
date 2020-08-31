package io.mdsl.generator.jolie.converter;

public class OperationModel {
	public OperationModel(String name, 
			String requestType, String responseType, 
			String reponsibilityPattern,String mep) {
		this.name = name;
		this.requestType = requestType;
		this.responseType = responseType;
		this.reponsibilityPattern = reponsibilityPattern;
	}
	private String name;
	private String requestType;
	private String responseType;
	private String reponsibilityPattern;
	private String mep;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getResponseType() {
		return responseType;
	}
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}
	public String getReponsibilityPattern() {
		return reponsibilityPattern;
	}
	public void setReponsibilityPattern(String reponsibilityPattern) {
		this.reponsibilityPattern = reponsibilityPattern;
	}
	public String getMep() {
		return mep;
	}
	public void setMep(String mep) {
		this.mep = mep;
	}

}

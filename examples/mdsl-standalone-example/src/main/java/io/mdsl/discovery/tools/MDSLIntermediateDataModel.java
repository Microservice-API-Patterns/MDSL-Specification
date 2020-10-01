package io.mdsl.discovery.tools;

import java.util.ArrayList;

public class MDSLIntermediateDataModel {
	public MDSLIntermediateDataModel(String apiName, String apiRole, ArrayList<Endpoint> endpoints) {
		super();
		this.apiName = apiName;
		this.apiRole = apiRole;
		this.endpoints = endpoints;
	}
	
	private String apiName;
	private String apiRole; // TODO set from @ServiceAPI annotation, use in template
	private ArrayList<Endpoint> endpoints;
	
	public String getApiName() {
		return apiName;
	}
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	public String getApiRole() {
		return apiRole;
	}
	public void setApiRole(String apiRole) {
		this.apiRole = apiRole;
	}
	public ArrayList<Endpoint> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(ArrayList<Endpoint> endpoints) {
		this.endpoints = endpoints;
	} 
}

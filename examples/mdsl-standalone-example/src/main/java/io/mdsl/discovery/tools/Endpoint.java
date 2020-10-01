package io.mdsl.discovery.tools;

import java.util.ArrayList;

public class Endpoint {
	public Endpoint(String name, String role, ArrayList<Operation> operations) {
		super();
		this.name = name;
		this.role = role;
		this.operations = operations;
	}
	
	String name;
	String role;
	ArrayList<Operation> operations;
	// TODO add responsibility 
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Operation> getOperations() {
		return operations;
	}
	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}

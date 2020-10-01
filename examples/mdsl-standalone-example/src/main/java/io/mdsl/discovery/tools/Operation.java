package io.mdsl.discovery.tools;

public class Operation {
	public Operation(String name, String responsibility, String mep, String expecting, String delivering) {
		super();
		this.name = name;
		this.responsibility = responsibility;
		this.mep = mep;
		this.expecting = expecting;
		this.delivering = delivering;
	}
	String name;
	String responsibility;
	String mep;
	String expecting;
	String delivering;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMep() {
		return mep;
	}
	public void setMep(String mep) {
		this.mep = mep;
	}
	public String getExpecting() {
		return expecting;
	}
	public void setExpecting(String expecting) {
		this.expecting = expecting;
	}
	public String getDelivering() {
		return delivering;
	}
	public void setDelivering(String delivering) {
		this.delivering = delivering;
	}
	public String getResponsibility() {
		return responsibility;
	}
	public void setResponsibility(String responsibility) {
		this.responsibility = responsibility;
	}
}

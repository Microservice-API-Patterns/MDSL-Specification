package io.mdsl.generator.model.carving;

import java.util.HashMap;
import io.mdsl.generator.model.composition.Command;

public class CommandCluster {
	private HashMap<String, Command> connectedCommands;
	private String clusterName = "tbd";
	
	public CommandCluster(String name) {
		this.clusterName = name;
		this.connectedCommands = new HashMap<String, Command>();
	}
	
	public HashMap<String, Command> getConnectedCommands() {
		return connectedCommands;
	}

	public void addConnectedCommand(String name, Command connectedCommand) {
		System.out.println("Adding " + name + " to " + clusterName);
		this.connectedCommands.put(name, connectedCommand);
	} 
	
	public String getCommandClusterName() {
		return clusterName;
	}
}

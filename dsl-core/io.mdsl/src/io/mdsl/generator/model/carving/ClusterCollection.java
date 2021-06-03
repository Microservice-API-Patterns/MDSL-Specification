package io.mdsl.generator.model.carving;

import java.util.HashMap;

public class ClusterCollection {
	private HashMap<String, CommandCluster> clusters;
	
	public ClusterCollection() {
		this.clusters = new HashMap<String, CommandCluster>();
	}

	public HashMap<String, CommandCluster> getClusters() {
		return clusters;
	}

	public void addCluster(CommandCluster cluster) {
		this.clusters.put(cluster.getCommandClusterName(), cluster);
	}
	
	// TODO implement design heuristics (might require additional input in MDSL): 
	// JH, VV, SC, tbc
}

package io.mdsl.generator.model.composition.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Path {
	private LinkedHashMap<String, PathElement> parts = new LinkedHashMap<String, PathElement>();
	
	public Path() {
	}
	
	public Path(Path pathSoFar) {
		Collection<PathElement> emissions = pathSoFar.getEmissions();
		emissions.forEach(emission->parts.put(emission.getSource(), emission));
	}
	
	public Path cloneDeeply() {
		Path result = new Path();
		for(Entry<String, PathElement> nextPart : this.parts.entrySet()) {
			PathElement nextEmission = new PathElement(nextPart.getValue().getSource(), nextPart.getValue().getSink());
			result.addEmission(nextEmission);
		}
	    return result;
	}

	public Path addEmission(PathElement flowStep) {
		parts.put(flowStep.getName(), flowStep);
		return this;
	}
	
	public void insertEmissionAtStart(PathElement newElement) {
		Path tmp = this.cloneDeeply();
		this.parts.clear();
		this.parts.put(newElement.getName(), newElement);
		tmp.getEmissions().forEach(emission->this.parts.put(emission.getName(), emission));
	}
	
	public void addPaths(ArrayList<Path> paths) {
		for(Path path : paths) {
			addPath(path);
		}
	}

	public void addPath(Path path) {
		Collection<PathElement> emissions = path.getEmissions();
		emissions.forEach(emission->this.parts.put(emission.getSource(), emission));
	}
	
	public int length() {
		return parts.size();
	}

	public String getEventAt(int index) {
		PathElement emissionAt = (PathElement) parts.values().toArray()[index];
		return emissionAt.getSource();
	}
	
	public String getCommandAt(int index) {
		PathElement emissionAt = (PathElement) parts.values().toArray()[index];
		return emissionAt.getSink();
	}
	
	public Collection<PathElement> getEmissions() {
		return parts.values();
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		for(PathElement part : this.parts.values()) {
			result.append(part.toString());
		}
		return result.toString();
	}
	
	public String dump() {
		StringBuffer result = new StringBuffer();
		for(PathElement part : this.parts.values()) {
			result.append(part.dump());
		}
		return result.toString();
	}
}

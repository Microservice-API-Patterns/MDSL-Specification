package io.mdsl.generator.model.composition.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.mdsl.generator.model.composition.Event;
import io.mdsl.utils.MDSLLogger;

public class PathCollection {
	
	private ArrayList<Path> paths = new ArrayList<Path>();

	public PathCollection() {
	}
	
	public void clear() {
		this.paths.clear();
		
	}
	
	public int size() {
		return paths.size();
	}
	
	// required for Jackson-based JSON/YAML serialization
	public List<Path> getPaths() {
		List<Path> result = new ArrayList<Path>();
		for(Path path : this.paths) {
			result.add(path);
		}
		return result;
	}
	
	public Path getPath(int index) {
		return paths.get(index);
	}
	
	public void addPath(Path path) {
		paths.add(path);
	}
	
	public void insertAtStartOfAllPaths(PathElement aggregateIn) {
		for(Path existingPath : this.paths) {
			existingPath.insertEmissionAtStart(aggregateIn);
		}	
	}

	public void mergeWithPathCollection(PathCollection collection) {
		if(collection!=null) {
			if(paths.size()==1&&paths.get(0).length()==0) {
				// replacing empty collection with incoming one:
				this.paths = collection.paths;
			}
			else {
				this.paths.addAll(collection.paths);
			}
		}
	}
	
	public void addRawPaths(Collection<Path> paths) {
		paths.addAll(paths);
	}
	
	public void addRawCollection(Collection<PathCollection> pcl) {
		for(PathCollection nextCollection : pcl) {
			paths.addAll(nextCollection.asRawCollection());
		}
	}
	
	private Collection<? extends Path> asRawCollection() {
		return paths;
	}

	public void addEmissionToAllPaths(PathElement emission) {
		paths.forEach(path->path.addEmission(emission));
	}
	
	public void addEmissionAtStartOfAllPaths(PathElement aggregation) {
		paths.forEach(path->this.insertAtStartOfAllPaths(aggregation));
	}
	
	// ** advanced modifiers
	
	public PathCollection cloneDeeply() {
		PathCollection clonedCollection = new PathCollection();
		for(Path pathToBeCloned : this.paths) {
			Path clonedPath = pathToBeCloned.cloneDeeply();
			clonedCollection.addPath(clonedPath);
		}
		return clonedCollection; 
	}
	
	public void mergeEachPathInSecondCollectionIntoEachExistingPath (PathCollection pathsToMergedIn) {
		for(Path existingPath : this.paths) {
			for(Path nextPathToBeMergedIn : pathsToMergedIn.paths) {
				existingPath.addPath(nextPathToBeMergedIn);
			}
		}
	}
	
	// ** diagnostics
	
	public int numberOfInvocations() {
		int result =  0;
		for(Path path : this.paths) {
			result += path.length();
		}
		return result;
	}

	public boolean eventAlreadyAppearsInPaths(Event event) {
		for(Path nextPath : this.paths) {
			for(PathElement nextHop : nextPath.getEmissions()) {
				if(nextHop.getSource().equals(event.getName())) {
					MDSLLogger.reportInformation("(PC) event found in paths " + event.getName());
					return true;
				}
			}
		}
		MDSLLogger.reportInformation("(PC) event NOT found in paths " + event.getName());
		return false;
	}
	
	// TODO simplify, DRY
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		for(Path path : this.paths) {
			result.append(path.toString()+"\n");
		}
		return result.toString();
	}
	
	public String dumpSizeInfo() {
		StringBuffer result = new StringBuffer("- Path collection has " + paths.size() + " entries\n");
		int index=0;
		for(Path path : this.paths) {
			result.append("-- Path " + index++ + " has size " + path.length() + "\n");
		}
		return result.toString();
	}
	
	public String dump(boolean withSizeInfo) {
		StringBuffer result = new StringBuffer();
		if(withSizeInfo) {
			result.append("- Path collection has " + paths.size() + " entries\n");
		}
		for(Path path : this.paths) {
			if(withSizeInfo) {
				result.append("-- Path has size " + path.length() + "\n");
			}
			result.append(path.dump()+"\n");
		}
		return result.toString();
	}
}

/*
 * Copyright 2020 The MDSL Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mdsl.generator.model.composition.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.composition.Event;
import io.mdsl.generator.model.composition.views.PathCollection;
import io.mdsl.tests.AbstractMDSLInputIntegrationTest;
import io.mdsl.transformations.TransformationHelpers;

public class ProcessViewHelpersTest extends AbstractMDSLInputIntegrationTest {

	@Test
	public void canCreateAndAccessPathCollection() throws IOException {
		// given
		// PathCollection pc = Process.createFreshPathCollection();
		PathCollection pc = new PathCollection();
		PathElement pe1 = new PathElement("From1", "To1");
		PathElement pe2 = new PathElement("From2", "To2");
		PathElement pe3 = new PathElement("From0", "To0");
		Path path1 = new Path();
		
		// when
		path1.addEmission(pe2);
		path1.insertEmissionAtStart(pe1);
		pc.addPath(path1);
		
		pc.insertAtStartOfAllPaths(pe3);
		System.out.println(pc.toString());
		
		// then
		assertEquals(1, pc.size());
		assertEquals(3, pc.getPath(0).length());
		PathElement element1 = (PathElement) pc.getPath(0).getEmissions().toArray()[0];
		assertEquals("From0", element1.getSource());
		assertEquals("To0", element1.getSink());
		PathElement element2 = (PathElement) pc.getPath(0).getEmissions().toArray()[1];
		assertEquals("From1", element2.getSource());
		assertEquals("To1", element2.getSink());
	}
	
	@Test
	public void canMergePathCollections() throws IOException {
		// given
		PathCollection pc1 = new PathCollection();
		PathElement pe1 = new PathElement("From1", "To1");
		PathElement pe2 = new PathElement("From2", "To2");
		PathElement pe3 = new PathElement("From0", "To0");
		Path path1 = new Path();
		path1.addEmission(pe2);
		path1.insertEmissionAtStart(pe1);
		pc1.addPath(path1);
		pc1.insertAtStartOfAllPaths(pe3);
		
		PathCollection pc2 = new PathCollection();
		PathElement peB = new PathElement("FromB0", "ToB0");
		Path path2B = new Path();
		path2B.addEmission(peB);
		pc2.addPath(path2B);
		
		// when
		pc1.mergeWithPathCollection(pc2);
		System.out.println(pc1.toString());
		
		// then
		assertEquals(2, pc1.size());
		assertEquals(3, pc1.getPath(0).length());
		assertEquals(1, pc1.getPath(1).length());
		
		// when
		PathElement peB2 = new PathElement("FromB1", "ToB1");
		Path path2B2 = new Path();
		path2B2.addEmission(peB2);
		pc1.mergeWithPathCollection(pc2);
		System.out.println(pc1.toString());
		
		// then
		assertEquals(3, pc1.size());
		assertEquals(3, pc1.getPath(0).length());
		assertEquals(1, pc1.getPath(1).length());
		assertEquals(1, pc1.getPath(2).length());
	}
	
	@Test
	public void canMergeEachPathInSecondCollectionIntoEachExistingPath() throws IOException {
		// given
		PathCollection pc1 = new PathCollection();
		PathElement pe1 = new PathElement("From1", "To1");
		PathElement pe2 = new PathElement("From2", "To2");
		PathElement pe3 = new PathElement("From0", "To0");
		Path path1 = new Path();
		path1.addEmission(pe2);
		path1.insertEmissionAtStart(pe1);
		pc1.addPath(path1);
		pc1.insertAtStartOfAllPaths(pe3);
		
		PathCollection pc2 = new PathCollection();
		PathElement peB = new PathElement("FromB0", "ToB0");
		Path path2B = new Path();
		path2B.addEmission(peB);
		pc2.addPath(path2B);
		
		// when
		pc1.mergeEachPathInSecondCollectionIntoEachExistingPath(pc2);
		System.out.println(pc1.toString());
		
		// then
		assertEquals(1, pc1.size());
		assertEquals(4, pc1.getPath(0).length());
	}
	
	@Test
	public void canMergeEachPathInSecondCollectionIntoEachExistingPath2() throws IOException {
		// given
		PathCollection pc1 = new PathCollection();
		PathElement pe1 = new PathElement("From1", "To1");
		PathElement pe2 = new PathElement("From2", "To2");
		PathElement pe3 = new PathElement("From3", "To3");
		PathElement pe4 = new PathElement("From4", "To4");
		
		Path path1 = new Path();
		path1.addEmission(pe1);
		path1.addEmission(pe2);
		pc1.addPath(path1);
		
		PathCollection pc2 = new PathCollection();
		Path path2 = new Path();
		path2.insertEmissionAtStart(pe4);
		path2.insertEmissionAtStart(pe3);
		pc2.addPath(path2);
		
		// when
		pc1.mergeEachPathInSecondCollectionIntoEachExistingPath(pc2);
		System.out.println(pc1.toString());
		
		// then
		assertEquals(1, pc1.size());
		assertEquals(4, pc1.getPath(0).length());
	}
	
	@Test
	public void canAddNewPathElementToExistingPaths() throws IOException {
		// given
		PathCollection pc1 = new PathCollection();
		PathElement pe1 = new PathElement("From1", "To1");
		PathElement pe2 = new PathElement("From2", "To2");
		PathElement pe3 = new PathElement("From3", "To3");
		PathElement pe4 = new PathElement("From4", "To4");
		PathElement pe5 = new PathElement("From5", "To5");
		
		Path path1 = new Path();
		path1.addEmission(pe1);
		path1.addEmission(pe2);
		pc1.addPath(path1);

		Path path2 = new Path();
		path2.insertEmissionAtStart(pe3);
		pc1.addPath(path2);

		// when
		pc1.addEmissionToAllPaths(pe4);
		pc1.addEmissionAtStartOfAllPaths(pe5);
		System.out.println(pc1.toString());
		
		// then
		assertEquals(2, pc1.size());
		assertEquals(4, pc1.getPath(0).length());
		assertEquals(3, pc1.getPath(1).length());
		PathElement firstElementInPath1 = (PathElement) pc1.getPath(0).getEmissions().toArray()[0];
		assertEquals("From5", firstElementInPath1.getSource());
		PathElement lastElementInPath2 = (PathElement) pc1.getPath(1).getEmissions().toArray()[2];
		assertEquals("From4", lastElementInPath2.getSource());
	}
	
	@Test
	public void canClonePathCollectionDeeply() throws IOException {
		// given
		PathCollection pc1 = new PathCollection();
		PathElement pe1 = new PathElement("From1", "To1");
		PathElement pe2 = new PathElement("From2", "To2");
		Path path1 = new Path();
		path1.addEmission(pe1);
		path1.addEmission(pe2);
		pc1.addPath(path1);
		
		// when
		PathCollection pc1Clone = pc1.cloneDeeply();
		
		assertEquals(1, pc1Clone.size());
		assertEquals(2, pc1Clone.getPath(0).length());
		PathElement element1 = (PathElement) pc1.getPath(0).getEmissions().toArray()[0];
		PathElement element1Clone = (PathElement) pc1Clone.getPath(0).getEmissions().toArray()[0];
		assertEquals("From1", element1.getSource());
		assertEquals("From1", element1Clone.getSource());
		assertTrue(element1==pe1);
		assertTrue(element1.equals(pe1)); // equals defined by id/instance pointer, not by value
		assertTrue(element1!=element1Clone);
		assertTrue(!element1.equals(element1Clone)); // equals defined by id/instance pointer 
		assertFalse(element1Clone==pe1);
		assertFalse(element1Clone.equals(pe1));
	}
	
	// mimic usage of helpers in Process View more closely (freshCollection helper causing problems?)
	@Test
	public void canUseHelperAsProcessViewDoes() throws IOException {
		// given
		PathCollection result = new PathCollection();
		Path path = new Path();
		PathElement pe1 = new PathElement("From1", "To1");
		path.addEmission(pe1);
		result.addPath(path);
		PathElement pe2 = new PathElement("From2", "To2");
		result.addEmissionToAllPaths(pe2);
		List<String> eventList = new ArrayList<>();
		eventList.add("Event1");
		eventList.add("Event2");
		eventList.add("Event3");
		PathCollection copyOfResultSoFar = result.cloneDeeply();
		result.clear();
		
		// when
		for(String nextEvent : eventList) {
			PathCollection nextSetOfPaths = copyOfResultSoFar.cloneDeeply();
			
			PathCollection pathsFromHere = Process.createFreshPathCollection();
			PathElement newPE = new PathElement(nextEvent + "PathElementSource", nextEvent + "PathElementSink");
			pathsFromHere.insertAtStartOfAllPaths(newPE);
			visitEvent(pathsFromHere, nextEvent);
			
			nextSetOfPaths.mergeEachPathInSecondCollectionIntoEachExistingPath(pathsFromHere);
			result.mergeWithPathCollection(nextSetOfPaths);
		}
		
		// then 
		System.out.println(result.toString());	
		assertEquals(3, result.size());
		assertEquals(4, result.getPath(0).length());
		PathElement element1 = (PathElement) result.getPath(0).getEmissions().toArray()[0];
		assertEquals("From1", element1.getSource());
		PathElement element2 = (PathElement) result.getPath(1).getEmissions().toArray()[2];
		assertEquals("Event2PathElementSource", element2.getSource());
		PathElement element3 = (PathElement) result.getPath(2).getEmissions().toArray()[3];
		assertEquals("Event3PathElementSink2", element3.getSink());
	}
	
	private void visitEvent(PathCollection pathsFromHere, String nextEvent) {
		PathElement anotherPE = new PathElement(nextEvent + "PathElementSource2", nextEvent + "PathElementSink2");
		pathsFromHere.addEmissionToAllPaths(anotherPE);
	}

	/*
			PathCollection copyOfResultSoFar = result.cloneDeeply();
			result.clear();
			for(Event nextEvent : events) {
				TransformationHelpers.reportInformation("(PV) visitEventsAndAttachTheirPathsToAllPathsInCollection, next event: " + nextEvent.getName());
				PathCollection nextSetOfPaths = copyOfResultSoFar.cloneDeeply();
				// starting a new relative path/path collection:
				PathCollection pathsFromHere = createFreshPathCollection();
				pathsFromHere = visitEvent(pathsFromHere, nextEvent);
				// System.out.println("*** visitEventsAndAttachTheirPathsToAllPathsInCollection, pathsFromHere is: " + pathsFromHere.dump(true));
				nextSetOfPaths.mergeEachPathInSecondCollectionIntoEachExistingPath(pathsFromHere);
				// System.out.println("*** visitEventsAndAttachTheirPathsToAllPathsInCollection, nextSetOfPaths is: " + nextSetOfPaths.dump(true));
				result.mergeWithPathCollection(nextSetOfPaths);
			}	 
	 */
	@Override
	protected String testDirectory() {
		return "/test-data/freemarker-generation/";
	}
}

/*
 * Copyright 2020 The Context Mapper Project Team
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
package io.mdsl.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.Orchestration;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.bpmn.SketchMinerModelCreator;

public class SketchMinerGenerator extends AbstractMDSLGenerator {

	private static final String SKETCH_MINER_FILE_EXT = "sketch_miner";

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, 
		IFileSystemAccess2 fsa, URI inputFileURI) {
		
		String fileName = inputFileURI.trimFileExtension().lastSegment();
		StringBuffer bpmnStories = new StringBuffer();
		
		for(Orchestration flow : mdslSpecification.getOrchestrations()) {
			bpmnStories.delete(0, bpmnStories.length()); // no clear or emptyBuffer methods
			bpmnStories.append(new SketchMinerModelCreator().createText(flow));
			fsa.generateFile(fileName + "_" + flow.getName() + "." + SKETCH_MINER_FILE_EXT, bpmnStories);
		}
	}
}

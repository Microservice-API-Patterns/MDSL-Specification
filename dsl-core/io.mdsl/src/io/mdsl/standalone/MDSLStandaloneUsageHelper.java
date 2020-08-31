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
package io.mdsl.standalone;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.IGenerator2;

import io.mdsl.APIDescriptionStandaloneSetup;
import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ApiDescriptionFactory;

public class MDSLStandaloneUsageHelper implements MDSLStandaloneAPI {

	public MDSLStandaloneUsageHelper() {
		APIDescriptionStandaloneSetup.doSetup();
	}

	@Override
	public MDSLResource loadMDSL(String filepath) {
		return new MDSLResource(new ResourceSetImpl().getResource(URI.createURI(filepath), true));
	}

	@Override
	public MDSLResource loadMDSL(File mdslFile) {
		return new MDSLResource(new ResourceSetImpl().getResource(URI.createFileURI(mdslFile.getAbsolutePath()), true));
	}

	@Override
	public MDSLResource createMDSL(String filepath) {
		return createNewMDSLResource(new File(filepath));
	}

	@Override
	public MDSLResource createMDSL(File mdslFile) {
		return createNewMDSLResource(mdslFile);
	}

	@Override
	public void callGenerator(MDSLResource mdsl, IGenerator2 generator) {
		generator.doGenerate(mdsl, FileSystemHelper.getFileSystemAccess(), new GeneratorContext());
	}

	@Override
	public void callGenerator(MDSLResource mdsl, IGenerator2 generator, String outputDir) {
		generator.doGenerate(mdsl, FileSystemHelper.getFileSystemAccess(outputDir), new GeneratorContext());
	}

	private MDSLResource createNewMDSLResource(File file) {
		Resource resource = new ResourceSetImpl().createResource(URI.createFileURI(file.getAbsolutePath()));
		resource.getContents().add(ApiDescriptionFactory.eINSTANCE.createServiceSpecification());
		return new MDSLResource(resource);
	}

}

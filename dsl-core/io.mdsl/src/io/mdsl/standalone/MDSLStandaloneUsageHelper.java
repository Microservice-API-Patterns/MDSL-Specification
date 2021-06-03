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
// import java.io.IOException;
// import java.io.InputStream;
import java.util.Map;
// import java.util.Map.Entry;
// import java.util.Set;
// import java.util.jar.Attributes;

//import org.eclipse.emf.common.notify.Adapter;
//import org.eclipse.emf.common.notify.AdapterFactory;
//import org.eclipse.emf.common.notify.Notification;
//import org.eclipse.emf.common.notify.Notifier;
//import org.eclipse.emf.common.util.EList;
//import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
//import org.eclipse.emf.ecore.EObject;
//import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
// import org.eclipse.emf.ecore.resource.ResourceSet;
// import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
// import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.xtext.generator.GeneratorContext;
// import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator2;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;

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
	
	/*
	@Override
	public MDSLResource loadMDSL(InputStream mdslStream) {
		try {
			return createNewMDSLResource(mdslStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	*/

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
	
	/*
	// added in V5.1.1 (not working)
	private MDSLResource createNewMDSLResource(InputStream istream) throws IOException {
		if(istream==null) throw new IllegalArgumentException("Incoming istream is null!");
		ResourceSet resourceSet = new ResourceSetImpl();
		// Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        // Map<String, Object> m = reg.getExtensionToFactoryMap();
		// ResourceSet resourceSet = new ResourceSetImpl();
        // m.put("website", new XMIResourceFactoryImpl());
        // Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createURI("http://dummy.inmemory.ext"));
		if(resource==null) 
			throw new IllegalArgumentException("Resource still is null.");		
        resource.load(istream, resourceSet.getLoadOptions());
		Attributes settings = new Attributes();
		resource.load(istream, settings);

		// Attributes settings = new Attributes();
		// Resource resource = resourceSet.createResource(uri, settings);
		// resource.load(istream, null); // new ResourceImpl();
		resource.getContents().add(ApiDescriptionFactory.eINSTANCE.createServiceSpecification());
		return new MDSLResource(resource);
	}
	*/

	// added in V5.1.1
	@Override
	public String callGeneratorInMemory(MDSLResource mdsl, IGenerator2 generator) {
		String result = "n/a";
		InMemoryFileSystemAccess imfsa = new InMemoryFileSystemAccess();
		generator.doGenerate(mdsl, imfsa, new GeneratorContext());
		Map<String, Object> genFiles = imfsa.getAllFiles();
		for (Map.Entry<String,Object> entry : genFiles.entrySet()) {
			if(entry.getKey().contains("DEFAULT_OUT"))
				result = (String) entry.getValue();
		}

		return result;
	}
}

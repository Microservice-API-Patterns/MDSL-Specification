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
import java.io.InputStream;

import org.eclipse.xtext.generator.IGenerator2;

import io.mdsl.MDSLResource;

/**
 * Interface that eases the usage of MDSL in standalone (usage as library)
 * scenario.
 * 
 * @author Stefan Kapferer, socadk
 */
public interface MDSLStandaloneAPI {
	/**
	 * Loads an MDSL model, given a *.mdsl file.
	 * 
	 * @param filepath the path to the *.mdsl file that shall be loaded
	 * @return the MDSLResource loaded from the *.mdsl file
	 */
	MDSLResource loadMDSL(String filepath);

	/**
	 * Loads an MDSL model, given a *.mdsl file.
	 * 
	 * @param mdslFile the *.mdsl file
	 * @return the MDSLResource loaded from the *.mdsl file
	 */
	MDSLResource loadMDSL(File mdslFile);

	/**
	 * Creates a new MDSL model.
	 * 
	 * @param filepath the filepath where the *.mdsl file will be stored, in case
	 *                 the resource is saved/persisted
	 * @return the new MDSLResource (not yet persisted; you have to call save() on
	 *         the resource)
	 */
	MDSLResource createMDSL(String filepath);

	/**
	 * Creates a new MDSL model.
	 * 
	 * @param mdslFile the file where the *.mdsl file will be stored, in case the
	 *                 resource is saved/persisted
	 * @return the new MDSLResource (not yet persisted; you have to call save() on
	 *         the resource)
	 */
	MDSLResource createMDSL(File mdslFile);

	/**
	 * Calls a generator that produces output (OAS, Proto, Jolie, etc.) given an
	 * MDSL resource as input. This method generates all output files into the
	 * default directory "./src-gen".
	 * 
	 * @param mdsl      the MDSL resource for which the generator shall be called
	 * @param generator the generator that shall be called
	 */
	void callGenerator(MDSLResource mdsl, IGenerator2 generator);

	/**
	 * Calls a generator that produces output (OAS, Proto, Jolie, etc.) given an
	 * MDSL resource as input. This method allows defining the directory into which
	 * the output shall be generated (start with "./" and provide a directory
	 * relative to your execution home).
	 * 
	 * @param mdsl      the MDSL resource for which the generator shall be called
	 * @param generator the generator that shall be called
	 * @param outputDir the directory in which you want to generate the output
	 *                  (start with "./", relative to your execution directory)
	 */
	void callGenerator(MDSLResource mdsl, IGenerator2 generator, String outputDir);
	
	/**
	 * Calls a generator that produces output (OAS, Proto, Jolie, etc.) given an
	 * MDSL resource as input. Returns output as string.
	 * 
	 * @param mdsl      the MDSL resource for which the generator shall be called
	 * @param generator the generator that shall be called
	 */
	String callGeneratorInMemory(MDSLResource mdsl, IGenerator2 generator);

	/*
	MDSLResource loadMDSL(InputStream mdslStream);
	*/
}

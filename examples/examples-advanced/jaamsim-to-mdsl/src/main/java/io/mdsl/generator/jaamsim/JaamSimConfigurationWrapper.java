package io.mdsl.generator.jaamsim;

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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.input.Input;

import io.mdsl.utils.MDSLLogger;

public class JaamSimConfigurationWrapper {
	private static final String NEXT_COMPONENT_KEYWORD = "NextComponent";
		
	private String configFileName = null;
	private JaamSimModel jsm = null;
	
	public JaamSimConfigurationWrapper(String pathToConfigFile) {
		super();
		this.configFileName = pathToConfigFile;
		try {
			initializeJSM(pathToConfigFile);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	
	// ** JaamSim config file access (file/string based)
	
	public String findModelNameInConfigFile() throws IOException {
		ArrayList<String> overlayTexts = this.findObjectDefinitionInConfigFile("OverlayText");
		if(overlayTexts.size()!=1) {
			throw new IllegalArgumentException("There should be one and only one OverlayText entity");
		}
		String formatInput = this.findInputString(overlayTexts.get(0), "Format");
		return formatInput.substring(1, formatInput.length()-1);
	}

	
	public List<String> serversWithThisObjectAsNextComponent(String branchName) throws IOException {
		// no suited API operation, so direct, custom access to config file
		ArrayList<String> result = new ArrayList<String>(); 

		List<String> jaamSimConfiguration = Files.readAllLines(Paths.get(this.configFileName));
		for(String configFileEntry : jaamSimConfiguration) {
			if(configFileEntry.contains(branchName)&&configFileEntry.contains(NEXT_COMPONENT_KEYWORD)) {
				// is relevant and is a reference not the definition of branch name entity
				if(!configFileEntry.startsWith("Define")&&!configFileEntry.startsWith(branchName)) {
					result.add(configFileEntry.substring(0, configFileEntry.indexOf(" ")));
					// System.out.println("[I] Added: " + configFileEntry.substring(0, configFileEntry.indexOf(" ")));
				}
			}
		}

		return result;
	}
	
	public String findFirstDefinitionInConfigFile(String entityTypeKeyword) throws IOException {
		String result = "";
		List<String> jaamSimConfiguration = Files.readAllLines(Paths.get(this.configFileName));
		for(String configFileEntry : jaamSimConfiguration) {
			if(configFileEntry.startsWith(entityTypeKeyword)) {
				int firstQuotePos = configFileEntry.indexOf("'");
				int lastQuotePos = configFileEntry.lastIndexOf("'");
				result = configFileEntry.substring(firstQuotePos+1, lastQuotePos); 
			}
		}
		return result;
	}
	
	public ArrayList<String> findObjectDefinitionInConfigFile(String modelElementKeyword) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		modelElementKeyword = "Define " + modelElementKeyword;
		List<String> jaamSimConfiguration = Files.readAllLines(Paths.get(this.configFileName));
		for(String configFileEntry : jaamSimConfiguration) {
			// assuming that definitions start in line 1 of config file:
			if(configFileEntry.startsWith(modelElementKeyword)) {
				int offset = modelElementKeyword.length() + 2;
				String servers = configFileEntry.substring(offset, configFileEntry.length()-1); // could also use lastIndexOf }
				String[] modelObjectList = servers.split(" ");
				for(int i=0; i<modelObjectList.length;i++) {
					if(modelObjectList[i]!=null&&!modelObjectList[i].equals("")&&!modelObjectList[i].equals(" ")) {
						if(!modelObjectList[i].equals("{") && !modelObjectList[i].equals("}")) {
							result.add(modelObjectList[i]);
						}
					}
				}
			}
		}
		return result;
	}

	public List<String> findAttributeValueInEntity(String objectName, String attribute) {
		Entity entity = jsm.getEntity(objectName); 

		if(entity==null) {
			throw new IllegalArgumentException("Object entity " + objectName + " not found in simulation configuration.");
		}

		Input<?> inputField = entity.getInput(attribute);

		if(inputField==null) {
			// System.out.println("[D] Object entity " + joinName + " does not have an attribute " + attribute);
			return null;
		}
		
		return inputField.getValueTokens();
	}

	public List<String> findWaitQueueListOfEntity(String objectName) {
		Entity entity = jsm.getEntity(objectName); 

		if(entity==null) {
			throw new IllegalArgumentException("Object entity " + objectName + " not found in simulation configuration.");
		}

		Input<?> inputField = entity.getInput("WaitQueueList");

		if(inputField==null) {
			throw new IllegalArgumentException("Object entity " + objectName + " does not have NextComponentList input.");
		}

		return inputField.getValueTokens();
	}
	
	// ** JaamSim API access
	
	public void initializeJSM(String pathToConfigFile) throws URISyntaxException {
		if(jsm==null) {
			jsm = new JaamSimModel();
			jsm.autoLoad();
			File jaamSimModelConfiguration = new File (pathToConfigFile); // must be a URI or an absolute path
			jsm.configure(jaamSimModelConfiguration);
		}
	}

	public String getSimulationObjectType(String configurationElement) {
		Entity entity = jsm.getEntity(configurationElement);
		if(entity==null) {
			System.err.println("[W] getSimulationObjectType: Entity not found in configuration: " + configurationElement);
			return null;
		}
		return entity.getObjectType().getName();
	}

	public String findInputString(String elementName, String input) {
		Entity entity = jsm.getEntity(elementName); 
		if(entity==null) {
			throw new IllegalArgumentException("Did not find an entity " + elementName);
		}
		Input<?> inputValue = entity.getInput(input);
		if(inputValue==null) {
			throw new IllegalArgumentException("Did not find an input value " + input + " in " + elementName);
		}
		return trimTitle(inputValue.getValueString());
	}

	public List<String> findNextComponentsOfEntity(String simObject) {
		Entity entity = jsm.getEntity(simObject); 

		if(entity==null) {
			throw new IllegalArgumentException("findNextComponentsOfEntity: entity " + simObject + " not found in simulation configuration.");
		}

		Input<?> inputField = entity.getInput("NextComponentList");

		if(inputField==null) {
			throw new IllegalArgumentException("Object entity " + simObject + " does not have NextComponentList input.");
		}

		return inputField.getValueTokens();
	}

	public List<String> findTargetComponentListOfEntity(String modelElement) {
		Entity entity = jsm.getEntity(modelElement); 

		if(entity==null) {
			throw new IllegalArgumentException("Object entity " + modelElement + " not found in simulation configuration.");
		}

		Input<?> inputField = entity.getInput("TargetComponentList");

		if(inputField==null) {
			throw new IllegalArgumentException("Object entity " + modelElement + " does not have TargetComponentList input.");
		}

		return inputField.getValueTokens();
	}
	
	public String findNextComponentOfEntity(String modelElement) {
		Entity entity = jsm.getEntity(modelElement); 

		if(entity==null) {
			throw new IllegalArgumentException("Object entity " + modelElement + " not found in simulation configuration.");
		}

		Input<?> inputField = entity.getInput(NEXT_COMPONENT_KEYWORD);

		if(inputField==null) {
			throw new IllegalArgumentException("Object entity " + modelElement + " does not have NextComponent input.");
		}
		
		if(inputField.getValueTokens().size()==0) {
			MDSLLogger.reportError("No value object found for " + modelElement);
		}
		else if (inputField.getValueTokens().size()>1) {
			MDSLLogger.reportWarning("More than one value object found for " + modelElement + ", picking first");
		}

		return inputField.getValueTokens().get(0); 
	}
	
	// TODO merge previous and next method (String vs. List<String>), previous one could call next one, or client changed
	
	public List<String> findNextComponentsFollowingEntity(String simObject) {
		Entity entity = jsm.getEntity(simObject); 

		if(entity==null) {
			throw new IllegalArgumentException("Object entity " + simObject + " not found in simulation configuration.");
		}

		Input<?> inputField = entity.getInput(NEXT_COMPONENT_KEYWORD);

		if(inputField==null) {
			throw new IllegalArgumentException("Object entity " + simObject + " does not have NextComponent input.");
		}

		return inputField.getValueTokens();
	}
	
	// TODO ADL helper 
	
	// TODO COL support 
	
	// ** misc helpers
	
	static public String trimTitle(String text) {
		return text.replace("Title", "").replace("-", "_").replace(" ", "_").trim();
	}

	static public String normalizeName(String name) {
		String result = name.replaceAll("[&()]", "_");
		return result.replace('?', '_').replace('+', '_').replace('-', '_');
	}
}

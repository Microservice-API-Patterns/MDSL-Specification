/*
 * Copyright 2020 The Context Mapper and MDSL Project Teams
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
package io.mdsl.generator.bpmn;

import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MDSLLogger;

// mixed MDSL FreemarkerEngineWrapper and CML AbstractFreemarkerTextCreator

public abstract class AbstractFreemarkerTextCreator<T> {

	private Configuration freemarkerConfig;
	private Template freemarkerTemplate;
	
	public AbstractFreemarkerTextCreator(File templateFile) {
		loadFreemarkerTemplate();
	}
	
	public AbstractFreemarkerTextCreator(Class<?> templateLoadingClass, String templateName) {
		// this.templateLoadingClass = templateLoadingClass;
		// this.templateName = templateName;
		loadFreemarkerTemplate();
	}

	public String createText(T modelObject) {
		Map<String, Object> root = new HashMap<>();
		registerModelObjects(root, modelObject);
		StringWriter writer = new StringWriter();

		try {
			freemarkerTemplate.process(root, writer);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MDSLException("Error in processing freemarker template.", e);
		}
		return writer.toString();
	}

	protected abstract void registerModelObjects(Map<String, Object> root, T modelObject);

	protected abstract String getTemplateName();

	protected abstract Class<?> getTemplateClass();

	/*
	private File getTemplateDirectory() {
		
		try {
			URL url = getTemplateClass().getResource(getTemplateName());
		    URI home;
			home = url.toURI();
			// requires an extra bundle:
			String path = Paths.get(home).toAbsolutePath().toString();
			return new File(path, "");
		} catch (URISyntaxException e) {
			MDSLLogger.reportWarning("FTL not found at expected place (class loading)");
			e.printStackTrace();
		}
		// this is the directory that the IDE is installed to:
		return new File(Paths.get("").toAbsolutePath().toString(), "/temp/");
	}
	*/
	
	private void loadFreemarkerTemplate() {
		try {
			freemarkerConfig = new Configuration(Configuration.VERSION_2_3_30);
			
			// freemarkerConfig.setDirectoryForTemplateLoading(getTemplateDirectory());
			freemarkerConfig.setClassForTemplateLoading(getTemplateClass(), "");
			
			freemarkerConfig.setDefaultEncoding("UTF-8");
			freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			freemarkerTemplate = freemarkerConfig.getTemplate(this.getTemplateName());
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new MDSLException("Cannot load freemarker template.", e);
		}
	}
}

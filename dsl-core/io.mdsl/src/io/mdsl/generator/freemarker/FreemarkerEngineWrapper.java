package io.mdsl.generator.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.mdsl.apiDescription.serviceSpecification;
import io.mdsl.exception.MDSLException;

public class FreemarkerEngineWrapper {

	private File templateFile;
	private Map<String, Object> inputData;

	/**
	 * Wraps Freemarker generation engine to generate text from MDSL model.
	 * 
	 * @param template the Freemarker template to be used for generation
	 */
	public FreemarkerEngineWrapper(File templateFile) {
		this.templateFile = templateFile;
		this.inputData = new HashMap<>();
	}

	/**
	 * Generates the textual output using the Freemarker template engine.
	 * 
	 * @param mdslSpecification the MDSL model to be used for generation
	 * @return returns the generated text as String
	 */
	public String generate(serviceSpecification mdslSpecification) {
		try {
			// configure Freemarker
			Configuration configuration = configureFreemarker();
			Template template = configuration.getTemplate(templateFile.getName());

			// register data
			inputData.put("serviceSpecification", mdslSpecification);
			
			StringWriter writer = new StringWriter();
			template.process(this.inputData, writer);
			return writer.toString();
		} catch (Exception e) {
			throw new MDSLException("Freemarker generation exception occured: " + e.getMessage(), e);
		}
	}

	/**
	 * Can be used to register additional data used in Freemarker templates. Has to
	 * be called before the generate method.
	 * 
	 * @param propertyName the name of the property under which the data can be
	 *                     accessed in the templates
	 * @param object       the data object
	 */
	public void registerCustomData(String propertyName, Object object) {
		this.inputData.put(propertyName, object);
	}

	private Configuration configureFreemarker() throws IOException {
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
		configuration.setDirectoryForTemplateLoading(templateFile.getParentFile());
		configuration.setDefaultEncoding("UTF-8");
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		return configuration;
	}

}

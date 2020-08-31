package io.mdsl.generator.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;

public class FreemarkerEngineWrapper {

	private File templateFile;
	private Class<?> templateLoadingClass;
	private String templateName;
	private Map<String, Object> inputData;

	private FreemarkerEngineWrapper() {
		this.inputData = new HashMap<>();
	}

	/**
	 * Wraps Freemarker generation engine to generate text from MDSL model.
	 * 
	 * @param templateFile the Freemarker template to be used for generation
	 */
	public FreemarkerEngineWrapper(File templateFile) {
		this();
		this.templateFile = templateFile;
		this.templateName = templateFile.getName();
	}

	/**
	 * Wraps Freemarker generation engine to generate text from MDSL model.
	 * 
	 * @param templateLoadingClass a class that shall be used for template loading
	 * @param templateName         the name of the template (FTL file)
	 */
	public FreemarkerEngineWrapper(Class<?> templateLoadingClass, String templateName) {
		this();
		this.templateLoadingClass = templateLoadingClass;
		this.templateName = templateName;
	}

	/**
	 * Generates the textual output using the Freemarker template engine.
	 * 
	 * @param mdslSpecification the MDSL model to be used for generation
	 * @return returns the generated text as String
	 */
	public String generate(ServiceSpecification mdslSpecification) {
		try {
			// configure Freemarker
			Configuration configuration = configureFreemarker();
			Template template = configuration.getTemplate(templateName);

			// register data
			inputData.put("serviceSpecification", mdslSpecification);
			inputData.put("genModel", new MDSL2GeneratorModelConverter(mdslSpecification).convert());

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
		if (this.templateFile != null) { // template from workspace is used (user selection)
			configuration.setDirectoryForTemplateLoading(templateFile.getParentFile());
		} else { // template given by MDSL plugin is used (load via classloader)
			configuration.setClassForTemplateLoading(templateLoadingClass, "");
		}
		configuration.setDefaultEncoding("UTF-8");
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		return configuration;
	}

}

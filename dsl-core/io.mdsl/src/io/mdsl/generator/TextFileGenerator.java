package io.mdsl.generator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.serviceSpecification;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.freemarker.FreemarkerEngineWrapper;

/**
 * Generator to generate arbitrary text files (using a Freemarker template).
 * 
 * @author ska
 * 
 */
public class TextFileGenerator extends AbstractMDSLGenerator {

	private File freemarkerTemplateFile;
	private String targetFileName;
	private Map<String, Object> customDataMap = new HashMap<>();

	/**
	 * Configures the Freemarker template. Must be called before generation.
	 */
	public void setFreemarkerTemplateFile(File freemarkerTemplateFile) {
		this.freemarkerTemplateFile = freemarkerTemplateFile;
	}

	/**
	 * Configures the target/output file name. Must be called before generation.
	 */
	public void setTargetFileName(String targetFileName) {
		this.targetFileName = targetFileName;
	}

	/**
	 * Registers additional data to be used in the Freemarker template. (optional)
	 */
	public void registerCustomModelProperty(String propertyName, Object object) {
		customDataMap.put(propertyName, object);
	}

	@Override
	protected void generateFromServiceSpecification(serviceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		// check that preconditions are fulfilled:
		if (freemarkerTemplateFile == null)
			throw new MDSLException("The freemarker template has not been set!");
		if (!freemarkerTemplateFile.exists())
			throw new MDSLException("The file '" + freemarkerTemplateFile.getAbsolutePath().toString() + "' does not exist!");
		if (targetFileName == null || "".equals(targetFileName))
			throw new MDSLException("Please provide a name for the file that shall be generated.");

		// register some additional data:
		registerCustomModelProperty("timeStamp", new SimpleDateFormat("dd.MM.YYYY HH:mm:ss z").format(new Date()));
		registerCustomModelProperty("fileName", mdslSpecification.eResource().getURI().lastSegment().toString());
		registerCustomModelProperty("apiName", mdslSpecification.getName());

		// generate the file:
		FreemarkerEngineWrapper freemarkerWrapper = new FreemarkerEngineWrapper(this.freemarkerTemplateFile);
		for (Map.Entry<String, Object> customDataEntry : customDataMap.entrySet()) {
			freemarkerWrapper.registerCustomData(customDataEntry.getKey(), customDataEntry.getValue());
		}
		fsa.generateFile(targetFileName, freemarkerWrapper.generate(mdslSpecification));
	}

}

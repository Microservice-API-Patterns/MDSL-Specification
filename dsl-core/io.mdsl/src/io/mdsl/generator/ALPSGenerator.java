package io.mdsl.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.SimpleHash;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.freemarker.FreemarkerEngineWrapper;
import io.mdsl.generator.jolie.converter.MDSL2JolieConverter;
import io.mdsl.generator.jolie.converter.OperationModel;
import io.mdsl.generator.jolie.converter.TypeModel;

/**
* TODO
 */
public class ALPSGenerator extends AbstractMDSLGenerator {
	
	// private String targetFileName;
	private Map<String, Object> customDataMap = new HashMap<>();
	
	/**
	 * Registers additional data to be used in the Freemarker template. (optional)
	 */
	public void registerCustomModelProperty(String propertyName, Object object) {
		customDataMap.put(propertyName, object); // TODO move to base class?
	}


	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {

		String outputFilePrefix = inputFileURI.trimFileExtension().lastSegment();

		// register some additional data:
		registerCustomModelProperty("timeStamp", new SimpleDateFormat("dd.MM.YYYY HH:mm:ss z").format(new Date()));
		registerCustomModelProperty("fileName", mdslSpecification.eResource().getURI().lastSegment().toString());
		registerCustomModelProperty("apiName", mdslSpecification.getName());

		
		FreemarkerEngineWrapper freemarkerWrapper = new FreemarkerEngineWrapper(ALPSGenerator.class, "MDSLEndpointTypeToALPS.yaml.ftl");
		for (Map.Entry<String, Object> customDataEntry : customDataMap.entrySet()) {
			freemarkerWrapper.registerCustomData(customDataEntry.getKey(), customDataEntry.getValue());
		}
		String filledOutTemplate = freemarkerWrapper.generate(mdslSpecification);

		fsa.generateFile(outputFilePrefix + "-alps.yaml", filledOutTemplate);
	}
}

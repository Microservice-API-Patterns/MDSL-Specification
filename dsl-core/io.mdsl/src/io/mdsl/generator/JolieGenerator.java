package io.mdsl.generator;

import java.util.HashMap;

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
 * Generates Jolie interface and port (https://www.jolie-lang.org/) with an MDSL
 * model as input. Jolie, in turn, can be converted to WSDL/XSD: jolie2wsdl
 * --namespace "http://tbc.tcb" --portName nnPort --portAddr "localhost:8080"
 * --outputFile nn.wsdl ./nn.ol
 */
public class JolieGenerator extends AbstractMDSLGenerator {

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {

		MDSL2JolieConverter jolConv = new MDSL2JolieConverter(mdslSpecification);
		HashMap<String, OperationModel> targetModelEndpoints = jolConv.convertEndpoints();
		DefaultObjectWrapperBuilder dowb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_27);
		DefaultObjectWrapper ow = dowb.build();
		SimpleHash sh = new SimpleHash(ow);
		sh.put("operations", targetModelEndpoints);
		HashMap<String, TypeModel> targetModelTypes = jolConv.convertDataTypes();
		sh.put("types", targetModelTypes);
		String outputFilePrefix = inputFileURI.trimFileExtension().lastSegment();
		sh.put("specificationFilename", outputFilePrefix);
		
		FreemarkerEngineWrapper fmew = new FreemarkerEngineWrapper(JolieGenerator.class, "MDSL2JolieTemplate.ol.ftl");
		fmew.registerCustomData("jolieModel", sh);
		String filledOutTemplate = fmew.generate(mdslSpecification);

		fsa.generateFile(outputFilePrefix + ".ol", filledOutTemplate);
	}
}

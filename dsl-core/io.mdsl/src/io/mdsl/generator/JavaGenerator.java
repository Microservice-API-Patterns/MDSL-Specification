package io.mdsl.generator;

import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.freemarker.FreemarkerEngineWrapper;
import io.mdsl.generator.java.CapitalizeMethod;
import io.mdsl.generator.java.JavaOperationNameResolver;
import io.mdsl.generator.java.JavaPackageResolver;
import io.mdsl.generator.java.JavaPrimitiveTypeCheckingMethod;
import io.mdsl.generator.java.JavaPrimitiveTypeValueGenerationMethod;
import io.mdsl.generator.java.JavaTypeMappingMethod;
import io.mdsl.generator.model.DataType;
import io.mdsl.generator.model.EndpointContract;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;

/**
 * Generates Java code with Freemarker template
 */
public class JavaGenerator extends AbstractMDSLGenerator {

	private ServiceSpecificationAdapter mdsl;
	private MDSLGeneratorModel model;
	private IFileSystemAccess2 fsa;

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI) {
		this.mdsl = new ServiceSpecificationAdapter(mdslSpecification);
		this.model = new MDSL2GeneratorModelConverter(mdsl).convert();
		this.fsa = fsa;

		generateJavaCode4Endpoint();
	}

	private void generateClasses4DataTypes(EndpointContract endpoint) {
		for (DataType contract : model.getDataTypes().stream().filter(d -> !d.getName().equals("VoidResponse")).collect(Collectors.toList())) {
			FreemarkerEngineWrapper dataTypeEngine = getEngine("DataType.java.ftl");
			dataTypeEngine.registerCustomData("dataTypeName", contract.getName());
			dataTypeEngine.registerCustomData("endpointName", endpoint.getName());
			generateFile(endpoint, "/types/" + capitalize(contract.getName()) + ".java", dataTypeEngine.generate(mdsl));
		}
	}

	private void generateJavaCode4Endpoint() {
		for (EndpointContract endpoint : model.getEndpoints()) {
			generateClasses4DataTypes(endpoint);

			FreemarkerEngineWrapper interfaceEngine = getEngine("EndpointInterface.java.ftl");
			interfaceEngine.registerCustomData("endpointName", endpoint.getName());
			generateFile(endpoint, "/services/" + capitalize(endpoint.getName()) + ".java", interfaceEngine.generate(mdsl));

			FreemarkerEngineWrapper implEngine = getEngine("EndpointImplementation.java.ftl");
			implEngine.registerCustomData("endpointName", endpoint.getName());
			generateFile(endpoint, "/services/impl/" + capitalize(endpoint.getName()) + "Impl.java", implEngine.generate(mdsl));

			FreemarkerEngineWrapper testEngine = getEngine("EndpointTest.java.ftl");
			testEngine.registerCustomData("endpointName", endpoint.getName());
			generateFile(endpoint, "/services/test/" + capitalize(endpoint.getName()) + "Test.java", testEngine.generate(mdsl));
		}
	}

	private void generateFile(EndpointContract endpoint, String filename, String content) {
		fsa.generateFile(new JavaPackageResolver().getJavaPackage(model, endpoint).replace(".", "/") + filename, content);
	}

	private FreemarkerEngineWrapper getEngine(String templateName) {
		FreemarkerEngineWrapper engine = new FreemarkerEngineWrapper(JavaGenerator.class, "java/" + templateName);
		registerCommonCustomData(engine);
		return engine;
	}

	private void registerCommonCustomData(FreemarkerEngineWrapper engine) {
		engine.registerCustomData("mapType", new JavaTypeMappingMethod());
		engine.registerCustomData("isPrimitiveType", new JavaPrimitiveTypeCheckingMethod());
		engine.registerCustomData("generateRandomValue4PrimitiveType", new JavaPrimitiveTypeValueGenerationMethod());
		engine.registerCustomData("capitalize", new CapitalizeMethod());
		engine.registerCustomData("resolveOperationName", new JavaOperationNameResolver());
		engine.registerCustomData("resolveJavaPackage", new JavaPackageResolver());
	}

	private String capitalize(String name) {
		if (name == null || "".equals(name))
			return "";
		return name.length() == 1 ? name.substring(0, 1).toUpperCase() : name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}

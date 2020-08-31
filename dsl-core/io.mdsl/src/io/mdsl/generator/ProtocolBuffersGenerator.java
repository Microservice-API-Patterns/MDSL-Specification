package io.mdsl.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.protobuf.converter.MDSL2ProtobufConverter;

/**
 * Generates a Protocol Buffers (*.proto) file with an MDSL model as input.
 * 
 */
public class ProtocolBuffersGenerator extends AbstractMDSLGenerator {

	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {
		fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + ".proto",
				new MDSL2ProtobufConverter(mdslSpecification).convert().toString());
	}

}

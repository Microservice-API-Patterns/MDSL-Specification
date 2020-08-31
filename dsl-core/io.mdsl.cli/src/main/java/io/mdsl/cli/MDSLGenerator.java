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
package io.mdsl.cli;

import org.eclipse.xtext.generator.IGenerator2;

import io.mdsl.generator.GenModelJSONExporter;
import io.mdsl.generator.GenModelYAMLExporter;
import io.mdsl.generator.JolieGenerator;
import io.mdsl.generator.OpenAPIGenerator;
import io.mdsl.generator.ProtocolBuffersGenerator;
import io.mdsl.generator.TextFileGenerator;

/**
 * Enum representing the generators that are available in the CLI.
 * 
 * @author ska
 */
public enum MDSLGenerator {

	OPEN_API_SPEC("oas", "Open API Specification"), JOLIE("jolie", "Jolie"),
	ARBITRATY_TEXT_BY_TEMPLATE("text", "arbitraty text file by using a Freemarker template"),
	PROTOCOL_BUFFERS("proto", "Protocol Buffers"),
	GEN_MODEL_JSON_EXPORT("gen-model-json", "Generator model as JSON (exporter)"),
	GEN_MODEL_YAML_EXPORT("gen-model-yaml", "Generator model as YAML (exporter)");

	private String name;
	private String description;

	MDSLGenerator(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return this.name + " (" + this.description + ")";
	}

	public static MDSLGenerator byName(String name) {
		if (name == null)
			throw new RuntimeException("Please provide a name for the generator.");

		for (MDSLGenerator generator : values()) {
			if (generator.getName().equals(name))
				return generator;
		}

		throw new RuntimeException("No generator found for the name '" + name + "'.");
	}

	/**
	 * Generator mapping ...
	 */
	public IGenerator2 getGenerator() {
		if (this == OPEN_API_SPEC)
			return new OpenAPIGenerator();
		if (this == JOLIE)
			return new JolieGenerator();
		if (this == PROTOCOL_BUFFERS)
			return new ProtocolBuffersGenerator();
		if (this == GEN_MODEL_JSON_EXPORT)
			return new GenModelJSONExporter();
		if (this == GEN_MODEL_YAML_EXPORT)
			return new GenModelYAMLExporter();
		return new TextFileGenerator();
	}

}

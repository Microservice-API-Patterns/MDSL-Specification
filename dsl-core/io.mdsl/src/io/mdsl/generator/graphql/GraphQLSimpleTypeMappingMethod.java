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
package io.mdsl.generator.graphql;

import java.util.List;

// import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class GraphQLSimpleTypeMappingMethod implements TemplateMethodModelEx {

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 2)
			throw new TemplateModelException("Wrong amount of arguments for method 'mapType2GraphQL'.");

		String typeName = ((SimpleScalar) arguments.get(0)).getAsString();
		String customTypePostfix = ((SimpleScalar) arguments.get(1)).getAsString();
		switch (typeName) {
		case "string":
			return "String";
		case "bool":
			return "Boolean";
		case "int":
			return "Int";
		case "long":
			return "Int";
		case "double":
			return "Float";
		case "void":
			return "VoidResponse";
		case "VoidResponse":
			return "VoidResponse";
		case "raw":
			return "Raw";
		}
		return typeName + customTypePostfix;
	}

}

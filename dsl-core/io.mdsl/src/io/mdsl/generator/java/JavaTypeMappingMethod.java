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
package io.mdsl.generator.java;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class JavaTypeMappingMethod implements TemplateMethodModelEx {

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1 && arguments.size() != 2)
			throw new TemplateModelException("Wrong amount of arguments for method 'mapType'.");

		String typeName = ((SimpleScalar) arguments.get(0)).getAsString();
		boolean isResponseType = false;
		if (arguments.size() == 2)
			isResponseType = ((TemplateBooleanModel) arguments.get(1)).getAsBoolean();

		switch (typeName) {
		case "string":
			return "String";
		case "bool":
			return "Boolean";
		case "int":
			return "Integer";
		case "long":
			return "Long";
		case "double":
			return "Double";
		case "void":
			return isResponseType ? "void" : "Void";
		case "raw":
			return "byte[]";
		case "VoidResponse":
			return isResponseType ? "void" : "Void";
		}
		return typeName.length() == 1 ? typeName.substring(0, 1).toUpperCase() : typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
	}

}

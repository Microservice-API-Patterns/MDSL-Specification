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

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class GraphQLOperationInputTypeNameResolver implements TemplateMethodModelEx {

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1)
			throw new TemplateModelException(
					"Wrong amount of arguments for method 'resolveOperationInputName'. We expect one single String as parameter (the name of the operation).");

		String operationName = ((SimpleScalar) arguments.get(0)).getAsString();

		if (operationName.length() > 1)
			operationName = operationName.substring(0, 1).toUpperCase() + operationName.substring(1);
		else
			operationName = operationName.toUpperCase();

		return operationName + "Input";
	}

}

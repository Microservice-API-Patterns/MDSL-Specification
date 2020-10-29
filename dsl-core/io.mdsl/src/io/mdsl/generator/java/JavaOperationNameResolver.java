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

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mdsl.generator.model.EndpointContract;
import io.mdsl.generator.model.JavaBinding;
import io.mdsl.generator.model.Operation;

public class JavaOperationNameResolver implements TemplateMethodModelEx {

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 2)
			throw new TemplateModelException("Wrong amount of arguments for method 'resolveOperationName'.");
		if (!(((StringModel) arguments.get(0)).getWrappedObject() instanceof EndpointContract))
			throw new TemplateModelException("The first parameter must be the EndpointContract (generator model) object.");
		if (!(((StringModel) arguments.get(1)).getWrappedObject() instanceof Operation))
			throw new TemplateModelException("The first parameter must be the Operation (generator model) object.");

		EndpointContract endpoint = (EndpointContract) ((StringModel) arguments.get(0)).getWrappedObject();
		Operation operation = (Operation) ((StringModel) arguments.get(1)).getWrappedObject();

		return endpoint.getProtocolBinding() != null && endpoint.getProtocolBinding() instanceof JavaBinding
				? ((JavaBinding) endpoint.getProtocolBinding()).getJavaMethodName4Operation(operation.getName())
				: operation.getName();
	}

}

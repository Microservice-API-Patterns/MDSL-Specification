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
import io.mdsl.generator.model.MDSLGeneratorModel;

public class JavaPackageResolver implements TemplateMethodModelEx {

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 2)
			throw new TemplateModelException("Wrong amount of arguments for method 'resolvePackageName'.");
		if (!(((StringModel) arguments.get(0)).getWrappedObject() instanceof MDSLGeneratorModel))
			throw new TemplateModelException("The first parameter must be the MDSLGeneratorModel (generator model) object.");
		if (!(((StringModel) arguments.get(1)).getWrappedObject() instanceof EndpointContract))
			throw new TemplateModelException("The first parameter must be the EndpointContract (generator model) object.");

		return getJavaPackage((MDSLGeneratorModel) ((StringModel) arguments.get(0)).getWrappedObject(), (EndpointContract) ((StringModel) arguments.get(1)).getWrappedObject());
	}

	/**
	 * Returns the root package that shall be used for generating the Java code.
	 * 
	 * @param model    the MDSL generator model instance
	 * @param endpoint the endpoint for which the java code is generated
	 * @return the package root name that shall be used
	 */
	public String getJavaPackage(MDSLGeneratorModel model, EndpointContract endpoint) {
		if (endpoint.getProtocolBinding() != null && endpoint.getProtocolBinding() instanceof JavaBinding) {
			String javaPackage = ((JavaBinding) endpoint.getProtocolBinding()).getPackage();
			if (javaPackage != null && !"".equals(javaPackage))
				return javaPackage.toLowerCase();
		}
		return model.getApiName().toLowerCase() + "." + endpoint.getName().toLowerCase();
	}

}

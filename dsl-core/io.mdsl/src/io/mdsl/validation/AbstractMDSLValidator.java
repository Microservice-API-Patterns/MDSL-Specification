/*
 * Copyright 2020 Olaf Zimmermann. All rights reserved.
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
package io.mdsl.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;

import io.mdsl.apiDescription.ServiceSpecification;

public class AbstractMDSLValidator extends AbstractDeclarativeValidator {

	public static final String ID_VALIDATION_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]*";

	protected ServiceSpecification getRootMDSLModel(EObject modelElement) {
		return (ServiceSpecification) EcoreUtil.getRootContainer(modelElement);
	}

}
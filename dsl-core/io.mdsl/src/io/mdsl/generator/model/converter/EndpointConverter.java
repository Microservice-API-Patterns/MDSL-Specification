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
package io.mdsl.generator.model.converter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointList;
import io.mdsl.apiDescription.JavaBinding;
import io.mdsl.apiDescription.JavaOperationBinding;
import io.mdsl.apiDescription.OperationResponsibility;
// import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.generator.AnonymousFieldNameGenerator;
import io.mdsl.generator.CardinalityHelper;
import io.mdsl.generator.model.DataType;
import io.mdsl.generator.model.DataTypeField;
import io.mdsl.generator.model.EndpointContract;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.Operation;
import io.mdsl.generator.model.OperationParameter;
import io.mdsl.generator.model.ProtocolBinding;

/**
 * Converts MDSL endpoints (AST model) into endpoints of our generator model.
 *
 */
public class EndpointConverter {

	private MDSLGeneratorModel model;
	private DataTypeConverter dataTypeConverter;
	private ServiceSpecificationAdapter serviceSpecification;

	public EndpointConverter(ServiceSpecificationAdapter serviceSpecification, MDSLGeneratorModel model, DataTypeConverter dataTypeConverter) {
		this.serviceSpecification = serviceSpecification;
		this.model = model;
		this.dataTypeConverter = dataTypeConverter;
	}

	public EndpointContract convert(io.mdsl.apiDescription.EndpointContract mdslEndpoint) {
		EndpointContract endpoint = new EndpointContract(mdslEndpoint.getName());
		endpoint.setProtocolBinding(createProtocolBindingIfAvailable(mdslEndpoint.getName()));
		for (io.mdsl.apiDescription.Operation operation : mdslEndpoint.getOps()) {
			endpoint.addOperation(convertOperation(operation));
		}
		return endpoint;
	}

	private Operation convertOperation(io.mdsl.apiDescription.Operation operation) {
		DataType input = null;
		String inputName = "anonymousInput";
		DataType output = null;
		if (operation.getRequestMessage() != null) {
			// handle references specially: in this case we can assume the message as
			// already been created
			if (operation.getRequestMessage().getPayload().getNp() != null && operation.getRequestMessage().getPayload().getNp().getTr() != null) {
				TypeReference ref = operation.getRequestMessage().getPayload().getNp().getTr();
				if (ref.getName() != null && !"".equals(ref.getName()))
					inputName = new AnonymousFieldNameGenerator().getUniqueName(ref.getName());
				input = wrapDataTypeIntoListTypeIfNecessary(getExistingDataTypeOrCreateEmpty(ref.getDcref().getName()), ref.getCard());
			} else {
				ElementStructure payload = operation.getRequestMessage().getPayload();
				String optName = getElementStructureName(payload);
				if (optName != null && !"".equals(optName))
					inputName = new AnonymousFieldNameGenerator().getUniqueName(optName);
				input = wrapDataTypeIntoListTypeIfNecessary(createNewDataType(operation.getName() + "RequestDataType", payload), getCardinality4ElementStructure(payload));
			}
		}
		if (operation.getResponseMessage() != null) {
			// handle references specially: in this case we can assume the message as
			// already been created
			if (operation.getResponseMessage().getPayload().getNp() != null && operation.getResponseMessage().getPayload().getNp().getTr() != null) {
				TypeReference ref = operation.getResponseMessage().getPayload().getNp().getTr();
				output = wrapDataTypeIntoListTypeIfNecessary(getExistingDataTypeOrCreateEmpty(ref.getDcref().getName()), ref.getCard());
			} else {
				ElementStructure payload = operation.getResponseMessage().getPayload();
				output = wrapDataTypeIntoListTypeIfNecessary(
						createNewDataType(operation.getName().substring(0, 1).toUpperCase() + operation.getName().substring(1) + "ResponseDataType", payload),
						getCardinality4ElementStructure(payload));
			}
		}

		if (output == null) {
			output = getExistingDataTypeOrCreateEmpty("VoidResponse");
		}

		Operation genModelOperation = new Operation(operation.getName());
		genModelOperation.setResponse(output);
		if (input != null) {
			OperationParameter parameter = new OperationParameter(inputName, input);
			genModelOperation.addParameter(parameter);
		}
		genModelOperation.setResponsibility(getOperationResponsibility(operation.getResponsibility()));

		return genModelOperation;
	}

	private DataType getExistingDataTypeOrCreateEmpty(String name) {
		Optional<DataType> optDataType = this.model.getDataTypes().stream().filter(d -> d.getName().equals(name)).findFirst();
		if (optDataType.isPresent()) {
			return optDataType.get();
		} else {
			DataType dataType = new DataType(name);
			this.model.addDataType(dataType);
			return dataType;
		}
	}

	private DataType createNewDataType(String name, ElementStructure elementStructure) {
		DataType dataType = new DataType(name);
		this.dataTypeConverter.mapElementStructure(elementStructure, dataType);
		this.model.addDataType(dataType);
		return dataType;
	}

	private DataType wrapDataTypeIntoListTypeIfNecessary(DataType dataType, Cardinality card) {
		if (CardinalityHelper.isList(card)) {
			Optional<DataType> alreadyExistingList = getDataTypeIfAlreadyExists(dataType.getName() + "List");
			if (alreadyExistingList.isPresent())
				return alreadyExistingList.get();

			DataType wrapper = new DataType(dataType.getName() + "List");
			DataTypeField field = new DataTypeField("entries");
			field.setType(dataType);
			field.isList(true);
			wrapper.addField(field);

			model.addDataType(wrapper);
			return wrapper;
		} else if (CardinalityHelper.isOptional(card)) {
			Optional<DataType> alreadyExistingOptionalType = getDataTypeIfAlreadyExists(dataType.getName() + "Optional");
			if (alreadyExistingOptionalType.isPresent())
				return alreadyExistingOptionalType.get();

			DataType wrapper = new DataType(dataType.getName() + "Optional");
			DataTypeField field = new DataTypeField("value");
			field.setType(dataType);
			field.isList(false);
			field.isNullable(true);
			wrapper.addField(field);

			model.addDataType(wrapper);
			return wrapper;
		}

		return dataType;
	}

	private Optional<DataType> getDataTypeIfAlreadyExists(String name) {
		return model.getDataTypes().stream().filter(d -> d.getName().equals(name)).findFirst();
	}

	private Cardinality getCardinality4ElementStructure(ElementStructure elementStructure) {
		if (elementStructure.getPt() != null) {
			return elementStructure.getPt().getCard();
		} else if (elementStructure.getApl() != null) {
			return elementStructure.getApl().getCard();
		} else if (elementStructure.getNp() != null) {
			if (elementStructure.getNp().getAtomP() != null) {
				return elementStructure.getNp().getAtomP().getCard();
			} else if (elementStructure.getNp().getTr() != null) {
				return elementStructure.getNp().getTr().getCard();
			}
		}
		return null;
	}

	private String getOperationResponsibility(OperationResponsibility operationResponsibility) {
		if (operationResponsibility == null)
			return "";
		if (operationResponsibility.getCf() != null && !"".equals(operationResponsibility.getCf()))
			return operationResponsibility.getCf();
		if (operationResponsibility.getBap() != null && !"".equals(operationResponsibility.getBap()))
			return operationResponsibility.getBap();
		if (operationResponsibility.getEp() != null && !"".equals(operationResponsibility.getEp()))
			return operationResponsibility.getEp();
		if (operationResponsibility.getRo() != null && !"".equals(operationResponsibility.getRo()))
			return operationResponsibility.getRo();
		if (operationResponsibility.getSco() != null && !"".equals(operationResponsibility.getSco()))
			return operationResponsibility.getSco();
		if (operationResponsibility.getSto() != null && !"".equals(operationResponsibility.getSto()))
			return operationResponsibility.getSto();
		if (operationResponsibility.getOther() != null && !"".equals(operationResponsibility.getOther()))
			return operationResponsibility.getOther();
		return "";
	}

	private ProtocolBinding createProtocolBindingIfAvailable(String endpointName) {
		List<EndpointList> endpointListList = EcoreUtil2.eAllOfType(serviceSpecification, EndpointList.class).stream()
				.filter(e -> e.getContract() != null && endpointName.equals(e.getContract().getName())).collect(Collectors.toList());
		for (EndpointList endpointList : endpointListList) {
			Optional<JavaBinding> javaBinding = EcoreUtil2.eAllOfType(endpointList, JavaBinding.class).stream().findFirst();
			if (javaBinding.isPresent())
				return mapJavaBinding(javaBinding.get());
			// TODO: handle other bindings (for now we just respect Java bindings)
		}
		return null;
	}

	private ProtocolBinding mapJavaBinding(JavaBinding mdslJavaBinding) {
		io.mdsl.generator.model.JavaBinding binding = new io.mdsl.generator.model.JavaBinding();
		if (mdslJavaBinding.getPackage() != null)
			binding.setPackage(mdslJavaBinding.getPackage());
		for (JavaOperationBinding operationBinding : mdslJavaBinding.getOpsBinding()) {
			binding.mapOperationName(operationBinding.getBoundOperation(), operationBinding.getMethod());
		}
		return binding;
	}

	private String getElementStructureName(ElementStructure structure) {
		if (structure.getApl() != null)
			return structure.getApl().getName();
		if (structure.getNp() != null && structure.getNp().getAtomP() != null && structure.getNp().getAtomP().getRat() != null)
			return structure.getNp().getAtomP().getRat().getName();
		if (structure.getNp() != null && structure.getNp().getGenP() != null)
			return structure.getNp().getGenP().getName();
		if (structure.getNp() != null && structure.getNp().getTr() != null)
			return structure.getNp().getTr().getName();
		if (structure.getPt() != null)
			structure.getPt().getName();
		return "";
	}

}

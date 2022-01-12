package io.mdsl.transformations;

import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.utils.MDSLLogger;

public class TransformationChains {
	public EndpointContract fromStoryToEndpointTypeWithHTTPBinding(ServiceSpecification apiDescription, String scenarioName) {
		IntegrationScenario is = TransformationHelpers.findScenarioInSpecifcation(apiDescription, scenarioName);
		ScenarioTransformations sts = new ScenarioTransformations();
		EndpointContract ec = sts.addEndpointForScenario(is, true);

		MAPDecoratorHelpers.setRole(ec, MAPDecoratorHelpers.PROCESSING_RESOURCE);
		OperationTransformations ots = new OperationTransformations();
		ots.addOperationsForRole(ec); // move to EndpointTransformations class?

		HTTPBindingTransformations hbts = new HTTPBindingTransformations();
		hbts.addBinding(ec);
		// TODO (future work) fix binding for OASgen-ability (such QFs exist)
		return ec;
	}

	public void addQosManagement(ServiceSpecification apiDescription, String endpointTypeName) {
		EndpointContract ept = TransformationHelpers.findOrCreateEndpointType(apiDescription, endpointTypeName);

		OperationTransformations ot = new OperationTransformations();
		for (Operation operation : ept.getOps()) {
			if (operation.getReports() == null) {
				ot.completeOperationWithErrorReport(operation);
			}
			if (operation.getPolicies() == null) {
				ot.completeOperationWithSecurityPolicy(operation);
			}
			/*
			if (operation.getUndo() == null) {
				MDSLLogger.reportInformation("Could apply Add Compensation " + operation.getName());
				// cannot decide which method to hand over to
				// ot.completeOperationWithCompensation(operation, null); // add an operation in this case?
			}
			*/
			try {
				if (operation.getRequestMessage().getPayload().getPt() != null) {
					MessageTransformations.addContextRepresentation(operation, null); // null DTO is ok
				}
			} catch (Exception e) {
				MDSLLogger.reportWarning("Unexpected/unsupported request payload in " + operation.getName());
			}
			// TODO also apply to response
		}
		// TODO Event Management operations, AsyncMDSL (?)
	}

	public void improveQueryPerformanceWithPaginationAndWishList(ServiceSpecification apiDescription, String endpointTypeName) {
		// TODO could also apply Make Request Conditional, Add WishTemplate (?)
		EndpointContract ept = TransformationHelpers.findOrCreateEndpointType(apiDescription, endpointTypeName); // TODO use constant or navigation
		for (Operation operation : ept.getOps()) {
			ElementStructure requestPayload = operation.getRequestMessage().getPayload();
			if (MAPDecoratorHelpers.isRetrievalOperation(operation)) {
				if (requestPayload != null) {
					hardenAndCompletePayload(requestPayload);
					MessageTransformations.addWishList(operation);
				} else {
					continue;
				}

				ElementStructure responsePayload = operation.getResponseMessage().getPayload();
				if (responsePayload != null) {
					hardenAndCompletePayload(responsePayload);
					MessageTransformations.addPagination(responsePayload, MessageTransformations.OFFSET_FROM_OPERATION);
				}
			} else {
				MDSLLogger.reportWarning("This composite transformation can only be applied to retrieval operations, not to " 
					+ operation.getName() + " in endpoint type " + ept.getName());
			}
		}
	}

	public void improveCommandPerformanceWithRequestBundleAndCQRS(ServiceSpecification apiDescription, String endpointTypeName) {
		EndpointContract ept = TransformationHelpers.findOrCreateEndpointType(apiDescription, endpointTypeName);
		EndpointTransformations ets = new EndpointTransformations();
		ets.separateCommandsFromQueries(ept);

		for (Operation operation : ept.getOps()) {
			ElementStructure requestElement = operation.getRequestMessage().getPayload();
			if (requestElement != null && !MAPDecoratorHelpers.isRetrievalOperation(operation) && !MAPDecoratorHelpers.isDeleteOperation(operation)) {
				hardenAndCompletePayload(requestElement);
				if (requestElement.getPt() != null) {
					MessageTransformations.addRequestBundle(requestElement, true);
				}
			}
			ElementStructure responseElement = operation.getRequestMessage().getPayload();
			if (responseElement != null && !MAPDecoratorHelpers.isRetrievalOperation(operation) && !MAPDecoratorHelpers.isDeleteOperation(operation)) {
				hardenAndCompletePayload(responseElement);
				if (requestElement.getPt() != null) {
					MessageTransformations.addRequestBundle(responseElement, false);
				}
			}
		}
	}

	private void hardenAndCompletePayload(ElementStructure payload) {
		if (payload.getPt() != null) {
			return; // no need to harden (could set name if not there)
		}
		if (payload.getNp() != null) {
			if (payload.getNp().getGenP() != null) {
				DataTypeTransformations.convertToStringType(payload.getNp().getGenP());
			}

			if (payload.getNp().getAtomP() != null) {
				MessageTransformations.addParameterTreeWrapper(payload.getNp().getAtomP());
			} else if (payload.getNp().getTr() != null) {
				MessageTransformations.addParameterTreeWrapper(payload.getNp().getTr());
			}
		}
	}

	public void improveCohesionAndCouplingScore(ServiceSpecification apiDescription, String endpointTypeName) {
		// TODO apply (many) MAP role decorators, see soad.md in MDSL docs
		// TODO extract IH, inline IH
		// TODO split operation, move operation, extract endpoint
	}

	// TODO (L) more HTTP transformations, patch binding for OASgen, more data type transformations

	public void applyEntireChainToAllScenariosAndStories(ServiceSpecification apiDescription) {
		for (IntegrationScenario scenario : apiDescription.getScenarios()) {
			EndpointContract ec = fromStoryToEndpointTypeWithHTTPBinding(apiDescription, scenario.getName());
			addQosManagement(apiDescription, ec.getName());
			improveQueryPerformanceWithPaginationAndWishList(apiDescription, ec.getName());
			improveCommandPerformanceWithRequestBundleAndCQRS(apiDescription, ec.getName());
		}
	}

	public void applyEntireChainToScenariosAndItsStories(IntegrationScenario scenario, String desiredQuality) {
		if (desiredQuality != "performance") {
			MDSLLogger.reportError("Unsupported target quality: " + desiredQuality);
		}

		ServiceSpecification apiDescription = (ServiceSpecification) scenario.eContainer();
		// TODO be more selective
		EndpointContract ec = fromStoryToEndpointTypeWithHTTPBinding(apiDescription, scenario.getName());
		addQosManagement(apiDescription, ec.getName());
		improveQueryPerformanceWithPaginationAndWishList(apiDescription, ec.getName());
		improveCommandPerformanceWithRequestBundleAndCQRS(apiDescription, ec.getName());
	}
}

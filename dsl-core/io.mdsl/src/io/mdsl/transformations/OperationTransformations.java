package io.mdsl.transformations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.SecurityPolicies;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.StatusReport;
import io.mdsl.apiDescription.StatusReports;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;

public class OperationTransformations {

	private static final String STATELESS_PROCESSING_RESOURCE = "STATELESS_PROCESSING_RESOURCE";
	private static final String TRANSFORMATION_RESOURCE = "TRANSFORMATION_RESOURCE";
	private static final String VALIDATION_RESOURCE = "VALIDATION_RESOURCE";
	private static final String LINK_LOOKUP_RESOURCE = "LINK_LOOKUP_RESOURCE";
	private static final String DATA_TRANSFER_RESOURCE = "DATA_TRANSFER_RESOURCE";
	private static final String REFERENCE_DATA_HOLDER = "REFERENCE_DATA_HOLDER";
	private static final String OPERATIONAL_DATA_HOLDER = "OPERATIONAL_DATA_HOLDER";
	private static final String MASTER_DATA_HOLDER = "MASTER_DATA_HOLDER";
	private static final String STATEFUL_PROCESSING_RESOURCE = "STATEFUL_PROCESSING_RESOURCE";

	private static final String OUTPUT = "output";
	private static final String INPUT = "input";
	private static final String PERFORM_ROLE_PREFIX = "performRole";

	// ** MAP transformations

	public void addOperationsForRole(EndpointContract ec) {
		boolean foundMatchingRole = false;
		EList<String> otherRoles = ec.getOtherRoles();

		// pattern explanations: https://microservice-api-patterns.org/patterns/responsibility/

		if (ec.getPrimaryRole() == null) {
			throw new MDSLException("Endpoint is not decorated with a primary role");
		}

		// * Processing Resource (PR)

		if (ec.getPrimaryRole().equals(MAPDecoratorHelpers.PROCESSING_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, MAPDecoratorHelpers.PROCESSING_RESOURCE)) {
			OperationTransformationHelpers.addProcessingResourceOperations(ec);
			foundMatchingRole = true;
		} else if (ec.getPrimaryRole().equals(STATEFUL_PROCESSING_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, STATEFUL_PROCESSING_RESOURCE)) {
			OperationTransformationHelpers.addProcessingResourceOperations(ec); //same as PROCESSING_RESOURCE
			foundMatchingRole = true;
		}

		// * Information Holder Resource (IHR)

		if (ec.getPrimaryRole().equals(MAPDecoratorHelpers.INFORMATION_HOLDER_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, MAPDecoratorHelpers.INFORMATION_HOLDER_RESOURCE)) {
			OperationTransformationHelpers.addInformationHolderOperations(ec);
			foundMatchingRole = true;
		} else if (ec.getPrimaryRole().equals(MASTER_DATA_HOLDER) || MAPDecoratorHelpers.alsoServesAs(otherRoles, MASTER_DATA_HOLDER)) {
			OperationTransformationHelpers.addInformationHolderOperations(ec); // TODO (L) could be more specific
			foundMatchingRole = true;
		} else if (ec.getPrimaryRole().equals(OPERATIONAL_DATA_HOLDER) || MAPDecoratorHelpers.alsoServesAs(otherRoles, OPERATIONAL_DATA_HOLDER)) {
			OperationTransformationHelpers.addInformationHolderOperations(ec); // TODO (L) could be more specific
			foundMatchingRole = true;
		} else if (ec.getPrimaryRole().equals(REFERENCE_DATA_HOLDER) || MAPDecoratorHelpers.alsoServesAs(otherRoles, REFERENCE_DATA_HOLDER)) {
			OperationTransformationHelpers.addInformationHolderOperations(ec); // TODO (L) could be more specific
			foundMatchingRole = true;
		}

		// * Other IHR specializations and PR variants

		if (ec.getPrimaryRole().equals(DATA_TRANSFER_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, DATA_TRANSFER_RESOURCE)) {
			OperationTransformationHelpers.addDataTransferResourceOperations(ec);
			foundMatchingRole = true;
		}
		if (ec.getPrimaryRole().equals(LINK_LOOKUP_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, LINK_LOOKUP_RESOURCE)) {
			OperationTransformationHelpers.addLinkLookupOperations(ec);
			foundMatchingRole = true;
		}
		if (ec.getPrimaryRole().equals(VALIDATION_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, VALIDATION_RESOURCE)) {
			OperationTransformationHelpers.addCheckOperation(ec);
			foundMatchingRole = true;
		}
		if (ec.getPrimaryRole().equals(TRANSFORMATION_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, TRANSFORMATION_RESOURCE)) {
			// could be an "other" string, using this to add CF operation
			OperationTransformationHelpers.addTransformationOperation(ec);
			foundMatchingRole = true;
		}

		// * Non-MAP IHR specializations and PR variants

		// not performing a primaryRole check (on purpose)
		if (MAPDecoratorHelpers.alsoServesAs(otherRoles, MAPDecoratorHelpers.COLLECTION_RESOURCE)) {
			OperationTransformationHelpers.addCollectionOperations(ec, false);
			foundMatchingRole = true;
		}

		else if (MAPDecoratorHelpers.alsoServesAs(otherRoles, MAPDecoratorHelpers.MUTABLE_COLLECTION_RESOURCE)) {
			OperationTransformationHelpers.addCollectionOperations(ec, true);
			foundMatchingRole = true;
		}

		if (ec.getPrimaryRole().equals(STATELESS_PROCESSING_RESOURCE) || MAPDecoratorHelpers.alsoServesAs(otherRoles, STATELESS_PROCESSING_RESOURCE)) {
			// could be an "other" string, using this to add CF op. somewhere
			OperationTransformationHelpers.addRunOperation(ec);
			foundMatchingRole = true;
		}

		if (!foundMatchingRole) {
			Operation defaultOp = TransformationHelpers.createOperationWithGenericParameters(PERFORM_ROLE_PREFIX + TransformationHelpers.trimRoleName(ec.getPrimaryRole()), INPUT, OUTPUT, true);
			ec.getOps().add(defaultOp);
		}
	}

	// ** MDSL completion transformations

	public void completeOperationWithCompensation(Operation operation, String opRefName) {
		EndpointContract ec = (EndpointContract) operation.eContainer();
		Operation opRef = TransformationHelpers.findOperationInContract(ec, opRefName);
		if (opRef != null) {
			operation.setUndo(opRef);
		} else {
			MDSLLogger.reportError("Compensating operation " + opRefName + " not found in contract " + ec.getName());
		}
	}

	public void completeOperationWithErrorReport(Operation operation) {
		StatusReports srs = ApiDescriptionFactory.eINSTANCE.createStatusReports();
		StatusReport sr = ApiDescriptionFactory.eINSTANCE.createStatusReport();
		sr.setName("errorReport");
		AtomicParameter ap = DataTypeTransformations.createAtomicDataParameter("statusCode", "int");
		ElementStructure es = DataTypeTransformations.wrapAtomicParameterAsElementStructure(ap);
		sr.setReportData(es);
		srs.getReportList().add(sr);
		operation.setReports(srs);
	}

	public void completeOperationWithSecurityPolicy(Operation operation) {
		SecurityPolicies sps = ApiDescriptionFactory.eINSTANCE.createSecurityPolicies();
		SecurityPolicy sp = ApiDescriptionFactory.eINSTANCE.createSecurityPolicy();
		sp.setName("accessControlRule");
		AtomicParameter ap = DataTypeTransformations.createAtomicDataParameter("policyData", OperationTransformationHelpers.STRING_TYPE);
		ElementStructure es = DataTypeTransformations.wrapAtomicParameterAsElementStructure(ap);
		sp.setSecurityObject(es);
		sps.getPolicyList().add(sp);
		operation.setPolicies(sps);
	}

	// ** structural refactorings

	// this renameOperation transformation is not needed, as EMF offers "Rename Element":
	public MDSLResource renameOperation(MDSLResource mdslResource, String opName, String opNameNew) {
		ServiceSpecification mdslSpecRoot = mdslResource.getServiceSpecification();
		boolean found = false;

		List<Operation> operations = EcoreUtil2.eAllOfType(mdslSpecRoot, Operation.class);
		for (Operation nextOperation : operations) {
			if (nextOperation.getName().equals(opName)) {
				found = true;
				nextOperation.setName(opNameNew);
			}
		}
		if (!found) {
			throw new MDSLException("Operation " + opName + " not found.");
		}
		return mdslResource;
	}

	public MDSLResource moveOperation(Operation opToBeMoved, String targetEndpointName) {
		ServiceSpecification mdslSpecRoot = (ServiceSpecification) opToBeMoved.eContainer().eContainer();

		EndpointContract sourceEndpointType = (EndpointContract) opToBeMoved.eContainer();
		EndpointContract targetEndpointType = TransformationHelpers.findOrCreateEndpointType(mdslSpecRoot, targetEndpointName);

		if (TransformationHelpers.hasOperationOfName(targetEndpointType, opToBeMoved)) {
			throw new MDSLException("Target endpoint " + targetEndpointType.getName() + " already has an operation of this name.");
		}
		checkCompensationRelations(sourceEndpointType, opToBeMoved);

		// create in new or existing endpoint contract:
		targetEndpointType.getOps().add(opToBeMoved);
		
		// add map ROLE decorator to target endpoint if present in source
		if(sourceEndpointType.getPrimaryRole()!=null) {
			MAPDecoratorHelpers.addRole(targetEndpointType, sourceEndpointType.getPrimaryRole());
			if(sourceEndpointType.getOtherRoles()!=null) {
				sourceEndpointType.getOtherRoles().forEach(secondaryRole->MAPDecoratorHelpers.addRole(targetEndpointType, secondaryRole));
			}
		}
		
		// copy flow and story links to target ept (unless this would overwrite existing ones)
		if(sourceEndpointType.getScenario()!=null&&targetEndpointType.getScenario()==null) {
			targetEndpointType.setScenario(sourceEndpointType.getScenario());
		}
		
		if(sourceEndpointType.getFlow()!=null&&targetEndpointType.getFlow()==null) {
			targetEndpointType.setFlow(sourceEndpointType.getFlow());
		}

		// delete in current epc endpoint contract not required here, EMF has move semantics

		TransformationHelpers.findAndAdjustHTTPResourceBindingOfOperation(mdslSpecRoot, sourceEndpointType, opToBeMoved, targetEndpointType);

		return new MDSLResource(targetEndpointType.eResource());
	}

	public void splitOperation(Operation opToBeSplit, boolean touchResponse) {
		// check request message payload: should be a PT with TODO optional top-level nodes

		String nameOfSplitOperation = opToBeSplit.getName();
		ElementStructure reqES = opToBeSplit.getRequestMessage().getPayload(); // TODO null checks
		if (reqES.getPt() == null) {
			throw new MDSLException("Split operation can only operate on operations with a Parameter Tree as request payload.");
		}

		EndpointContract ec = (EndpointContract) opToBeSplit.eContainer();
		checkCompensationRelations(ec, opToBeSplit);

		// create one new operation for each top-level nodes: TN as request message, same response message (depending on touchResponse, tbd)
		Collection<Operation> newOps = splitPayloadTree(reqES.getPt(), opToBeSplit.getResponseMessage());

		// remove old operation
		ec.getOps().remove(opToBeSplit);

		// add new ones
		ec.getOps().addAll(newOps);

		// adjust first HTTP binding (if present)
		adjustHTTPBindingOfOperation(ec, nameOfSplitOperation, newOps);
	}

	private void checkCompensationRelations(EndpointContract ec, Operation opToBeSplit) {
		if (opToBeSplit.getUndo() != null) {
			MDSLLogger.reportError(opToBeSplit.getName() + " has an undo/compensating operation specified: " + opToBeSplit.getUndo().getName() + ". This property would be lost during the split/move.");
		}

		// compensation relations may only exist within same endpoint
		for (Operation nextOp : ec.getOps()) {
			if (nextOp.getUndo() == opToBeSplit) {
				MDSLLogger.reportError("Unable to split/move operation " + opToBeSplit.getName() + " because it undoes and compensates " + nextOp.getName());
			}
		}
	}

	private void adjustHTTPBindingOfOperation(EndpointContract ec, String nameOfMovingOp, Collection<Operation> newOps) throws MDSLException {
		ServiceSpecification mdslSpecRoot = (ServiceSpecification) ec.eContainer();
		MDSLSpecificationWrapper msw = new MDSLSpecificationWrapper(new ServiceSpecificationAdapter(mdslSpecRoot));
		// TODO (M) v55 handle case that multiple bindings exist
		List<Provider> providers = msw.findProvidersFor(ec);
		if(providers.size()==0) {
			MDSLLogger.reportWarning("No providers found for endpoint contract " + ec.getName() + " skipping it");
		}
		else if(providers.size()>1){
			MDSLLogger.reportWarning("Endpoint contract " + ec.getName() + " has more than one provider, will process first one only");
		}
		
		EndpointInstance httpb = msw.findFirstProviderAndHttpBindingFor(ec);
		if (httpb != null) {
			// TODO (M) v55 handle case that multiple resources exist in binding (validation?); findFirstOperationBindingInEndpointInstance warns about it
			HTTPOperationBinding opBindingToBeSplit = HTTPBindingTransformations.findFirstOperationBindingInEndpointInstance(nameOfMovingOp, httpb);
			if (opBindingToBeSplit == null) {
				throw new MDSLException("No binding for " + nameOfMovingOp + " found in first/only HTTP resource. Others bindings not yet checked.");
			}

			// remove operation binding as operation is moved out *is* needed here, EMF cannot take care of it; the get(0)s are ok now (checked)
			HTTPResourceBinding currentResource = (HTTPResourceBinding) opBindingToBeSplit.eContainer();
			currentResource.getOpsB().remove(opBindingToBeSplit);

			for (Operation nextOpToBeBound : newOps) {
				HTTPOperationBinding newBinding = EcoreUtil.copy(opBindingToBeSplit); // copy needed? sufficient (parameters)?
				newBinding.setBoundOperation(nextOpToBeBound.getName());
				// TODO not all element bindings (if present) make sense any longer, so removing all for now (QF to bring back?)
				newBinding.getParameterBindings().clear();
				currentResource.getOpsB().add(newBinding);
			}
		}
	}

	private Collection<Operation> splitPayloadTree(ParameterTree treeToBeSplit, DataTransferRepresentation response) {
		List<Operation> result = new ArrayList<>();

		// iterate through request payload tree, check optionality of parameters
		TreeNode tn = treeToBeSplit.getFirst();
		result.add(createOperationWithRequestMessageFromTreeNode(tn, response));

		for (TreeNode next : treeToBeSplit.getNexttn()) {
			result.add(createOperationWithRequestMessageFromTreeNode(next, response));
		}

		return result;
	}

	public static Operation createOperationWithRequestMessageFromTreeNode(TreeNode node, DataTransferRepresentation response) {
		Operation newOp = ApiDescriptionFactory.eINSTANCE.createOperation();
		String nodeName = DataTypeTransformationHelpers.nameOf(node);
		String c1 = DataTypeTransformationHelpers.decapitalizeName(DataTypeTransformationHelpers.replaceSpacesWithUnderscores(nodeName));
		newOp.setName(c1);

		DataTransferRepresentation inDtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		// get child tree, spn, or apl and turn it into new DTR
		ElementStructure esForNewOp = TransformationHelpers.findElementStructureOf(node);
		inDtr.setPayload(esForNewOp);
		newOp.setRequestMessage(inDtr);

		// just copying response (known limitation of split operation):
		if (response != null) {
			newOp.setResponseMessage(EcoreUtil.copy(response));
		}

		return newOp;
	}
}

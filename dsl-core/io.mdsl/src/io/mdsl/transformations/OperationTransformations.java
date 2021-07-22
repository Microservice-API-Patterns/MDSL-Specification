package io.mdsl.transformations;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.impl.EndpointContractImpl;
import io.mdsl.exception.MDSLException;

public class OperationTransformations {

	// renameOperation is an MVP/pOC (not needed, done by EMF Rename Element):
	public MDSLResource renameOperation(MDSLResource mdslResource, String opName, String opNameNew) {
		ServiceSpecification mdslSpecRoot = mdslResource.getServiceSpecification();
		boolean found=false;

		List<Operation> operations = EcoreUtil2.eAllOfType(mdslSpecRoot, Operation.class);
		for(int i=0; i<operations.size();i++) {
			Operation nextOperation = operations.get(i);
			if(nextOperation.getName().equals(opName)) {
				found=true;
				System.out.println("Found operation " + opName);
				nextOperation.setName(opNameNew);
			}
		}
		if(!found) {
			throw new MDSLException("Operation " + opName + " not found.");
		}
		return mdslResource;
	}

	public MDSLResource moveOperation(Operation opToBeMoved, String targetEndpointName) {
		// op -> epc -> ss
		ServiceSpecification mdslSpecRoot = (ServiceSpecification) opToBeMoved.eContainer().eContainer();
		boolean found=false;

		EndpointContract targetEndpoint = findOrCreateEndpoint(mdslSpecRoot, targetEndpointName);
				
		if(hasOperationOfName(targetEndpoint, opToBeMoved))
			throw new MDSLException("Target endpoint " + targetEndpoint.getName() + " already has an operation of this name.");

		// create in new or existing endpoint contract:
		targetEndpoint.getOps().add(opToBeMoved);
		
		// delete in current epc endpoint contract not required, EMF has move semantics (?)
		
		findAnAdjustHTTPResourceBindingOfOperation(mdslSpecRoot, opToBeMoved); // TODO not yet implemented

		return new MDSLResource(targetEndpoint.eResource());
	}

	public void findAnAdjustHTTPResourceBindingOfOperation(ServiceSpecification mdslSpecRoot, Operation operation) {
		// [O] look at all providers that expose the old contract via HTTP 
		// [O] remove operation binding as operation is moved out 
		// [O] if moving to a new endpoint: copy first or all bindings? 
		// [O] if moving to an existing endpoint: create binding if not present (?)
		// [O] add an operation binding that matches the existing one (but check that this does not cause conflicts in OASgen?)
	}

	private EndpointContract findOrCreateEndpoint(ServiceSpecification mdslSpecRoot, String epName) {
		List<EObject> contracts = mdslSpecRoot.getContracts(); // could be channel too
		for(int i=0; i<contracts.size();i++) {
			if(contracts.get(i).getClass() == EndpointContractImpl.class) { 
				EndpointContract nextContract = (EndpointContract) contracts.get(i);
				if(nextContract.getName().equals(epName)) {
					return nextContract;
				}
			}
		}
		return createEndpointType(mdslSpecRoot, epName);
	}

	private EndpointContract createEndpointType(ServiceSpecification mdslSpecRoot, String epName) {
		EndpointContract newEPC = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		newEPC.setName(epName);
		// TODO check (again) that name is unique?
		mdslSpecRoot.getContracts().add(newEPC);
		return newEPC;
	}
	
	public boolean hasOperationOfName(EndpointContract endpointType, Operation opToBeMoved) {
		for(Operation nextOpInContract : endpointType.getOps()) {
			if(nextOpInContract.getName().equals(opToBeMoved.getName()))
				return true;
		}
		return false;
	}
	
	/*
	// this one is available a (basic) quick fix, so not implemented here
	public MDSLResource addResourceBinding(MDSLResource mdslResource, String endpointName, String resourceName) {
		// ServiceSpecification serviceSpecification = mdslResource.getServiceSpecification();
		throw new RuntimeException("NYI");
	}
	*/
}

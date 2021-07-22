package io.mdsl.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.ApiDescriptionPackage;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.EndpointList;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.HTTPParameterBinding;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.HTTPVerb;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ReportBinding;
import io.mdsl.apiDescription.SecurityBinding;
import io.mdsl.apiDescription.SecurityPolicies;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.StatusReport;
import io.mdsl.apiDescription.StatusReports;
import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.impl.EndpointInstanceImpl;
import io.mdsl.apiDescription.impl.EndpointListImpl;

/**
 * This class contains custom validation rules.
 *
 * See
 * https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class HTTPBindingValidator extends AbstractMDSLValidator {
	
	public final static String RESOURCE_SHOULD_BE_SPLIT = "RESOURCE_SHOULD_BE_SPLIT";
	public final static String SECURITY_POLICY_UNBOUND = "SECURITY_POLICY_UNBOUND"; 
	public final static String SECURITY_POLICY_NOT_FOUND = "SECURITY_POLICY_NOT_FOUND";
	public final static String ERROR_REPORT_UNBOUND = "ERROR_REPORT_UNBOUND";
	public final static String ERROR_REPORT_NOT_FOUND = "ERROR_REPORT_NOT_FOUND";
	
	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	// TODO (M) some more checks missing:
	
	// * all link relations are bound and bound ones refer existing abstract ones?
	// * location/(sub-)resource URIs must make sense
	// * could also check MIME types etc., and offer quick fix to add JSON as content type
	
	/*
	// old and replaced:
	@Check 
	void checkWhetherAllOperationsInContractAreBound(final HTTPResourceBinding httpResourceBinding) {
		TechnologyBinding tp = (TechnologyBinding) httpResourceBinding.eContainer().eContainer().eContainer();
		EndpointList eil = null;
		
		if(tp.eContainer().getClass()==EndpointInstanceImpl.class) {
			EndpointInstance ei = (EndpointInstance) tp.eContainer();
			if(ei.eContainer().getClass()==EndpointListImpl.class) {
				eil = (EndpointList) ei.eContainer();
			}
			else {
				return; // TODO check binding in gateway etc.
			}
		}
		else { 
			return; // TODO check binding in gateway etc.
		}
		
		EndpointContract ec = eil.getContract();
		
		// is there a more efficient way? (direct access, name equality)
		for (Operation operation : ec.getOps()) {
			boolean found=false;
			for(HTTPOperationBinding httpOperationBinding : httpResourceBinding.getOpsB())
				if(operation.getName().equals(httpOperationBinding.getBoundOperation()))
					found=true;
			if(!found) 
				warning("Operation " + operation.getName() + " from contract " + ec.getName() + " not bound, default verb POST assumed.", 
					httpResourceBinding,
					ApiDescriptionPackage.eINSTANCE.getHTTPResourceBinding_Name());
		}
	}
	*/
	
	@Check 
	void checkWhetherAllOperationsInContractAreBound(final HTTPBinding httpBinding) {
		
		TechnologyBinding tp = (TechnologyBinding) httpBinding.eContainer().eContainer();
		EndpointList eil = null;
		
		if(tp.eContainer().getClass()==EndpointInstanceImpl.class) {
			EndpointInstance ei = (EndpointInstance) tp.eContainer();
			if(ei.eContainer().getClass()==EndpointListImpl.class) {
				eil = (EndpointList) ei.eContainer();
			}
			else {
				return; // TODO (M) check binding in gateway etc.
			}
		}
		else { 
			return; // TODO (M) check binding in gateway etc.
		}
		
		EndpointContract ec = eil.getContract();
		
		for (Operation operation : ec.getOps()) {
			boolean found=false;
			// checking all resource bindings in this HTTP binding at once:
			for(HTTPResourceBinding resourceBinding : httpBinding.getEb())
				for(HTTPOperationBinding httpOperationBinding : resourceBinding.getOpsB())
					if(operation.getName().equals(httpOperationBinding.getBoundOperation()))
						found=true;
			
			if(!found) {
				warning("Operation " + operation.getName() + " from contract " + ec.getName() + " not bound, default verb POST assumed by OAS generator.", 
						httpBinding,
					ApiDescriptionPackage.eINSTANCE.getHTTPBinding_Eb());
			}
		}
	}
	
	@Check 
	void operationsActuallyDefinedInEndpointType(final HTTPOperationBinding httpOperationBinding) {
		if(httpOperationBinding.getMethod()==HTTPVerb.TRACE || httpOperationBinding.getMethod()==HTTPVerb.HEAD || httpOperationBinding.getMethod()==HTTPVerb.OPTIONS) {
		    warning("Operation " + httpOperationBinding.getBoundOperation() + " is bound to " + httpOperationBinding.getMethod().getName() + ", an HTTP verb that is not seen often in APIs. Prefer POST, PUT, PATCH, GET, DELETE", httpOperationBinding,
				ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_Method());
		}
		
		checkWhetherAllBoundOperationsAppearInContract(httpOperationBinding);
		checkErrorReportBindings(httpOperationBinding);
		checkSecurityPolicyBindings(httpOperationBinding);
	}
	
	private void checkSecurityPolicyBindings(HTTPOperationBinding httpOperationBinding) {

		EList<SecurityBinding> sbl = httpOperationBinding.getSecurityBindings();
		EndpointContract ec = findContract(httpOperationBinding);

		SecurityPolicies secPols = null;
		for (Operation operation : ec.getOps()) {
			if(operation.getName().equals(httpOperationBinding.getBoundOperation())) {
				secPols = operation.getPolicies();
			}
		}
		if(secPols==null&&(sbl!=null&&sbl.size()>0)) {
			error("No policy definitions found in endpoint type operation, but the operation binding has one or more.", 
				httpOperationBinding,
				ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_SecurityBindings(),
				SECURITY_POLICY_UNBOUND);
			return;
		}
		for(SecurityBinding sb : sbl) {
			boolean found=false;
			for(SecurityPolicy sp: secPols.getPolicyList())
				if(sp.getName().equals(sb.getName())) 
					found=true;
			if(!found)
				error("No policy definition for " + sb.getName() + " found in endpoint type operation.", 
					httpOperationBinding,
					ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_SecurityBindings(),
					SECURITY_POLICY_NOT_FOUND);
		}
	}
	
	// TODO (M) offer quick fixes to correct errors? rename, add (see Eclipse Java)

	private void checkErrorReportBindings(HTTPOperationBinding httpOperationBinding) {

		EList<ReportBinding> rpl = httpOperationBinding.getReportBindings();
		EndpointContract ec = findContract(httpOperationBinding);

		StatusReports responseReports=null;
		for (Operation operation : ec.getOps()) {
			if(operation.getName().equals(httpOperationBinding.getBoundOperation())) {
				responseReports = operation.getReports();
			}
		}
		if(responseReports==null&&(rpl!=null&&rpl.size()>0)) {
			error("No report definitions found in endpoint type operation, but the operation binding has one or more.", 
				httpOperationBinding,
				ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_ReportBindings(),
				ERROR_REPORT_UNBOUND);
			return;
		}
		for(ReportBinding rp : rpl) {
			boolean found=false;
			for(StatusReport sr: responseReports.getReportList())
				if(sr.getName().equals(rp.getName()))
					found=true;
			if(!found)
				error("No report definition for " + rp.getName() + " found in endpoint type operation.", 
					httpOperationBinding,
					ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_ReportBindings(),
					ERROR_REPORT_NOT_FOUND);
		}
	}
	
	// TODO (M) offer quick fixes to correct errors? rename, add (see Eclipse Java)

	private boolean checkWhetherAllBoundOperationsAppearInContract(HTTPOperationBinding httpOperationBinding) {
		
		EndpointContract ec = findContract(httpOperationBinding);
		if(ec==null) {
			System.err.println("No endpoint contract found, cannot check binding.");
			return true;
		}
		
		for (Operation operation : ec.getOps()) {
			if(operation.getName().equals(httpOperationBinding.getBoundOperation())) 
				return true;
		}
		
		error("Bound operation " + httpOperationBinding.getBoundOperation() + " is not defined in the endpoint type " + ec.getName() + ". Add it there or delete binding here.", httpOperationBinding,
				ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_BoundOperation());
		
		return false;
		
	}

	private EndpointContract findContract(HTTPOperationBinding httpOperationBinding) {
		TechnologyBinding tb = (TechnologyBinding) httpOperationBinding.eContainer().eContainer().eContainer().eContainer();
		
		if(tb.eContainer().getClass()==EndpointInstanceImpl.class) {
			EndpointInstance ei = (EndpointInstance) tb.eContainer();
			if(ei.eContainer().getClass()==EndpointListImpl.class) {
				EndpointList eil = (EndpointList) ei.eContainer();
				return eil.getContract();
			}
			else {
				return null; // TODO check binding in gateway etc.
			}
		}
		else { 
			return null; // TODO check binding in gateway etc.
		}
	}

	/*
	private boolean checkWhetherAllOperationsInContractAreBound(HTTPOperationBinding httpOperationBinding) {
		EndpointContract ec = findContract(httpOperationBinding);
		
		for (Operation operation : ec.getOps()) {
			if(operation.getName().equals(httpOperationBinding.getBoundOperation())) 
				return true;
		}
		
		return true;
	}
	*/

	@Check 
	void elementsActuallyDefinedInRequestPayload(final HTTPParameterBinding parameterBinding) {

		HTTPOperationBinding httpOperationBinding = (HTTPOperationBinding) parameterBinding.eContainer();
		EndpointContract ec = findContract(httpOperationBinding);
		Operation op = findOperation(ec, httpOperationBinding.getBoundOperation());
		
		if(!isPresentInRequestPayload(parameterBinding.getBoundParameter(), op))
			error("Bound parameter " + parameterBinding.getBoundParameter() + " is not defined in the bound operation " + op.getName() + ". Add it there or delete binding here.", parameterBinding,
						ApiDescriptionPackage.eINSTANCE.getHTTPParameterBinding_BoundParameter());

	}

	private boolean isPresentInRequestPayload(String boundParameter, Operation op) {

		DataTransferRepresentation rm = op.getRequestMessage();
		ElementStructure structure = rm.getPayload(); // TODO look in header too?
		
		if(structure==null)
			return false;
		
		return checkElements(structure, boundParameter);
	}

	private boolean checkElements(ElementStructure structure, String boundParameter) {
	
		// partially duplicating code from generator utils here:
		List<AtomicParameter> atomicParameterList = Lists.newLinkedList();
		
		if (structure.getApl() != null) {
			atomicParameterList.add(structure.getApl().getFirst());
			atomicParameterList.addAll(structure.getApl().getNextap());
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		} else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			// find referenced explicit type and call same method again 
			return checkElements(structure.getNp().getTr().getDcref().getStructure(), boundParameter);
		} else if (structure.getPt() != null) {
			// not the most efficient way to do this:
			List<TreeNode> nodes = Lists.newLinkedList();
			nodes.add(structure.getPt().getFirst());
			nodes.addAll(structure.getPt().getNexttn());
			for(TreeNode nextLevel1Node : nodes) 
				if(nextLevel1Node.getPn()!=null && nextLevel1Node.getPn().getAtomP()!=null)
					atomicParameterList.add(nextLevel1Node.getPn().getAtomP());
		} else if (structure.getNp().getGenP() != null) {
			if(structure.getNp().getGenP().getName()!=null && structure.getNp().getGenP().getName().equals(boundParameter))
				return true;
		}
		
		for(AtomicParameter atomicParameter : atomicParameterList) 
			if(atomicParameter.getRat()!=null && atomicParameter.getRat().getName()!=null && atomicParameter.getRat().getName().equals(boundParameter)) {
				return true;
			}
		
		return false;
	}
	
	private Operation findOperation(EndpointContract ec, String boundOperation) {
		for(Operation operation : ec.getOps())
			if(operation.getName().equals(boundOperation)) 
				return operation;
		return null;
	}
	
	@Check
	public void doNotAllowBODYMappingForGETsAndDELETEs(final HTTPOperationBinding httpOperationBinding) {
		// element bindings to BODY is ok if operation method is not GET or DELETE (FORM deprecated)
		if (!(httpOperationBinding.getMethod() == HTTPVerb.GET || httpOperationBinding.getMethod() == HTTPVerb.DELETE))
			return;
		
		// TODO missing: check "all elements bound to" default (for GET and DELETE, BODY is not ok)

		for (HTTPParameterBinding parameterBinding : EcoreUtil2.eAllOfType(httpOperationBinding, HTTPParameterBinding.class).stream()
				.filter(pb -> pb.getParameterMapping() != null && pb.getParameterMapping() != null && (pb.getParameterMapping() == HTTPParameter.BODY /* || pb.getParameterMapping().getHttp() == HTTPParameter.FORM */))
				.collect(Collectors.toList())) {
			error("HTTP operations with the verbs GET and DELETE do not support BODY parameters.", parameterBinding,
					ApiDescriptionPackage.eINSTANCE.getHTTPParameterBinding_ParameterMapping()); // Literals.HTTP_PARAMETER_BINDING__PARAMETER_MAPPING);
		}
	}
	
	@Check
	public void reportDifferentVerbBindings(final HTTPResourceBinding httpResourceBinding) {
		HashMap<HTTPVerb, Integer> verbCountMap = new HashMap<HTTPVerb, Integer>();
		for (HTTPOperationBinding operationBinding : httpResourceBinding.getOpsB()) {
			Integer currentCount = verbCountMap.get(operationBinding.getMethod());
			if(currentCount==null)
				currentCount=0;
			verbCountMap.put(operationBinding.getMethod(), currentCount.intValue()+1);
		}
		for(Entry<HTTPVerb, Integer> entry : verbCountMap.entrySet()) {
			if(entry.getValue() > 1)
				warning("Resource binds mulitple operations to the same HTTP verb. Split it or change the operation bindings.", httpResourceBinding,
					ApiDescriptionPackage.eINSTANCE.getHTTPResourceBinding_Name(), RESOURCE_SHOULD_BE_SPLIT); // Literals.HTTP_BINDING__EB);
		}
	}
}

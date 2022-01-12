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
import io.mdsl.apiDescription.HTTPGlobalParameterBinding;
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
	
	public final static String GLOBAL_PARAMETER_BINDING_FOUND = "GLOBAL_PARAMETER_BINDING_FOUND";
	public final static String URI_TEMPLATE_FOR_PATH_PARAM_MISSING = "URI_TEMPLATE_FOR_PATH_PARAM_MISSING";
	public static final String URI_TEMPLATE_MISSING_TEXT = "requires a URI template in resource URI";
	
	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}
	
	// TODO (future work): more checks would be useful
	
	// * all link relations are bound and bound ones refer existing abstract ones?
	// * location/(sub-)resource URIs must make sense
	// * could also check MIME types etc., and offer quick fix to add JSON as content type
	
	@Check 
	void checkWhetherAllOperationsInContractAreBound(final HTTPBinding httpBinding) {
		
		TechnologyBinding tp = (TechnologyBinding) httpBinding.eContainer().eContainer();
		EndpointList eil = null;
		
		if(tp.eContainer() instanceof EndpointInstance) {
			EndpointInstance ei = (EndpointInstance) tp.eContainer();
			if(ei.eContainer() instanceof EndpointList) {
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
	
	// TODO (M) offer quick fixes to correct errors? rename, add (as Eclipse Java does)
	
	@Check 
	void operationsActuallyDefinedInEndpointType(final HTTPOperationBinding httpOperationBinding) {
		if(httpOperationBinding.getMethod()==HTTPVerb.TRACE || httpOperationBinding.getMethod()==HTTPVerb.HEAD || httpOperationBinding.getMethod()==HTTPVerb.OPTIONS) {
		    warning("Operation " + httpOperationBinding.getBoundOperation() + " is bound to " + httpOperationBinding.getMethod().getName() + ", an HTTP verb that is not seen often in APIs. Prefer POST, PUT, PATCH, GET, DELETE", httpOperationBinding,
				ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_Method());
		}
		
		checkWhetherAllBoundOperationsAppearInContract(httpOperationBinding);
		lookForErrorReportBindings(httpOperationBinding);
		lookForSecurityPolicyBindings(httpOperationBinding);
	}
	
	private void lookForSecurityPolicyBindings(HTTPOperationBinding httpOperationBinding) {

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
	
	private void lookForErrorReportBindings(HTTPOperationBinding httpOperationBinding) {

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

	@Check 
	void elementsActuallyDefinedInRequestPayload(final HTTPParameterBinding parameterBinding) {

		HTTPOperationBinding httpOperationBinding = (HTTPOperationBinding) parameterBinding.eContainer();
		EndpointContract ec = findContract(httpOperationBinding);
		Operation op = findOperation(ec, httpOperationBinding.getBoundOperation());
		
		if(!isPresentInRequestPayload(parameterBinding.getBoundParameter(), op))
			error("Bound parameter " + parameterBinding.getBoundParameter() + " is not defined in the bound operation " + op.getName() + ". Add it there or delete binding here.", 
					parameterBinding, ApiDescriptionPackage.eINSTANCE.getHTTPParameterBinding_BoundParameter());
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
		} 
		else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		} 
		else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			// new QF creating element bindings and this validator must match (or be compatible)
			if(structure.getNp().getTr().getName()!=null &&structure.getNp().getTr().getName().equals(boundParameter))
				return true;
			else { 
				// find referenced explicit type and call same method again
				return checkElements(structure.getNp().getTr().getDcref().getStructure(), boundParameter);
			}
		}
		else if (structure.getNp().getGenP() != null) {
			if(structure.getNp().getGenP().getName()!=null && structure.getNp().getGenP().getName().equals(boundParameter))
				return true;
		}
		else if (structure.getPt() != null) {
			// not the most efficient way to do this:
			List<TreeNode> nodes = Lists.newLinkedList();
			nodes.add(structure.getPt().getFirst());
			nodes.addAll(structure.getPt().getNexttn());
			for(TreeNode nextLevel1Node : nodes) 
				if(nextLevel1Node.getPn()!=null && nextLevel1Node.getPn().getAtomP()!=null)
					atomicParameterList.add(nextLevel1Node.getPn().getAtomP());
		} 
		
		for(AtomicParameter atomicParameter : atomicParameterList) 
			if(atomicParameter.getRat()!=null && atomicParameter.getRat().getName()!=null && atomicParameter.getRat().getName().equals(boundParameter)) {
				return true;
			}
		
		return false;
	}
	
	@Check
	public void doNotAllowBODYMappingForGETsAndDELETEs(final HTTPOperationBinding httpOperationBinding) {
		// element bindings to BODY is ok if operation method is not GET or DELETE (FORM deprecated)
		if (!(httpOperationBinding.getMethod() == HTTPVerb.GET || httpOperationBinding.getMethod() == HTTPVerb.DELETE))
			return;
		
		// check "all elements bound to" default (for GET and DELETE, BODY is not ok)
		HTTPGlobalParameterBinding gb = httpOperationBinding.getGlobalBinding();
		if(gb!=null && gb.getParameterMapping() == HTTPParameter.BODY) {
			if(httpOperationBinding.getMethod() == HTTPVerb.GET || httpOperationBinding.getMethod() == HTTPVerb.DELETE)
				error("HTTP operations using the verbs GET and DELETE must not contain BODY parameters.", gb,
					ApiDescriptionPackage.eINSTANCE.getHTTPGlobalParameterBinding_ParameterMapping()); // Literals.HTTP_PARAMETER_BINDING__PARAMETER_MAPPING);
		}

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
				warning("Resource binds multiple operations to the same HTTP verb. Split it or change the operation bindings.", httpResourceBinding,
					ApiDescriptionPackage.eINSTANCE.getHTTPResourceBinding_Name(), RESOURCE_SHOULD_BE_SPLIT); // Literals.HTTP_BINDING__EB);
		}
	}
	
	@Check
	public void reportMissingURITemplate(final HTTPResourceBinding httpResourceBinding) {
		for (HTTPOperationBinding operationBinding : httpResourceBinding.getOpsB()) {
			// check global binding first
			HTTPGlobalParameterBinding gb = operationBinding.getGlobalBinding();
			if(gb!=null) {
				if(gb.getParameterMapping().toString().equals("PATH")) {
					if(httpResourceBinding.getUri()==null || !httpResourceBinding.getUri().matches(".*\\{.*\\}.*")) // TODO not a complete check (but good enough)
						error("PATH parameter(s) of " + operationBinding.getBoundOperation() + " " + URI_TEMPLATE_MISSING_TEXT + ": {id}", operationBinding,
							ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_BoundOperation(), URI_TEMPLATE_FOR_PATH_PARAM_MISSING);
				}
			}
			else {
				// next, check all individual element bindings
				for(HTTPParameterBinding parameterBinding : operationBinding.getParameterBindings() ) {
					if(parameterBinding.getParameterMapping().toString().equals("PATH")) {
						if(httpResourceBinding.getUri()==null || !httpResourceBinding.getUri().contains("{" + parameterBinding.getBoundParameter() + "}")) {
							error("PATH parameter of " + operationBinding.getBoundOperation() + " " + URI_TEMPLATE_MISSING_TEXT + ": {" + parameterBinding.getBoundParameter() + "}", 
									operationBinding, ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_BoundOperation(), URI_TEMPLATE_FOR_PATH_PARAM_MISSING);
						}
					}
				}
			}
		}
	}
	
	@Check
	public void reportGlobalParameterBindingForOperation(final HTTPGlobalParameterBinding globalParameterBinding) {
		info("All elements bound to same HTTP parameter type.", globalParameterBinding.eContainer(),
				ApiDescriptionPackage.eINSTANCE.getHTTPOperationBinding_BoundOperation(), GLOBAL_PARAMETER_BINDING_FOUND);
	}

	// ** helpers: 
		
	private EndpointContract findContract(HTTPOperationBinding httpOperationBinding) {
		TechnologyBinding tb = (TechnologyBinding) httpOperationBinding.eContainer().eContainer().eContainer().eContainer();
		
		if(tb.eContainer() instanceof EndpointInstance) {
			EndpointInstance ei = (EndpointInstance) tb.eContainer();
			if(ei.eContainer() instanceof EndpointList) {
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

	private Operation findOperation(EndpointContract ec, String boundOperation) {
		for(Operation operation : ec.getOps())
			if(operation.getName().equals(boundOperation)) 
				return operation;
		return null;
	}
	
}

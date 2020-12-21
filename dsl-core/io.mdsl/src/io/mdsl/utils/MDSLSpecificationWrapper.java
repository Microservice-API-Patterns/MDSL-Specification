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
package io.mdsl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.EcoreUtil2;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.CustomMediaType;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.EndpointList;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.HTTPParameterBinding;
// import io.mdsl.apiDescription.HTTPParameterMapping;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.HTTPTypeBinding;
import io.mdsl.apiDescription.HTTPVerb;
import io.mdsl.apiDescription.MediaTypeList;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.ReportBinding;
import io.mdsl.apiDescription.SecurityBinding;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.StandardMediaType;
import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;

/**
 * Helper class to resolve specific objects in an MDSL model.
 *
 */
public class MDSLSpecificationWrapper {
	
	// TODO move to utility class (violates SRP)

	private int logLevel = 1; // -1= off, 0=errors, 1=warn, 2=log, 3=all (could use enum) 

	private static final String DEFAULT_MEDIA_TYPE = "application/json"; 

	private ServiceSpecificationAdapter mdslSpecification;

	public MDSLSpecificationWrapper(ServiceSpecificationAdapter mdslSpecification) {
		this.mdslSpecification = mdslSpecification;
	}
	
	public ServiceSpecificationAdapter getSpecification() {
		return this.mdslSpecification;
	}

	public String getElementName(ElementStructure representationElement) {
		// TODO implement
		return "Default Element Name (Feature NYI)"; 
	}
		
	// checkers:
	
	public boolean isAtomicOrIsFlatParameterTree(ElementStructure structure) {
		if(structure==null) {
			throw new MDSLException("Empty structure type (?)"); 
		}
		// find out whether we deal with a simple or a complex parameter
		if (structure.getApl() != null) {
			// is not flat if cardinality is set to n (*, +)
			return !isMultiplicity(structure.getApl().getCard());
			//return true;
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			return !isMultiplicity(structure.getNp().getAtomP().getCard());
			// return true;
		} else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			// find referenced explicit type and call same method again 
			// TODO test what about cardinality? (done 12/20)
			if(isMultiplicity(structure.getNp().getTr().getCard()))
				return false;
			else
				return isAtomicOrIsFlatParameterTree(structure.getNp().getTr().getDcref().getStructure());
		} else if (structure.getPt() != null) {
			if(isParameterTreeAtomic(structure.getPt()))
				return true;
			else 
				return false;
		} else if (structure.getNp().getGenP() != null) {
			return true;
		}
		else {
			return false; // other non-atomic structure type (?)
		}
	}
	
	private boolean isMultiplicity(Cardinality card) {
		if(card==null) 
			return false;
		
		return card.getAtLeastOne()!=null || card.getZeroOrMore()!=null;
	}
	
	public boolean isParameterTreeAtomic(ParameterTree tree) {
		if(isMultiplicity(tree.getCard()))
			return false;
		
		List<TreeNode> nodes = collectTreeNodes(tree);
		for (TreeNode node : nodes) {
			if (node.getPn() != null && node.getPn().getAtomP() != null)
				continue;
			if (node.getApl() != null)
				continue;
			return false;
		}
		return true;
	}
	
	public boolean operationHasPayload(Operation mdslOperation) {
		return mdslOperation.getRequestMessage() != null && mdslOperation.getRequestMessage().getPayload() != null;
	}
	
	public boolean operationHasHeader(Operation mdslOperation) {
		return mdslOperation.getRequestMessage() != null && mdslOperation.getRequestMessage().getHeaders() != null;
	}

	public boolean operationHasReturnValue(Operation mdslOperation) {
		return mdslOperation.getResponseMessage() != null && mdslOperation.getResponseMessage().getPayload() != null;
	}

	public boolean operationHasReturnValueWithReports(Operation mdslOperation) {
		// payload of response message can't be null according to grammar but checking anyway
		return mdslOperation.getResponseMessage() != null && mdslOperation.getResponseMessage().getPayload() != null && mdslOperation.getReports() != null;
	}
	
	// collectors:
	
	public List<AtomicParameter> extractElements(ElementStructure structure) {
		List<AtomicParameter> atomicParameterList = Lists.newLinkedList();
		
		if (structure.getApl() != null) {
			atomicParameterList.add(structure.getApl().getFirst());
			atomicParameterList.addAll(structure.getApl().getNextap());
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		} else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			// find referenced explicit type and call same method again 
			return extractElements(structure.getNp().getTr().getDcref().getStructure());
		} else if (structure.getPt() != null) {
			if(this.isParameterTreeAtomic(structure.getPt()))
				atomicParameterList.addAll(this.collectAtomicParameters(structure.getPt()));
			else 
				 throw new MDSLException("Cannot extract atoms from nested tree '"); // no PF, for instance
		} else if (structure.getNp().getGenP() != null) {
			// fill in a new AP, but how to create one?
			this.log("[W] Known limitation: Cannot handle a top-level generic parameter, skipping it: " +  structure.getNp().getGenP().getName());
			// throw new MDSLException("Would like to add the genP to the apl, NYI");
		}
		else {
			throw new MDSLException("Cannot extract from this type of element.");
		}
		
		return atomicParameterList;
	}
	
	public List<AtomicParameter> collectAtomicParameters(ParameterTree tree) {
		List<AtomicParameter> list = Lists.newLinkedList();
		List<TreeNode> nodes = collectTreeNodes(tree);
		for (TreeNode node : nodes) {
			if (node.getPn() != null && node.getPn().getAtomP() != null)
				list.add(node.getPn().getAtomP());
			if (node.getApl() != null) {
				list.add(node.getApl().getFirst());
				list.addAll(node.getApl().getNextap());
			}
		}
		return list;
	}

	private List<TreeNode> collectTreeNodes(ParameterTree tree) {
		List<TreeNode> nodes = Lists.newLinkedList();
		nodes.add(tree.getFirst());
		nodes.addAll(tree.getNexttn());
		return nodes;
	}

	// finders:
	
	/*
	public List<EndpointInstance> findEndpointInstancesForContract(EndpointContract endpointType) {
		List<EndpointList> providers = EcoreUtil2.eAllOfType(mdslSpecification, EndpointList.class);
		for(int i=0;i<providers.size();i++) {
			if(providers.get(i).getContract().getName().equals(endpointType.getName())) {
				return providers.get(i).getEndpoints();
			}
		}
		return null;
	}
	*/
	
	public List<EndpointInstance> findProviderEndpointInstancesFor(EndpointContract endpointType) {
		List<EndpointList> endpoints = EcoreUtil2.eAllOfType(mdslSpecification, EndpointList.class);
		List<EndpointInstance> result = new ArrayList<EndpointInstance>();

		for(int i=0; i<endpoints.size();i++) {
			EndpointList nextEndpointList = endpoints.get(i);
			if(nextEndpointList.getContract().getName().equals(endpointType.getName())) {
				
				EList<EndpointInstance> endpointInstances = nextEndpointList.getEndpoints();
				for(int j=0; j<endpointInstances.size(); j++) {
					EndpointInstance nextEndpoint = endpointInstances.get(j);
					EList<TechnologyBinding> tbs = nextEndpoint.getPb();
					for(int k=0;k<tbs.size();k++) {
						TechnologyBinding tb = tbs.get(k);
						if(tb.getProtBinding().getHttp()!= null ) {
							result.add(nextEndpoint);
						}
						else 
							this.log("[EB] Non-HTTP binding found for " + nextEndpointList.getContract().getName());
					}
				}
			}
		}
		
		return result;
	}

	public EndpointInstance findFirstAndOnlyHttpBindingIfExisting(EndpointContract endpointType) {
		List<TechnologyBinding> bindings = EcoreUtil2.eAllOfType(mdslSpecification, TechnologyBinding.class);
		List<TechnologyBinding> httpBindings = bindings.stream()
				.filter(b -> b.getProtBinding() != null && b.getProtBinding().getHttp() != null && b.getProtBinding().getHttp() instanceof HTTPBinding)
				.collect(Collectors.toList());

		if (httpBindings.size() == 1) // use HTTP binding, if there is only one
		    return (EndpointInstance) httpBindings.get(0).eContainer();

		return null;
	}
	
	public HTTPVerb findVerbBindingFor(String operation, HTTPResourceBinding binding) {
		HTTPOperationBinding opB = findOperationBindingFor(operation, binding);
		if(opB!=null)
			return opB.getMethod();
		else 
			return null;
	}
	
	public HTTPTypeBinding findLinkTypeBindingFor(String name, HTTPResourceBinding binding) {
		EList<HTTPTypeBinding> tps = binding.getTB();
		for(HTTPTypeBinding tb : tps) {
			if(tb.getLt()!=null && tb.getLt().getName().equals(name)) {
				return tb;
			}
		}
		return null;
	}
	
	/*
	public static HTTPOperationBinding findOperationBinding(String operationName, HTTPResourceBinding binding) {
		EList<HTTPOperationBinding> opsBindings = binding.getOpsB();
		for(int i=0;i<opsBindings.size(); i++) {
			if(opsBindings.get(i).getBoundOperation().equals(operationName))
				return opsBindings.get(i);
		}
		this.log("[E] No operation binding found for " + operationName);
		return null;
	}
	*/
	
	public HTTPOperationBinding findOperationBindingFor(String operation, HTTPResourceBinding binding) {
		if(binding==null)
			return null;
		
		EList<HTTPOperationBinding> operationBindings = binding.getOpsB();
		for(int i=0;i<operationBindings.size();i++) {
			HTTPOperationBinding opB = operationBindings.get(i);
			if(opB.getBoundOperation().equals(operation)) {
				// this.log("[OB] Found an operation binding for " + operation + " in " + binding.getName() 
				//	+ ": " + opB.getMethod().getName());
				return opB;
			}
		}
		// this.log("[I] No operation binding found for " + operation + " in " + binding.getName());
		return null;
	}
	
	public HTTPParameter findParameterBindingFor(String operation, String parameter, HTTPResourceBinding binding) {
		if(binding==null)
			return null;
		
		HTTPOperationBinding opB = findOperationBindingFor(operation, binding);
		
		if(opB==null)
			return null; // no binding, so default mapping of "body" (?)
		
		if(opB.getGlobalBinding()!=null) {
			this.log("[P] Found a global parameter mapping in " + operation);
				return opB.getGlobalBinding().getParameterMapping();
		}

		EList<HTTPParameterBinding> parameterBindings = opB.getParameterBindings();
		for(int i=0;i<parameterBindings.size();i++) {
			HTTPParameterBinding pB = parameterBindings.get(i);
			if(pB.getBoundParameter().equals(parameter)) {
				this.log("[P] Found a parameter binding for " + operation + " in " + binding.getName());
				return pB.getParameterMapping();
			}
		}
		this.log("[P] No parameter binding found for " + parameter + " in " + binding.getName());
		return null;
	}
	
	public String findReportCodeInBinding(String operation, String reportNameInEndpointType, HTTPResourceBinding binding) {
		if(binding==null)
			return "x-742"; // OAS only accepts three-digit codes up to 500 and vendor extensions
		
		// TODO could move navigation to helper, used in several methods
		EList<HTTPOperationBinding> operationBindings = binding.getOpsB();
		for(int i=0;i<operationBindings.size();i++) {
			HTTPOperationBinding opB = operationBindings.get(i);
			
			if(opB.getBoundOperation().equals(operation)) {
				this.logInformation("Found an operation binding for " + operation + " in " + binding.getName() 
					+ ": " + opB.getMethod().getName());
				EList<ReportBinding> reports = opB.getReportBindings();

				for(int j=0;j<reports.size();j++) {
					ReportBinding report = reports.get(j);
					this.logInformation("Processing a report binding: " + report.getName());
					if(report.getName().equals(reportNameInEndpointType)) {
						this.logInformation("Report binding found for " + operation + " in " + binding.getName());
						return String.valueOf(report.getHttpStatusCode());
					}
				}
			}
		}
		this.log("[W] No report binding found for " + operation + " in " + binding.getName());
		return "x-743"; // handle contract inconsistency differently (validator)?
	}

	public String findReportTextInBinding(String operation, String reportNameInEndpointType,
			HTTPResourceBinding binding) {
		if(binding==null)
			return "null"; 
		
		// TODO could move navigation to helper, used in several methods
		EList<HTTPOperationBinding> operationBindings = binding.getOpsB();
		for(int i=0;i<operationBindings.size();i++) {
			HTTPOperationBinding opB = operationBindings.get(i);
			if(opB.getBoundOperation().equals(operation)) {
				this.log("[OB] Found an operation binding for " + operation + " in " + binding.getName() 
					+ ": " + opB.getMethod().getName());
				EList<ReportBinding> reports = opB.getReportBindings();
				for(int j=0;j<reports.size();j++) {
					ReportBinding report = reports.get(j);
					if(report.getName().equals(reportNameInEndpointType)) {
						this.log("[RB] Report binding found for " + operation + " in " + binding.getName());
						return report.getDetails();
					}
				}
			}
		}
		this.logWarning("No report binding found for " + operation + " in " + binding.getName());
		return "n/a"; // TODO handle contract inconsistency differently (validator)?
	}
	
	public SecurityBinding findPolicyInBinding(String operation, String policyNameInEndpointType, HTTPResourceBinding binding) {
		if(binding==null)
			return null;
		
		EList<HTTPOperationBinding> operationBindings = binding.getOpsB();
		for(int i=0;i<operationBindings.size();i++) {
			HTTPOperationBinding opB = operationBindings.get(i);
			
			if(opB.getBoundOperation().equals(operation)) {
				EList<SecurityBinding> policies = opB.getSecurityBindings();

				for(int j=0;j<policies.size();j++) {
					SecurityBinding policy = policies.get(j);
					if(policy.getName().equals(policyNameInEndpointType)) {
						this.log("[PB] Policy binding found for " + operation + " in " + binding.getName());
						return policy;
					}
				}
			}
		}
		this.logWarning("No policy binding found for " + operation + " in " + binding.getName() + ", skipping this one.");
		return null; // TODO handle contract inconsistency differently (validator)?
	}

	public List<String> findMediaTypeForRequest(Operation mdslOperation, HTTPResourceBinding binding) {
		HTTPOperationBinding opB = findOperationBindingFor(mdslOperation.getName(), binding);
		
		if(opB==null || opB.getInContentTypes()==null) {
			List<String> defaultTypeList = new ArrayList<String>();
			defaultTypeList.add(DEFAULT_MEDIA_TYPE);
			return defaultTypeList;
		}
		
		// TODO also work with MIME type info in links (in endpoint type, in binding)

		return findMediaTypes(opB.getInContentTypes());		
	}
	
	public List<String> findMediaTypeForResponse(Operation mdslOperation, HTTPResourceBinding binding) {
		HTTPOperationBinding opB = findOperationBindingFor(mdslOperation.getName(), binding);
		
		if(opB==null || opB.getOutContentTypes()==null) {
			List<String> defaultTypeList = new ArrayList<String>();
			defaultTypeList.add(DEFAULT_MEDIA_TYPE);
			return defaultTypeList;
		}
		
		return findMediaTypes(opB.getOutContentTypes());	
	}

	private List<String> findMediaTypes(MediaTypeList mimeTypes) {
		List<String> result = new ArrayList<>();
		if(mimeTypes.getCmt()!=null) {
			EList<CustomMediaType> cmtl = mimeTypes.getCmt();
			if(cmtl!=null) {
				cmtl.forEach(cmt->result.add(cmt.getValue()));
			}
		}
		if(mimeTypes.getSmt()!=null) {
			EList<StandardMediaType> smtl = mimeTypes.getSmt();
			if(smtl!=null&&smtl.size()>0)
				smtl.forEach(smt->result.add(smt.getIanaName())); 
		}
		return result;
	}

	private String findKVPInPolicy(SecurityBinding spb, String key) {
		EList<String> keys = spb.getKeys();
		EList<String> values = spb.getValues();
			
		if(keys.contains(key)) {
			int aurlPos = keys.indexOf(key);
			return values.get(aurlPos);
		}
		return null;
	}

	public String findAuthorizationUrlInPolicy(SecurityBinding spb) {
		return findKVPInPolicy(spb, "authorizationUrl"); 
	}

	public String findTokenUrlInPolicy(SecurityBinding spb) {
		return findKVPInPolicy(spb, "tokenUrl"); 
	}
	
	public String findOIDUrlInPolicy(SecurityBinding spb) {
		return findKVPInPolicy(spb, "openIdConnectUrl"); 
	}
	
	public Map<String,String> findScopesInPolicyOrBinding(SecurityPolicy sp, SecurityBinding spb) {
		HashMap<String,String> result = new HashMap<String,String>();

		EList<String> keys = spb.getKeys();
		EList<String> values = spb.getValues();
			
		for(int i=0;i<keys.size();i++) {
			// TODO document, make more flexible/assertive 
			if(keys.get(i).startsWith("Scope")) {
				result.put(keys.get(i), values.get(i));
			}
		}
		return result;
	}

	public void logError(String string) {
		if(logLevel>=0)
			System.err.println("[E] " + string);
	}
	
	public void logWarning(String string) {
		if(logLevel>=1)
			this.log("[W] " + string);
	}
	
	public void logInformation(String string) {
		if(logLevel>=2)
			this.log("[I] " + string);
	}
	
	public void log(String string) {
		System.out.println(string);
	}
}
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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
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
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.apiDescription.ProtocolBinding;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.ReportBinding;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.SecurityBinding;
import io.mdsl.apiDescription.SecurityPolicy;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.transformations.DataTypeTransformations;


/**
 * Helper class to resolve specific objects in an MDSL model. TODO rename?
 *
 */
public class MDSLSpecificationWrapper {
	
	private static final String OAS_CODE_X_742 = "x-742";
	private static final String OAS_CODE_X_743 = "x-743";
	private static final String OPEN_ID_CONNECT_URL = "openIdConnectUrl";
	private static final String TOKEN_URL = "tokenUrl";
	private static final String AUTHORIZATION_URL = "authorizationUrl";
	private static final String SCOPE_PREFIX = "Scope";
	private ServiceSpecificationAdapter mdslSpecification;

	public MDSLSpecificationWrapper(ServiceSpecificationAdapter mdslSpecification) {
		this.mdslSpecification = mdslSpecification;
	}
	
	public ServiceSpecificationAdapter getSpecification() {
		return this.mdslSpecification;
	}

	public String getElementName(ElementStructure representationElement) {
		if(representationElement.getPt()!=null)
			return representationElement.getPt().getName();
		else if(representationElement.getNp()!=null) {
			if(representationElement.getNp().getAtomP()!=null)
				return representationElement.getNp().getAtomP().getRat().getName();
			else if(representationElement.getNp().getTr()!=null)
				return representationElement.getNp().getTr().getName();
			else if(representationElement.getNp().getGenP()!=null)
				return representationElement.getNp().getGenP().getName();
		}
		return "unnamedElement"; 
	}
		
	// structure checkers:
	
	public boolean isSimplePayload(ElementStructure structure) {
		if(structure==null) {
			throw new MDSLException("Unexpected input for atomicity check: empty structure type"); 
		}
		
		// find out whether we deal with a simple or a complex parameter, starting with APL
		if (structure.getApl() != null) {
			return !isMultiplicity(structure.getApl().getCard()); // is not flat if cardinality is set to n (*, +)
		} 
		
		if(structure.getNp()!=null) {
			if(structure.getNp().getAtomP() != null) {
				return !isMultiplicity(structure.getNp().getAtomP().getCard());
			} else if(structure.getNp().getTr() != null) {
				if(isMultiplicity(structure.getNp().getTr().getCard())) {
					return false;
				}
				else {
					// find referenced explicit type and call same method again 
					MDSLLogger.reportInformation("Checking structure of type reference " + structure.getNp().getTr().getName());
					// return isSimplePayload(structure.getNp().getTr().getDcref().getStructure());
					return true;
				}
			}
			else if (structure.getNp().getGenP() != null) {
				// what about cardinality (does not have one?) 
				return true;
			}
			else {
				MDSLLogger.reportWarning("Unkown/unsupported single parameter node structure");
				return false; // other non-atomic structure type (?)
			}
		}
		else if (structure.getPt() != null) {
			if(isParameterTreeAtomic(structure.getPt())) {
				return true;
			}
			else { 
				return false;
			}
		} else if (structure.getPf() != null) {
			return false;
		}
		
		else {
			MDSLLogger.reportWarning("Unkown/unsupported structure");
			return false; // other non-atomic structure type (?)
		}
	}
	
	public boolean isParameterTreeAtomic(ParameterTree tree) {
		if(isMultiplicity(tree.getCard()))
			return false;
		
		List<TreeNode> nodes = collectTreeNodes(tree);
		for (TreeNode node : nodes) {
			if (node.getPn() != null && node.getPn().getAtomP() != null) {
				continue;
			}
			else if (node.getPn() != null && node.getPn().getGenP() != null) {
				continue;
			}
			else if (node.getPn() != null && node.getPn().getTr() != null) {
				if(node.getPn().getTr().getDcref().getStructure().getNp()!=null) {
					// catch false positive: tree in type reference (needs recursion for full solution):
					if(node.getPn().getTr().getDcref().getStructure().getNp().getTr()!=null) {
						return false;
					}
					else {
						continue;
					}
				}
				else {
					return false;
				}
			}
			else if (node.getApl() != null) {
				continue;
			}
			else if(node.getChildren() != null) {
				return false;
			}
			else {
				MDSLLogger.reportWarning("Unknown/unexpected type of tree node");
			}
		}
		return true;
	}
	
	private boolean isMultiplicity(Cardinality card) {
		if(card==null) 
			return false;
		
		return card.getAtLeastOne()!=null || card.getZeroOrMore()!=null;
	}
	
	public boolean treeHasMultiplicity(ParameterTree pt) {
		if(pt==null || pt.getCard()==null) 
			return false;
		
		return pt.getCard().getZeroOrOne()!=null
				|| pt.getCard().getAtLeastOne()!=null
				|| pt.getCard().getZeroOrMore()!=null;
	}
	
	public boolean referenceHasMultiplicity(TypeReference tr) {
		if(tr==null || tr.getCard()==null) 
			return false;
					
		return tr.getCard().getZeroOrOne()!=null
				|| tr.getCard().getAtLeastOne()!=null
				|| tr.getCard().getZeroOrMore()!=null;
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
	
	public List<AtomicParameter> extractAtomicElements(ElementStructure structure) {
		List<AtomicParameter> atomicParameterList = Lists.newLinkedList();
		
		if (structure.getApl() != null) {
			atomicParameterList.add(structure.getApl().getFirst());
			atomicParameterList.addAll(structure.getApl().getNextap());
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		} else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			// find referenced explicit type and call same method again 
			return extractAtomicElements(structure.getNp().getTr().getDcref().getStructure());
		} else if (structure.getPt() != null) {
			if(this.isParameterTreeAtomic(structure.getPt())) {
				atomicParameterList.addAll(this.collectAtomicParameters(structure.getPt()));
			}
			else 
				 throw new MDSLException("Cannot extract atoms from nested tree '"); // no PF, for instance
		} else if (structure.getNp().getGenP() != null) {
			// simple workaround/solution, others are possible:
			String name = structure.getNp().getGenP().getName(); // can be null
			MDSLLogger.reportInformation("Adding a surrogate ap for generic parameter " + name);
			AtomicParameter ap = DataTypeTransformations.createAtomicDataParameter(name, "string");
			atomicParameterList.add(ap);
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
			if (node.getPn() != null && node.getPn().getAtomP() != null) {
				list.add(node.getPn().getAtomP());
			}
			// edge case (v545): type reference that points at an AP 
			else if (node.getPn() != null && node.getPn().getTr() != null) {
				ElementStructure referencedType = node.getPn().getTr().getDcref().getStructure();
				if(referencedType.getNp()!=null&&referencedType.getNp().getAtomP()!=null) {
					list.add(referencedType.getNp().getAtomP());
				}
			}
			if (node.getApl() != null) {
				list.add(node.getApl().getFirst());
				list.addAll(node.getApl().getNextap());
			}
		}
		return list;
	}

	public List<TreeNode> collectTreeNodes(ParameterTree tree) {
		List<TreeNode> nodes = Lists.newLinkedList();
		nodes.add(tree.getFirst());
		nodes.addAll(tree.getNexttn());
		return nodes;
	}
	
	// finders:
	
	public static String getClassifierAndElementStereotype(PatternStereotype classifier, RoleAndType roleAndType) {
		String result = "";
		if (classifier != null) {
			String patternDecorator = classifier.getPattern();
			if (patternDecorator != null && !patternDecorator.isEmpty())
				result = "<<" + patternDecorator + ">>";
			else {
				patternDecorator = classifier.getEip();
				if (patternDecorator != null && !patternDecorator.isEmpty())
					result = "<<" + patternDecorator + ">>";
				else {
					String otherStereotype = classifier.getName();
					if (otherStereotype != null && !otherStereotype.isEmpty())
						result = "<<" + otherStereotype + "_Role>>"; // OAS ok, but Swagger tools need the "_" in the stereotype
				}
			}
			result += " ";
		}
		result += MAPLinkResolver.mapParameterRoleAndType(roleAndType);
		return result;
	}
	
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
							this.logInformation("(EB]) Non-HTTP binding found for " + nextEndpointList.getContract().getName());
					}
				}
			}
		}
		
		return result;
	}
	
	public EList<HTTPResourceBinding> getHTTPResourceBindings(EndpointInstance endpointInstance) {
		EList<TechnologyBinding> protocolBindings = endpointInstance.getPb();
		for(int i=0;i<protocolBindings.size();i++) {
			ProtocolBinding pb = endpointInstance.getPb().get(i).getProtBinding(); 
			HTTPBinding httpb = pb.getHttp();
			if(httpb!=null) {
				EList<HTTPResourceBinding> httprb = httpb.getEb();
				if(httprb==null) { 
					return null;
				}
				return httprb;
 			}
		}
		return null; 
	}
	
	public List<Provider> findProvidersFor(EndpointContract endpointType) {
		EList<EObject> providers = ((ServiceSpecification) endpointType.eContainer()).getProviders();
		List<Provider> result = new ArrayList<Provider>(); 
		for(EObject provider : providers) {
			if(provider instanceof Provider) {
				// expecting 0 index here:
				EList<EndpointList> epList = ((Provider) provider).getEpl();
				if(epList.size()==0) {
					MDSLLogger.reportError("No endpoint provider found for " + endpointType.getName());
				}
				
				if (epList.size()>=1){
					MDSLLogger.reportWarning("More than one provider instance found, using first.");
				}
				EndpointContract offeredContract = epList.get(0).getContract();
				if(offeredContract.getName().equals(endpointType.getName())) {
					providers.add(provider);
				}
			}
			else
				; // skip, must be AsyncAPI
		}
		return result;
	}

	public EndpointInstance findFirstProviderAndHttpBindingFor(EndpointContract endpointType) {
		EList<EObject> providers = ((ServiceSpecification) endpointType.eContainer()).getProviders();
		for(EObject provider : providers) {
			if(provider instanceof Provider) {
				// expecting 0 index here:
				EList<EndpointList> epList = ((Provider) provider).getEpl();
				if(epList.size()==0) {
					MDSLLogger.reportError("No endpoint provider found for " + endpointType.getName());
				}
				
				if (epList.size()>=1){
					MDSLLogger.reportWarning("More than one endpoint provider instance found, using first.");
				}
				EndpointContract offeredContract = epList.get(0).getContract();
				if(offeredContract.getName().equals(endpointType.getName())) {
					EList<EndpointInstance> eps = epList.get(0).getEndpoints();
					if(eps.size()==0) {
						MDSLLogger.reportWarning("No endpoint instance found for " + endpointType.getName());
						return null;
					}
					if(eps.size()>1) {
						MDSLLogger.reportWarning("Provider for " + endpointType.getName() + " has multiple endpoint instances, using first one.");
					}
					return eps.get(0); // TODO v55 collect in list rather than return first
				}
			}
			else
				; // skip, must be AsyncAPI
		}
		logInformation("Endpoint instance in provider for " + endpointType.getName() + " does not have an endpoint provider.");
		return null;
	}
		
	public String findReportCodeInBinding(String operation, String reportNameInEndpointType, HTTPResourceBinding binding) {
		if(binding==null)
			return OAS_CODE_X_742; // OAS only accepts three-digit codes up to 500 and vendor extensions
		
		// TODO could move navigation to helper, used in several methods
		EList<HTTPOperationBinding> operationBindings = binding.getOpsB();
		for(int i=0;i<operationBindings.size();i++) {
			HTTPOperationBinding opB = operationBindings.get(i);
			
			if(opB.getBoundOperation().equals(operation)) {
				MDSLLogger.reportInformation("Found an operation binding for " + operation + " in " + binding.getName() 
					+ ": " + opB.getMethod().getName());
				EList<ReportBinding> reports = opB.getReportBindings();

				for(int j=0;j<reports.size();j++) {
					ReportBinding report = reports.get(j);
					MDSLLogger.reportInformation("Processing a report binding: " + report.getName());
					if(report.getName().equals(reportNameInEndpointType)) {
						MDSLLogger.reportInformation("Report binding found for " + operation + " in " + binding.getName());
						return String.valueOf(report.getHttpStatusCode());
					}
				}
			}
		}
		MDSLLogger.reportWarning("No report binding found for " + operation + " in " + binding.getName());
		// TODO handle contract inconsistency differently (validator)?
		return OAS_CODE_X_743; 
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
				EList<ReportBinding> reports = opB.getReportBindings();
				for(int j=0;j<reports.size();j++) {
					ReportBinding report = reports.get(j);
					if(report.getName().equals(reportNameInEndpointType)) {
						// this.log("[RB] Report binding found for " + operation + " in " + binding.getName());
						return report.getDetails();
					}
				}
			}
		}
		MDSLLogger.reportWarning("No report binding found for " + operation + " in " + binding.getName());
		// TODO handle contract inconsistency differently (validator)?
		return "n/a"; 
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
						MDSLLogger.reportInformation("(PB) Policy binding found for " + operation + " in " + binding.getName());
						return policy;
					}
				}
			}
		}
		MDSLLogger.reportWarning("No policy binding found for " + operation + " in " + binding.getName() + ", skipping this one.");
		// TODO handle contract inconsistency differently (validator)?
		return null; 
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
		return findKVPInPolicy(spb, AUTHORIZATION_URL); 
	}

	public String findTokenUrlInPolicy(SecurityBinding spb) {
		return findKVPInPolicy(spb, TOKEN_URL); 
	}
	
	public String findOIDUrlInPolicy(SecurityBinding spb) {
		return findKVPInPolicy(spb, OPEN_ID_CONNECT_URL); 
	}
	
	public Map<String,String> findScopesInPolicyOrBinding(SecurityPolicy sp, SecurityBinding spb) {
		HashMap<String,String> result = new HashMap<String,String>();

		EList<String> keys = spb.getKeys();
		EList<String> values = spb.getValues();
			
		for(int i=0;i<keys.size();i++) {
			// TODO document, make more flexible/assertive 
			if(keys.get(i).startsWith(SCOPE_PREFIX)) {
				result.put(keys.get(i), values.get(i));
			}
		}
		return result;
	}
	
	// loggers
	
	public static void logError(String message) {
		MDSLLogger.reportError(message);
	}
	
	public static void logWarning(String message) {
		MDSLLogger.reportWarning(message);
	}
	
	public static void logInformation(String message) {
		MDSLLogger.reportInformation(message);
	}
}
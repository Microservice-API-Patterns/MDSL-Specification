package io.mdsl.ui.quickfix;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.EndpointList;
import io.mdsl.apiDescription.HTTPBinding;
import io.mdsl.apiDescription.HTTPGlobalParameterBinding;
import io.mdsl.apiDescription.HTTPOperationBinding;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.HTTPVerb;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ProtocolBinding;
import io.mdsl.apiDescription.Provider;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.TechnologyBinding;
import io.mdsl.apiDescription.impl.EndpointContractImpl;
import io.mdsl.apiDescription.impl.ProviderImpl;
import io.mdsl.transformations.TransformationHelpers;

class AddHttpProviderAndBindingForContract implements ISemanticModification {
	private final static String DEFAULT_LOCATION = "http://localhost:8080";
	private String defaultResourceName; 

	public AddHttpProviderAndBindingForContract(String defaultResourceName) {
		this.defaultResourceName = defaultResourceName;
	}

	@Override
	public void apply(EObject element, IModificationContext context) throws Exception {
		
		if(element.getClass()!=EndpointContractImpl.class) {
			TransformationHelpers.reportError("AddHttpProviderAndBindingForContract expects an Endpoint Contract.");
			return;
		}
		
		EndpointContract contract = (EndpointContract) element; 
		ServiceSpecification ss = (ServiceSpecification) contract.eContainer();
		String contractName = contract.getName();
		Provider httpBindingProvider = ApiDescriptionFactory.eINSTANCE.createProvider();
		httpBindingProvider.setName(contractName + "Provider");
		
		// add operations, apply verb mapping heuristics, put default URI and element mapping
		
		// TODO [R] handle MIME types, errors, status? separate QFs?

		EndpointList epl = ApiDescriptionFactory.eINSTANCE.createEndpointList();
		epl.setContract(contract);
		httpBindingProvider.getEpl().add(epl);
		
		EndpointInstance epi = ApiDescriptionFactory.eINSTANCE.createEndpointInstance();
		epi.setLocation(DEFAULT_LOCATION); // TODO tbc (might call for grammar cleanup/simplification)
		TechnologyBinding tb = ApiDescriptionFactory.eINSTANCE.createTechnologyBinding();
		ProtocolBinding pb = ApiDescriptionFactory.eINSTANCE.createProtocolBinding();
		HTTPBinding httpBinding = createHTTPBindingWithOperations(defaultResourceName, contract.getOps());
		pb.setHttp(httpBinding);
		tb.setProtBinding(pb);
		epi.getPb().add(tb);
		epl.getEndpoints().add(epi);
					
		addBindingIfNameAvailable(ss, httpBindingProvider);
	}

	private void addBindingIfNameAvailable(ServiceSpecification ss, Provider httpBindingProvider) {
		for(EObject providerOrBroker : ss.getProviders()) {
			if(providerOrBroker.getClass()==ProviderImpl.class) {
				if(((Provider) providerOrBroker).getName().equals(httpBindingProvider.getName())) {
					TransformationHelpers.reportError("A provider with the name " + httpBindingProvider.getName() + " already exists. Not adding binding.");
					return;
				}
			}
			// else: must be AsyncMDSL broker; check name anyway?
		}
		ss.getProviders().add(httpBindingProvider);
	}
	
	private HTTPBinding createHTTPBindingWithOperations(String homeResourceName, EList<Operation> operations) {
		HTTPBinding result = ApiDescriptionFactory.eINSTANCE.createHTTPBinding();
		result.setHttp("HTTP"); // string value needed?
		HTTPResourceBinding resource = ApiDescriptionFactory.eINSTANCE.createHTTPResourceBinding();
		// TODO (M) could get name from UI
		resource.setName(homeResourceName); // TODO more suited name? 
		resource.setUri("/" + homeResourceName); // e.g. resource name? 
		result.getEb().add(resource);
		
		for(Operation operation : operations) {
			HTTPOperationBinding opB = ApiDescriptionFactory.eINSTANCE.createHTTPOperationBinding();
			opB.setBoundOperation(operation.getName());
			HTTPVerb verb = mapOperationToMethod(operation);
			opB.setMethod(verb);
			opB.setGlobalBinding(defaultElementMappingFor(verb));
			// TODO (L) could add more specific element bindings (according to chosen HTTP verb)
			resource.getOpsB().add(opB);
		}
		
		return result;
	}
	
	private HTTPVerb mapOperationToMethod(Operation operation) {
		if(operation.getResponsibility()==null)
			return HTTPVerb.POST;
		
		OperationResponsibility opRespo = operation.getResponsibility();
		if(opRespo.getSco()!=null)
			return HTTPVerb.PUT;
		if(opRespo.getRo()!=null)
			return HTTPVerb.GET;
		if(opRespo.getCf()!=null)
			return HTTPVerb.POST;
		if(opRespo.getSro()!=null)
			return HTTPVerb.PATCH;
		if(opRespo.getSdo()!=null)
			return HTTPVerb.DELETE;
		
		// TODO (L) add other heuristics (see CML FM, see OASgen)
		
		// default
		return HTTPVerb.POST;
	}
	
	private HTTPGlobalParameterBinding defaultElementMappingFor(HTTPVerb verb) {
		HTTPGlobalParameterBinding gpb = ApiDescriptionFactory.eINSTANCE.createHTTPGlobalParameterBinding();
		if(verb==HTTPVerb.GET)
			gpb.setParameterMapping(HTTPParameter.QUERY);
		else if(verb==HTTPVerb.DELETE)
			gpb.setParameterMapping(HTTPParameter.PATH); 
		else 
			gpb.setParameterMapping(HTTPParameter.BODY);
		return gpb;
	}
}
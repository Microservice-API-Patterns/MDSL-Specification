package io.mdsl.generator.refactorings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.EndpointInstance;
import io.mdsl.apiDescription.HTTPResourceBinding;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.exception.MDSLException;

public class RefactoringHelpers {
	private static final String GENERATED_FILE_NAME_SUFFIX = "-transformed.mdsl";
	// private static final String GENERATED_FILE_NAME_SUFFIX = "_ref.mdsl";

	public static void generateRefactoringOutput(
			ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa, URI inputFileURI, EObject refactoringSubject, CharSequence result) {
		// TODO is the EObject refactoringSubject parameter really needed? could create MDSLResource from entire ServiceSpecification mdslSpecification (?)
		MDSLResource targetResource = new MDSLResource(refactoringSubject.eResource());
		result = result + targetResource.getXtextResource().getSerializer().serialize(mdslSpecification);
			
		// output file name ignored as we generate to main memory (string fsa?)
		fsa.generateFile(inputFileURI.trimFileExtension().lastSegment() + GENERATED_FILE_NAME_SUFFIX, result);
	}

	public static HTTPResourceBinding getFirstOnlyResourceBinding(EndpointInstance httpb) {
		if(httpb==null)
			throw new MDSLException("HTTP endpoint instance is null");
		if(httpb.getPb()==null)
			throw new MDSLException("No provider binding found at " + httpb.getLocation());
		if(!(httpb.getPb().size()==1))
			throw new MDSLException("There should be one and only one provider binding under " + httpb.getLocation());
		if(!(httpb.getPb().get(0).getProtBinding().getHttp().getEb().size()==1)) {
			throw new MDSLException("There should be one and only one eb instance under " + httpb.getLocation());
		}
		HTTPResourceBinding binding = httpb.getPb().get(0).getProtBinding().getHttp().getEb().get(0);
		
		return binding;
	}
	
	public static ElementStructure getRequestPayload(Operation operation) {
		return operation.getRequestMessage().getPayload();
	}
	
	public static ElementStructure getResponsePayload(Operation operation) {
		return operation.getResponseMessage().getPayload();
	}
	
	public static SingleParameterNode findWrappingTargetInRequest(io.mdsl.apiDescription.Operation operation) {
		if(operation.getRequestMessage()==null) {
			return null;
		}
		if(operation.getRequestMessage().getPayload()==null) {
			return null;
		}
		return operation.getRequestMessage().getPayload().getNp();
	}
	
	public static SingleParameterNode findWrappingTargetInResponse(io.mdsl.apiDescription.Operation operation) {
		if(operation.getResponseMessage()==null) {
			return null;
		}
		if(operation.getResponseMessage().getPayload()==null) {
			return null;
		}
		return operation.getResponseMessage().getPayload().getNp();
	}
}
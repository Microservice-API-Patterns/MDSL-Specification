package io.mdsl.exception;

import org.eclipse.emf.common.util.URI;

public class ResourceIsNoMDSLModelException extends MDSLException {

	public ResourceIsNoMDSLModelException(URI uri) {
		super("The resource '" + uri.toString() + "' does not contain a MDSL model.");
	}

	public ResourceIsNoMDSLModelException() {
		super("The given resource does not contain a MDSL model.");
	}

}

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
package io.mdsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.emf.common.util.TreeIterator;

import com.google.common.collect.Iterators;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.exception.MDSLException;
import io.mdsl.exception.ResourceIsNoMDSLModelException;

/**
 * An Eclipse/Ecore resource for which we can be sure that it contains an MDSL
 * model.
 * 
 * @author Stefan Kapferer
 */
public class MDSLResource implements Resource {
	private final Resource resource;

	public MDSLResource(Resource resource) {
		if (resource.getContents().isEmpty())
			throwResourceIsNoMDSLModelException(resource);
		if (!(resource.getContents().get(0) instanceof ServiceSpecification))
			throwResourceIsNoMDSLModelException(resource);

		this.resource = resource;
		this.resource.setTrackingModification(true);
	}

	public ServiceSpecification getServiceSpecification() {
		List<ServiceSpecification> mdslModels = IteratorExtensions.<ServiceSpecification>toList(
				Iterators.<ServiceSpecification>filter(resource.getAllContents(), ServiceSpecification.class));
		// MDSL resource only contains one ServiceSpecification
		return mdslModels.get(0);
	}

	private void throwResourceIsNoMDSLModelException(Resource resource) {
		if (resource.getURI() != null)
			throw new ResourceIsNoMDSLModelException(resource.getURI());
		else
			throw new ResourceIsNoMDSLModelException();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MDSLResource))
			return false;

		MDSLResource otherResource = (MDSLResource) obj;
		return resource.getURI().equals(otherResource.resource.getURI());
	}

	public XtextResource getXtextResource() {
		if (!(resource instanceof XtextResource))
			throw new MDSLException(
					"The given resource (" + resource.getURI().toString() + ") is not an XtextResource!");
		return (XtextResource) resource;
	}

	@Override
	public int hashCode() {
		return resource.hashCode();
	}

	@Override
	public EList<Adapter> eAdapters() {
		return resource.eAdapters();
	}

	@Override
	public boolean eDeliver() {
		return resource.eDeliver();
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		resource.eSetDeliver(deliver);
	}

	@Override
	public void eNotify(Notification notification) {
		resource.eNotify(notification);
	}

	@Override
	public ResourceSet getResourceSet() {
		return resource.getResourceSet();
	}

	@Override
	public URI getURI() {
		return resource.getURI();
	}

	@Override
	public void setURI(URI uri) {
		resource.setURI(uri);
	}

	@Override
	public long getTimeStamp() {
		return resource.getTimeStamp();
	}

	@Override
	public void setTimeStamp(long timeStamp) {
		resource.setTimeStamp(timeStamp);
	}

	@Override
	public EList<EObject> getContents() {
		return resource.getContents();
	}

	@Override
	public String getURIFragment(EObject eObject) {
		return resource.getURIFragment(eObject);
	}

	@Override
	public EObject getEObject(String uriFragment) {
		return resource.getEObject(uriFragment);
	}

	@Override
	public void save(Map<?, ?> options) throws IOException {
		resource.save(options);
	}

	@Override
	public void load(Map<?, ?> options) throws IOException {
		resource.load(options);
	}

	@Override
	public void save(OutputStream outputStream, Map<?, ?> options) throws IOException {
		resource.save(outputStream, options);
	}

	@Override
	public void load(InputStream inputStream, Map<?, ?> options) throws IOException {
		resource.load(inputStream, options);
	}

	@Override
	public boolean isTrackingModification() {
		return resource.isTrackingModification();
	}

	@Override
	public void setTrackingModification(boolean isTrackingModification) {
		resource.setTrackingModification(isTrackingModification);
	}

	@Override
	public boolean isModified() {
		return resource.isModified();
	}

	@Override
	public void setModified(boolean isModified) {
		resource.setModified(isModified);
	}

	@Override
	public boolean isLoaded() {
		return resource.isLoaded();
	}

	@Override
	public void unload() {
		resource.unload();
	}

	@Override
	public void delete(Map<?, ?> options) throws IOException {
		resource.delete(options);
	}

	@Override
	public EList<Diagnostic> getErrors() {
		return resource.getErrors();
	}

	@Override
	public EList<Diagnostic> getWarnings() {
		return resource.getWarnings();
	}

	@Override
	public TreeIterator<EObject> getAllContents() {
		return resource.getAllContents();
	}
}

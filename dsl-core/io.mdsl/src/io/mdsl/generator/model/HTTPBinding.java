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
package io.mdsl.generator.model;

import java.util.ArrayList;
import java.util.List;

public class HTTPBinding implements ProtocolBinding {

	private String uriPath;
	private List <HTTPResource> resources = new ArrayList<HTTPResource>();

	public HTTPBinding(String uriPath) {
		super();
		this.uriPath = uriPath;
	}

	public String getUriPath() {
		return uriPath;
	}

	public List<HTTPResource> getResources() {
		return resources;
	}

	public void setUriPath(String uriPath) {
		this.uriPath = uriPath;
	}

	public void addResource(HTTPResource resource) {
		this.resources.add(resource);
	}

	@Override
	public String getProtocolName() {
		return "HTTP";
	}
}

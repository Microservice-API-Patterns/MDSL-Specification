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

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

import com.google.common.collect.Lists;

/**
 * Represents an MDSL data type.
 */
public class DataType implements MDSLType {

	private String name;
	private List<DataTypeField> fields;
	// TODO v55 role (basic types) and element stereotype (all types)

	private String version = "N/A";
	private String defaultValue;

	/**
	 * Creates a new data type.
	 * 
	 * @param name the name of the new data type
	 */
	public DataType(String name) {
		this.name = name;
		this.fields = Lists.newLinkedList();
	}

	/**
	 * Returns the name of represented data type.
	 * 
	 * @return the name of the represented data type
	 */
	public String getName() {
		return name;
	}

	/**
	 * Adds a field to the represented data type.
	 * 
	 * @param field the field that shall be added to the date type
	 */
	public void addField(DataTypeField field) {
		this.fields.add(field);
	}

	/**
	 * Returns a list with all the fields of the represented data type.
	 * 
	 * @return a list with all fields of the represented data type
	 */
	public List<DataTypeField> getFields() {
		return Lists.newLinkedList(fields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DataType dataType = (DataType) o;
		return name.equals(dataType.name);
	}

	@Override
	public boolean isAtomic() {
		if(fields.size()>1) {
			return false;
		}
		
		if(fields.get(0).getType() instanceof BasicType) {
			return true;
		}
		
		return false;
	}
	
	@Override 
	public String sampleJSONWithEscapedQuotes(int levelOfDetail) {
		String result = this.sampleJSON(levelOfDetail);
		return result.toString().replaceAll("\"", Matcher.quoteReplacement("\\\""));
	}

	@Override
	public String sampleJSON(int levelOfDetail) {
		/*
		if(this.defaultValue!=null&&!this.defaultValue.equals("")) {
			// not checked for correctness, not mapped from abstract MDSL to JSON yet
			return this.defaultValue;
		}
		*/
		
		StringBuffer result = new StringBuffer(/*'"' + name + "\": */ "{ ");
		result.append("\"_version\": \"" + getVersion() + "\"");
		// TODO better solution for comma separation, full loop
		fields.forEach(field->result.append(field.sampleJSON(levelOfDetail))); 
		result.append("}");
		return result.toString();
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setVersion(String svi) {
		this.version = svi;
	}
}

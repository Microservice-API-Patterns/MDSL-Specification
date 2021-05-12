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

import com.google.common.collect.Lists;

/**
 * Represents an MDSL data type.
 */
public class DataType implements MDSLType {

	private String name;
	private List<DataTypeField> fields;

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
		return false;
	}
}

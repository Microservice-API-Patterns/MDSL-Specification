package io.mdsl.transformations;

import org.apache.commons.lang.WordUtils;

import io.mdsl.apiDescription.DataContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TreeNode;

public class DataTypeTransformationHelpers {
	static final String VOID = "void";
	static final String DOUBLE = "double";
	static final String LONG = "long";
	static final String RAW = "raw";
	static final String BOOL = "bool";
	static final String STRING = "string";
	static final String INT = "int";

	static final String ID_ROLE = "ID";
	final static String METADATA_ROLE = "MD";
	static final String DATA_ROLE = "D";
	static final String LINK_ROLE = "L";
	
	static final String ANONYMOUS_ID = "anonymousNode";
	static final String ANONYMOUS = "anonymous";
	static final String ANONYMOUS_TYPE = "AnonymousTypeReference";
	static final String DTO_SUFFIX = "DTO";
	
	static boolean isValidTypeRole(String role) {
		if (role == null) {
			// generic parameters (P, "idOnly", D) do not have a type:
			return false;
		}
		if (role.equals(DATA_ROLE)||role.equals(METADATA_ROLE)||role.equals(ID_ROLE)||role.equals(LINK_ROLE)) {
			return true;
		}
		return false;
	}

	static boolean isValidBaseType(String dataType) {
		if (dataType == null || dataType.equals("")) {
			// type information is optional, so can be absent
			return true;
		}
		if (dataType.equals(INT)) {
			return true;
		}
		if (dataType.equals(STRING)) {
			return true;
		}
		if (dataType.equals(BOOL)) {
			return true;
		}
		if (dataType.equals(RAW)) {
			return true;
		}
		if (dataType.equals(LONG)) {
			return true;
		}
		if (dataType.equals(DOUBLE)) {
			return true;
		}
		if (dataType.equals(VOID)) {
			return true;
		}
		return false;
	}

	static boolean addIfNotPresent(ServiceSpecification ss, DataContract dt) {
		for (DataContract type : ss.getTypes()) {
			if (type.getName().equals(dt.getName())) {
				return false;
			}
		}
		// not found, so can be added:
		ss.getTypes().add(dt);
		return true;
	}
	
	// ** naming related

		public static String nameOf(TreeNode tn) {
			String result = null;
			if (tn.getPn() != null) {
				result = nameOf(tn.getPn());
			} else if (tn.getApl() != null) {
				result = tn.getApl().getName();
			} else if (tn.getChildren() != null) {
				result = tn.getChildren().getName();
			}

			if (result == null || result.isEmpty()) {
				result = ANONYMOUS_ID; // will cause validation error (binding level)
			}

			return result;
		}

		public static String nameForElement(String name, String prefix) {
			if (name == null || name.isEmpty()) {
				return null;
			} else {
				return prefix + capitalizeName(name);
			}
		}

		public static String nameOf(SingleParameterNode spn) {
			String result = null;
			if (spn.getAtomP() != null) {
				result = spn.getAtomP().getRat().getName();
			} else if (spn.getGenP() != null) {
				result = spn.getGenP().getName();
			} else if (spn.getTr() != null) {
				result = spn.getTr().getName();
			}

			if (result == null || result.isEmpty()) {
				result = ANONYMOUS_ID; // will cause validation error (binding level)
			}

			return result;
		}

		public static String replaceSpacesWithUnderscores(String name) {
			return name.replace(" ", "_");
		}

		public static String decapitalizeName(String name) {
			return WordUtils.uncapitalize(name);
			/*
			String c1 = name.substring(0, 1).toLowerCase();
			c1 += name.substring(1);
			return c1;
			*/
		}

		public static String capitalizeName(String name) {
			return WordUtils.capitalize(name);
			/*
			String c1 = name.substring(0, 1).toUpperCase();
			c1 += name.substring(1);
			return c1;
			*/
		}
}

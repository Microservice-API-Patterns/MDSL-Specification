package io.mdsl.discovery.tools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This example illustrates how to transform Java classes and interfaces into MDSL endpoint types (via a Freemarker template).
 *
 * @author socadk
 */

public class Java2MDSL {
	
	static boolean ignoreStereotypes = false;
	static ArrayList<Endpoint> endpoints = new ArrayList<Endpoint>();
	static MDSLIntermediateDataModel mdslDTO = new MDSLIntermediateDataModel("NN", "tbc", endpoints);

	final static String UNDEFINED = "undefined";
	
	public static void main(String[] args) {
		String transformationInput;
		if(args.length==2 && (args[0].equals("-a") || args[0].equals("-all"))) {
			ignoreStereotypes = true;
			transformationInput = args[1];
		}
		else if( args.length==1) {
			ignoreStereotypes = false;
			transformationInput = args[0];
		}
		else 
			throw new IllegalArgumentException("Invocation syntax: Java2MDSL [-a|all] fully.qualified.classname");
			
		// could allow to run for entire package/module/folder (future work)
		transformServiceClass(transformationInput);
		
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("serviceSpecification", mdslDTO);
			
		String outputFileName = "src-gen/" + mdslDTO.getApiName() + ".mdsl";
		try {
			FreemarkerWrapper.generate("MDSLEndpointTypeTemplate.mdsl.ftl", input, outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Successfully generated " + outputFileName);
	}
	
	private static boolean checkClassEligibilityAndObtainRole(Class<?> endpointCandidate, StringBuffer endpointRole) {
		Annotation[] alist = endpointCandidate.getAnnotations();
		
		for(int i=0;i<alist.length;i++) {
			Annotation a = alist[i];
			Class<? extends Annotation> at = a.annotationType();
			System.out.println("Class " + endpointCandidate.getName() + " is annotated with " + at.getName());
			
			if(at.getName().equals("io.mdsl.annotations.ServiceEndpoint")) {

				String role = ((io.mdsl.annotations.ServiceEndpoint) a).role();
				
				if(role == null || role.equals("")) {
					role = "UNDEFINED";
				}
				
				endpointRole.append(wrapRoleIfNeeded(role));
				
				// no secondary role defined in stereotype at present (unlike in MAP)
				
				return true;
			}
		}
		return false;
	}
	
	private static boolean checkMethodEligibilityAndObtainResponsibilities(Method operationCandidate, StringBuffer operationResponsibility) {
		Annotation[] alist = operationCandidate.getAnnotations();
		
		for(int i=0;i<alist.length;i++) {
			Annotation a = alist[i];
			Class<? extends Annotation> at = a.annotationType();
			System.out.println("Method " + operationCandidate.getName() + " is annotated with " + at.getName());
			
			if(at.getName().equals("io.mdsl.annotations.ServiceOperation")) {
				String responsibility = ((io.mdsl.annotations.ServiceOperation) a).responsibility();
				
				if(responsibility == null || responsibility.equals("")) {
					responsibility = "UNDEFINED";
				}
				
				operationResponsibility.append(wrapResponsiblityIfNeeded(responsibility));
				
				return true;
			}
		}
		return false;
	}

	private static Object wrapRoleIfNeeded(String role) {
		if(role==null) 
			throw new IllegalArgumentException("Won't wrap a null string");
		
		if(role.equals("PROCESSING_RESOURCE"))
			return role;
		else if(role.equals("INFORMATION_HOLDER_RESOURCE"))
			return role;
		else if(role.equals("OPERATIONAL_DATA_HOLDER"))
			return role;
		else if(role.equals("MASTER_DATA_HOLDER"))
			return role;
		else if(role.equals("REFERENCE_DATA_HOLDER"))
			return role;
		else if(role.equals("DATA_TRANSFER_RESOURCE"))
			return role;
		else if(role.equals("LINK_LOOKUP_RESOURCE"))
			return role;
		else 
			return "\"" + role + "\"";
	}
	
	private static Object wrapResponsiblityIfNeeded(String responsibility) {
		if(responsibility==null) 
			throw new IllegalArgumentException("Won't wrap a null string");
		
		if(responsibility.equals("COMPUTATION_FUNCTION"))
			return responsibility;
		else if(responsibility.equals("STATE_CREATION_OPERATION"))
			return responsibility;
		else if(responsibility.equals("RETRIEVAL_OPERATION"))
			return responsibility;
		else if(responsibility.equals("STATE_TRANSITION_OPERATION"))
			return responsibility;
		else 
			return "\"" + responsibility + "\"";
	}

	private static void transformServiceClass(String name) {
		Class<?> classToBeTransformed = findAndLoadClass(name);
			
		mdslDTO.setApiName(trimClassName(name));
		StringBuffer role = new StringBuffer("");
		boolean isServiceEndpoint = checkClassEligibilityAndObtainRole(classToBeTransformed, role);
		
		if(ignoreStereotypes || isServiceEndpoint) {
			ArrayList<Operation> operations = new ArrayList<Operation>();
			Endpoint epForClass = new Endpoint(name, role.toString(), operations);
			mdslDTO.getEndpoints().add(epForClass);
				
			// TODO (H) handle inherited methods (?)
			transformMethods(classToBeTransformed.getDeclaredMethods(), operations);
		}
	}

	private static String trimClassName(String name) {
		int indexOfLastDot = name.lastIndexOf('.');
		return name.substring(indexOfLastDot+1);
	}

	private static Class<?> findAndLoadClass(String name) {
		Class<?> classToBeTransformed;
		try {
			classToBeTransformed = (Class<?>) Class.forName(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Java2MDSL can't load class <classname>");
		}
		return classToBeTransformed;
	}

	private static void transformMethods(Method[] dmlist, ArrayList<Operation> operations) {		
		for(int j=0;j<dmlist.length;j++) {
			Method m = dmlist[j];
			
			// skip private and protected methods (could warn if MDSL annotation is present)
			if(!Modifier.isPublic(m.getModifiers())) {
				System.out.println("Skipping non-public operation " + m.getName());
				return;
			}
					
			StringBuffer operationResponsibility = new StringBuffer();
			boolean hasMDSLAnnotation = checkMethodEligibilityAndObtainResponsibilities(m, operationResponsibility);
			
			if(ignoreStereotypes || hasMDSLAnnotation) {
				Operation op = new Operation(m.getName(), operationResponsibility.toString(), "unknownMEP", "dummy", "dummy");
				operations.add(op);
					
				Parameter[] pta = m.getParameters();
				String signature = transformParameters(pta);	
				op.setExpecting(signature);
					
				Class<?> rt = m.getReturnType();
				String returnType = transformParameter("returnType", rt);
				op.setDelivering(returnType);
			}
		}
	}

	private static String transformParameter(String name, Class<?> type) {	
		
		// TODO (L) turn parameters classifier (MAP, other) in stereotype to comment (if present)
		
		if(type.getTypeName().equals("int")) {
			return '"' + name + "\":" + "D<int>";
		}
		else if(type.getTypeName().equals("boolean")) {
			return '"' + name + "\":" + "D<bool>";
		}
		else if(type.getTypeName().equals("byte")) {
			return '"' + name + "\":" + "D<raw>";
		}
		else if(type.getTypeName().equals("long")) {
			return '"' + name + "\":" + "D<long>";
		}
		else if(type.getTypeName().equals("double")) {
			return '"' + name + "\":" + "D<double>";
		}
		// no float in MDSL at present (v3.5)  
		else if(type.getTypeName().equals("byte")) {
			return '"' + name + "\":" + "D<raw>";
		}
		// could not include but ignore void parameters (can only be present in response anyway)
		else if(type.getTypeName().equals("void")) {
			return '"' + name + "\":" + "D<void>";
		}
		else if(type.getTypeName().equals("java.lang.String")) {
			return '"' + name + "\":" + "D<string>";
		}
		else if (type.getTypeName().endsWith("[]")) {
			return transformParameter(name, type.componentType()) + "*"; 
		}
		else {
			// TODO (M) turn into explicit "data type" in MDSL
			return '"' + name + "\":" + transformDataClass(type);
		}
	}

	private static String transformDataClass(Class<?> type) {
		boolean first=true;
		String result = "{";
		// TODO (H) handle inherited attributes (?)
		Field[] attrArray = type.getDeclaredFields();
		for(int i=0;i<attrArray.length;i++) {
			Field attr = attrArray[i];
			if(first) 
				first = false;
			else 
				result += ", ";
			result += transformParameter(attr.getName(), attr.getType());
		}
		return result + "}";
	}

	private static String transformParameters(Parameter[] pta) {
		boolean first=true;
		String result = "{";
		for(int i=0;i<pta.length;i++) {
			// note: parameter names get lost during compilation by default
			// would get them during JDK/JVM annotation processing (?)
			if(first)
				first=false;
			else
				result += ", ";
			
			result += transformParameter(pta[i].getName(), pta[i].getType());
		}
		return result + "}";
	}
}
package io.mdsl.annotations;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes(
		  "io.mdsl.discovery.ServiceEndpoint")
public class ContractProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {}
    
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		for (TypeElement annotation : annotations) {
	        Set<? extends Element> annotatedElements 
	          = roundEnv.getElementsAnnotatedWith(annotation);
	        
	        Iterator<? extends Element> aEIt = annotatedElements.iterator();
        	Element aE = aEIt.next();
	        while (aEIt.hasNext()) {
	        	String className = ((TypeElement) aE
	        			  .getEnclosingElement()).getQualifiedName().toString();
	        	System.out.println("Found a service endpoint: " + className);
	        	
	        	try {
	        		JavaFileObject builderFile = processingEnv.getFiler()
	        			  .createSourceFile(className + ".mdsl" );
	        		PrintWriter out = new PrintWriter(builderFile.openWriter());
	        		out.write("API description for " + className + "TODO");
	        		out.close();
	        	}
	        	catch(Exception e) {
	        		System.err.print(e);
	        	}
	        	aE = aEIt.next();
	        }
		}
//		Class<?> nextClass = object.getClass();
//	    if (!nextClass.isAnnotationPresent(ServiceEndpoint.class)) {
//	    }
		return true;
	}
}

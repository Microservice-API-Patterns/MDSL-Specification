package io.mdsl.utils;

//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.Reader;
//
import io.mdsl.apiDescription.ServiceSpecification;

public class MDSLStandaloneParserApplication {

	public static void main(String[] args) {
		
		// Reader r;
		// try {
			// r = new FileReader(args[0]);
			// MDSLXtextParserWrapper parser = new MDSLXtextParserWrapper();
			// ServiceSpecification mdslTree = (ServiceSpecification) parser.parse(r);
			ServiceSpecification mdslTree = MDSLParser.parse(args[0]);
			System.out.println ("MDSL specification parsed, API name is: " + mdslTree.getName());
		// } catch (FileNotFoundException e) {
		//	e.printStackTrace();
		// } catch (IOException e) {
		//	e.printStackTrace();
		// }
	}
}

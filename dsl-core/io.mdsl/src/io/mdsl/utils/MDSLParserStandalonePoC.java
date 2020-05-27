package io.mdsl.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import io.mdsl.apiDescription.serviceSpecification;

public class MDSLParserStandalonePoC {

	public static void main(String[] args) {
		Reader r;
		try {
			r = new FileReader("HelloMDSLWorld.mdsl");
			
			MDSLXtextParserWrapper parser = new MDSLXtextParserWrapper();
			serviceSpecification mdslTree = (serviceSpecification) parser.parse(r);
			System.out.println ("MDSL spec parsed, name is: " + mdslTree.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

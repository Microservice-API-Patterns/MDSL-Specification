package io.mdsl.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.exception.MDSLException;

public class MDSLParser {

	public static ServiceSpecification parse(String mdslFile) throws MDSLException {
		Reader r;
		try {
			r = new FileReader(mdslFile);
			MDSLXtextParserWrapper parser = new MDSLXtextParserWrapper();
			return (ServiceSpecification) parser.parse(r);
		} catch (FileNotFoundException e) {
			throw new MDSLException("spec" + mdslFile + " not found.", e);
		} catch (IOException e) {
			throw new MDSLException("spec" + mdslFile + " caused IOException.", e);
		}
	}
}

package io.mdsl.utils;

import io.mdsl.exception.MDSLException;

public class MDSLLogger {
	
	// TODO (future work) set from outside (Eclipse preference, CLI parameter, tbd for MDSL Web)
	public static int logLevel = 1; // -1=off, 0=errors, 1=warn, 2=inform, 3=all (could use enum) 
	public static boolean reportPatternUsage = true;
	
	public static void reportError(String message) {		
		if(MDSLLogger.logLevel>=0) {
			System.err.println("[E] " + message);
		}
		throw new MDSLException(message);
	}

	public static void reportWarning(String message) {
		if(MDSLLogger.logLevel>=1) {
			System.err.println("[W] " + message);
		}
	}
	
	public static void reportInformation(String message) {
		if(MDSLLogger.logLevel>=2) {
			System.err.println("[I] " + message);
		}
	}
	
	public static void reportDetailedInformation(String message) {
		if(MDSLLogger.logLevel>=3) {
			System.err.println("[D] " + message);
		}
	}
}

package org.junit.gen5.engine.junit5.execution;

import java.util.*;

/**
 * 
 * very early thoughts...
 * 
 * This class is intended to facilitate reporting of different kinds of data to its calling environment.
 * Such data would include measurements (execution time, number of successful and failed assertions), 
 * report items provided explicitly by the user (say, via a @ReportData Map test method argument),
 * and report items obtained from the parameter resolution process.
 * 
 * Thread safety might become an issue for parallel execution strategies.
 * 
 * @since 5.0
 */
public class TestReportData {

	
	private long duration;


	// successful and failed assertions
	private int assertionSuccessCount;
	private int assertionFailureCount;


	// obtained from a @ReportData Map test method argument
	private Map<String, String> userProvidedReportItems; 

	
	//no Serializable needed as intended solely for reporting and _not_ for rerunning tests - possible duplication here?
	private Map<String, String> injectedParameterItems; 
	
	
	//is this the right place?
	private Set<String> tags;	
	



}

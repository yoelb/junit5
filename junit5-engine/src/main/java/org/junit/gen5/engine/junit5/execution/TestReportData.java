/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.util.*;

/**
 * 
 * very early thoughts...
 *
 * General question:
 * should such a reporting data container be attached to the testdescriptor or
 * rather just be injected into the various callbacks of org.junit.gen5.engine.TestExecutionListener
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

	//measured in milliseconds
	private long duration;

	// successful and failed assertions

	// needs some form of instrumentation of the assertion process
	private int assertionSuccessCount;

	// > 0 for aggregated asserts
	private int assertionFailureCount;

	// obtained from a @ReportData Map test method argument
	private Map<String, String> userProvidedReportItems;

	//no Serializable needed as intended solely for reporting and _not_ for rerunning tests - possible duplication here?
	private Map<String, String> injectedParameterItems;

	//is this the right place?
	private Set<String> tags;

}

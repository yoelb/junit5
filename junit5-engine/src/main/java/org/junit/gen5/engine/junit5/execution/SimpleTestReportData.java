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

import org.junit.gen5.api.extension.*;

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
public class SimpleTestReportData implements TestReportData {

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
	//we might want to preserve the order - map not ideal?
	private Map<String, String> injectedParameterItems = new HashMap<String, String>();

	//is this the right place?
	private Set<String> tags;

	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public int getAssertionSuccessCount() {
		return assertionSuccessCount;
	}

	@Override
	public int getAssertionFailureCount() {
		return assertionFailureCount;
	}

	@Override
	public Map<String, String> getUserProvidedReportItems() {
		return userProvidedReportItems;
	}

	@Override
	public Map<String, String> getInjectedParameterItems() {
		return injectedParameterItems;
	}

	@Override
	public Set<String> getTags() {
		return tags;
	}

	@Override
	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public void setAssertionSuccessCount(int assertionSuccessCount) {
		this.assertionSuccessCount = assertionSuccessCount;
	}

	@Override
	public void setAssertionFailureCount(int assertionFailureCount) {
		this.assertionFailureCount = assertionFailureCount;
	}

	@Override
	public void setUserProvidedReportItems(Map<String, String> userProvidedReportItems) {
		this.userProvidedReportItems = userProvidedReportItems;
	}

	@Override
	public void setInjectedParameterItems(Map<String, String> injectedParameterItems) {
		this.injectedParameterItems = injectedParameterItems;
	}

	@Override
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

}

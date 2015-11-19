/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.util.*;

/**
 * @since 5.0
 */

public interface TestReportData {

	long getDuration();

	int getAssertionSuccessCount();

	int getAssertionFailureCount();

	Map<String, String> getUserProvidedReportItems();

	Map<String, String> getInjectedParameterItems();

	Set<String> getTags();

	void setDuration(long duration);

	void setAssertionSuccessCount(int assertionSuccessCount);

	void setAssertionFailureCount(int assertionFailureCount);

	void setUserProvidedReportItems(Map<String, String> userProvidedReportItems);

	void setInjectedParameterItems(Map<String, String> injectedParameterItems);

	void setTags(Set<String> tags);
}

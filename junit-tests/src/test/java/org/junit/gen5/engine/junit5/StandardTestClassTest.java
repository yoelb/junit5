/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.*;

import org.junit.Assert;
import org.junit.gen5.api.Test;

public class StandardTestClassTest extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void executeTestsForClass() {
		TrackingTestExecutionListener listener = executeTestsForClass(MyStandardTestCase.class, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
	}

}

class MyStandardTestCase {

	@Test
	void succeedingTest() {

		assertTrue(true);

	}

}

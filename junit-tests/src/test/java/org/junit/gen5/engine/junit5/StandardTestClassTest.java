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
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;

public class StandardTestClassTest extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void allTestsInClassAreRunWithBeforeEach() {
		TrackingTestExecutionListener listener = executeTestsForClass(MyStandardTestCase.class, 4);

		Assert.assertEquals("# tests started", 3, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 2, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());

		Assert.assertEquals("# before each calls", 3, MyStandardTestCase.countBefore);
	}

	@org.junit.Test
	public void testsFailWhenBeforeEachFails() {
		TrackingTestExecutionListener listener = executeTestsForClass(TestCaseWithFailingBefore.class, 3);

		Assert.assertEquals("# tests started", 2, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());

		Assert.assertEquals("# before each calls", 2, TestCaseWithFailingBefore.countBefore);
	}

}

class MyStandardTestCase {

	static int countBefore = 0;

	@BeforeEach
	void before() {
		countBefore++;
	}

	@Test
	void succeedingTest1() {
		assertTrue(true);
	}

	@Test
	void succeedingTest2() {
		assertTrue(true);
	}

	@Test
	void failingTest() {
		fail("always fails");
	}

}

class TestCaseWithFailingBefore {

	static int countBefore = 0;

	@BeforeEach
	void before() {
		countBefore++;
		throw new RuntimeException("Problem during setup");
	}

	@Test
	void test1() {
	}

	@Test
	void test2() {
	}

}

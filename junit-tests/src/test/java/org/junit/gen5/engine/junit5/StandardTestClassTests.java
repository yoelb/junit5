/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder.forClass;
import static org.junit.gen5.engine.dsl.TestPlanSpecificationBuilder.testPlanSpecification;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder;
import org.opentest4j.TestAbortedException;

/**
 * Testing execution in standard test cases {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class StandardTestClassTests extends AbstractJUnit5TestEngineTests {

	@BeforeEach
	public void init() {
		MyStandardTestCase.countBefore1 = 0;
		MyStandardTestCase.countBefore2 = 0;
		MyStandardTestCase.countAfter = 0;
	}

	@Test
	public void standardTestClassIsCorrectlyDiscovered() {
		TestPlanSpecification spec = testPlanSpecification().withElements(forClass(MyStandardTestCase.class)).build();
		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void moreThanOneTestClassIsCorrectlyDiscovered() {
		TestPlanSpecification spec = testPlanSpecification().withElements(
			ClassTestPlanSpecificationElementBuilder.forClass(SecondOfTwoTestCases.class)).build();

		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(2 + 2, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void moreThanOneTestClassIsExecuted() {
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			ClassTestPlanSpecificationElementBuilder.forClass(SecondOfTwoTestCases.class)).build();

		executeTests(testPlanSpecification);

		assertEquals(3, tracker.testStartedCount.get(), "# tests started");
		assertEquals(3, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(2, tracker.containerStartedCount.get(), "# containers started");
		assertEquals(2, tracker.containerFinishedCount.get(), "# containers finished");
	}

	@Test
	public void allTestsInClassAreRunWithBeforeEach() {
		executeTestsForClass(MyStandardTestCase.class);

		assertEquals(4, tracker.testStartedCount.get(), "# tests started");
		assertEquals(2, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(1, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(2, tracker.containerStartedCount.get(), "# containers started");
		assertEquals(2, tracker.containerFinishedCount.get(), "# containers finished");

		assertEquals(4, MyStandardTestCase.countBefore1, "# before1 calls");
		assertEquals(4, MyStandardTestCase.countBefore2, "# before2 calls");
	}

	@Test
	public void allTestsInClassAreRunWithAfterEach() {
		executeTestsForClass(MyStandardTestCase.class);

		assertEquals(4, tracker.testStartedCount.get(), "# tests started");
		assertEquals(4, MyStandardTestCase.countAfter, "# after each calls");

		assertEquals(2, tracker.containerStartedCount.get(), "# containers started");
		assertEquals(2, tracker.containerFinishedCount.get(), "# containers finished");
	}

	@Test
	public void testsFailWhenBeforeEachFails() {
		executeTestsForClass(TestCaseWithFailingBefore.class);

		assertEquals(2, tracker.testStartedCount.get(), "# tests started");
		assertEquals(0, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(2, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(2, tracker.containerStartedCount.get(), "# containers started");
		assertEquals(2, tracker.containerFinishedCount.get(), "# containers finished");

		assertEquals(2, TestCaseWithFailingBefore.countBefore, "# before each calls");
	}

	@Test
	public void testsFailWhenAfterEachFails() {
		executeTestsForClass(TestCaseWithFailingAfter.class);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(0, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(2, tracker.containerStartedCount.get(), "# containers started");
		assertEquals(2, tracker.containerFinishedCount.get(), "# containers finished");

		assertTrue(TestCaseWithFailingAfter.testExecuted, "test executed?");
	}

	private static class MyStandardTestCase {

		static int countBefore1 = 0;
		static int countBefore2 = 0;
		static int countAfter = 0;

		@BeforeEach
		void before1() {
			countBefore1++;
		}

		@BeforeEach
		void before2() {
			countBefore2++;
		}

		@AfterEach
		void after() {
			countAfter++;
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

		@Test
		void abortedTest() {
			throw new TestAbortedException("aborted!");
		}

	}

	private static class FirstOfTwoTestCases {

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

	private static class SecondOfTwoTestCases {

		@Test
		void succeedingTest1() {
			assertTrue(true);
		}

		@Test
		void succeedingTest2() {
			assertTrue(true);
		}

		@Test
		void succeedingTest3() {
			assertTrue(true);
		}

	}

	private static class TestCaseWithFailingBefore {

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

	private static class TestCaseWithFailingAfter {

		static boolean testExecuted = false;

		@AfterEach
		void after() {
			throw new RuntimeException("Problem during 'after'");
		}

		@Test
		void test1() {
			testExecuted = true;
		}

	}

}

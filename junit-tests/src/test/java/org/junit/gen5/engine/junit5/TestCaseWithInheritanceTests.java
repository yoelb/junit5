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
import static org.junit.gen5.api.Assumptions.assumeTrue;
import static org.junit.gen5.engine.dsl.TestPlanSpecificationBuilder.testPlanSpecification;
import static org.junit.gen5.engine.dsl.UniqueIdTestPlanSpecificationElementBuilder.forUniqueId;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.dsl.UniqueIdTestPlanSpecificationElementBuilder;

/**
 * Testing execution in test case hierarchy {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class TestCaseWithInheritanceTests extends AbstractJUnit5TestEngineTests {

	@BeforeEach
	void initStatics() {
		LocalTestCase.countBeforeInvoked = 0;
		LocalTestCase.countAfterInvoked = 0;
		AbstractTestCase.countSuperBeforeInvoked = 0;
		AbstractTestCase.countSuperAfterInvoked = 0;
	}

	@Test
	public void executeAllTestsInClass() {
		LocalTestCase.countAfterInvoked = 0;

		executeTestsForClass(LocalTestCase.class);

		assertEquals(6, tracker.testStartedCount.get(), "# tests started");
		assertEquals(3, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(1, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(2, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(6, LocalTestCase.countBeforeInvoked, "# before calls");
		assertEquals(6, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(6, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(6, AbstractTestCase.countSuperAfterInvoked, "# super after calls");
	}

	@Test
	public void executeSingleTest() {
		TestPlanSpecification spec = testPlanSpecification().withElements(forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.TestCaseWithInheritanceTests$LocalTestCase#alwaysPasses()")).build();

		executeTests(spec);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, tracker.testFailedCount.get(), "# tests failed");
	}

	@Test
	public void executeTestDeclaredInSuperClass() {
		TestPlanSpecification spec = testPlanSpecification().withElements(forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.TestCaseWithInheritanceTests$LocalTestCase#superclassTest()")).build();

		executeTests(spec);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(1, LocalTestCase.countBeforeInvoked, "# after calls");
		assertEquals(1, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(1, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(1, AbstractTestCase.countSuperAfterInvoked, "# super after calls");

	}

	@Test
	public void executeTestWithExceptionThrownInAfterMethod() {
		TestPlanSpecification spec = testPlanSpecification().withElements(forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.TestCaseWithInheritanceTests$LocalTestCase#throwExceptionInAfterMethod()")).build();

		executeTests(spec);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(0, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, tracker.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, tracker.testAbortedCount.get(), "# tests aborted");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");
	}

	// -------------------------------------------------------------------

	private static abstract class AbstractTestCase {

		static int countSuperBeforeInvoked = 0;
		static int countSuperAfterInvoked = 0;

		@BeforeEach
		void superBefore() {
			countSuperBeforeInvoked++;
		}

		@AfterEach
		void superAfter() {
			countSuperAfterInvoked++;
		}

		@Test
		void superclassTest() {
			/* no-op */
		}
	}

	private static class LocalTestCase extends AbstractTestCase {

		boolean throwExceptionInAfterMethod = false;

		static int countBeforeInvoked = 0;
		static int countAfterInvoked = 0;

		@BeforeEach
		void before() {
			countBeforeInvoked++;
			// Reset state, since the test instance is retained across all test methods;
			// otherwise, after() always throws an exception.
			this.throwExceptionInAfterMethod = false;
		}

		@AfterEach
		void after() {
			countAfterInvoked++;
			if (this.throwExceptionInAfterMethod) {
				throw new RuntimeException("Exception thrown from @AfterEach method");
			}
		}

		@Test
		void otherTest() {
			/* no-op */
		}

		@Test
		void throwExceptionInAfterMethod() {
			this.throwExceptionInAfterMethod = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}

		@Test
		void aborted() {
			assumeTrue(false);
		}

		@Test
		void alwaysFails() {
			fail("#fail");
		}

	}

}

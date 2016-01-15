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
import static org.junit.gen5.engine.specification.dsl.ClassTestPlanSpecificationElementBuilder.forClass;
import static org.junit.gen5.engine.specification.dsl.TestPlanSpecificationBuilder.testPlanSpecification;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@linkplain Nested nested contexts}
 * in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class NestedTestClassesTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void nestedTestsAreCorrectlyDiscovered() {
		TestPlanSpecification spec = testPlanSpecification().withElements(forClass(TestCaseWithNesting.class)).build();
		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void nestedTestsAreExecuted() {
		executeTestsForClass(TestCaseWithNesting.class);

		assertEquals(3, tracker.testStartedCount.get(), "# tests started");
		assertEquals(2, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(3, tracker.containerStartedCount.get(), "# containers started");
		assertEquals(3, tracker.containerFinishedCount.get(), "# containers finished");
	}

	@Test
	public void doublyNestedTestsAreCorrectlyDiscovered() {
		TestPlanSpecification spec = testPlanSpecification().withElements(
			forClass(TestCaseWithDoubleNesting.class)).build();
		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(8, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void doublyNestedTestsAreExecuted() {
		executeTestsForClass(TestCaseWithDoubleNesting.class);

		assertEquals(5, tracker.testStartedCount.get(), "# tests started");
		assertEquals(3, tracker.testSucceededCount.get(), "# tests succeeded");
		assertEquals(2, tracker.testFailedCount.get(), "# tests failed");

		assertEquals(4, tracker.containerStartedCount.get(), "# containers started");
		assertEquals(4, tracker.containerFinishedCount.get(), "# containers finished");

		assertAll("before each counts", //
			() -> assertEquals(5, TestCaseWithDoubleNesting.beforeTopCount),
			() -> assertEquals(4, TestCaseWithDoubleNesting.beforeNestedCount),
			() -> assertEquals(2, TestCaseWithDoubleNesting.beforeDoublyNestedCount));

		assertAll("after each counts", //
			() -> assertEquals(5, TestCaseWithDoubleNesting.afterTopCount),
			() -> assertEquals(4, TestCaseWithDoubleNesting.afterNestedCount),
			() -> assertEquals(2, TestCaseWithDoubleNesting.afterDoublyNestedCount));

	}

	// -------------------------------------------------------------------

	private static class TestCaseWithNesting {

		@Test
		void someTest() {
		}

		@Nested
		class NestedTestCase {

			@Test
			void successful() {
			}

			@Test
			void failing() {
				Assertions.fail("Something went horribly wrong");
			}
		}
	}

	static private class TestCaseWithDoubleNesting {

		static int beforeTopCount = 0;
		static int beforeNestedCount = 0;
		static int beforeDoublyNestedCount = 0;

		static int afterTopCount = 0;
		static int afterNestedCount = 0;
		static int afterDoublyNestedCount = 0;

		@BeforeEach
		void beforeTop() {
			beforeTopCount++;
		}

		@AfterEach
		void afterTop() {
			afterTopCount++;
		}

		@Test
		void someTest() {
		}

		@Nested
		class NestedTestCase {

			@BeforeEach
			void beforeNested() {
				beforeNestedCount++;
			}

			@AfterEach
			void afterNested() {
				afterNestedCount++;
			}

			@Test
			void successful() {
			}

			@Test
			void failing() {
				Assertions.fail("Something went horribly wrong");
			}

			@Nested
			class DoublyNestedTestCase {

				@BeforeEach
				void beforeDoublyNested() {
					beforeDoublyNestedCount++;
				}

				@BeforeEach
				void afterDoublyNested() {
					afterDoublyNestedCount++;
				}

				@Test
				void successful() {
				}

				@Test
				void failing() {
					Assertions.fail("Something went horribly wrong");
				}
			}
		}
	}

}

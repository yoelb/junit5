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

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.ExecutionEventConditions.*;
import static org.junit.gen5.engine.TestExecutionResultConditions.*;
import static org.junit.gen5.engine.dsl.MethodTestPlanSpecificationElementBuilder.forMethod;
import static org.junit.gen5.engine.dsl.TestPlanSpecificationBuilder.testPlanSpecification;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.dsl.MethodTestPlanSpecificationElementBuilder;
import org.opentest4j.AssertionFailedError;

/**
 * Integration tests that verify correct exception handling in the {@link JUnit5TestEngine}.
 */
public class ExceptionHandlingTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void failureInTestMethodIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("failingTest");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		executeTests(testPlanSpecification);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		Throwable failure = tracker.throwables.get(0);
		assertEquals(AssertionFailedError.class, failure.getClass());
		assertEquals("always fails", failure.getMessage());
	}

	@Test
	public void uncheckedExceptionInTestMethodIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("testWithUncheckedException");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		executeTests(testPlanSpecification);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		Throwable failure = tracker.throwables.get(0);
		assertEquals(RuntimeException.class, failure.getClass());
		assertEquals("unchecked", failure.getMessage());
	}

	@Test
	public void checkedExceptionInTestMethodIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("testWithCheckedException");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		executeTests(testPlanSpecification);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		Throwable failure = tracker.throwables.get(0);
		assertEquals(IOException.class, failure.getClass());
		assertEquals("checked", failure.getMessage());
	}

	@Test
	public void checkedExceptionInBeforeEachIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("succeedingTest");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		FailureTestCase.exceptionToThrowInBeforeEach = Optional.of(new IOException("checked"));

		executeTests(testPlanSpecification);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		Throwable failure = tracker.throwables.get(0);
		assertEquals(IOException.class, failure.getClass());
		assertEquals("checked", failure.getMessage());
	}

	@Test
	public void checkedExceptionInAfterEachIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("succeedingTest");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		FailureTestCase.exceptionToThrowInAfterEach = Optional.of(new IOException("checked"));

		executeTests(testPlanSpecification);

		assertEquals(1, tracker.testStartedCount.get(), "# tests started");
		assertEquals(1, tracker.testFailedCount.get(), "# tests failed");

		Throwable failure = tracker.throwables.get(0);
		assertEquals(IOException.class, failure.getClass());
		assertEquals("checked", failure.getMessage());
	}

	@Test
	public void checkedExceptionInAfterEachIsSuppressedByExceptionInTest() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("testWithUncheckedException");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		FailureTestCase.exceptionToThrowInAfterEach = Optional.of(new IOException("checked"));

		executeTests(testPlanSpecification);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(FailureTestCase.class), started()), //
			event(test("testWithUncheckedException"), started()), //
			event(test("testWithUncheckedException"),
				finishedWithFailure(allOf( //
					isA(RuntimeException.class), //
					message("unchecked"), //
					suppressed(0, allOf(isA(IOException.class), message("checked")))))), //
			event(container(FailureTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	public void checkedExceptionInBeforeAllIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("succeedingTest");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		FailureTestCase.exceptionToThrowInBeforeAll = Optional.of(new IOException("checked"));

		executeTests(testPlanSpecification);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(FailureTestCase.class), started()), //
			event(container(FailureTestCase.class),
				finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	public void checkedExceptionInAfterAllIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("succeedingTest");
		TestPlanSpecification testPlanSpecification = testPlanSpecification().withElements(
			forMethod(FailureTestCase.class, method)).build();

		FailureTestCase.exceptionToThrowInAfterAll = Optional.of(new IOException("checked"));

		executeTests(testPlanSpecification);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(FailureTestCase.class), started()), //
			event(test("succeedingTest"), started()), //
			event(test("succeedingTest"), finishedSuccessfully()), //
			event(container(FailureTestCase.class),
				finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(engine(), finishedSuccessfully()));
	}

	@AfterEach
	public void cleanUpExceptions() {
		FailureTestCase.exceptionToThrowInBeforeAll = Optional.empty();
		FailureTestCase.exceptionToThrowInAfterAll = Optional.empty();
		FailureTestCase.exceptionToThrowInBeforeEach = Optional.empty();
		FailureTestCase.exceptionToThrowInAfterEach = Optional.empty();
	}

	private static class FailureTestCase {

		static Optional<Throwable> exceptionToThrowInBeforeAll = Optional.empty();
		static Optional<Throwable> exceptionToThrowInAfterAll = Optional.empty();
		static Optional<Throwable> exceptionToThrowInBeforeEach = Optional.empty();
		static Optional<Throwable> exceptionToThrowInAfterEach = Optional.empty();

		@BeforeAll
		static void beforeAll() throws Throwable {
			if (exceptionToThrowInBeforeAll.isPresent())
				throw exceptionToThrowInBeforeAll.get();
		}

		@AfterAll
		static void afterAll() throws Throwable {
			if (exceptionToThrowInAfterAll.isPresent())
				throw exceptionToThrowInAfterAll.get();
		}

		@BeforeEach
		void beforeEach() throws Throwable {
			if (exceptionToThrowInBeforeEach.isPresent())
				throw exceptionToThrowInBeforeEach.get();
		}

		@AfterEach
		void afterEach() throws Throwable {
			if (exceptionToThrowInAfterEach.isPresent())
				throw exceptionToThrowInAfterEach.get();
		}

		@Test
		void succeedingTest() {
		}

		@Test
		void failingTest() {
			Assertions.fail("always fails");
		}

		@Test
		void testWithUncheckedException() {
			throw new RuntimeException("unchecked");
		}

		@Test
		void testWithCheckedException() throws IOException {
			throw new IOException("checked");
		}

	}

}

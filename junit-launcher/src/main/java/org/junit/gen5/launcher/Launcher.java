/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.logging.Logger;

import org.junit.gen5.engine.*;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Facade for <em>discovering</em> and <em>executing</em> tests using
 * dynamically registered test engines.
 *
 * <p>Test engines are registered at runtime using the
 * {@link java.util.ServiceLoader ServiceLoader} facility. For that purpose, a
 * text file named {@code META-INF/services/org.junit.gen5.engine.TestEngine}
 * has to be added to the engine's JAR file in which the fully qualified name
 * of the implementation class of the {@link TestEngine} interface is stated.
 *
 * <p>Discovering or executing tests requires a {@link TestPlanSpecification}
 * which is passed to all registered engines. Each engine decides which tests
 * it can discover and later execute according to this specification.
 *
 * <p>Users of this class may optionally call {@link #discover} prior to
 * {@link #execute} in order to inspect the {@link TestPlan} before executing
 * it.
 *
 * <p>Prior to executing tests, users of this class should
 * {@linkplain #registerTestExecutionListeners register} one or more
 * {@link TestExecutionListener} instances in order to get feedback about the
 * progress and results of test execution. Listeners are notified of events
 * in the order in which they were registered.
 *
 * @since 5.0
 * @see TestPlanSpecification
 * @see TestPlan
 * @see TestExecutionListener
 */
public class Launcher {

	private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

	private final TestExecutionListenerRegistry listenerRegistry = new TestExecutionListenerRegistry();

	private final TestEngineRegistry testEngineRegistry;

	public Launcher() {
		this(new ServiceLoaderTestEngineRegistry());
	}

	// for tests only
	Launcher(TestEngineRegistry testEngineRegistry) {
		this.testEngineRegistry = testEngineRegistry;
	}

	/**
	 * Register one or more listeners for test execution.
	 *
	 * @param listeners the listeners to be notified of test execution events
	 */
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		listenerRegistry.registerListener(listeners);
	}

	/**
	 * Discover tests and build a {@link TestPlan} according to the supplied
	 * {@link TestPlanSpecification} by querying all registered engines and
	 * collecting their results.
	 *
	 * @param specification the specification to be resolved
	 * @return a {@code TestPlan} that contains all resolved
	 * {@linkplain TestIdentifier identifiers} from all registered engines
	 */
	public TestPlan discover(TestPlanSpecification specification) {
		return TestPlan.from(discoverRootDescriptor(specification, "discovery"));
	}

	/**
	 * Execute a {@link TestPlan} which is built according to the supplied
	 * {@link TestPlanSpecification} by querying all registered engines and
	 * collecting their results, and notify {@linkplain #registerTestExecutionListeners
	 * registered listeners} about the progress and results of the execution.
	 *
	 * @param specification the specification to be resolved
	 */
	public void execute(TestPlanSpecification specification) {
		execute(discoverRootDescriptor(specification, "execution"));
	}

	private RootTestDescriptor discoverRootDescriptor(TestPlanSpecification specification, String phase) {
		RootTestDescriptor root = new RootTestDescriptor();
		for (TestEngine testEngine : testEngineRegistry.getTestEngines()) {
			LOG.fine(() -> String.format("Discovering tests during launcher %s phase in engine '%s'.", phase,
				testEngine.getId()));
			EngineAwareTestDescriptor engineRoot = testEngine.discoverTests(specification);
			root.addChild(engineRoot);
		}
		root.applyFilters(specification);
		root.prune();
		return root;
	}

	private void execute(RootTestDescriptor root) {
		TestPlan testPlan = TestPlan.from(root);
		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();
		testExecutionListener.testPlanExecutionStarted(testPlan);
		ExecutionListenerAdapter engineExecutionListener = new ExecutionListenerAdapter(testPlan,
			testExecutionListener);
		for (TestEngine testEngine : root.getTestEngines()) {
			TestDescriptor testDescriptor = root.getTestDescriptorFor(testEngine);
			testEngine.execute(new ExecutionRequest(testDescriptor, engineExecutionListener));
		}
		testExecutionListener.testPlanExecutionFinished(testPlan);
	}

	static class ExecutionListenerAdapter implements EngineExecutionListener {

		private final TestPlan testPlan;
		private final TestExecutionListener testExecutionListener;

		public ExecutionListenerAdapter(TestPlan testPlan, TestExecutionListener testExecutionListener) {
			this.testPlan = testPlan;
			this.testExecutionListener = testExecutionListener;
		}

		@Override
		public void dynamicTestRegistered(TestDescriptor testDescriptor) {
			TestIdentifier testIdentifier = TestIdentifier.from(testDescriptor);
			testPlan.add(testIdentifier);
			testExecutionListener.dynamicTestRegistered(testIdentifier);
		}

		@Override
		public void executionStarted(TestDescriptor testDescriptor) {
			testExecutionListener.executionStarted(getTestIdentifier(testDescriptor));
		}

		@Override
		public void executionSkipped(TestDescriptor testDescriptor, String reason) {
			testExecutionListener.executionSkipped(getTestIdentifier(testDescriptor), reason);
		}

		@Override
		public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
			testExecutionListener.executionFinished(getTestIdentifier(testDescriptor), testExecutionResult);
		}

		private TestIdentifier getTestIdentifier(TestDescriptor testDescriptor) {
			return testPlan.getTestIdentifier(new TestId(testDescriptor.getUniqueId()));
		}
	}

}

/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.extension.AfterAllExtensionPoint;
import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeAllExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.Container;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.ConditionEvaluator;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.RegisteredExtensionPoint;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.gen5.engine.junit5.descriptor.MethodInvocationContextFactory.methodInvocationContext;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 * <p>
 * The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name}</code>.
 *
 * @since 5.0
 */
public class ClassTestDescriptor extends JUnit5TestDescriptor implements Container<JUnit5EngineExecutionContext> {

	private final String displayName;

	private final Class<?> testClass;

	ClassTestDescriptor(String uniqueId, Class<?> testClass) {
		super(uniqueId);

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.displayName = determineDisplayName(testClass, testClass.getName());

		setSource(new JavaSource(testClass));
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	@Override
	public final String getName() {
		return getTestClass().getName();
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	@Override
	public final Set<TestTag> getTags() {
		return getTags(this.testClass);
	}

	@Override
	public final boolean isTest() {
		return false;
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) {
		TestExtensionRegistry newExtensionRegistry = populateNewTestExtensionRegistryFromExtendWith(testClass,
			context.getTestExtensionRegistry());

		registerBeforeAllMethods(newExtensionRegistry);
		registerAfterAllMethods(newExtensionRegistry);
		registerBeforeEachMethods(newExtensionRegistry);
		registerAfterEachMethods(newExtensionRegistry);

		context = context.extend().withTestExtensionRegistry(newExtensionRegistry).build();

		ContainerExtensionContext containerExtensionContext = new ClassBasedContainerExtensionContext(
			context.getExtensionContext(), this);

		// @formatter:off
		return context.extend()
				.withTestInstanceProvider(testInstanceProvider(context))
				.withExtensionContext(containerExtensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JUnit5EngineExecutionContext context) throws Exception {
		ConditionEvaluationResult evaluationResult = new ConditionEvaluator().evaluateForContainer(
			context.getTestExtensionRegistry(), (ContainerExtensionContext) context.getExtensionContext());
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse(""));
		}
		return SkipResult.dontSkip();
	}

	@Override
	public JUnit5EngineExecutionContext beforeAll(JUnit5EngineExecutionContext context) throws Exception {
		TestExtensionRegistry extensionRegistry = context.getTestExtensionRegistry();
		ContainerExtensionContext containerExtensionContext = (ContainerExtensionContext) context.getExtensionContext();

		invokeBeforeAllExtensionPoints(extensionRegistry, containerExtensionContext);

		return context;
	}

	@Override
	public JUnit5EngineExecutionContext afterAll(JUnit5EngineExecutionContext context) throws Exception {
		ThrowableCollector throwableCollector = new ThrowableCollector();

		throwableCollector.execute(() -> invokeAfterAllExtensionPoints(context.getTestExtensionRegistry(),
			(ContainerExtensionContext) context.getExtensionContext(), throwableCollector));

		throwableCollector.assertEmpty();

		return context;
	}

	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext context) {
		return () -> ReflectionUtils.newInstance(testClass);
	}

	private void invokeBeforeAllExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			ContainerExtensionContext containerExtensionContext) throws Exception {

		Consumer<RegisteredExtensionPoint<BeforeAllExtensionPoint>> applyBeforeEach = registeredExtensionPoint -> {
			executeAndMaskThrowable(() -> {
                //TODO: Should that be in a central place?
				((InstanceAwareExtensionContext) containerExtensionContext).setCurrentExtension(
					registeredExtensionPoint.getExtensionInstance());
				registeredExtensionPoint.getExtensionPoint().beforeAll(containerExtensionContext);
			});
		};

		newTestExtensionRegistry.stream(BeforeAllExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(applyBeforeEach);
	}

	private void invokeAfterAllExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			ContainerExtensionContext containerExtensionContext, ThrowableCollector throwableCollector)
					throws Exception {

		Consumer<RegisteredExtensionPoint<AfterAllExtensionPoint>> applyAfterAll = registeredExtensionPoint -> {
			throwableCollector.execute(
				() -> registeredExtensionPoint.getExtensionPoint().afterAll(containerExtensionContext));
		};

		newTestExtensionRegistry.stream(AfterAllExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.BACKWARD).forEach(applyAfterAll);
	}

	private void registerBeforeAllMethods(TestExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, BeforeAll.class, BeforeAllExtensionPoint.class,
			this::assertStatic, this::synthesizeBeforeAllExtensionPoint);
	}

	private void registerAfterAllMethods(TestExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, AfterAll.class, AfterAllExtensionPoint.class,
			this::assertStatic, this::synthesizeAfterAllExtensionPoint);
	}

	private void registerBeforeEachMethods(TestExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, BeforeEach.class, BeforeEachExtensionPoint.class,
			this::assertNonStatic, this::synthesizeBeforeEachExtensionPoint);
	}

	private void registerAfterEachMethods(TestExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, AfterEach.class, AfterEachExtensionPoint.class,
			this::assertNonStatic, this::synthesizeAfterEachExtensionPoint);
	}

	private void registerAnnotatedMethodsAsExtensions(TestExtensionRegistry extensionRegistry,
			Class<? extends Annotation> annotationType, Class<?> extensionType,
			BiConsumer<Class<?>, Method> methodValidator,
			BiFunction<TestExtensionRegistry, Method, ExtensionPoint> extensionPointSynthesizer) {

		// @formatter:off
		findAnnotatedMethods(testClass, annotationType, MethodSortOrder.HierarchyDown).stream()
			.peek(method -> methodValidator.accept(extensionType, method))
			.forEach(method ->
				extensionRegistry.registerExtension(extensionPointSynthesizer.apply(extensionRegistry, method),
					ExtensionPoint.Position.DEFAULT, method));
		// @formatter:on
	}

	private BeforeAllExtensionPoint synthesizeBeforeAllExtensionPoint(TestExtensionRegistry registry, Method method) {
		return (BeforeAllExtensionPoint) extensionContext -> {
			new MethodInvoker(extensionContext, registry).invoke(methodInvocationContext(null, method));
		};
	}

	private AfterAllExtensionPoint synthesizeAfterAllExtensionPoint(TestExtensionRegistry registry, Method method) {
		return (AfterAllExtensionPoint) extensionContext -> {
			new MethodInvoker(extensionContext, registry).invoke(methodInvocationContext(null, method));
		};
	}

	private BeforeEachExtensionPoint synthesizeBeforeEachExtensionPoint(TestExtensionRegistry registry, Method method) {
		return (BeforeEachExtensionPoint) extensionContext -> {
			runMethodInTestExtensionContext(method, extensionContext, registry);
		};
	}

	private AfterEachExtensionPoint synthesizeAfterEachExtensionPoint(TestExtensionRegistry registry, Method method) {
		return (AfterEachExtensionPoint) extensionContext -> {
			runMethodInTestExtensionContext(method, extensionContext, registry);
		};
	}

	private void runMethodInTestExtensionContext(Method method, TestExtensionContext context,
			TestExtensionRegistry registry) {

		// @formatter:off
		ReflectionUtils.getOuterInstance(context.getTestInstance(), method.getDeclaringClass())
			.ifPresent(instance -> new MethodInvoker(context, registry).invoke(methodInvocationContext(instance, method)));
		// @formatter:on
	}

	private void assertStatic(Class<?> extensionType, Method method) {
		if (!ReflectionUtils.isStatic(method)) {
			String message = String.format("Cannot register method '%s' as a(n) %s since it is not static.",
				method.getName(), extensionType.getSimpleName());
			throw new ExtensionConfigurationException(message);
		}
	}

	private void assertNonStatic(Class<?> extensionType, Method method) {
		if (ReflectionUtils.isStatic(method)) {
			String message = String.format("Cannot register method '%s' as a(n) %s since it is static.",
				method.getName(), extensionType.getSimpleName());
			throw new ExtensionConfigurationException(message);
		}
	}

}

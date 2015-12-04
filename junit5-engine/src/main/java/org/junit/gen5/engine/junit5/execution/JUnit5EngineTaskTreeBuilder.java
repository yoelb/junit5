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

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Executable;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

public class JUnit5EngineTaskTreeBuilder {
	private final JUnit5EngineDescriptor jUnit5EngineDescriptor;

	public JUnit5EngineTaskTreeBuilder(JUnit5EngineDescriptor jUnit5EngineDescriptor) {
		this.jUnit5EngineDescriptor = jUnit5EngineDescriptor;
	}

	public Executable buildTaskTree(TestExecutionListener testExecutionListener) {
		ClassTestDescriptor classDescriptor = (ClassTestDescriptor) jUnit5EngineDescriptor.getChildren().iterator().next();

		// @formatter:off
		List<Executable> methodTasks = classDescriptor.getChildren().stream()
                .map(descriptor -> (MethodTestDescriptor) descriptor)
                .map(methodDescriptor -> createTestMethodTask(methodDescriptor, classDescriptor, testExecutionListener))
                .collect(toList());
        // @formatter:on

		return new TaskList(methodTasks);
	}

	private Executable createTestMethodTask(MethodTestDescriptor methodDescriptor, ClassTestDescriptor classDescriptor,
			TestExecutionListener testExecutionListener) {

		Object testInstance = createTestInstance(classDescriptor);
		Executable beforeEachTasks = createBeforeEachTasks(classDescriptor, testInstance);
		Executable testAndAfterEachTasks = createTestAndAfterEachTask(methodDescriptor, classDescriptor, testInstance);

		Executable testMethodTask = new TestMethodTask(beforeEachTasks, testAndAfterEachTasks, testExecutionListener,
			methodDescriptor);
		Consumer<Throwable> exceptionHandler = exception -> testExecutionListener.testFailed(methodDescriptor,
			exception);
		return new FailureHandlingTask(testMethodTask, exceptionHandler);
	}

	private Executable createTestAndAfterEachTask(MethodTestDescriptor methodDescriptor,
			ClassTestDescriptor classDescriptor, Object testInstance) {
		Executable methodTask = createRawMethodTask(methodDescriptor.getTestMethod(), testInstance);
		List<Executable> testAndAfterEach = new ArrayList<>();
		testAndAfterEach.add(methodTask);
		testAndAfterEach.addAll(createAfterEachTasks(classDescriptor, testInstance));
		return new ExceptionCollectingTaskList(testAndAfterEach);
	}

	private Object createTestInstance(ClassTestDescriptor classDescriptor) {
		return ReflectionUtils.newInstance(classDescriptor.getTestClass());
	}

	private TaskList createBeforeEachTasks(ClassTestDescriptor classDescriptor, Object testInstance) {
		List<Method> beforeEaches = AnnotationUtils.findAnnotatedMethods(classDescriptor.getTestClass(),
			BeforeEach.class, ReflectionUtils.MethodSortOrder.HierarchyDown);
		List<Executable> beforeEachTaskList = beforeEaches.stream().map(
			method -> createRawMethodTask(method, testInstance)).collect(toList());
		return new TaskList(beforeEachTaskList);
	}

	private List<Executable> createAfterEachTasks(ClassTestDescriptor classDescriptor, Object testInstance) {
		List<Method> beforeEaches = AnnotationUtils.findAnnotatedMethods(classDescriptor.getTestClass(),
			AfterEach.class, ReflectionUtils.MethodSortOrder.HierarchyUp);
		return beforeEaches.stream().map(method -> createRawMethodTask(method, testInstance)).collect(toList());
	}

	private Executable createRawMethodTask(Method rawMethod, Object target) {
		return new MethodTask(rawMethod, target);
	}

}

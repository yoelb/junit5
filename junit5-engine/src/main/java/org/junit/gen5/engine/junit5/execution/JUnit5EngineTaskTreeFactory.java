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
import java.util.List;
import java.util.function.Consumer;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Executable;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

public class JUnit5EngineTaskTreeFactory {
	private final JUnit5EngineDescriptor jUnit5EngineDescriptor;

	public JUnit5EngineTaskTreeFactory(JUnit5EngineDescriptor jUnit5EngineDescriptor) {
		this.jUnit5EngineDescriptor = jUnit5EngineDescriptor;
	}

	public Executable createTaskTree(TestExecutionListener testExecutionListener) {
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
		Object testInstance = ReflectionUtils.newInstance(classDescriptor.getTestClass());
		Method testMethod = methodDescriptor.getTestMethod();
		MethodTask methodTask = createRawMethodTask(testMethod, testInstance);
		List<Executable> beforeEachTaskList = createBeforeEachTaskList(classDescriptor, testInstance);
		TaskList beforeTasks = new TaskList(beforeEachTaskList);
		TestMethodTask testMethodTask = new TestMethodTask(methodTask, beforeTasks, testExecutionListener,
			methodDescriptor);
		Consumer<Throwable> exceptionHandler = exception -> testExecutionListener.testFailed(methodDescriptor,
			exception);
		return new FailureHandlingTask(testMethodTask, exceptionHandler);
	}

	private MethodTask createRawMethodTask(Method rawMethod, Object target) {
		return new MethodTask(rawMethod, target);
	}

	private List<Executable> createBeforeEachTaskList(ClassTestDescriptor classDescriptor, Object target) {
		List<Method> beforeEaches = AnnotationUtils.findAnnotatedMethods(classDescriptor.getTestClass(),
			BeforeEach.class, ReflectionUtils.MethodSortOrder.HierarchyDown);
		return beforeEaches.stream().map(method -> createRawMethodTask(method, target)).collect(toList());
	}

}

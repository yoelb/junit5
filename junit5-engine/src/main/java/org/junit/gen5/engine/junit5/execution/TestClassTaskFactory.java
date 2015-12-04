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

import java.lang.reflect.Method;

import org.junit.gen5.api.Executable;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.junit5.TaskFactory;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

public class TestClassTaskFactory implements TaskFactory {

	private final ClassTestDescriptor classTestDescriptor;

	public TestClassTaskFactory(ClassTestDescriptor classTestDescriptor) {
		this.classTestDescriptor = classTestDescriptor;
	}

	@Override
	public Executable createWith(TestExecutionListener testExecutionListener) {
		MethodTestDescriptor methodDescriptor = (MethodTestDescriptor) this.classTestDescriptor.getChildren().iterator().next();
		Method testMethod = methodDescriptor.getTestMethod();
		Object target = ReflectionUtils.newInstance(this.classTestDescriptor.getTestClass());
		MethodTask methodTask = new MethodTask(testMethod, target);
		return new TestMethodTask(methodTask, testExecutionListener, methodDescriptor);
	}
}

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
		MethodTestDescriptor methodDescriptor = (MethodTestDescriptor) classDescriptor.getChildren().iterator().next();
		Method testMethod = methodDescriptor.getTestMethod();
		Object target = ReflectionUtils.newInstance(classDescriptor.getTestClass());
		MethodTask methodTask = new MethodTask(testMethod, target);
		return new TestMethodTask(methodTask, testExecutionListener, methodDescriptor);
	}
}

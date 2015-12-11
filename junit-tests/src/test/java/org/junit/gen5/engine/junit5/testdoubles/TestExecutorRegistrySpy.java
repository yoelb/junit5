/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testdoubles;

import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.executor.ExecutionContext;
import org.junit.gen5.engine.junit5ext.executor.TestExecutor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutorRegistry;

public class TestExecutorRegistrySpy implements TestExecutorRegistry {
	public List<TestDescriptor> testDescriptors = new LinkedList<>();

	@Override
	public void register(TestExecutor testExecutor) {
	}

	@Override
	public void executeAll(ExecutionContext context) {
		testDescriptors.add(context.getTestDescriptor());
	}
}
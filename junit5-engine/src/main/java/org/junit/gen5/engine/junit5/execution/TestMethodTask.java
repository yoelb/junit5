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

import org.junit.gen5.api.Executable;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;

public class TestMethodTask implements Executable {

	private final MethodTask methodTask;
	private final TestExecutionListener testExecutionListener;
	private final TestDescriptor testDescriptor;

	public TestMethodTask(MethodTask methodTask, TestExecutionListener testExecutionListener,
			TestDescriptor testDescriptor) {
		this.methodTask = methodTask;
		this.testExecutionListener = testExecutionListener;
		this.testDescriptor = testDescriptor;
	}

	@Override
	public void execute() throws Throwable {
		this.testExecutionListener.testStarted(this.testDescriptor);
		this.methodTask.execute();
		this.testExecutionListener.testSucceeded(this.testDescriptor);
	}

}

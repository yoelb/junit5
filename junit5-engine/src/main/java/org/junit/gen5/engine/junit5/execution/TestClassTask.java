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

import org.junit.gen5.api.*;
import org.junit.gen5.engine.*;

public class TestClassTask implements Executable {

	private final TaskList methodTasks;

	//do we need to call the listener at this level? new callbacks needed?
	private final TestExecutionListener testExecutionListener;
	private final TestDescriptor testDescriptor;

	public TestClassTask(TaskList methodTasks, TestExecutionListener testExecutionListener,
			TestDescriptor testDescriptor) {
		this.methodTasks = methodTasks;
		this.testExecutionListener = testExecutionListener;
		this.testDescriptor = testDescriptor;
	}

	@Override
	public void execute() throws Throwable {
		//TODO: before and after; call execution listener?

		this.methodTasks.execute();
	}

}

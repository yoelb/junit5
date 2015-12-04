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

import java.util.function.Consumer;

import org.junit.gen5.api.Executable;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;

public class FailureHandlingTask implements Executable {
	private final Executable task;
	private final Consumer<Throwable> handleException;

	public FailureHandlingTask(Executable task, Consumer<Throwable> handleException) {
		this.task = task;
		this.handleException = handleException;
	}

	@Override
	public void execute() throws Throwable {
		try {
			task.execute();
		}
		catch (Throwable throwable) {
			handleException.accept(throwable);
		}
	}

}

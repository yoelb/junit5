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

import java.util.ArrayList;
import java.util.List;

import org.junit.gen5.api.Executable;

public class ExceptionCollectingTaskList implements Executable {
	private final List<Executable> tasks;
	private final List<Throwable> exceptionCollector;

	public ExceptionCollectingTaskList(List<Executable> tasks) {
		this.tasks = tasks;
		this.exceptionCollector = new ArrayList<>();
	}

	@Override
	public void execute() throws Throwable {
		for (Executable task : this.tasks) {
			try {
				task.execute();
			}
			catch (Throwable throwable) {
				exceptionCollector.add(throwable);
			}
		}
		if (!exceptionCollector.isEmpty())
			throw createExceptionToThrow();
	}

	private Throwable createExceptionToThrow() {
		Throwable throwable = exceptionCollector.remove(0);
		exceptionCollector.stream().forEach(throwable::addSuppressed);
		return throwable;
	}

}

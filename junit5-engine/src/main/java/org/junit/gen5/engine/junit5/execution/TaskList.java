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

import java.util.List;

import org.junit.gen5.api.Executable;

public class TaskList implements Executable {

	final List<Executable> children;

	public TaskList(List<Executable> children) {
		this.children = children;
	}

	@Override
	public void execute() throws Throwable {

		this.children.stream().forEach(executable -> {
			try {
				executable.execute();
			}
			catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		});

	}

}

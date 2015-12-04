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

public class MethodTask implements Executable {

	final Method method;
	final Object target;

	public MethodTask(Method method, Object target) {
		this.method = method;
		this.target = target;
	}

	@Override
	public void execute() throws Throwable {

		ReflectionUtils.invokeMethod(this.method, this.target);

	}

}

/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

public class JUnit5EngineDescriptor extends JUnit5TestDescriptor {

	public JUnit5EngineDescriptor(String uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getDisplayName() {
		return "Engine: " + getUniqueId();
	}

	@Override
	public boolean isTest() {
		return false;
	}
}

/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.specification;

import java.io.File;

import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.TestPlanSpecificationElementVisitor;

public class AllTestsSpecification implements TestPlanSpecificationElement {
	private final File classpathRoot;

	public AllTestsSpecification(File classpathRoot) {
		this.classpathRoot = classpathRoot;
	}

	@Override
	public void accept(TestPlanSpecificationElementVisitor visitor) {
		visitor.visitAllTests(classpathRoot);
	}

	public File getClasspathRoot() {
		return classpathRoot;
	}
}

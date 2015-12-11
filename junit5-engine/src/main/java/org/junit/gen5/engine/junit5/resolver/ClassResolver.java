/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElementVisitor;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

public class ClassResolver implements TestResolver {
	@Override
	public List<TestDescriptor> resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
		ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

		if (parent.isRoot()) {
			return resolveAllClassesFromSpecification(parent, testPlanSpecification);
		}
		else {
			return Collections.emptyList();
		}
	}

	private List<TestDescriptor> resolveAllClassesFromSpecification(TestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		List<TestDescriptor> result = new LinkedList<>();

		testPlanSpecification.accept(new TestPlanSpecificationElementVisitor() {
			@Override
			public void visitClass(Class<?> testClass) {
				result.add(getTestGroupForClass(parent, testClass));
			}
		});

		return result;
	}

	private TestDescriptor getTestGroupForClass(TestDescriptor parentTestDescriptor, Class<?> testClass) {
		String parentUniqueId = parentTestDescriptor.getUniqueId();
		String uniqueId = String.format("%s:%s", parentUniqueId, testClass.getCanonicalName());

		ClassTestDescriptor testDescriptor = new ClassTestDescriptor(uniqueId, testClass);
		parentTestDescriptor.addChild(testDescriptor);
		return testDescriptor;
	}
}
/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.List;

import org.junit.gen5.api.Executable;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.EngineFilter;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.SpecificationResolver;

public class JUnit5TestEngine implements TestEngine {
	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return "junit5";
	}

	@Override
	public TestDescriptor discoverTests(TestPlanSpecification specification) {
		Preconditions.notNull(specification, "specification must not be null");
		JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(this.getId());
		resolveSpecification(specification, engineDescriptor);
		return engineDescriptor;
	}

	private void resolveSpecification(TestPlanSpecification specification, JUnit5EngineDescriptor engineDescriptor) {
		SpecificationResolver resolver = new SpecificationResolver(engineDescriptor);
		for (TestPlanSpecificationElement element : specification) {
			resolver.resolveElement(element);
		}
		applyEngineFilters(specification.getEngineFilters(), engineDescriptor);
	}

	private void applyEngineFilters(List<EngineFilter> engineFilters, JUnit5EngineDescriptor engineDescriptor) {
		// TODO Currently only works with a single ClassFilter
		if (engineFilters.isEmpty()) {
			return;
		}
		ClassFilter filter = (ClassFilter) engineFilters.get(0);
		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (descriptor.getClass() == ClassTestDescriptor.class) {
				ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) descriptor;
				if (!filter.acceptClass(classTestDescriptor.getTestClass()))
					remove.run();
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

	@Override
	public void execute(ExecutionRequest request) {

		TestExecutionListener testExecutionListener = request.getTestExecutionListener();

		//TODO: check explicitly
		JUnit5TestDescriptor rootTestDescriptor = (JUnit5TestDescriptor) request.getRootTestDescriptor();

		Executable rootExecutable = rootTestDescriptor.getTaskFactory().createWith(testExecutionListener);
		try {
			rootExecutable.execute();
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

}
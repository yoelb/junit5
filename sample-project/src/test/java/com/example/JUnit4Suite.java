/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
public class JUnit4Suite {

	public static TestPlanSpecification createSpecification() {
		return TestPlanSpecification.build(TestPlanSpecification.forClassName(SampleTestCase.class.getName()));
	}
}
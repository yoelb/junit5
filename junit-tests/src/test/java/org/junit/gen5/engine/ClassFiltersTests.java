/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.api.Assertions.*;

import java.util.Collection;
import java.util.StringJoiner;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.specification.PredicateBasedClassFilter;
import org.junit.gen5.engine.specification.dsl.ClassFilters;

class ClassFiltersTests {

	@Test
	void classNameMatches() {
		String regex = "^java\\.lang\\..*";

		ClassFilter filter = ClassFilters.classNameMatches(regex);

		assertEquals("Filter class names with regular expression: " + regex, filter.getDescription());
		assertTrue(filter.acceptClass(String.class));
		assertFalse(filter.acceptClass(Collection.class));
	}

	@Test
	void anyClass() {
		ClassFilter filter = ClassFilters.anyClass();

		assertEquals("Any class", filter.getDescription());
		assertTrue(filter.acceptClass(String.class));
		assertTrue(filter.acceptClass(this.getClass()));
	}

	@Test
	void allOfWithoutFilter() {
		ClassFilter[] noFilters = {};

		ClassFilter filter = ClassFilters.allOf(noFilters);

		assertEquals("Any class", filter.getDescription());
		assertTrue(filter.acceptClass(String.class));
		assertTrue(filter.acceptClass(Object.class));
	}

	@Test
	void allOfWithSingleFilter() {
		ClassFilter singleFilter = ClassFilters.classNameMatches(".*ring.*");

		ClassFilter filter = ClassFilters.allOf(singleFilter);

		assertSame(singleFilter, filter);
	}

	@Test
	void allOfWithMultipleFiltersIsConjunction() {
		ClassFilter firstFilter = ClassFilters.classNameMatches(".*ring.*");
		ClassFilter secondFilter = ClassFilters.classNameMatches(".*Join.*");

		ClassFilter filter = ClassFilters.allOf(firstFilter, secondFilter);

		assertFalse(filter.acceptClass(String.class));
		assertTrue(filter.acceptClass(StringJoiner.class));
	}

	@Test
	void allOfWithMultipleFiltersHasReadableDescription() {
		ClassFilter firstFilter = new PredicateBasedClassFilter(o -> false, () -> "1st");
		ClassFilter secondFilter = new PredicateBasedClassFilter(o -> true, () -> "2nd");

		ClassFilter filter = ClassFilters.allOf(firstFilter, secondFilter);

		assertEquals("(1st) and (2nd)", filter.getDescription());
	}

}

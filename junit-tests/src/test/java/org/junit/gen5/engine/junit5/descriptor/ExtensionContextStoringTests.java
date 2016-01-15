/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.api.Assertions.*;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtensionContext.Visibility;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
public class ExtensionContextStoringTests {

	private ClassTestDescriptor childDescriptor;
	private ClassTestDescriptor parentDescriptor;
	private ClassBasedContainerExtensionContext parentContext;
	private ClassBasedContainerExtensionContext childContext;
	private Object currentExtension;

	@BeforeEach
	void init() {
		childDescriptor = nestedClassDescriptor();
		parentDescriptor = outerClassDescriptor(childDescriptor);
		parentContext = new ClassBasedContainerExtensionContext(null, parentDescriptor);
		childContext = new ClassBasedContainerExtensionContext(parentContext, childDescriptor);

		currentExtension = new Object();
		childContext.setCurrentExtension(currentExtension);
	}

	@Nested
	class DefaultVisibility {

		@Test
		void valueCanBeRetrievedAndChanged() {
			assertNull(parentContext.get("a key"));
			parentContext.store("a key", "a value");
			assertEquals("a value", parentContext.get("a key"));

			parentContext.store("a key", "other value");
			assertEquals("other value", parentContext.get("a key"));
		}

		@Test
		void valueIsVisibleInChildButNotInParent() {
			parentContext.store("a key", "a value");
			assertEquals("a value", childContext.get("a key"));

			childContext.store("a key", "other value");
			assertEquals("other value", childContext.get("a key"));
			assertEquals("a value", parentContext.get("a key"));
		}

		@Test
		void valuesNotVisibleInOtherExtension() {
			parentContext.store("a key", "a value");
			parentContext.setCurrentExtension(new Object());
			assertNull(parentContext.get("a key"));

			parentContext.setCurrentExtension(currentExtension);
			assertEquals("a value", parentContext.get("a key"));
		}

		@Test
		void getWithDefaultWillStoreTheValue() {
			String value = (String) parentContext.getWithDefault("a key", key -> "a value for " + key);

			assertEquals("a value for a key", value);
			assertEquals("a value for a key", parentContext.get("a key"));
		}
	}

	@Nested
	class LocalVisibility {

		@Test
		void valueCanBeRetrievedAndChanged() {
			assertNull(parentContext.get("a key"));
			parentContext.store("a key", "a value", Visibility.LOCAL);
			assertEquals("a value", parentContext.get("a key"));

			parentContext.store("a key", "other value", Visibility.LOCAL);
			assertEquals("other value", parentContext.get("a key"));
		}

		@Test
		void valueFromParentIsNotVisibleInChild() {
			parentContext.store("a key", "a value", Visibility.LOCAL);
			assertNull(childContext.get("a key"));

			childContext.store("a key", "other value");
			assertEquals("other value", childContext.get("a key"));
			assertEquals("a value", parentContext.get("a key"));
		}

		@Test
		void valuesNotVisibleInOtherExtension() {
			parentContext.store("a key", "a value");
			parentContext.setCurrentExtension(new Object());
			assertNull(parentContext.get("a key"));

			parentContext.setCurrentExtension(currentExtension);
			assertEquals("a value", parentContext.get("a key"));
		}

		@Test
		void getWithDefaultWillStoreTheValue() {
			String value = (String) parentContext.getWithDefault("a key", key -> "a value for " + key,
				Visibility.LOCAL);

			assertEquals("a value for a key", value);
			assertEquals("a value for a key", parentContext.get("a key"));
		}
	}

	@Test
	void sameKeyCannotBeUsedWithDifferentVisibility() {
		parentContext.store("a key", "a value", Visibility.DEFAULT);

		RuntimeException thrown = expectThrows(RuntimeException.class,
			() -> parentContext.store("a key", "a value", Visibility.LOCAL));
	}

	//local visibility

	//extension visibility

	//global visibility

	private ClassTestDescriptor nestedClassDescriptor() {
		return new ClassTestDescriptor("NestedClass", OuterClass.NestedClass.class);
	}

	private ClassTestDescriptor outerClassDescriptor(TestDescriptor child) {
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor("OuterClass", OuterClass.class);
		if (child != null)
			classTestDescriptor.addChild(child);
		return classTestDescriptor;
	}

	static class OuterClass {

		class NestedClass {

		}
	}

}

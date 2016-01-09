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

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.Store;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
public class ExtensionContextStoreTests {

	@Test
	public void storeWithLocalScope() {
		ClassTestDescriptor outerClassDescriptor = outerClassDescriptor(null);

		Object extensionInstance = new Object();

		ClassBasedContainerExtensionContext parentContext = new ClassBasedContainerExtensionContext(null,
			outerClassDescriptor);
		parentContext.setCurrentExtension(extensionInstance);
		Store<String> aStore = parentContext.getStore(String.class, "a key", Store.Scope.LOCAL);

		assertNull(aStore.get());
		aStore.set("a value");
		assertEquals("a value", aStore.get());

		Store<String> sameStore = parentContext.getStore(String.class, "a key", Store.Scope.LOCAL);
		assertSame(sameStore, aStore);
		assertEquals("a value", aStore.get());
	}

	//store with local scope not visible in child

	//store with local scope not visible in other extension

	//error: same store with different scope

	//error: same store with different type

	//error: store with default scope

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

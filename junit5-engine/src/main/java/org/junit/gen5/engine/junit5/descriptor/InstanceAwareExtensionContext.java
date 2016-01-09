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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.Store;
import org.junit.gen5.api.extension.Store.Scope;
import org.junit.gen5.commons.util.Preconditions;

abstract class InstanceAwareExtensionContext implements ExtensionContext {

	private final Map<String, Object> attributes = new HashMap<>();

	private final ExtensionContext parent;

	protected final Map<String, Store> stores = new HashMap<>();
	private Object currentExtension = null;

	InstanceAwareExtensionContext(ExtensionContext parent) {
		this.parent = parent;
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public Object getAttribute(String key) {
		Object value = attributes.get(key);
		if (value == null && parent != null)
			return parent.getAttribute(key);
		return value;
	}

	@Override
	public void putAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	@Override
	public Object removeAttribute(String key) {
		return attributes.remove(key);
	}

	@Override
	public <T> Store<T> getStore(Class<T> type, String key, Scope scope) {
		Preconditions.notBlank(key, "A key is needed");
		Preconditions.notNull(this.currentExtension, "The current extension must be set for stores to work.");

		Store<T> existingStore = stores.get(key);
		if (existingStore == null) {
			existingStore = new LocalStore<T>();
			stores.put(key, existingStore);
		}

		return existingStore;
	}

	public void setCurrentExtension(Object currentExtension) {
		this.currentExtension = currentExtension;
	}

	private static class LocalStore<T> implements Store<T> {

		T value;

		@Override
		public T get() {
			return value;
		}

		@Override
		public void set(T value) {
			this.value = value;
		}
	}
}

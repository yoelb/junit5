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
import org.junit.gen5.commons.util.Preconditions;

abstract class InstanceAwareExtensionContext implements ExtensionContext {

	private final Map<String, Object> attributes = new HashMap<>();

	private final ExtensionContext parent;

	private Object currentExtension = null;
	private Map<Object, Store> stores = new HashMap<>();

	protected InstanceAwareExtensionContext(ExtensionContext parent) {
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
	public void store(Object key, Object value, Visibility visibility) {
		Preconditions.notNull(this.currentExtension, "current extension must be set");

		if (stores.containsKey(key) && stores.get(key).visibility != visibility) {
			String message = String.format("Key '%s' used with conflicting visibilities [%s, %s] in extension '%s'",
				key.toString(), stores.get(key).visibility, visibility, currentExtension);
			throw new RuntimeException(message);
		}

		Store newStore = new Store(value, currentExtension, visibility);
		stores.put(key, newStore);
	}

	@Override
	public Object get(Object key) {
		Preconditions.notNull(this.currentExtension, "current extension must be set");

		if (stores.containsKey(key) && stores.get(key).extensionInstance == currentExtension) {
			return stores.get(key).value;
		}
		else {
			if (getInstanceAwareParent().isPresent()) {
				return getInstanceAwareParent().get().get(key);
			}
		}
		return null;
	}

	protected Optional<InstanceAwareExtensionContext> getInstanceAwareParent() {
		if (parent != null && parent instanceof InstanceAwareExtensionContext) {
			return Optional.of((InstanceAwareExtensionContext) parent);
		}
		else {
			return Optional.empty();
		}
	}

	public void setCurrentExtension(Object currentExtension) {
		this.currentExtension = currentExtension;
		getInstanceAwareParent().ifPresent(parent -> parent.setCurrentExtension(currentExtension));
	}

	private static class Store {
		private final Object extensionInstance;
		private final Visibility visibility;
		private final Object value;

		private Store(Object value, Object extensionInstance, Visibility visibility) {
			this.value = value;
			this.extensionInstance = extensionInstance;
			this.visibility = visibility;
		}
	}
}

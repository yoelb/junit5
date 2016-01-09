/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

public interface Store<T> {
	T get();

	void set(T value);

	enum Scope {
		DEFAULT, //visible only in this extension but inherited from ancestor extension contexts
		EXTENSION, //visible only in this extension but visible in the full tree of extension contexts
		LOCAL, //only visible in this extension and this extension context
		GLOBAL //visible in all extensions on this level and in ancestor extension contexts
	}
}

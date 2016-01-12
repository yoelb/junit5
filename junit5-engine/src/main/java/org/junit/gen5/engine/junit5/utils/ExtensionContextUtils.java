/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.utils;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.engine.junit5.descriptor.InstanceAwareExtensionContext;
import org.junit.gen5.engine.junit5.execution.RegisteredExtensionPoint;

public class ExtensionContextUtils {

	public static void setExtensionInstanceInContext(
			RegisteredExtensionPoint<? extends ExtensionPoint> registeredExtensionPoint,
			ExtensionContext extensionContext) {
		if (extensionContext instanceof ExtensionPoint)
			((InstanceAwareExtensionContext) extensionContext).setCurrentExtension(
				registeredExtensionPoint.getExtensionInstance());
	}

}

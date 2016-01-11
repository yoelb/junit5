/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.meta;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
// TODO Check if SOURCE is sufficient
@Retention(RetentionPolicy.CLASS)
@Documented
@API(Internal)
public @interface API {

	Usage value();

	enum Usage {

		Deprecated,

		Internal,

		Experimental,

		Maintained,

		Stable

	}

}

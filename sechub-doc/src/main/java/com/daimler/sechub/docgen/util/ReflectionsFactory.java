// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import com.daimler.sechub.docgen.reflections.Reflections;

public class ReflectionsFactory {

    private static Reflections INSTANCE = new Reflections();

    private ReflectionsFactory() {

    }

/* @formatter:off */
	public static Reflections create() {
	    /* we not create but use shard instance */
	    return INSTANCE;
	}
	/* @formatter:on */
}

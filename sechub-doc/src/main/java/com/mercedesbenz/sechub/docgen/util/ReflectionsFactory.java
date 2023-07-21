// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import com.mercedesbenz.sechub.docgen.reflections.Reflections;

public class ReflectionsFactory {

    private static Reflections INSTANCE = new Reflections();

    private ReflectionsFactory() {

    }

/* @formatter:off */
	public static Reflections create() {
	    /* we not create but use shared instance */
	    return INSTANCE;
	}
	/* @formatter:on */
}

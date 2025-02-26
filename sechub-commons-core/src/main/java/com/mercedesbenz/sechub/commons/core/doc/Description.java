// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A special annotation to describe parts to document
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface Description {

    /**
     * The description what the documented part is used for
     *
     * @return description
     */
    String value();
}

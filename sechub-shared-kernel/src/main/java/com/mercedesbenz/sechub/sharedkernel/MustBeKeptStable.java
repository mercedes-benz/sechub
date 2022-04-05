// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

/**
 * Marker interface for parts given to user as official API, being machine
 * readable and so must be kept stable. <strong>So changes must be backward
 * compatible!</strong>
 *
 * @author Albert Tregnaghi
 *
 */
public @interface MustBeKeptStable {

    /**
     * A description why this must be kept stable
     *
     * @return description
     */
    String value() default "";
}

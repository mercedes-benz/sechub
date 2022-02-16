// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for all other domain messaging annotations. This is used
 * for automated documentation!
 *
 * @author Albert Tregnaghi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface DomainMessaging {

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.reflections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class is for testing only - because sechub-doc.jar is never part of
 * delivery it doesn't matter. Why inside /src/main/java and not inside
 * /src/test/java ? Because our Reflections class will only lookup for java
 * source code inside main/java and we do not want to have a configuration here
 * which changes for testing.
 *
 * @author Albert Tregnaghi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ReflectionsExampleDefinitionAnnotation {

}

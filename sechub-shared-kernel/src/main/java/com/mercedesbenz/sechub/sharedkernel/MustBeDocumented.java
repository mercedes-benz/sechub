// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A marker interface for code parts which HAS TO BE documented. E.g. when
 * having a special setup attribute necessary to be described in documented for
 * correct application startup.<br>
 * <br>
 *
 * Following counter parts in conjunction with this annotation will result in
 * <b>automated documentation</b>:
 * <ul>
 * <li>org.springframework.beans.annotation.Value</li> will be settled in
 * gen_systemproperties.adoc
 * <li>org.springframework.scheduling.annotation.Scheduled</li> will be found in
 * gen_scheduling.adoc
 * </ul>
 * Other parts are currently only a marker. Just implement more, to have more
 * documentation generated...
 *
 * @author Albert Tregnaghi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MustBeDocumented {

    /**
     * A description what the documented part is used for
     *
     * @return description
     */
    String value() default "";

    /**
     * The scope name for the documentation - default fallback value is
     * {@link DocumentationScopeConstants#SCOPE_USE_DEFINED_CLASSNAME_LOWERCASED}
     * (will result in a generated scope name depending on annotated class package).
     * The scope information is used for generating documentation and separate
     * groups etc.
     *
     * @return scope
     */
    String scope() default DocumentationScopeConstants.SCOPE_USE_DEFINED_CLASSNAME_LOWERCASED;

    /**
     * When <code>true</code> the information of this annotation must be handled
     * secret. Interesting for generation
     *
     * @return <code>true</code> when secret
     */
    boolean secret() default false;

}

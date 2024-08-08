// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.io.IOException;

import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;

public interface PrepareWrapperModule {

    /**
     * Checks if the module is responsible to prepare.
     *
     * @param context the current context with data inside to check responsibility
     * @return <code>true</code> when responsible, <code>false</code> otherwise
     */
    boolean isResponsibleToPrepare(PrepareWrapperContext context);

    /**
     * Does prepare
     *
     * @param context current context
     * @throws IOException when preparation failed
     */
    void prepare(PrepareWrapperContext context) throws IOException;

    /**
     * Resolves user message which is send back to user when preparation was done by
     * module
     *
     * @return user message or <code>null</code> when no message shall be sent to
     *         user
     */
    String getUserMessageForPreparationDone();

    /**
     * Resolves if this module is enabled or not
     *
     * @return <code>true</code> when enabled, otherwise <code>false</code>
     */
    boolean isEnabled();

}

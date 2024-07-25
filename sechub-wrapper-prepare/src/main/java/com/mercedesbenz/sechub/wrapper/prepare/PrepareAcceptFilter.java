// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

public interface PrepareAcceptFilter {

    /**
     * Checks if it is possible to prepare by the given context informatoin
     *
     * @param context current context
     * @return <code>true</code> when prepare is possible, otherwise
     *         <code>false</code>
     */
    public boolean isAccepting(PrepareWrapperContext context);
}

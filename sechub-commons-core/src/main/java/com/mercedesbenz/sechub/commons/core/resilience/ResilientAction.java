// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

public interface ResilientAction<R> {

    /**
     * Action method which shall be executed in resilient way
     *
     * @return
     * @throws Exception
     */
    public R execute() throws Exception;
}

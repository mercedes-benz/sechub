// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.resilience;

public interface ActionWhichShallBeResilient<R> {

    /**
     * Action method which shall be executed in resilient way
     *
     * @return
     * @throws Exception
     */
    public R execute() throws Exception;
}

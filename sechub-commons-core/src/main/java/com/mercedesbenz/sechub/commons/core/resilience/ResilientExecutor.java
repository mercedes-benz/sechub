// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

public interface ResilientExecutor<T, R> {

    /**
     * Adds a consultant which gives proposals
     *
     * @param consultant
     */
    public void add(ResilienceConsultant consultant);

    /**
     * Execute target resilient
     *
     * @param target the target which shall be executed
     * @return result
     * @throws Exception - any unhandled exception
     */
    public default R executeResilient(T target) throws Exception {
        return executeResilient(target, null);
    }

    /**
     * Execute target resilient
     *
     * @param target
     * @param callback
     * @return result
     * @throws Exception - any unhandled exception
     */
    public R executeResilient(T target, ResilienceCallback callback) throws Exception;
}

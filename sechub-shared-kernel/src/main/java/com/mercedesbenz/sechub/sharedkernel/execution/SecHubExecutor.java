// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.execution;

/**
 * Execute something for sechub
 *
 * @author Albert Tregnaghi
 *
 * @param <T> result type
 * @param <P> given parameter
 */
public interface SecHubExecutor<T, P> {

    /**
     * Execute within SecHub execution context
     *
     * @param context
     * @return result, or <code>null</code> when execution was not possible
     * @throws SecHubExecutionException when any problems occuring
     */
    public T execute(SecHubExecutionContext context, P param) throws SecHubExecutionException;
}

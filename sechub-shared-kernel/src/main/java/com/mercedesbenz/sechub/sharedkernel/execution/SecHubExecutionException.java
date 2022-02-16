// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.execution;

public class SecHubExecutionException extends Exception {

    private static final long serialVersionUID = 8598361450155972170L;

    public SecHubExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecHubExecutionException(String message) {
        super(message);
    }

    /**
     * Throws given exception as SECHUB execution exception. If the exception is
     * already a SECHUB execution exception the origin SECHUB exception will be
     * thrown (the given message will be ignored then)
     *
     * @param message
     * @param e
     * @throws SecHubExecutionException
     */
    public static void throwAsSecHubExecutionException(String message, Exception e) throws SecHubExecutionException {
        if (e instanceof SecHubExecutionException) {
            throw (SecHubExecutionException) e;
        } else {
            throw new SecHubExecutionException(message, e);
        }
    }

}

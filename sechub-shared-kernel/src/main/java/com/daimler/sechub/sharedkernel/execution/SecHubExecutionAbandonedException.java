// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.execution;

public class SecHubExecutionAbandonedException extends SecHubExecutionException{

    private static final long serialVersionUID = -4955001873521948736L;

    public SecHubExecutionAbandonedException(SecHubExecutionContext context, String message, Exception cause) {
        super(message,cause);
        context.markAbandonded();
    }


}

package com.daimler.sechub.sharedkernel.execution;

public class SecHubExecutionAbandonedException extends SecHubExecutionException{

    private static final long serialVersionUID = -4955001873521948736L;

    public SecHubExecutionAbandonedException(String message, Exception cause) {
        super(message,cause);
    }


}

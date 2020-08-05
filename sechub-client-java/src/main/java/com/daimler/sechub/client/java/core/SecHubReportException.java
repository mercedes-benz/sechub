package com.daimler.sechub.client.java.core;

public class SecHubReportException extends Exception {
    
    private static final long serialVersionUID = -6503309024764618812L;

    public SecHubReportException(String message, Throwable cause) {
        super(message, cause);
    }

    protected SecHubReportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

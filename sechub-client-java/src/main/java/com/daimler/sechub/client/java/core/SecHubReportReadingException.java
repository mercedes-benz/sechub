package com.daimler.sechub.client.java.core;

/**
 * Represents an error happening while reading a sechub report
 * @author Jeremias Eppler, Albert Tregnaghi
 *
 */
public class SecHubReportReadingException extends SecHubReportException {
    private static final long serialVersionUID = 3145080055023035667L;

    public SecHubReportReadingException(String message, Throwable cause) {
        super(message, cause);
    }

    protected SecHubReportReadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

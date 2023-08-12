// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.pdsclient;

public class PDSClientException extends Exception {

    private static final long serialVersionUID = 1L;

    public PDSClientException(String message) {
        super(message);
    }

    public PDSClientException(String message, Throwable cause) {
        super(message, cause);
    }

}

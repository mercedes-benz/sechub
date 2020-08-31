// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds;

public class PDSJSONConverterException extends Exception {

    public PDSJSONConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public PDSJSONConverterException(String message) {
        super(message);
    }

    public PDSJSONConverterException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 5620610528856742295L;

}

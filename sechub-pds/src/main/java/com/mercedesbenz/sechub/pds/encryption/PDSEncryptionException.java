// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.encryption;

public class PDSEncryptionException extends Exception {

    private static final long serialVersionUID = 1L;

    public PDSEncryptionException(String message) {
        super(message);
    }

    public PDSEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

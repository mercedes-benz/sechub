// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

public class AES256EncryptionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AES256EncryptionException(String errMsg, Exception e) {
        super(errMsg, e);
    }
}

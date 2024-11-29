// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

public class AES256EncryptionException extends RuntimeException {

    public AES256EncryptionException(String errMsg, Exception e) {
        super(errMsg, e);
    }
}

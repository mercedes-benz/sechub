// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.encryption;

public class AES256EncryptionException extends RuntimeException {

    public AES256EncryptionException(String errMsg, Exception e) {
        super(errMsg, e);
    }
}

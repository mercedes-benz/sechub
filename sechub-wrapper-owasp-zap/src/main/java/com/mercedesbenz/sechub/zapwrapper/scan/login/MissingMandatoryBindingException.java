// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

public class MissingMandatoryBindingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MissingMandatoryBindingException(String message) {
        super(message);
    }
}

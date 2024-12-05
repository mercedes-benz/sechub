// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

public class ConfigurationFailureException extends Exception {

    private static final long serialVersionUID = -384180667154600386L;

    public ConfigurationFailureException(String message) {
        super(message);
    }

    public ConfigurationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
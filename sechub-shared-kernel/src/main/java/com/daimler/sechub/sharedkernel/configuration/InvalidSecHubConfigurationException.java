// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class InvalidSecHubConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 765872519353527865L;

    public InvalidSecHubConfigurationException(String message) {
        super(message);
    }
}

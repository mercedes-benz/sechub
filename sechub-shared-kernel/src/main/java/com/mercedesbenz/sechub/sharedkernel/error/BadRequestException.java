// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 4600354118569290009L;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable t) {
        super(message, t);
    }

}

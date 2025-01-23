// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {
    private static final long serialVersionUID = 8392017456124983765L;

    public InternalServerErrorException() {
        this("An internal server error occurred!");
    }

    public InternalServerErrorException(String message) {
        super(message);
    }
}

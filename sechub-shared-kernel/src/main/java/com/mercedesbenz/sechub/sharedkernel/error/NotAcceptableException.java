// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class NotAcceptableException extends RuntimeException {

    private static final long serialVersionUID = 5454520580803371252L;

    public NotAcceptableException(String message) {
        super(message);
    }

}

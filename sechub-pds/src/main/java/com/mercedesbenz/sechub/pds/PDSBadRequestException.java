// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PDSBadRequestException extends RuntimeException {

    private static final long serialVersionUID = 4600354118569290009L;

    public PDSBadRequestException(String message) {
        super(message);
    }

    public PDSBadRequestException(String message, Throwable t) {
        super(message, t);
    }

}

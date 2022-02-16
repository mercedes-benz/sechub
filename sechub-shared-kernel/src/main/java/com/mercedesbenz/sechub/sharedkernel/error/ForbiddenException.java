// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 9184322887033026055L;

    public ForbiddenException() {
        this("This action is forbidden");
    }

    public ForbiddenException(String message) {
        super(message);
    }

}

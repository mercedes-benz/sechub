// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 9184322887033026055L;

    public NotFoundException() {
        this("The wanted object was not found!");
    }

    public NotFoundException(String message) {
        super(message);
    }

}

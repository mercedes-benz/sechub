// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PDSNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -8862841800470932125L;

    public PDSNotFoundException(String message) {
        super(message);
    }

}

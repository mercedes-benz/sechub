// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotAuthorizedException extends RuntimeException {

    private static final long serialVersionUID = 5454520580803371252L;

    public NotAuthorizedException() {
        this("You do not have the authorization for this!");
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

}

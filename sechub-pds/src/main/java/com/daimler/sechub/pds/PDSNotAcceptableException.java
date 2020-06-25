package com.daimler.sechub.pds;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class PDSNotAcceptableException extends RuntimeException {

    private static final long serialVersionUID = -9007553619000082845L;

    public PDSNotAcceptableException(String message) {
        super(message);
    }

}

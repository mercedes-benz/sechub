package com.mercedesbenz.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class SameJsonHttpStatusExceptionServerErrorTestValidatorBuilder {
    private int status;
    private String error;
    private String message;
    private List<String> details = new ArrayList<>();

    public SameJsonHttpStatusExceptionServerErrorTestValidatorBuilder status(int status) {
        this.status = status;
        return this;
    }

    public SameJsonHttpStatusExceptionServerErrorTestValidatorBuilder status(HttpStatus status) {
        this.status = status.value();
        return this;
    }

    public SameJsonHttpStatusExceptionServerErrorTestValidatorBuilder error(String error) {
        this.error = error;
        return this;
    }

    public SameJsonHttpStatusExceptionServerErrorTestValidatorBuilder message(String message) {
        this.message = message;
        return this;
    }

    public SameJsonHttpStatusExceptionServerErrorTestValidatorBuilder addDetails(String detail) {
        this.details.add(detail);
        return this;
    }

    public SameJsonHttpStatusExceptionTestValidator build() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("status", Integer.valueOf(status));
        map.put("error", error);
        map.put("message", message);
        map.put("details", details.toArray(new String[details.size()]));

        return new SameJsonHttpStatusExceptionTestValidator(map);
    }

}
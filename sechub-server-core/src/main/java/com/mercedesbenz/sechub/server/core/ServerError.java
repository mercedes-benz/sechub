// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonInclude(Include.NON_NULL) // if trace is null we don't show it... so when not in debug mode nobody knows
                               // there can be a stacktrace...
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerError implements JSONable<ServerError> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerError.class);

    Integer status;
    String error;

    String message;
    List<String> details;
    String timeStamp;
    String trace;

    public ServerError(int status, Map<String, Object> errorAttributes) {
        this.status = status;
        this.error = (String) errorAttributes.get("error");
        this.message = (String) errorAttributes.get("message");

        this.timeStamp = errorAttributes.get("timestamp").toString();
        this.trace = (String) errorAttributes.get("trace");

        this.details = new ArrayList<>();

        Object errorObjects = errorAttributes.getOrDefault("errors", new ArrayList<>());

        if (errorObjects instanceof List) {
            List<?> list = (List<?>) errorObjects;

            for (Object object : list) {
                if (object instanceof FieldError) {
                    FieldError fieldError = (FieldError) object;

                    StringBuilder sb = new StringBuilder();
                    sb.append("Field '").append(fieldError.getField());
                    sb.append("' with value '" + fieldError.getRejectedValue());
                    sb.append("' was rejected. " + fieldError.getDefaultMessage());
                    details.add(sb.toString());
                } else if (object instanceof ObjectError) {
                    ObjectError objectError = (ObjectError) object;
                    details.add(objectError.getDefaultMessage());
                } else {
                    LOG.warn("Untreated object inside errors found:{}", object);
                }

            }
        } else {
            if (errorObjects != null) {
                LOG.error("Error objects is not a list or null but: {}", errorObjects);
            }
        }
    }

    @Override
    public Class<ServerError> getJSONTargetClass() {
        return ServerError.class;
    }

}
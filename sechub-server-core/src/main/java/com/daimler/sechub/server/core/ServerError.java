// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.validation.FieldError;

import com.daimler.sechub.commons.model.JSONable;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL) // if trace is null we don't show it... so when not in debug mode nobody knows there can be a stacktrace...
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerError implements JSONable<ServerError>{
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
        
        @SuppressWarnings("unchecked")
		List<FieldError> list = (List<FieldError>) errorAttributes.getOrDefault("errors", new ArrayList<>());
        this.details=new ArrayList<>();
        for (FieldError fieldError: list) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("Field '").append(fieldError.getField());
        	sb.append("' with value '"+fieldError.getRejectedValue());
        	sb.append("' was rejected. "+fieldError.getDefaultMessage());
        	details.add(sb.toString());
        }
        this.timeStamp = errorAttributes.get("timestamp").toString();
        this.trace = (String) errorAttributes.get("trace");
    }

	@Override
	public Class<ServerError> getJSONTargetClass() {
		return ServerError.class;
	}

}
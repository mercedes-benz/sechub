// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.core;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class SecHubExceptionHandler {

    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseBody
    public String handleFileUploadSizeExceeded(SizeLimitExceededException ex, HttpServletResponse response) {
        return commonHandleFileUploadSizeExceed(ex, response);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public String handleFileUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletResponse response) {
        return commonHandleFileUploadSizeExceed(ex, response);
    }
    
    @Bean
    RequestRejectedHandler requestRejectedHandler() {
       return new SecHubHttpStatusRequestRejectedHandler();
    }

    private String commonHandleFileUploadSizeExceed(Exception ex, HttpServletResponse response) {
        if (response == null) {
            throw new IllegalStateException("response missing");
        }
        if (ex == null) {
            throw new IllegalStateException("exception missing");
        }

        response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());

        return "File upload maximum reached. Please reduce your upload file size.";
    }
}

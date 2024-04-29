// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class PDSExceptionHandler {

    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseBody
    public Exception handleFileUploadSizeExceeded(SizeLimitExceededException ex, HttpServletResponse response) {
        return commonHandleFileUploadSizeExceed(ex, response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Exception handleFileUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletResponse response) {
        return commonHandleFileUploadSizeExceed(ex, response);

    }

    private Exception commonHandleFileUploadSizeExceed(Exception ex, HttpServletResponse response) {
        if (response == null) {
            throw new IllegalStateException("response missing");
        }
        if (ex == null) {
            throw new IllegalStateException("exception missing");
        }

        response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());

        return ex;
    }
}

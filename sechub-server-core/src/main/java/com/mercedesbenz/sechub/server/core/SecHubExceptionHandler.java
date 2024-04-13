// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonLocation;

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

    @ExceptionHandler(JacksonException.class)
    @ResponseBody
    public String handleJacksonException(JacksonException je, HttpServletResponse response) {
        /*
         * In this case the Jackson exception comes from Spring Boot framework - e.g.
         * when a user defines a wrong SecHubCongfigurationModel as JSON and the
         * framework is not able to create the object for RestController methods because
         * Jackson mapping fails. Our internal JSonConverter creates its own type of
         * exception so it is ensured that this is no internal problem.
         *
         * The Jackson exception would be already handled as a bad request by Spring,
         * but the problem is here, that the Jackson exception message does contain too
         * much internal information, stacktrace data etc. Those data shall not be
         * returned to user side.
         *
         * So we handle this special.
         *
         */
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String jsonProblem = "JSON data failure";

        JsonLocation location = je.getLocation();
        if (location != null) {
            jsonProblem += " at line: " + location.getLineNr() + ", column: " + location.getColumnNr();
        }
        jsonProblem += ". ";
        jsonProblem += je.getOriginalMessage();

        return jsonProblem;
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

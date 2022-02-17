// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
public class PDSServerErrorController implements ErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(PDSServerErrorController.class);

    @PDSMustBeDocumented(value = "When enabled, additional debug information are returned in case of failures. Do NOT use this in production.", scope = "development")
    @Value("${sechub.pds.server.debug:false}")
    private boolean debug;

    @Autowired
    ErrorAttributes errorAttributes;

    @RequestMapping(value = "${server.error.path}", produces = { "application/json" })
    ResponseEntity<PDSServerError> error(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("handling error on rest side");

        int status = response.getStatus();

        Map<String, Object> errorAttributes = getErrorAttributes(status, request, debug);

        return ResponseEntity.status(status).body(new PDSServerError(status, errorAttributes));

    }

    Map<String, Object> getErrorAttributes(int httpStatus, HttpServletRequest request, boolean debugMode) {

        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        options = includeErrorMessageForClientErrors(httpStatus, options);
        options = includeErrorStacktraceWhenDebugMode(debugMode, options);

        ServletWebRequest webRequest = new ServletWebRequest(request);
        return errorAttributes.getErrorAttributes(webRequest, options);
    }

    private ErrorAttributeOptions includeErrorStacktraceWhenDebugMode(boolean debugMode, ErrorAttributeOptions options) {
        if (debugMode) {
            options = options.including(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        return options;
    }

    private ErrorAttributeOptions includeErrorMessageForClientErrors(int httpStatus, ErrorAttributeOptions options) {
        try {
            HttpStatus httpStatusEnum = HttpStatus.valueOf(httpStatus);

            if (httpStatusEnum.is4xxClientError()) {
                options = options.including(ErrorAttributeOptions.Include.MESSAGE);
            }

        } catch (RuntimeException e) {
            LOG.error("Was not able to handle error message detection for http status:{}. Will not show include message in JSON error object.", httpStatus, e);
        }
        return options;
    }

}
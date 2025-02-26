// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import java.util.Map;

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

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ServerErrorController implements ErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(ServerErrorController.class);

    @MustBeDocumented(value = "When debug flag is set, rest call reponse error messages do also contain stacktraces.", scope = SCOPE_DEVELOPMENT_ONLY)
    @Value("${sechub.server.debug:false}")
    boolean debug;

    @Autowired
    ErrorAttributes errorAttributes;

    @RequestMapping(value = "${server.error.path}", produces = { "application/json" })
    ResponseEntity<ServerError> error(HttpServletRequest request, HttpServletResponse response) {
        int status = response.getStatus();

        Map<String, Object> errorAttributes = getErrorAttributes(status, request, debug);

        ServerError serverError = new ServerError(status, errorAttributes);
        LOG.info("Returning status: {}, message: {}, details: {}, timeStamp: {}, withTrace: {}", serverError.status, serverError.message, serverError.details,
                serverError.timeStamp, serverError.trace != null);
        return ResponseEntity.status(status).body(serverError);
    }

    private Map<String, Object> getErrorAttributes(int httpStatus, HttpServletRequest request, boolean debugMode) {

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
                options = options.including(ErrorAttributeOptions.Include.BINDING_ERRORS);
            }

        } catch (RuntimeException e) {
            LOG.error("Was not able to handle error message detection for http status:{}. Will not show include message in JSON error object.", httpStatus, e);
        }
        return options;
    }

}
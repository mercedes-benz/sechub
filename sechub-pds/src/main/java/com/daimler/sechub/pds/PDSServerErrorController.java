// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
public class PDSServerErrorController implements ErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(PDSServerErrorController.class);

    @PDSMustBeDocumented(value="When enabled, additional debug information are returned in case of failures. Do NOT use this in production.",scope="development")
    @Value("${sechub.pds.server.debug:false}")
    private boolean debug;

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = "${server.error.path}", produces = { "application/json" })
    ResponseEntity<PDSServerError> error(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("handling error on rest side");
        
        return ResponseEntity.status(response.getStatus()).body(new PDSServerError(response.getStatus(), getErrorAttributes(request, debug)));
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        ServletWebRequest webRequest = new ServletWebRequest(request);
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        if (includeStackTrace) {
            options=options.including(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        return errorAttributes.getErrorAttributes(webRequest, options);
    }

}
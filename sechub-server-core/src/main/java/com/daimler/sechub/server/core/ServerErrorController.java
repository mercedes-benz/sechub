// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.core;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

@RestController
public class ServerErrorController implements ErrorController {

    @MustBeDocumented("When debug flag is set, rest call reponse error messages do also contain stacktraces.")
    @Value("${sechub.server.debug:false}")
    private boolean debug;

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = "${server.error.path}")
    ResponseEntity<ServerError> error(HttpServletRequest request, HttpServletResponse response){
         return ResponseEntity.status(response.getStatus())
             .body(
                 new ServerError(response.getStatus(), getErrorAttributes(request, debug)
             )
         );
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
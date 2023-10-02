// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import java.io.IOException;

import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedException;

import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A special request reject handler which logs client IP adresses into security
 * log
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubHttpStatusRequestRejectedHandler extends HttpStatusRequestRejectedHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubHttpStatusRequestRejectedHandler.class);

    @Autowired
    SecurityLogService securityLogService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, RequestRejectedException requestRejectedException) throws IOException {
        String clientIPAddress = request.getRemoteAddr();

        if (request instanceof RequestFacade) {
            /*
             * we only log the request face call, because there are additional ones like
             * org.apache.catalina.core.ApplicationHttpRequest which will lead to doubled
             * entries
             */
            securityLogService.log(SecurityLogType.POTENTIAL_INTRUSION, "Rejected request, reason:{}", requestRejectedException.getMessage());
        } else {
            LOG.trace("Ignored request - treated as a duplicate, client IP adress was {}, request class was: {}", clientIPAddress, request.getClass());
        }

        super.handle(request, response, requestRejectedException);
    }
}

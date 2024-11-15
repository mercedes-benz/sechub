// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.LogConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;

@Service
public class AuditLogService {

    @Autowired
    UserContextService userContextService;

    @Autowired
    LogSanitizer logSanitizer;

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogService.class);

    private static String AUDIT = "[AUDIT]";
    private static String AUDIT_USERNAME = AUDIT + " ({}) :";

    /**
     * Logs an audit log entry. Will always contain user id at the beginning,
     * followed by given message
     *
     * @param message
     * @param objects
     */
    public void log(String message, Object... objects) {
        String userId = userContextService.getUserId();

        /*
         * convert this to a new list, otherwise slf4j becomes problems with identifying
         * this as list and having wrong output
         */
        List<Object> list = new ArrayList<>();
        list.add(userId);
        list.addAll(Arrays.asList(objects));

        try {
            MDC.put(LogConstants.MDC_SECHUB_AUDIT_USERID, userId);
            LOG.info(AUDIT_USERNAME + message, list.toArray());
        } finally {
            MDC.remove(LogConstants.MDC_SECHUB_AUDIT_USERID);
        }
    }

}

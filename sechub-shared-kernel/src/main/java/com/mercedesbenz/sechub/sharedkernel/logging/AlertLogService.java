// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service will be used for alert logging. The output format will always be
 *
 * <pre>
 *
 * [ALERT] [${type}] [${reason}] ${message}
 * </pre>
 *
 * so it can easily be used for log monitoring etc.<br>
 * <br>
 * An example output:
 * <code>"[ALERT] [SCHEDULER PROBLEM] [MEMORY OVERLOAD] Job processing is skipped."</code>
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class AlertLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertLogService.class);

    private static String ALERT = "[ALERT] [{}] [{}] ";

    /**
     * Log special alert - format is [ALERT] [${type}] [${reason}] ${message},
     * objects are integrated into message in slf4j format
     *
     * @param type
     * @param reason
     * @param message
     * @param objects
     */
    public void log(AlertLogType type, AlertLogReason reason, String message, Object... objects) {
        if (type == null) {
            type = AlertLogType.UNKNOWN;
            LOG.warn("Alert log service was called with no type id! Wrong implemented! Use fallback:{}", type);
        }
        if (reason == null) {
            reason = AlertLogReason.UNKNOWN;
        }
        /*
         * convert this to a new list, otherwise slf4j becomes problems with identifying
         * this as list and having wrong output
         */
        List<Object> list = new ArrayList<>();
        list.add(type.getTypeId());
        list.add(reason.getTypeId());
        list.addAll(Arrays.asList(objects));

        Object[] array = list.toArray();
        LOG.warn(ALERT + message, array);
    }

}

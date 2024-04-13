// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;

@Component
public class PDSStreamContentUpdateChecker {

    private static final int DEFAULT_CACHE_REFRESH_IN_MILLISECONDS = 2000;

    @PDSMustBeDocumented("PDS job stream data caching time in milliseconds. This defines the maximum period of time between an update time stamp and the request timestamp in database where current data is handled as still valid")
    @Value("${pds.config.job.stream.cachetime:" + DEFAULT_CACHE_REFRESH_IN_MILLISECONDS + "}")
    private int streamDataCacheTimeMilliseconds = DEFAULT_CACHE_REFRESH_IN_MILLISECONDS;

    private static final Logger LOG = LoggerFactory.getLogger(PDSStreamContentUpdateChecker.class);

    public boolean isUpdateNecessaryWhenRefreshRequestedNow(PDSJob job) {
        notNull(job, "Job may not be null!");

        if (!isJobInStateWhereUpdateNecessary(job)) {
            return false;
        }
        /* currently running so check last refresh */
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = job.getLastStreamTextUpdate();

        return isLastUpdateTooOld(lastUpdate, now);
    }

    public boolean isUpdateRequestedAndNecessary(PDSJob job) {
        notNull(job, "Job may not be null!");

        if (!isJobInStateWhereUpdateNecessary(job)) {
            return false;
        }
        /* currently running so check last refresh */
        LocalDateTime lastRequest = job.getLastStreamTextRefreshRequest();
        LocalDateTime lastUpdate = job.getLastStreamTextUpdate();

        return isLastUpdateTooOld(lastUpdate, lastRequest);
    }

    public boolean isJobInStateWhereUpdateNecessary(PDSJob job) {
        notNull(job, "Job may not be null!");

        PDSJobStatusState state = job.getState();
        if (!PDSJobStatusState.RUNNING.equals(state)) {
            LOG.trace("State of job:{} currently:{}, so no update necessary.", job.getUUID(), state);
            return false;
        }
        return true;
    }

    public boolean isLastUpdateTooOld(LocalDateTime lastUpdate, LocalDateTime requestTime) {
        if (requestTime == null) {
            /* in this case a there was never a user request - so never time out... */
            LOG.trace("Never requested - so never too old");
            return false;
        }
        if (lastUpdate == null) {
            /*
             * in this case no stream data is available but requested so must be fetched /
             * "too old"
             */
            LOG.trace("Never updated but requested - so always too old");
            return true;
        }
        /* measure time */
        Duration duration = Duration.between(lastUpdate, requestTime);

        long durationMillis = duration.toMillis();
        if (durationMillis > streamDataCacheTimeMilliseconds) {
            /* gap between update and refresh too big, so cache timed out */
            LOG.trace("Duration time timed out: streamDataCacheTimeMilliseconds={}, durationMillis={} - so too old", streamDataCacheTimeMilliseconds,
                    durationMillis);
            return true;
        }
        /* still okay */
        LOG.trace("Duration time NOT timed out: streamDataCacheTimeMilliseconds={}, durationMillis={} - so still valid", streamDataCacheTimeMilliseconds,
                durationMillis);
        return false;
    }
}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.Map;

import com.mercedesbenz.sechub.api.SecHubStatus.SchedulerData;
import com.mercedesbenz.sechub.api.internal.DefaultJobOverviewData;
import com.mercedesbenz.sechub.api.internal.DefaultSchedulerData;

public class SecHubStatusFactory {

    public static final String STATUS_SCHEDULER = "status.scheduler";

    public static final String STATUS_SCHEDULER_ENABLED = STATUS_SCHEDULER + ".enabled";

    public static final String STATUS_SCHEDULER_JOBS = STATUS_SCHEDULER + ".jobs";

    public static final String STATUS_SCHEDULER_JOBS_ALL = STATUS_SCHEDULER_JOBS + ".all";
    public static final String STATUS_SCHEDULER_JOBS_STARTED = STATUS_SCHEDULER_JOBS + ".started";
    public static final String STATUS_SCHEDULER_JOBS_READY_TO_START = STATUS_SCHEDULER_JOBS + ".ready_to_start";
    public static final String STATUS_SCHEDULER_JOBS_INITIALIZING = STATUS_SCHEDULER_JOBS + ".initializing";
    public static final String STATUS_SCHEDULER_JOBS_ENDED = STATUS_SCHEDULER_JOBS + ".ended";
    public static final String STATUS_SCHEDULER_JOBS_CANCELED = STATUS_SCHEDULER_JOBS + ".canceled";
    public static final String STATUS_SCHEDULER_JOBS_CANCEL_REQUESTED = STATUS_SCHEDULER_JOBS + ".cancel_requested";

    public SecHubStatus createFromMap(Map<String, String> statusInformation) {
        SchedulerData schedulerData = convertToSchedulerData(statusInformation);
        return new SecHubStatus(schedulerData);
    }

    private SchedulerData convertToSchedulerData(Map<String, String> statusMap) {
        boolean enabled = resolveBoolean(statusMap, STATUS_SCHEDULER_ENABLED);

        DefaultJobOverviewData jobOverviewData = new DefaultJobOverviewData();
        jobOverviewData.setAll(resolveLong(statusMap, STATUS_SCHEDULER_JOBS_ALL));
        jobOverviewData.setCancelRequested(resolveLong(statusMap, STATUS_SCHEDULER_JOBS_CANCEL_REQUESTED));
        jobOverviewData.setCanceled(resolveLong(statusMap, STATUS_SCHEDULER_JOBS_CANCELED));
        jobOverviewData.setEnded(resolveLong(statusMap, STATUS_SCHEDULER_JOBS_ENDED));
        jobOverviewData.setInitializating(resolveLong(statusMap, STATUS_SCHEDULER_JOBS_INITIALIZING));
        jobOverviewData.setReadyToStart(resolveLong(statusMap, STATUS_SCHEDULER_JOBS_READY_TO_START));
        jobOverviewData.setStarted(resolveLong(statusMap, STATUS_SCHEDULER_JOBS_STARTED));

        return new DefaultSchedulerData(enabled, jobOverviewData);
    }

    private boolean resolveBoolean(Map<String, String> statusMap, String key) {
        if (statusMap == null) {
            return false;
        }
        String value = statusMap.get(key);
        if (value == null) {
            return false;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    private long resolveLong(Map<String, String> statusMap, String key) {
        if (statusMap == null) {
            return 0;
        }
        String value = statusMap.get(key);
        if (value == null) {
            return 0;
        }
        return Long.valueOf(value);
    }

}

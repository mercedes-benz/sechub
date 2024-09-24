// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.scheduler;

import com.mercedesbenz.sechub.domain.administration.status.StatusEntryKey;

public enum SchedulerStatusEntryKeys implements StatusEntryKey {
    SCHEDULER_ENABLED("status.scheduler.enabled"),

    SCHEDULER_JOBS_ALL("status.scheduler.jobs.all"),

    SCHEDULER_JOBS_INITIALIZING("status.scheduler.jobs.initializing"),

    SCHEDULER_JOBS_READY_TO_START("status.scheduler.jobs.ready_to_start"),

    SCHEDULER_JOBS_STARTED("status.scheduler.jobs.started"),

    SCHEDULER_JOBS_CANCELED("status.scheduler.jobs.canceled"),

    SCHEDULER_JOBS_CANCEL_REQUESTED("status.scheduler.jobs.cancel_requested"),

    SCHEDULER_JOBS_SUSPENDED("status.scheduler.jobs.suspended"),

    SCHEDULER_JOBS_ENDED("status.scheduler.jobs.ended"),

    ;

    private String statusEntryKey;

    private SchedulerStatusEntryKeys(String key) {
        this.statusEntryKey = key;
    }

    @Override
    public String getStatusEntryKey() {
        return statusEntryKey;
    }

}

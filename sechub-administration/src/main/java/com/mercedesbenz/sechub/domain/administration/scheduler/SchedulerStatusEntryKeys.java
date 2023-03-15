// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.scheduler;

import com.mercedesbenz.sechub.domain.administration.status.StatusEntryKey;

public enum SchedulerStatusEntryKeys implements StatusEntryKey {
    SCHEDULER_ENABLED("status.scheduler.enabled"),

    SCHEDULER_JOBS_ALL("status.scheduler.jobs.all"),

    SCHEDULER_JOBS_RUNNING("status.scheduler.jobs.running"),

    SCHEDULER_JOBS_WAITING("status.scheduler.jobs.waiting");

    private String statusEntryKey;

    private SchedulerStatusEntryKeys(String key) {
        this.statusEntryKey = key;
    }

    @Override
    public String getStatusEntryKey() {
        return statusEntryKey;
    }

}

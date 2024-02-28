// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

public class SecHubStatus {

    private SchedulerData scheduler;

    public SecHubStatus(SchedulerData schedulerData) {
        if (schedulerData == null) {
            throw new IllegalArgumentException("scheduler may not be null!");
        }
        this.scheduler = schedulerData;
    }

    public SchedulerData getScheduler() {
        return scheduler;
    }

    public interface SchedulerData {
        boolean isEnabled();

        public JobsOverviewData getJobs();
    }

    public interface JobsOverviewData {

        long getAll();

        long getCancelRequested();

        long getCanceled();

        long getEnded();

        long getInitializating();

        long getReadyToStart();

        long getStarted();

    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal;

import com.mercedesbenz.sechub.api.SecHubStatus.JobsOverviewData;
import com.mercedesbenz.sechub.api.SecHubStatus.SchedulerData;

public class DefaultSchedulerData implements SchedulerData {

    private boolean enabled;
    private JobsOverviewData jobsOverviewData = new DefaultJobOverviewData();

    public DefaultSchedulerData(boolean enabled, JobsOverviewData jobOverview) {
        this.enabled = enabled;
        this.jobsOverviewData = jobOverview;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setJobOverviewData(JobsOverviewData jobsOverviewData) {
        this.jobsOverviewData = jobsOverviewData;
    }

    public JobsOverviewData getJobs() {
        return jobsOverviewData;
    }
}
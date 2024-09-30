// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

/**
 * This message data object contains all possible information about scheduler
 * status
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) schedule domain and administration - and maybe others")
public class SchedulerMessage implements JSONable<SchedulerMessage> {

    private boolean jobProcessingEnabled;

    private long amountOfAllJobs;

    private long amountOfInitializingJobs;
    private long amountOfJobsReadyToStart;
    private long amountOfJobsStarted;
    private long amountOfJobsCanceled;
    private long amountOfJobsCancelRequested;
    private long amountOfJobsEnded;

    private long amountOfJobsSuspended;

    public boolean isJobProcessingEnabled() {
        return jobProcessingEnabled;
    }

    public void setJobProcessingEnabled(boolean enabled) {
        this.jobProcessingEnabled = enabled;
    }

    @Override
    public Class<SchedulerMessage> getJSONTargetClass() {
        return SchedulerMessage.class;
    }

    public long getAmountOfAllJobs() {
        return amountOfAllJobs;
    }

    public void setAmountOfAllJobs(long amountOfAllJobs) {
        this.amountOfAllJobs = amountOfAllJobs;
    }

    public long getAmountOfJobsReadyToStart() {
        return amountOfJobsReadyToStart;
    }

    public void setAmountOfJobsReadyToStart(long amountOfJobsReadyToStart) {
        this.amountOfJobsReadyToStart = amountOfJobsReadyToStart;
    }

    public long getAmountOfJobsStarted() {
        return amountOfJobsStarted;
    }

    public void setAmountOfJobsStarted(long amountOfJobsStarted) {
        this.amountOfJobsStarted = amountOfJobsStarted;
    }

    public long getAmountOfInitializingJobs() {
        return amountOfInitializingJobs;
    }

    public void setAmountOfInitializingJobs(long amountOfInitializingJobs) {
        this.amountOfInitializingJobs = amountOfInitializingJobs;
    }

    public long getAmountOfJobsCanceled() {
        return amountOfJobsCanceled;
    }

    public void setAmountOfJobsCanceled(long amountOfJobsCanceled) {
        this.amountOfJobsCanceled = amountOfJobsCanceled;
    }

    public long getAmountOfJobsCancelRequested() {
        return amountOfJobsCancelRequested;
    }

    public void setAmountOfJobsCancelRequested(long amountOfJobsCancelRequested) {
        this.amountOfJobsCancelRequested = amountOfJobsCancelRequested;
    }

    public long getAmountOfJobsEnded() {
        return amountOfJobsEnded;
    }

    public void setAmountOfJobsEnded(long amountOfJobsEnded) {
        this.amountOfJobsEnded = amountOfJobsEnded;
    }

    public void setAmountOfJobsSuspended(long amountOfJobsSuspended) {
        this.amountOfJobsSuspended = amountOfJobsSuspended;
    }

    public long getAmountOfJobsSuspended() {
        return amountOfJobsSuspended;
    }

}

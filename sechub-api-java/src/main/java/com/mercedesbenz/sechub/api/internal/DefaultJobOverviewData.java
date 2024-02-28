package com.mercedesbenz.sechub.api.internal;

import com.mercedesbenz.sechub.api.SecHubStatus;
import com.mercedesbenz.sechub.api.SecHubStatus.JobsOverviewData;

public class DefaultJobOverviewData implements JobsOverviewData {

    private long all;
    private long cancelRequested;
    private long canceled;
    private long ended;
    private long initializating;
    private long readyToStart;
    private long started;

    public void setAll(long all) {
        this.all = all;
    }

    public void setCancelRequested(long cancelRequested) {
        this.cancelRequested = cancelRequested;
    }

    public void setCanceled(long canceled) {
        this.canceled = canceled;
    }

    public void setEnded(long ended) {
        this.ended = ended;
    }

    public void setInitializating(long initializating) {
        this.initializating = initializating;
    }

    public void setReadyToStart(long readyToStart) {
        this.readyToStart = readyToStart;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    @Override
    public long getAll() {
        return all;
    }

    @Override
    public long getCancelRequested() {
        return cancelRequested;
    }

    @Override
    public long getCanceled() {
        return canceled;
    }

    @Override
    public long getEnded() {
        return ended;
    }

    @Override
    public long getInitializating() {
        return initializating;
    }

    @Override
    public long getReadyToStart() {
        return readyToStart;
    }

    @Override
    public long getStarted() {
        return started;
    }

}
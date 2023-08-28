package com.mercedesbenz.sechub.api;

public record SecHubStatus(Scheduler scheduler, Jobs jobs) {

    public record Scheduler(boolean isEnabled) {
    }

    public record Jobs(long all, long cancelRequested, long canceled, long ended, long initializating, long readyToStart, long started) {
    }
}

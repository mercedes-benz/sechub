package com.mercedesbenz.sechub.pds.execution;

import java.util.concurrent.TimeUnit;

/**
 * An adapter for process access. Via this class we can mock processes in tests.
 * (Process is not correctly mockable via Mockito because the implementation
 * methods are still called)
 */
public class ProcessAdapter {

    private Process process;

    public ProcessAdapter(Process process) {
        this.process = process;
    }

    public boolean waitFor(long minutesToWaitForResult, TimeUnit minutes) throws InterruptedException {
        if (process == null) {
            return false;
        }
        return process.waitFor(minutesToWaitForResult, minutes);
    }

    public boolean isAlive() {
        if (process == null) {
            return false;
        }
        return process.isAlive();
    }

    public void destroyForcibly() {
        if (process == null) {
            return;
        }
        process.destroyForcibly();
    }

    public int exitValue() {
        if (process == null) {
            return -1;
        }
        return process.exitValue();
    }

}
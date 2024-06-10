// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import java.util.concurrent.TimeUnit;

/**
 * An adapter for processes. Via this class we can mock processes in tests very
 * easy with Mockito.
 */
public class ProcessAdapter {

    private Process process;

    public ProcessAdapter(Process process) {
        this.process = process;
    }

    public boolean waitFor(long minutesToWaitForResult, TimeUnit timeUnit) throws InterruptedException {
        if (process == null) {
            return false;
        }
        return process.waitFor(minutesToWaitForResult, timeUnit);
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
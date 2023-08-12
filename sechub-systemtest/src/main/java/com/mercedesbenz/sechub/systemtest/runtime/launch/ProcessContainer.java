// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.launch;

import java.io.File;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;

public class ProcessContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessContainer.class);

    int exitValue;
    String errorMessage;
    String outputMessage;
    long pid = -1;
    long number;

    @JsonIgnore
    private Process process;

    private boolean stillRunning;
    ScriptDefinition scriptDefinition;
    private boolean timedOut;
    private UUID uuid;
    private SystemTestExecutionState systemTestExecutionState;
    private File targetFile;

    /**
     * Creates a process container object which contains information about the
     * process, definitions and more, all of which are stored at runtime in a file .
     * It represents the state of the process
     *
     * @param number           the number of the process container
     * @param scriptDefinition configuration for the script to execute
     * @param state            the state of the system test execution
     * @param targetFile       the file which will be executed by the process
     */
    public ProcessContainer(int number, ScriptDefinition scriptDefinition, SystemTestExecutionState state, File targetFile) {
        if (scriptDefinition == null) {
            throw new IllegalArgumentException("script definition must be defined but is null!");
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("target file must be defined but is null!");
        }
        this.stillRunning = true;
        this.scriptDefinition = scriptDefinition;
        this.uuid = UUID.randomUUID();
        this.systemTestExecutionState = state;
        this.targetFile = targetFile;
        this.number = number;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public SystemTestExecutionState getSystemTestExecutionState() {
        return systemTestExecutionState;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getExitValue() {
        return exitValue;
    }

    public String getOutputMessage() {
        return outputMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String waitForOutputMessage() {
        waitWhileTrue("Fetch output message", () -> outputMessage == null);
        return outputMessage;
    }

    public String waitForErrorMessage() {
        waitWhileTrue("Fetch error message", () -> errorMessage == null);
        return errorMessage;
    }

    public void markProcessStarted(Process process) {
        this.process = process;
        this.pid = process.pid(); // for serialization...
    }

    private interface WaitForNecessary {
        public boolean waitIsStillNecessary();
    }

    private void waitWhileTrue(String info, WaitForNecessary waitForNecessary) {
        waitWhileTrue(info, waitForNecessary, 100, 10);
    }

    private void waitWhileTrue(String info, WaitForNecessary waitForNecessary, long millisecondsForOneWait, int maximumWaits) {

        int amountOfWaits = 0;

        while (waitForNecessary.waitIsStillNecessary()) {
            // give system IO chance to write stream data to disk
            try {
                amountOfWaits++;
                LOG.debug("{} - Waiting ({}) {} milliseconds", info, amountOfWaits, millisecondsForOneWait);

                Thread.sleep(millisecondsForOneWait);

                if (amountOfWaits >= maximumWaits) {
                    LOG.warn("{} - Waited {} * {} milliseconds for process container {}, maximum of {} wait times is reached - will skip here.", info,
                            amountOfWaits, millisecondsForOneWait, uuid, maximumWaits);
                    return;
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException("Was not able to wait for streams?!", e);
            }
        }
    }

    public void markTimedOut() {
        this.timedOut = true;
        markFailed();
    }

    public void markFailed() {
        this.exitValue = -1;
        markNoLongerRunning();
    }

    public void markNoLongerRunning() {
        this.stillRunning = false;
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public boolean isNotLongerRunning() {
        return !isStillRunning();
    }

    public ScriptDefinition getScriptDefinition() {
        return scriptDefinition;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    @Override
    public String toString() {
        String scriptName = scriptDefinition.getPath();
        return "ProcessContainer [uuid = " + uuid + ", pid = " + pid + ", path = " + scriptName + ", stillRunning=" + stillRunning + ", timedOut=" + timedOut
                + ", exitValue=" + exitValue + "]";
    }

    public boolean isProcessStartedSucessfully() {
        return pid != 0;
    }

    public boolean hasFailed() {
        return pid == -1 || exitValue < 0 || exitValue > 0;
    }

    /**
     * If the process is still alive it will be destroyed
     */
    public void terminateProcess() {
        if (process.isAlive()) {
            process.destroy();
        }
    }

    public void waitForProcessTerminated(long startTime, long maximumMillisecondsToWait) {
        if (!process.isAlive()) {
            return;
        }
        LOG.info("Process container {} [{}]: Wait {} milliseconds (max) for process with PID {} to terminate. Path={}", uuid, systemTestExecutionState,
                maximumMillisecondsToWait, pid, scriptDefinition.getPath());

        while (process.isAlive() && !isTimeOutReached(startTime, maximumMillisecondsToWait)) {
            try {
                LOG.debug("Process with PID {} still alive", pid);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (process.isAlive()) {
            LOG.warn("Process with PID {} has NOT terminated!", pid);
        } else {
            LOG.info("Process with PID {} has terminated", pid);
        }
    }

    private boolean isTimeOutReached(long startTime, long maxMilliseconds) {
        long current = System.currentTimeMillis();
        if (current > startTime + maxMilliseconds) {
            return true;
        }
        return false;
    }

    public long getNumber() {
        return number;
    }

}

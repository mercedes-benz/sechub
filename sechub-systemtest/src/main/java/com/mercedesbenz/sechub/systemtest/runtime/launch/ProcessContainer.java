package com.mercedesbenz.sechub.systemtest.runtime.launch;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;

public class ProcessContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessContainer.class);
    private static long amountOfContainers;
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

    public ProcessContainer(ScriptDefinition scriptDefinition, SystemTestExecutionState state) {

        this.stillRunning = true;
        this.scriptDefinition = scriptDefinition;
        this.uuid = UUID.randomUUID();
        this.systemTestExecutionState = state;

        /* additional stuff to have an ordering of the created process containers */
        amountOfContainers++;
        this.number = amountOfContainers;
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

        int waited = 0;

        while (waitForNecessary.waitIsStillNecessary()) {
            // give system IO chance to write stream data to disk
            try {
                waited++;
                LOG.debug("{} - Waiting ({}) {} milliseconds", info, waited, millisecondsForOneWait);

                Thread.sleep(millisecondsForOneWait);

                if (waited >= maximumWaits) {
                    LOG.warn("{} - {} waits done for container {}, maximum {} reached - will skip here.", info, waited, uuid, maximumWaits);
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

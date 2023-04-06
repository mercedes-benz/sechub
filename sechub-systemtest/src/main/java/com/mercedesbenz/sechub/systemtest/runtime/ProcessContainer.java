package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;

import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;

public class ProcessContainer {

    int exitValue;
    String errorMessage;
    String outputMessage;
    private boolean stillRunning;
    ScriptDefinition scriptDefinition;
    private boolean timedOut;

    public ProcessContainer(ScriptDefinition scriptDefinition) {
        this.stillRunning = true;
        this.scriptDefinition = scriptDefinition;
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

    public void markProcessNotStartable(IOException e) {
        this.exitValue = -1;
        this.errorMessage = "Start of process was not possible:" + e.getMessage();
    }

    public void markNoLongerRunning() {
        this.stillRunning = false;
    }

    public void markTimedOut() {
        this.timedOut = true;
        markNoLongerRunning();
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public ScriptDefinition getScriptDefinition() {
        return scriptDefinition;
    }

    public boolean hasTimedOut() {
        return timedOut;
    }

    @Override
    public String toString() {
        String scriptName = scriptDefinition.getPath();
        return "ProcessContainer [path = " + scriptName + ", stillRunning=" + stillRunning + ", timedOut=" + timedOut + ", exitValue=" + exitValue + "]";
    }

    public boolean hasFailed() {
        return exitValue < 0 || exitValue > 0;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

public class ProductCancellationProcessHandlingData implements ProcessHandlingData {

    private int millisecondsToWaitForNextCheck;
    private int secondsToWaitForProcess;
    long processStartTimeStamp;

    public ProductCancellationProcessHandlingData(int secondsToWaitForProcess, int millisecondsToWaitForNextCheck) {

        this.secondsToWaitForProcess = secondsToWaitForProcess;
        this.millisecondsToWaitForNextCheck = millisecondsToWaitForNextCheck;

        this.processStartTimeStamp = System.currentTimeMillis();
    }

    public int getMillisecondsToWaitForNextCheck() {
        return millisecondsToWaitForNextCheck;
    }

    public int getSecondsToWaitForProcess() {
        return secondsToWaitForProcess;
    }

    public long getProcessStartTimeStamp() {
        return processStartTimeStamp;
    }

    public boolean isStillWaitingForProcessAccepted() {
        long maxAllowedTimeStamp = processStartTimeStamp + (secondsToWaitForProcess * 1000);

        long currentTimeStampInMilliseconds = System.currentTimeMillis();
        return currentTimeStampInMilliseconds < maxAllowedTimeStamp;
    }
}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

public class PDSExecutionResult {

    private int exitCode;
    private boolean failed;

    private String result;
    private boolean canceled;
    private boolean encryptionFailure;

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isEncryptionFailure() {
        return encryptionFailure;
    }

    public void setEncryptionFailure(boolean encryptionFailure) {
        this.encryptionFailure = encryptionFailure;
    }

}

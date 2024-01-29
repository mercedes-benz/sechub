// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.job;

/**
 * Represents the execution result of a SecHub job
 *
 * @author Albert Tregnaghi
 *
 */
public enum ExecutionResult {

    /**
     * No execution triggered
     */
    NONE,

    /**
     * Execution was DONE without failure on execution. This information is only
     * about the execution process - e.g. a Scan which finds vulnerabilities and
     * found 3 HIGH level CVEs will return OK when the scan was done and the
     * reporting fulfilled !
     *
     */
    OK,

    /**
     * Execution was NOT DONE because of failures or canceled- This information is
     * only about the execution process - e.g. the scan would not find any CVEs but
     * the scan server is done than FAILED will be returned!
     */
    FAILED,

    ;

    /**
     * @return <code>true</code> when execution has been done and a result must be
     *         available
     */
    public boolean hasFinished() {
        return !this.equals(NONE);
    }

    /**
     * Resolves execution result from given string - if not found it will return
     * <code>null</code>
     *
     * @param string
     * @return result or <code>null</code>
     */
    public static ExecutionResult fromString(String string) {
        for (ExecutionResult result : values()) {
            if (result.name().equalsIgnoreCase(string)) {
                return result;
            }
        }
        return null;
    }

}

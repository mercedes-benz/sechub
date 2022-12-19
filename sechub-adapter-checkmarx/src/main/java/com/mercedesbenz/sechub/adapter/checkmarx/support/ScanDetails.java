// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

public class ScanDetails {

    String statusName;
    boolean notFound;

    public boolean isRunning() {
        return !notFound && !hasFinished();
    }

    private boolean hasFinished() {
        return "Finished".equals(statusName);
    }

}

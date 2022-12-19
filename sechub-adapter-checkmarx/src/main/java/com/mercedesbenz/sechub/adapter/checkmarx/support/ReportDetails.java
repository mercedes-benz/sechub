// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

public class ReportDetails {

    String status;
    boolean notFound;

    public boolean isNotFound() {
        return notFound;
    }

    public boolean isRunning() {
        return isCheckPossible() && !isReportCreated() && !didReportCreationFail();
    }

    private boolean isCheckPossible() {
        return !isNotFound();
    }

    private boolean isReportCreated() {
        return "Created".equalsIgnoreCase(status);
    }

    private boolean didReportCreationFail() {
        return "Failed".equalsIgnoreCase(status);
    }
}

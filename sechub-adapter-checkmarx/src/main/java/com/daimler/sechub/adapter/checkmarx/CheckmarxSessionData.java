// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

public class CheckmarxSessionData {
    private long projectId;
    private String projectName;
    private long scanId;
    private long reportId;

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setScanId(long id) {
        this.scanId = id;
    }

    public long getScanId() {
        return scanId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public long getReportId() {
        return reportId;
    }

}
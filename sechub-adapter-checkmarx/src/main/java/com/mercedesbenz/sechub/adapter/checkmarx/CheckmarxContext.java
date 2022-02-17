// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.mercedesbenz.sechub.adapter.AbstractSpringRestAdapterContext;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxOAuthData;
import com.mercedesbenz.sechub.adapter.checkmarx.support.QueueDetails;
import com.mercedesbenz.sechub.adapter.checkmarx.support.ReportDetails;
import com.mercedesbenz.sechub.adapter.checkmarx.support.ScanDetails;

/**
 * Context for checkmarx execution.
 *
 * @author Albert Tregnaghi
 *
 */
public class CheckmarxContext extends AbstractSpringRestAdapterContext<CheckmarxAdapterConfig, CheckmarxAdapter> implements CheckmarxAdapterContext {

    private CheckmarxOAuthData oAuthData;
    private CheckmarxSessionData sessionData;
    private QueueDetails queueDetails;
    private ScanDetails scanDetails;
    private ReportDetails reportDetails;
    private boolean fullScan;
    private Boolean newProject;

    public CheckmarxContext(CheckmarxAdapterConfig config, CheckmarxAdapter adapter, AdapterRuntimeContext runtimeContext) {
        super(config, adapter, runtimeContext);
        queueDetails = new QueueDetails();
        scanDetails = new ScanDetails();
        reportDetails = new ReportDetails();
    }

    public CheckmarxOAuthData getoAuthData() {
        return oAuthData;
    }

    public CheckmarxAdapter getCheckmarxAdapter() {
        return super.getAdapter();
    }

    public QueueDetails getQueueDetails() {
        return queueDetails;
    }

    public void markQueueRetry() {
        queueDetails = new QueueDetails();
    }

    public void setSessionData(CheckmarxSessionData sessionData) {
        this.sessionData = sessionData;
    }

    public CheckmarxSessionData getSessionData() {
        if (sessionData == null) {
            throw new IllegalStateException("Session data is not initialized/set!");
        }
        return sessionData;
    }

    public boolean isOAuthenticated() {
        return oAuthData != null;
    }

    public void markAuthenticated(CheckmarxOAuthData data) {
        this.oAuthData = data;
    }

    public boolean isIncrementalScan() {
        return !fullScan;
    }

    public boolean isFullScan() {
        return fullScan;
    }

    public void setFullScan(boolean fullScan) {
        this.fullScan = fullScan;
    }

    @Override
    public long getScanId() {
        return getSessionData().getScanId();
    }

    @Override
    public long getReportId() {
        return getSessionData().getReportId();
    }

    @Override
    public void setReportId(long reportId) {
        getSessionData().setReportId(reportId);
    }

    @Override
    protected ClientHttpRequestInterceptor createInterceptorOrNull(CheckmarxAdapterConfig config) {
        return new CheckmarxClientHttpRequestInterceptor(this);
    }

    public String getAuthorizationHeaderValue() {
        if (!isOAuthenticated()) {
            return "";
        }
        return oAuthData.getTokenType() + " " + oAuthData.getAccessToken();
    }

    public ScanDetails getScanDetails() {
        return scanDetails;
    }

    public ReportDetails getReportDetails() {
        return reportDetails;
    }

    public boolean isNewProject() {
        return Boolean.TRUE.equals(newProject);
    }

    public boolean isNewProjectInfoAvailable() {
        return newProject != null;
    }

    public void setNewProject(boolean newProject) {
        this.newProject = newProject;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubResult;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportRequestListener;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportViewUpdater;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.SecHubFindingToFindingModelTransformer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class SecHubReportTabSwitcher implements SecHubReportRequestListener {

    private static final Logger LOG = Logger.getInstance(SecHubToolWindowFactory.class);

    private final ContentManager contentManager;
    private final String reportPanelName;
    private final SecHubFindingToFindingModelTransformer secHubReportTransformer;
    private final SecHubReportViewUpdater secHubReportViewUpdater;

    public SecHubReportTabSwitcher(ContentManager contentManager, String reportPanelName) {
        this.contentManager = requireNonNull(contentManager, "Property 'contentManager' must not be null");
        this.reportPanelName = requireNonNull(reportPanelName, "Property 'reportPanelName' must not be null");
        secHubReportTransformer = new SecHubFindingToFindingModelTransformer();
        secHubReportViewUpdater = new SecHubReportViewUpdater();
    }

    @Override
    public void onReportRequested(String projectId, UUID jobUUID) {
        Optional<Content> optReportPanel = getContentByName(contentManager, reportPanelName);
        if (optReportPanel.isEmpty()) {
            LOG.error("SecHub Report panel not found");
            throw new IllegalStateException("SecHub Report panel not found");
        }
        contentManager.setSelectedContent(optReportPanel.get());
        ProgressIndicator progressIndicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
        ProgressManager.getInstance().executeProcessUnderProgress(() -> fetchAndDisplayReport(projectId, jobUUID, progressIndicator), progressIndicator);
    }

    private static Optional<Content> getContentByName(ContentManager contentManager, String name) {
        for (Content content : contentManager.getContents()) {
            if (content.getDisplayName().equals(name)) {
                return Optional.of(content);
            }
        }
        return Optional.empty();
    }

    private void fetchAndDisplayReport(String projectId, UUID jobUUID, ProgressIndicator progressIndicator) {

        if (progressIndicator != null) {
            progressIndicator.setText("Fetch SecHub report from " + "testyyyyyy");
        }

        SecHubReport report = SecHubAccessFactory.create().getSecHubReport(projectId, jobUUID);

        if (report == null) {
            LOG.error("Failed to fetch SecHub report for job UUID: " + jobUUID);
            return;
        }

        SecHubResult result = report.getResult();

        if (result == null) {
            LOG.error("SecHub report result is null for job UUID: " + jobUUID);
            return;
        }

        List<SecHubFinding> findings = result.getFindings();

        if (findings == null) {
            LOG.error("SecHub report findings are null for job UUID: " + jobUUID);
            return;
        }

        FindingModel model = secHubReportTransformer.transform(findings);
        model.setJobUUID(report.getJobUUID());
        model.setTrafficLight(report.getTrafficLight());

        secHubReportViewUpdater.updateReportViewInAWTThread(report.getJobUUID(), report.getTrafficLight(), model);
    }
}

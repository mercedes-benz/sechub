// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportRequestListener;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportViewUpdater;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.SecHubReportFindingModelService;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class SecHubReportTabSwitcher implements SecHubReportRequestListener {

    private static final Logger LOG = Logger.getInstance(SecHubToolWindowFactory.class);
    private static final SecHubReportViewUpdater secHubReportViewUpdater = SecHubReportViewUpdater.getInstance();
    private static final SecHubReportFindingModelService secHubReportFindingModelService = SecHubReportFindingModelService.getInstance();

    private final ContentManager contentManager;
    private final String reportPanelName;

    public SecHubReportTabSwitcher(ContentManager contentManager, String reportPanelName) {
        this.contentManager = requireNonNull(contentManager, "Property 'contentManager' must not be null");
        this.reportPanelName = requireNonNull(reportPanelName, "Property 'reportPanelName' must not be null");
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
        ProgressManager.getInstance().executeProcessUnderProgress(() -> {
            FindingModel findingModel = secHubReportFindingModelService.fetchAndBuildFindingModel(projectId, jobUUID, progressIndicator);
            secHubReportViewUpdater.updateReportViewInAWTThread(findingModel);
        }, progressIndicator);
    }

    private static Optional<Content> getContentByName(ContentManager contentManager, String name) {
        for (Content content : contentManager.getContents()) {
            if (content.getDisplayName().equals(name)) {
                return Optional.of(content);
            }
        }
        return Optional.empty();
    }
}

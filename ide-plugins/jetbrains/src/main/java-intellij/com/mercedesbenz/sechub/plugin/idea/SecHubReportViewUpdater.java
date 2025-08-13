// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.mercedesbenz.sechub.plugin.idea.window.SecHubReportPanel;
import com.mercedesbenz.sechub.plugin.model.FindingModel;

public class SecHubReportViewUpdater {

    private static final Logger LOG = Logger.getInstance(SecHubReportViewUpdater.class);
    private static final SecHubReportViewUpdater instance = new SecHubReportViewUpdater();

    public static SecHubReportViewUpdater getInstance() {
        return instance;
    }

    private SecHubReportViewUpdater() {
        /* private constructor to enforce singleton */
    }

    public void updateReportViewInAWTThread(FindingModel model) {
        ProgressManager.getInstance().executeProcessUnderProgress(() -> internalUpdateReportView(model),
                ProgressIndicatorProvider.getGlobalProgressIndicator());
    }

    private void internalUpdateReportView(FindingModel model) {
        Project project = null;

        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            project = projects[0];
        } else {
            project = ProjectManager.getInstance().getDefaultProject();
        }
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = windowManager.getToolWindow("SecHub");
        if (toolWindow == null) {
            LOG.error("Did not found Tool window with id 'SecHub' !");
            return;
        }
        toolWindow.show(() -> {
            // at this point the factory must have created and registered the
            // SecHubToolWindow instance
            /*
             * SecHubToolWindow sechubToolWindow = SecHubToolWindow.getInstance(); if
             * (sechubToolWindow == null) { LOG.error("Did not found SecHub tool window!");
             * return; } sechubToolWindow.update(model);
             */

            SecHubReportPanel reportPanel = SecHubReportPanel.getInstance();
            if (reportPanel == null) {
                LOG.error("Did not found SecHub tool window!");
                return;
            }
            reportPanel.update(model);
        });

    }
}

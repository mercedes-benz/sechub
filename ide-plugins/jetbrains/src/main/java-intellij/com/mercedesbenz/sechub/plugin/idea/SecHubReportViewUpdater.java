// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.plugin.idea.window.SecHubReportPanel;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

import java.util.UUID;

public class SecHubReportViewUpdater {

    private static final Logger LOG = Logger.getInstance(SecHubReportViewUpdater.class);

    public void updateReportViewInAWTThread(UUID jobUUID, TrafficLight trafficLight, FindingModel model) {

        ProgressManager.getInstance().executeProcessUnderProgress(() -> internalUpdateReportView(jobUUID, trafficLight, model), ProgressIndicatorProvider.getGlobalProgressIndicator());
    }

    private void internalUpdateReportView(UUID jobUUID, TrafficLight trafficLight, FindingModel model) {
        Project project = null;

        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            project = projects[0];
        } else {
            project = ProjectManager.getInstance().getDefaultProject();
        }
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = windowManager.getToolWindow("SecHub");
        if (toolWindow==null){
            LOG.error("Did not found Tool window with id 'SecHub' !");
            return;
        }
        toolWindow.show(() -> {
                    // at this point the factory must have created and registered the SecHubToolWindow instance
                   /* SecHubToolWindow sechubToolWindow = SecHubToolWindow.getInstance();
                    if (sechubToolWindow == null) {
                        LOG.error("Did not found SecHub tool window!");
                        return;
                    }
                    sechubToolWindow.update(model);
                    */

            SecHubReportPanel reportPanel = SecHubReportPanel.getInstance();
            if (reportPanel == null) {
                LOG.error("Did not found SecHub tool window!");
                return;
            }
            reportPanel.update(model);
                }
        );


    }
}

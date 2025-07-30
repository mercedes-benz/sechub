// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.plugin.idea.SecHubSettingsDialogListener;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.mercedesbenz.sechub.plugin.idea.action.SechubOpenReportFromFileSystemAction;
import com.mercedesbenz.sechub.plugin.idea.action.SechubResetReportAction;

public class SecHubToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        SecHubSettingsDialogListener settingsDialogListener = new SecHubSettingsDialogListener(project, "SecHub");

        SecHubReportPanel reportPanel = new SecHubReportPanel(toolWindow);
        SecHubReportPanel.registerInstance(reportPanel);

        SecHubReportTabSwitcher reportTabSwitcher = new SecHubReportTabSwitcher(toolWindow.getContentManager(), "Report");
        SecHubServerPanel serverPanel = new SecHubServerPanel(settingsDialogListener, reportTabSwitcher);
        SecHubServerPanel.registerInstance(serverPanel);

        DefaultActionGroup toolbarActions = new DefaultActionGroup();
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("SecHub", toolbarActions, false);

        AnAction resetReportData = new SechubResetReportAction();
        SechubOpenReportFromFileSystemAction importAction = new SechubOpenReportFromFileSystemAction();

        // toolbarActions.add(importAction);
        // toolbarActions.add(resetReportData);

        addToolWindowTab(toolWindow, reportPanel, toolbar, "Report");
        addToolWindowTab(toolWindow, serverPanel, toolbar, "Server");

        List<AnAction> titleActions = new ArrayList<>();
        titleActions.add(importAction);
        titleActions.add(resetReportData);
        toolWindow.setTitleActions(titleActions);
    }

    private static void addToolWindowTab(@NotNull ToolWindow toolWindow, SecHubPanel panel, ActionToolbar toolbar, String name) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(false, false);
        toolWindowPanel.add(panel.getContent());

        toolbar.setTargetComponent(toolWindowPanel);
        toolWindowPanel.setToolbar(toolbar.getComponent());

        Content content = contentFactory.createContent(toolWindowPanel, name, false);
        toolWindow.getContentManager().addContent(content);
    }
}
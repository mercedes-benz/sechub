// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportImporter;
import com.mercedesbenz.sechub.plugin.idea.compatiblity.VirtualFileCompatibilityLayer;
import com.mercedesbenz.sechub.plugin.idea.window.SecHubReportPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class SechubResetReportAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(SechubResetReportAction.class);

    public SechubResetReportAction() {
    }

    @Override
    public void update(AnActionEvent event) {
        // Using the event, evaluate the context, and enable or disable the action.
        // Set the availability based on whether a project is open
        Project project = event.getProject();
        Presentation presentation = event.getPresentation();

        presentation.setIcon(AllIcons.General.Reset);
        presentation.setText("Reset report data and presentation");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        SecHubReportPanel.getInstance().reset();

    }

}

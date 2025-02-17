// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportImporter;
import com.mercedesbenz.sechub.plugin.idea.compatiblity.VirtualFileCompatibilityLayer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class SechubOpenReportFromFileSystemAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(SechubOpenReportFromFileSystemAction.class);

    public SechubOpenReportFromFileSystemAction(){
    }

    @Override
    public void update(AnActionEvent event) {
        // Using the event, evaluate the context, and enable or disable the action.
        // Set the availability based on whether a project is open
        Project project = event.getProject();

        Presentation presentation = event.getPresentation();
        //presentation.setEnabledAndVisible(project != null);
        presentation.setVisible(true);
        presentation.setEnabled(project!=null);
        presentation.setIcon(AllIcons.Actions.MenuOpen);
        presentation.setText("Open SecHub report from filesystem");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project currentProject = event.getProject();
        FileChooserDescriptor
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json");
        fileChooserDescriptor.setDescription("Open SecHub report");
        @Nullable VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, currentProject, null);

        if (file == null) {
            return;
        }
        @NotNull Path p = VirtualFileCompatibilityLayer.toNioPath(file);
        try {
            SecHubReportImporter.getInstance().importAndDisplayReport(p.toFile());
        } catch (IOException e) {
            LOG.error("Failed to import " + p, e);
        }

    }

}

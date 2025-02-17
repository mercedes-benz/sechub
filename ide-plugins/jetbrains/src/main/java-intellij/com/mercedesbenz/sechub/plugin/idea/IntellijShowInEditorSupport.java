// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.mercedesbenz.sechub.plugin.idea.compatiblity.VirtualFileCompatibilityLayer;
import com.mercedesbenz.sechub.plugin.model.FileLocationExplorer;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class IntellijShowInEditorSupport {

    private static final Logger LOG = Logger.getInstance(IntellijShowInEditorSupport.class);

    public void showInEditor(ToolWindow toolWindow, FindingNode callStep) {
        FileLocationExplorer explorer = new FileLocationExplorer();

        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
                break;
            }
        }
        if (activeProject == null) {
            LOG.error("No active project found, so cannot show current call step in editor!");
            return;
        }

        VirtualFile projectDir = ProjectUtil.guessProjectDir(activeProject);
        if (projectDir == null) {
            return;
        }

        Path searchFolderPath = VirtualFileCompatibilityLayer.toNioPath(projectDir);
        explorer.getSearchFolders().add(searchFolderPath);

        List<Path> pathes = null;
        try {
            pathes = explorer.searchFor(callStep.getLocation());
        } catch (IOException e) {
            LOG.error("Lookup for sources failed", e);
            return;
        }
        if (pathes.isEmpty()) {
            ToolWindowBalloonShowOptions options = new ToolWindowBalloonShowOptions(toolWindow.getId(), MessageType.WARNING,
                    "No source found for location: " + callStep.getLocation()+"<br>Search folder path: "+searchFolderPath, null, null, (builder) -> {
                builder.setFadeoutTime(2000);
            });
            ToolWindowManager.getInstance(activeProject).notifyByBalloon(options);
            return;
        }
        if (pathes.size() > 1) {
            LOG.warn("Multiple paths found using only first one");
        }
        Path first = pathes.get(0);
        @Nullable VirtualFile firstAsVirtualFile = VirtualFileManager.getInstance().findFileByUrl(first.toUri().toString());
        if (firstAsVirtualFile == null) {
            LOG.error("Found in normal filesystem but not in virtual one:" + first);
            return;
        }
        int line = callStep.getLine();
        int column = callStep.getColumn();

        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(activeProject, firstAsVirtualFile, line - 1, column);
        fileDescriptor.navigateInEditor(activeProject, true);
    }
}

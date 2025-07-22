package com.mercedesbenz.sechub.plugin.idea;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

import static java.util.Objects.requireNonNull;

public class SecHubSettingsDialogListener {

    private final Project project;
    private final String dialogName;

    public SecHubSettingsDialogListener(Project project, String dialogName) {
        this.project = requireNonNull(project, "Property 'project' must not be null");
        this.dialogName = requireNonNull(dialogName, "Property 'dialogName' must not be null");
    }

    public void onShowSettingsDialog() {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, dialogName);
    }
}

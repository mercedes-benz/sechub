// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.developertools.admin.ui.AbstractListDialogUI;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

public class ChangeProjectAccessLevelDialogUI extends AbstractListDialogUI<ProjectAccessLevel> {

    private ProjectAccessLevel currentAccessLevel;

    public ChangeProjectAccessLevelDialogUI(UIContext context, String projectId, ProjectAccessLevel currentAccessLevel) {
        super(context, "Change access level for project:" + projectId);
        this.currentAccessLevel = currentAccessLevel;
        setDescription("Current project access level:" + currentAccessLevel);
    }

    @Override
    protected List<String> createTableHeaders() {
        List<String> model = new ArrayList<>();
        model.add("Project access level");// 0
        return model;
    }

    @Override
    protected void initializeDataForShowDialog() {
        // not necessary
    }

    @Override
    protected int getSelectionColumn() {
        return 0; // we element form index 0
    }

    @Override
    protected List<Object[]> createTableContent() {
        List<Object[]> list = new ArrayList<>();

        for (ProjectAccessLevel accessLevel : ProjectAccessLevel.values()) {
            if (accessLevel == currentAccessLevel) {
                // we do not show current access level but only possible other values.
                continue;
            }
            list.add(new Object[] { accessLevel });
        }

        return list;
    }

}

// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.developertools.admin.ui.AbstractListDialogUI;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;

public class ChangeProjectAccessLevelDialogUI extends AbstractListDialogUI<ProjectAccessLevel> {

    private ProjectAccessLevel accessLevelBefore;

    public ChangeProjectAccessLevelDialogUI(UIContext context, String title, ProjectAccessLevel accessLevelBefore) {
        super(context, title);
        this.accessLevelBefore = accessLevelBefore;
    }

    @Override
    protected List<String> createTableHeaders() {
        List<String> model = new ArrayList<>();
        model.add("Project access level");// 0
        return model;
    }

    @Override
    protected void initializeDataForShowDialog() {

    }

    @Override
    protected int getSelectionColumn() {
        return 0; // we use 0 for access level name
    }

    @Override
    protected List<Object[]> createTableContent() {
        List<Object[]> list = new ArrayList<>();

        for (ProjectAccessLevel level : ProjectAccessLevel.values()) {
            if (level == accessLevelBefore) {
                // we do not show current value...
                continue;
            }
            list.add(new Object[] { level });
        }

        return list;
    }

}

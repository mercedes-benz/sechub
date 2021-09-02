// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.developertools.admin.DeveloperProjectDetailInformation;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;

public class ChangeProjectAccessLevelAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ChangeProjectAccessLevelAction(UIContext context) {
        super("Change access level", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> optProjectId = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
        if (!optProjectId.isPresent()) {
            return;
        }

        String projectId = optProjectId.get().toLowerCase().trim();
        DeveloperAdministration administration = getContext().getAdministration();

        ProjectAccessLevel currentAccessLevel = fetchCurrentProjectAccessLevel(projectId, administration);

        ChangeProjectAccessLevelDialogUI dialogUI = new ChangeProjectAccessLevelDialogUI(getContext(), projectId, currentAccessLevel);
        dialogUI.showDialog();

        if (!dialogUI.isOkPressed()) {
            return;
        }

        ProjectAccessLevel wantedAccessLevel = dialogUI.getSelectedValue();

        if (!confirm("Do you really want to change the project access level for project:" + projectId + " to " + wantedAccessLevel + " ?")) {
            return;
        }
        administration.changeProjectAccessLevel(projectId, wantedAccessLevel);

    }

    private ProjectAccessLevel fetchCurrentProjectAccessLevel(String projectId, DeveloperAdministration administration) {
        DeveloperProjectDetailInformation details = administration.fetchProjectDetailInformation(projectId);
        ProjectAccessLevel currentAccesslevel = ProjectAccessLevel.fromId(details.getAccessLevel());
        return currentAccesslevel;
    }

}
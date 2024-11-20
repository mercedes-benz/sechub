// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UnassignTemplateFromProjectAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public UnassignTemplateFromProjectAction(UIContext context) {
        super("Unassign template from project", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> templateIdOpt = getUserInput("Please enter templateId", InputCacheIdentifier.TEMPLATE_ID);
        if (!templateIdOpt.isPresent()) {
            return;
        }
        String templateId = templateIdOpt.get();
        Optional<String> projectIdOpt = getUserInput("Please enter projectId", InputCacheIdentifier.PROJECT_ID);
        if (!projectIdOpt.isPresent()) {
            return;
        }
        String projectId = projectIdOpt.get();

        getContext().getAdministration().unassignTemplateFromProject(templateId, projectId);

    }

}
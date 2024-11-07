// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AssignTemplateToProjectAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public AssignTemplateToProjectAction(UIContext context) {
        super("Assign template to project", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> templateIdOpt = getUserInput("Please enter templateId", InputCacheIdentifier.TEMPLATE_ID);
        if (!templateIdOpt.isPresent()) {
            return;
        }
        String templateId = templateIdOpt.get();
        TemplateDefinition foundTemplate = getContext().getAdministration().fetchTemplateOrNull(templateId);
        if (foundTemplate == null) {
            error("The template " + templateId + " does not exist!");
            return;
        }
        Optional<String> projectIdOpt = getUserInput("Please enter projectId", InputCacheIdentifier.PROJECT_ID);
        if (!projectIdOpt.isPresent()) {
            return;
        }
        String projectId = projectIdOpt.get();
        String projectInfo = getContext().getAdministration().fetchProjectInfo(projectId);

        if (projectInfo == null) {
            error("The project " + projectId + " does not exist!");
            return;
        }

        getContext().getAdministration().assignTemplateToProject(templateId, projectId);

    }

}
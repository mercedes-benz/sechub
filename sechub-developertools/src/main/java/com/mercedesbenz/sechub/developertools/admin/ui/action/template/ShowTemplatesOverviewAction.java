// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.developertools.admin.DeveloperProjectDetailInformation;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ShowTemplatesOverviewAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowTemplatesOverviewAction(UIContext context) {
        super("Create template overview", context);
    }

    @Override
    public void execute(ActionEvent e) {

        List<String> projects = getAdministration().fetchProjectIdList();
        List<DeveloperProjectDetailInformation> projectDetails = new ArrayList<>();
        List<String> templates = getAdministration().fetchAllTemplateIdentifiers();

        for (String projectId : projects) {
            DeveloperProjectDetailInformation detail = getAdministration().fetchProjectDetailInformation(projectId);
            projectDetails.add(detail);
        }
        output("Templates:");

        for (String templateId : templates) {
            output("  templateId:'" + templateId + "'");
            List<String> templateAssginedToProjects = new ArrayList<>();
            TemplateDefinition templateDefinition = getContext().getAdministration().fetchTemplateOrNull(templateId);
            if (templateDefinition == null) {
                error("Template definition for template: " + templateId + " was not found!");
            } else {
                for (DeveloperProjectDetailInformation projectDetail : projectDetails) {
                    if (projectDetail.getTemplateIds().contains(templateId)) {
                        templateAssginedToProjects.add(projectDetail.getProjectId());
                    }
                }
            }
            output("     assigned to projects: " + templateAssginedToProjects);

        }

    }

}
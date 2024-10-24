// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CreateOrUpdateTemplateAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public CreateOrUpdateTemplateAction(UIContext context) {
        super("Create or update template", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> templateIdOpt = getUserInput("Please enter templateId", InputCacheIdentifier.TEMPLATE_ID);
        if (!templateIdOpt.isPresent()) {
            return;
        }

        String templateId = templateIdOpt.get();
        TemplateDefinition templateDefinition = getContext().getAdministration().fetchTemplateOrNull(templateId);
        if (templateDefinition == null) {
            String title = "New template";
            String message = "Please enter template type";
            Optional<TemplateType> templateTypeOpt = getUserInputFromCombobox(title, TemplateType.WEBSCAN_LOGIN, message, TemplateType.values());
            if (!templateTypeOpt.isPresent()) {
                return;
            }
            templateDefinition = new TemplateDefinition();
            templateDefinition.setId(templateId);
            templateDefinition.setType(templateTypeOpt.get());
        }

        Optional<String> templateDefInputOpt = getUserInputFromTextArea("Template definition", templateDefinition.toFormattedJSON());
        if (templateDefInputOpt.isEmpty()) {
            return;
        }

        TemplateDefinition updatedTemplateDefinition = TemplateDefinition.from(templateDefInputOpt.get());

        getContext().getAdministration().createOrUpdateTemplate(templateId, updatedTemplateDefinition);
    }

}
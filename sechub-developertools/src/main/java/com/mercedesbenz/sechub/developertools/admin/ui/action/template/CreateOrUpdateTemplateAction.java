// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariableValidation;
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

        String dialogTitle = null;
        String templateId = templateIdOpt.get();
        TemplateDefinition templateDefinition = getContext().getAdministration().fetchTemplateOrNull(templateId);
        if (templateDefinition == null) {
            /* we create an example here */

            String title = "Create new template:" + templateId;
            String message = "Please enter template type";
            Optional<TemplateType> templateTypeOpt = getUserInputFromCombobox(title, TemplateType.WEBSCAN_LOGIN, message, TemplateType.values());
            if (!templateTypeOpt.isPresent()) {
                return;
            }
            templateDefinition = TemplateDefinition.builder().templateId(templateId).templateType(templateTypeOpt.get()).assetId("example-asset-id").build();
            TemplateVariable exampleVariable = new TemplateVariable();
            exampleVariable.setName("example-variable");
            exampleVariable.setOptional(true);
            TemplateVariableValidation validation = new TemplateVariableValidation();
            validation.setMinLength(2);
            validation.setMaxLength(100);
            validation.setRegularExpression("[0-9a-z].*");

            exampleVariable.setValidation(validation);
            templateDefinition.getVariables().add(exampleVariable);

            dialogTitle = "New Template:" + templateId + " (by example)";

        } else {
            dialogTitle = "Change existing template:" + templateId;
        }

        Optional<String> templateDefInputOpt = getUserInputFromTextArea(dialogTitle, templateDefinition.toFormattedJSON());
        if (templateDefInputOpt.isEmpty()) {
            return;
        }

        TemplateDefinition updatedTemplateDefinition = TemplateDefinition.from(templateDefInputOpt.get());

        getContext().getAdministration().createOrUpdateTemplate(templateId, updatedTemplateDefinition);
    }

}
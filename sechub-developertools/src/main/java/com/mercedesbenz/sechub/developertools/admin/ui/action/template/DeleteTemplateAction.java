// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DeleteTemplateAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public DeleteTemplateAction(UIContext context) {
        super("Delete template", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> templateIdOpt = getUserInput("Please enter templateId", InputCacheIdentifier.TEMPLATE_ID);
        if (!templateIdOpt.isPresent()) {
            return;
        }
        String templateId = templateIdOpt.get();
        boolean confirmed = confirm("Do you really want to delete template '" + templateId + "' ?");
        if (!confirmed) {
            output("Canceled by user");
            return;
        }
        getContext().getAdministration().deleteTemplate(templateId);

        outputAsTextOnSuccess("Template: " + templateId + " was deleted!");
    }

}
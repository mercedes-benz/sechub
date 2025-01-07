// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;
import java.util.List;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class FetchAllTemplateIdentifiersAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public FetchAllTemplateIdentifiersAction(UIContext context) {
        super("Fetch all template identifiers", context);
    }

    @Override
    public void execute(ActionEvent e) {
        List<String> identifiers = getContext().getAdministration().fetchAllTemplateIdentifiers();
        output("Found template identiiers:\n" + identifiers.toString());
    }

}
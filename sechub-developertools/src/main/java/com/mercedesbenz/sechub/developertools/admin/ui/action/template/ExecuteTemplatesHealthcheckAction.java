// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.template;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ExecuteTemplatesHealthcheckAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ExecuteTemplatesHealthcheckAction(UIContext context) {
        super("Execute templates healthcheck", context);
    }

    @Override
    public void execute(ActionEvent e) {

        String data = getContext().getAdministration().executeTemplatesHealthCheck();
        outputAsBeautifiedJSONOnSuccess(data);
    }

}
// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class GetJobInfoListForUserAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public GetJobInfoListForUserAction(UIContext context) {
        super("Get job info list for user", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter project id", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }
        Optional<String> pageSize = getUserInput("Please enter page size", InputCacheIdentifier.PAGE_SIZE);
        if (!pageSize.isPresent()) {
            return;
        }
        Optional<String> page = getUserInput("Please enter wanted page", InputCacheIdentifier.PAGE);
        if (!page.isPresent()) {
            return;
        }
        String data = getContext().getAdministration().fetchProjectJobInfoForUser(projectId.get(), Integer.valueOf(pageSize.get()),
                Integer.valueOf(page.get()));
        outputAsBeautifiedJSONOnSuccess(data);
    }

}
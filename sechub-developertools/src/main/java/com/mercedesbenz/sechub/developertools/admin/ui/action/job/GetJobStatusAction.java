// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class GetJobStatusAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public GetJobStatusAction(UIContext context) {
        super("Get job status", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter project id", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }
        Optional<String> jobUUID = getUserInput("Please enter job uuid", InputCacheIdentifier.JOB_UUID);
        if (!jobUUID.isPresent()) {
            return;
        }
        String data = getContext().getAdministration().fetchJobStatus(projectId.get(), jobUUID.get());
        outputAsBeautifiedJSONOnSuccess(data);
    }

}
// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CheckPDSJobResultOrErrorAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public CheckPDSJobResultOrErrorAction(UIContext context) {
        super("Show PDS job result or error", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        Optional<String> jobUUID = getUserInput("PDS job UUID", InputCacheIdentifier.PDS_JOBUUID);
        if (!jobUUID.isPresent()) {
            output("Canceled jobUUID");
            return;
        }
        String jobStatusOrError = pds.getJobResultOrError(jobUUID.get());
        outputAsBeautifiedJSONOnSuccess(jobStatusOrError);

    }

}
// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CheckPDSJobStatusAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public CheckPDSJobStatusAction(UIContext context) {
        super("Show PDS job status", context);
    }

    @Override
    protected void executePDS(String server, int port, String userId, String apiToken) {
        Optional<String> jobUUID = getUserInput("PDS job UUID",InputCacheIdentifier.PDS_JOBUUID);
        if (!jobUUID.isPresent()) {
            output("Canceled jobUUID");
            return;
        }
        String jobStatusOrError = getContext().getAdministration().pds(server, port, userId, apiToken).getJobStatus(jobUUID.get());
        outputAsBeautifiedJSONOnSuccess(jobStatusOrError);

    }

}
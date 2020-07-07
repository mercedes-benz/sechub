// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import com.daimler.sechub.developertools.admin.ui.UIContext;

public class CheckPDSAliveAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public CheckPDSAliveAction(UIContext context) {
        super("Check PDS server alive", context);
    }

    @Override
    protected void executePDS(String server, int port, String userId, String apiToken) {
        String configuration = getContext().getAdministration().pds(server, port, userId, apiToken).getServerAlive();
        outputAsBeautifiedJSONOnSuccess(configuration);

    }

}
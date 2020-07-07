// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import com.daimler.sechub.developertools.admin.ui.UIContext;

public class CheckPDSExecutionStatusAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public CheckPDSExecutionStatusAction(UIContext context) {
        super("Show PDS execution status", context);
    }

    @Override
    protected void executePDS(String server, int port, String userId, String apiToken) {
        String executionStatus = getContext().getAdministration().pds(server, port, userId, apiToken).getExecutionStatus();
        outputAsBeautifiedJSONOnSuccess(executionStatus);
    }

}
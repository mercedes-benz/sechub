// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;

public class FetchPDSMonitoringStatusAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public FetchPDSMonitoringStatusAction(UIContext context) {
        super("Fetch PDS monitoring status", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        String executionStatus = pds.getExecutionStatus();
        outputAsBeautifiedJSONOnSuccess(executionStatus);
    }

}
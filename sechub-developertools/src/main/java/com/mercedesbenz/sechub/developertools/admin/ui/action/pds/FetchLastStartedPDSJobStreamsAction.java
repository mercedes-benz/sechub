// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.util.UUID;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;

public class FetchLastStartedPDSJobStreamsAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public FetchLastStartedPDSJobStreamsAction(UIContext context) {
        super("Fetch last started PDS Job streams", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {

        UUID lastPDSjobUUID = pds.getUUIDOfLastStartedJob();
        StringBuilder sb = new StringBuilder();
        if (lastPDSjobUUID == null) {
            sb.append("PDS has not started any job!");
        } else {
            sb.append("Last started PDS job:" + lastPDSjobUUID);
            sb.append("\nOutput-Stream:\n------------------------------\n");
            String result1 = pds.getJobOutputStream(lastPDSjobUUID);
            sb.append(result1);
            sb.append("\nError-Stream:\n------------------------------\n");
            String result2 = pds.getJobErrorStream(lastPDSjobUUID);
            sb.append(result2);

        }
        outputAsTextOnSuccess(sb.toString());

    }

}
// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class FetchPDSJobMessagesAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public FetchPDSJobMessagesAction(UIContext context) {
        super("Fetch PDS Job messages", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {

        Optional<String> pdsJobUUID = getUserInput("PDS job uuid", InputCacheIdentifier.PDS_JOBUUID);
        if (!pdsJobUUID.isPresent()) {
            output("canceled - pds job uuid not set");
            return;
        }
        String result = pds.getJobMessages(UUID.fromString(pdsJobUUID.get()));

        outputAsTextOnSuccess(result);

    }

}
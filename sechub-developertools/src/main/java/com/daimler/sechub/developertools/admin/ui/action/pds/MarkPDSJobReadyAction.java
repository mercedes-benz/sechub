// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.util.Optional;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class MarkPDSJobReadyAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public MarkPDSJobReadyAction(UIContext context) {
        super("Mark PDS job as ready to start", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        Optional<String> pdsJobUUID = getUserInput("PDS job uuid", InputCacheIdentifier.PDS_JOBUUID);
        if (!pdsJobUUID.isPresent()) {
            output("cancel pds job uuid");
            return;
        }
        String result = pds.markJobAsReadyToStart(UUID.fromString(pdsJobUUID.get()));
        
        outputAsTextOnSuccess(result);

    }

}
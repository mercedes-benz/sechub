// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;

public class FetchPDSConfigurationAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public FetchPDSConfigurationAction(UIContext context) {
        super("Fetch PDS server configuration", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        String configuration = pds.fetchServerConfigurationAsString();
        outputAsBeautifiedJSONOnSuccess(configuration);

    }

}
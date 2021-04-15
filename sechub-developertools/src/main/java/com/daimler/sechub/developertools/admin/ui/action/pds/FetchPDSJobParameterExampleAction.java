// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;

public class FetchPDSJobParameterExampleAction extends AbstractCreatePDSExamplePropertiesAction {
    private static final long serialVersionUID = 1L;
    
    public FetchPDSJobParameterExampleAction(UIContext context) {
        super("Fetch PDS job parameter example configuration", context);
    }

    @Override
    protected void handleExamples(CreatePDSData data, PDSAdministration pds) {
        getContext().getOutputUI().output(data.jobParametersAsString);
    }


}
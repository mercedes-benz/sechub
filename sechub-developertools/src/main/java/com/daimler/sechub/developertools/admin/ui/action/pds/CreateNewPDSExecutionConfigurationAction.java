// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.util.UUID;

import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.config.ExecutorConfigDialogUI;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class CreateNewPDSExecutionConfigurationAction extends AbstractCreatePDSExamplePropertiesAction {
    private static final long serialVersionUID = 1L;
    
    public CreateNewPDSExecutionConfigurationAction(UIContext context) {
        super("Create new PDS execution config", context);
    }

    @Override
    protected void handleExamples(CreatePDSData data, PDSAdministration pds) {
        TestExecutorConfig config = new TestExecutorConfig();
         
        config.executorVersion=1;
        config.productIdentifier=pds.findProductIdentifier(data.serverConfig, data.productId).name();
        config.setup.baseURL=pds.getUrlBuilder().buildBaseURL();
        config.setup.credentials.user="env:EXAMPLE_USER_CHANGEME";
        config.setup.credentials.password="env:EXAMPLE_PASSWORD_CHANGEME";
        
        ExecutorConfigDialogUI ui = new ExecutorConfigDialogUI(getContext(),"Create NEW PDS executor config",config) {
            @Override
            protected String resolveInitialJobParamsAsString() {
                // we override this to have initial all comments inside...
                return data.jobParametersAsString;
            }
        };
        ui.showDialog();
        
        if (!ui.isOkPressed()) {
            return;
        }
        
        UUID uuid = getContext().getAdministration().createExecutorConfig(ui.getUpdatedConfig());
        outputAsTextOnSuccess("executor config created with uuid:"+uuid);
        
    }


}
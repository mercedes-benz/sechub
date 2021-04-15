// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.developerbatchops;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.developertools.admin.ui.util.DataCollectorUtils;
import com.daimler.sechub.integrationtest.api.TestExecutorProductIdentifier;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class DeveloperBatchCreateCheckmarxTestSetupAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public DeveloperBatchCreateCheckmarxTestSetupAction(UIContext context) {
		super("Create checkmarx test setup",context);
		tooltip("Will create a checkmarx installs setup (project,profile,config,relations etc. ready to scan");
	}

	@Override
	public void execute(ActionEvent e) {
	    
	    Optional<String> projectId = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }
        Optional<String> owner = getUserInput("Please enter owner user id (must exist)", InputCacheIdentifier.USERNAME);
        if (!owner.isPresent()) {
            return;
        }
        Map<String, String> metaData = new HashMap<>();
        List<String> whiteListURLs = new ArrayList<>();
        
        /* -------------- */
        /* CREATE PROJECT */
        /* -------------- */
        
        String projecIdentifier = asSecHubId(projectId.get());
        String userId = owner.get().toLowerCase().trim();
        
        String postResult = getContext().getAdministration().createProject(projecIdentifier, "created for just for development purposes",
                userId, whiteListURLs, metaData);
        outputAsBeautifiedJSONOnSuccess(postResult);
        outputAsTextOnSuccess("project created:" + projecIdentifier);
	    
        /* ------------------- */ 
        /* ASSIGN User2Project */
        /* ------------------- */ 
        getContext().getAdministration().assignUserToProject(userId, projecIdentifier);
        
        
        /* ------------------------------ */
        /* CREATE PRODUCT EXECUTOR CONFIG */
        /* ------------------------------ */
        long timeStamp = System.currentTimeMillis();

        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier=TestExecutorProductIdentifier.CHECKMARX.name();
        config.executorVersion=1;
        config.name="tmp-chkmx-cfg-"+timeStamp;
        config.enabled=true;
        config.setup.baseURL="htttps://checkmarx.example.com";
        config.setup.credentials.user="fake-user";
        config.setup.credentials.password="fake-password";
        
        UUID executorConfigUUID = getContext().getAdministration().createExecutorConfig(config);
        
        /* -------------- */
        /* CREATE PROFILE */
        /* -------------- */
        TestExecutionProfile profile = new TestExecutionProfile();
        config  = getContext().getAdministration().fetchExecutorConfiguration(executorConfigUUID);
        profile.id="tmp-profile-"+timeStamp;
        profile.configurations.add(config);
        profile.projectIds.add(projecIdentifier);
        profile.enabled=true;
        
        getContext().getAdministration().createExecutionProfile(profile);
        
        /* ----------------------------- */ 
        /* Show overview of created data */
        /* ----------------------------- */ 

        String info =  DataCollectorUtils.fetchProfileInformationAboutProject(projecIdentifier,getContext());
        
		outputAsTextOnSuccess(info);
	}

}
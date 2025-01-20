// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.client;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.WithSecHubClient;
import com.mercedesbenz.sechub.integrationtest.api.WithSecHubClient.ApiTokenStrategy;
import com.mercedesbenz.sechub.integrationtest.api.WithSecHubClient.ClientWaitMode;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;

public class TriggerSecHubClientSynchronousScanAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public TriggerSecHubClientSynchronousScanAction(UIContext context) {
        super("Start sechubclient scan", context);
        setIcon(getClass().getResource("/icons/material-io/twotone_launch_black_18dp.png"));
    }

    @Override
    public void execute(ActionEvent event) throws Exception {
        /* @formatter:off */
        Optional<String> optionalProject = getUserInput(
                "Please enter project id (or cancel to leave empty)", InputCacheIdentifier.PROJECT_ID
        );

		Optional<String> optionalPath = getUserInput(
		        "Please enter target folder for sechub scan:\n\n"+
		                (optionalProject.isPresent() ? "": "WARN: You didn't define a project. So this folder must contain a sechub.json having projectId defined inside to work!)")+"\nServer, user and apitoken are used by DAUI setup!\n"+
		        "INFO: You can set a a default by system property:\n"+
                ConfigurationSetup.SECHUB_TARGETFOLDER_FOR_SECHUB_CLIENT_SCAN.getSystemPropertyId(), InputCacheIdentifier.CLIENT_SCAN_TARGETFOLDER
        );
		/* @formatter:on */
        if (!optionalPath.isPresent()) {
            return;
        }
        String path = optionalPath.get();
        File file = new File(path);
        if (!file.exists()) {
            warn("File:" + file.getAbsolutePath() + " does not exist!");
            return;
        }
        WithSecHubClient withClient = getContext().getAdministration().withSecHubClientOnDefinedBinPath();
        Map<String, String> environmentVariables = new HashMap<>();
        ExecutionResult result = withClient.startSynchronScanFor(optionalProject.isPresent() ? new TestProject(optionalProject.get()) : null,
                environmentVariables, file, ApiTokenStrategy.HIDEN_BY_ENV, ClientWaitMode.WAIT_WITH_ENV_SETTINGS);

        output("synchronous scan done");
        output("RESULT:");
        output("- exitcode:" + result.getExitCode());
        output("- sechub job UUID:" + result.getSechubJobUUID());
        output("- traffic light:" + result.getTrafficLight());
        output("- last outputline:" + result.getLastOutputLine());
        output("- report file location:" + result.getJSONReportFile());

    }

}
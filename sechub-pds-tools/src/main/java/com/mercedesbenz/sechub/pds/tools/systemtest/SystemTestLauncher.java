// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.systemtest;

import static com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.pds.tools.SystemTestCommand;
import com.mercedesbenz.sechub.systemtest.SystemTestAPI;
import com.mercedesbenz.sechub.systemtest.SystemTestParameters;
import com.mercedesbenz.sechub.systemtest.SystemTestParameters.SystemTestParametersBuilder;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;

public class SystemTestLauncher {

    SystemTestAPI systemTestApi;

    public SystemTestLauncher() {
        systemTestApi = new SystemTestAPI();
    }

    public SystemTestResult launch(SystemTestCommand systemTestCommand) throws IOException {
        SystemTestParametersBuilder builder = SystemTestParameters.builder();

        handleTestConfigurationFile(systemTestCommand, builder);

        handleAdditionalResources(systemTestCommand, builder);
        handleRunModes(systemTestCommand, builder);
        handleTestsToRun(systemTestCommand, builder);
        handleSolutions(systemTestCommand, builder);
        handleWorkspace(systemTestCommand, builder);

        SystemTestParameters parameters = builder.build();

        SystemTestResult result = systemTestApi.runSystemTests(parameters);
        return result;

    }

    private void handleWorkspace(SystemTestCommand systemTestCommand, SystemTestParametersBuilder builder) {
        String workspacePath = systemTestCommand.getWorkspaceFolder();
        if (isNotEmpty(workspacePath)) {
            builder.workspacePath(workspacePath);
        }

    }

    private void handleSolutions(SystemTestCommand systemTestCommand, SystemTestParametersBuilder builder) {
        String pdsSolutionsRootFolder = systemTestCommand.getPdsSolutionsRootFolder();
        if (isNotEmpty(pdsSolutionsRootFolder)) {
            builder.pdsSolutionPath(pdsSolutionsRootFolder);
        }

        String sechubSolutionRootFolder = systemTestCommand.getSecHubSolutionRootFolder();
        if (isNotEmpty(sechubSolutionRootFolder)) {
            builder.secHubSolutionPath(sechubSolutionRootFolder);
        }

    }

    private void handleRunModes(SystemTestCommand systemTestCommand, SystemTestParametersBuilder builder) {
        if (systemTestCommand.isDryRun()) {
            builder.dryRun();
        }

        if (systemTestCommand.isRemoteRun()) {
            builder.remoteRun();
        } else {
            builder.localRun();
        }
    }

    private void handleTestsToRun(SystemTestCommand systemTestCommand, SystemTestParametersBuilder builder) {

        List<String> testsToRun = systemTestCommand.getTestsToRun();
        if (testsToRun != null) {
            builder.testsToRun(testsToRun.toArray(new String[testsToRun.size()]));
        }
    }

    private void handleAdditionalResources(SystemTestCommand systemTestCommand, SystemTestParametersBuilder builder) {
        String addtionalResourcesFolder = systemTestCommand.getAdditionalResourcesFolder();
        if (isNotEmpty(addtionalResourcesFolder)) {
            builder.additionalResourcesPath(addtionalResourcesFolder);
        }
    }

    private void handleTestConfigurationFile(SystemTestCommand systemTestCommand, SystemTestParametersBuilder builder)
            throws FileNotFoundException, IOException {
        String configFile = systemTestCommand.getPathToConfigFile();
        if (configFile == null) {
            throw new IllegalArgumentException("config file not defined!");
        }
        TextFileReader reader = new TextFileReader();
        File file = new File(configFile);
        if (!file.exists()) {
            throw new FileNotFoundException("Test configuration file does not exist:" + file.getAbsolutePath());
        }
        String configAsText = reader.readTextFromFile(file);
        SystemTestConfiguration testConfiguration = JSONConverter.get().fromJSON(SystemTestConfiguration.class, configAsText);

        builder.testConfiguration(testConfiguration);
    }

}

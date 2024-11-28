// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;

public class SecHubScanConfigProvider {
    public SecHubScanConfiguration fetchSecHubScanConfiguration(File secHubConfigFile, EnvironmentVariableReader environmentVariableReader) {
        if (secHubConfigFile != null) {
            TextFileReader fileReader = new TextFileReader();
            try {
                String sechubConfigJson = fileReader.readTextFromFile(secHubConfigFile);
                return SecHubScanConfiguration.createFromJSON(sechubConfigJson);
            } catch (IOException e) {
                throw new ZapWrapperRuntimeException("Was not able to read sechub config file: " + secHubConfigFile, e, ZapWrapperExitCode.IO_ERROR);
            }
        } else if (environmentVariableReader != null) {
            String sechubConfigJson = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION);

            if (sechubConfigJson == null) {
                return new SecHubScanConfiguration();
            }
            return SecHubScanConfiguration.createFromJSON(sechubConfigJson);
        }
        return new SecHubScanConfiguration();
    }
}

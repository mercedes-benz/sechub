// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;

class SecHubScanConfigProvider {
    SecHubScanConfiguration fetchSecHubScanConfiguration(File secHubConfigFile, EnvironmentVariableReader environmentVariableReader) {
        requireNonNull(environmentVariableReader, "The EnvironmentVariableReader must never be null!");
        if (secHubConfigFile != null) {
            TextFileReader fileReader = new TextFileReader();
            try {
                String sechubConfigJson = fileReader.readTextFromFile(secHubConfigFile);
                return SecHubScanConfiguration.createFromJSON(sechubConfigJson);
            } catch (IOException e) {
                throw new ZapWrapperRuntimeException("Was not able to read sechub config file: " + secHubConfigFile, e, ZapWrapperExitCode.IO_ERROR);
            }
        }
        String sechubConfigJson = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION);
        if (sechubConfigJson == null || sechubConfigJson.isBlank()) {
            return new SecHubScanConfiguration();
        }
        return SecHubScanConfiguration.createFromJSON(sechubConfigJson);
    }
}

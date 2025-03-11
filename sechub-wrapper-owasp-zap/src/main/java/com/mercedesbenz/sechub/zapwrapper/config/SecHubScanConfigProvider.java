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
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableToSystemPropertyConverter;

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
        String environmentVariableName = EnvironmentVariableConstants.PDS_SCAN_CONFIGURATION;
        String sechubConfigJson = environmentVariableReader.readAsString(environmentVariableName);
        if (sechubConfigJson == null || sechubConfigJson.isBlank()) {
            EnvironmentVariableToSystemPropertyConverter converter = new EnvironmentVariableToSystemPropertyConverter();
            /*
             * last step - suitable for unit tests - check if a system property similar to
             * the env variable is set
             */
            sechubConfigJson = System.getProperty(converter.convertEnvironmentVariableToSystemPropertyKey(environmentVariableName));
        }

        if (sechubConfigJson == null || sechubConfigJson.isBlank()) {
            return new SecHubScanConfiguration();
        }
        return SecHubScanConfiguration.createFromJSON(sechubConfigJson);
    }
}

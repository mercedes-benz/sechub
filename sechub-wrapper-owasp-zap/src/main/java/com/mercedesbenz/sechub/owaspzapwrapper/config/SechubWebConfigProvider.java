// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

public class SechubWebConfigProvider {

    public SecHubWebScanConfiguration getSecHubWebConfiguration(File secHubConfigFile) {
        TextFileReader fileReader = new TextFileReader();

        if (secHubConfigFile == null) {
            // can happen when an unauthenticated scan is started with only the target URL
            return new SecHubWebScanConfiguration();
        }
        String sechubConfigJson;
        SecHubScanConfiguration sechubConfig;
        try {
            sechubConfigJson = fileReader.loadTextFile(secHubConfigFile);
            sechubConfig = SecHubScanConfiguration.createFromJSON(sechubConfigJson);
        } catch (IOException e) {
            throw new MustExitRuntimeException("Was not able to read sechub config file: " + secHubConfigFile, e, MustExitCode.SECHUB_CONFIGURATION_INVALID);
        }
        return getSecHubWebConfiguration(sechubConfig);
    }

    private SecHubWebScanConfiguration getSecHubWebConfiguration(SecHubScanConfiguration sechubConfig) {
        if (sechubConfig == null) {
            throw new MustExitRuntimeException("SecHub web scan configuration may not be null at this point.", MustExitCode.SECHUB_CONFIGURATION_INVALID);
        }
        return sechubConfig.getWebScan().get();
    }
}

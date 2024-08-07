// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

public class SecHubScanConfigProvider {
    public SecHubScanConfiguration getSecHubWebConfiguration(File secHubConfigFile) {
        if (secHubConfigFile == null) {
            return new SecHubScanConfiguration();
        }
        TextFileReader fileReader = new TextFileReader();

        String sechubConfigJson;
        SecHubScanConfiguration sechubScanConfig;
        try {
            sechubConfigJson = fileReader.readTextFromFile(secHubConfigFile);
            sechubScanConfig = SecHubScanConfiguration.createFromJSON(sechubConfigJson);
        } catch (IOException e) {
            throw new ZapWrapperRuntimeException("Was not able to read sechub config file: " + secHubConfigFile, e, ZapWrapperExitCode.IO_ERROR);
        }
        return sechubScanConfig;
    }
}

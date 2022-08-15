package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

public class SecHubScanConfigProvider {
    public SecHubScanConfiguration getSecHubWebConfiguration(File secHubConfigFile) {
        TextFileReader fileReader = new TextFileReader();

        if (secHubConfigFile == null) {
            return new SecHubScanConfiguration();
        }
        String sechubConfigJson;
        SecHubScanConfiguration sechubScanConfig;
        try {
            sechubConfigJson = fileReader.loadTextFile(secHubConfigFile);
            sechubScanConfig = SecHubScanConfiguration.createFromJSON(sechubConfigJson);
        } catch (IOException e) {
            throw new MustExitRuntimeException("Was not able to read sechub config file: " + secHubConfigFile, e, MustExitCode.SECHUB_CONFIGURATION_INVALID);
        }
        return sechubScanConfig;
    }
}

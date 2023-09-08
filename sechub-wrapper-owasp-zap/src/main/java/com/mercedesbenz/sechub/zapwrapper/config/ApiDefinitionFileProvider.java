// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

public class ApiDefinitionFileProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ApiDefinitionFileProvider.class);

    /**
     *
     * This method takes the extracted sources folder path and the SecHub scan
     * configuration (both usually provided by SecHub) and fetches all API
     * definitions files uploaded for the current scan.
     *
     * @param extractedSourcesFolderPath
     * @param sechubConfig
     * @return Unmodifiable list of API definition files or an unmodifiable empty
     *         list no API definition files where found.
     */
    public List<File> fetchApiDefinitionFiles(String extractedSourcesFolderPath, SecHubScanConfiguration sechubConfig) {

        if (extractedSourcesFolderPath == null) {
            LOG.info("Extracted sources folder path env variable was not set.");
            return Collections.emptyList();
        }
        if (!isValidConfigWithDataSection(sechubConfig)) {
            return Collections.emptyList();
        }

        SecHubWebScanConfiguration secHubWebScanConfiguration = sechubConfig.getWebScan().get();
        if (secHubWebScanConfiguration.getApi().isEmpty()) {
            LOG.info("No API definition was configured for the webscan. Continuing without API definition");
            return Collections.emptyList();
        }

        SecHubWebScanApiConfiguration secHubWebScanApiConfiguration = secHubWebScanConfiguration.getApi().get();

        Set<String> namesOfUsedDataConfigurationObjects = secHubWebScanApiConfiguration.getNamesOfUsedDataConfigurationObjects();
        List<SecHubSourceDataConfiguration> sourceData = sechubConfig.getData().get().getSources();

        List<File> apiFiles = new LinkedList<>();
        LOG.info("Collecting all {} definitions files.", secHubWebScanApiConfiguration.getType().name());
        for (String use : namesOfUsedDataConfigurationObjects) {
            if (use == null) {
                continue;
            }
            for (SecHubSourceDataConfiguration dataConfig : sourceData) {
                if (!use.equals(dataConfig.getUniqueName())) {
                    continue;
                }

                if (dataConfig.getFileSystem().isEmpty()) {
                    continue;
                }
                List<String> files = dataConfig.getFileSystem().get().getFiles();
                for (String file : files) {
                    apiFiles.add(new File(extractedSourcesFolderPath, file));
                }
            }
        }
        return Collections.unmodifiableList(apiFiles);
    }

    private boolean isValidConfigWithDataSection(SecHubScanConfiguration sechubConfig) {
        if (sechubConfig == null) {
            LOG.info("SecHub scan configuration was not set.");
            return false;
        }
        if (sechubConfig.getWebScan().isEmpty()) {
            LOG.info("Cannot read API definition, because no webscan was configured.");
            return false;
        }
        if (sechubConfig.getData().isEmpty()) {
            LOG.info("No data section was found. Continuing without API definition.");
            return false;
        }
        return true;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.ClientCertificateConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

public class ZapWrapperDataSectionFileProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ZapWrapperDataSectionFileProvider.class);

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
        if (!isValidWebScanConfigWithDataSection(sechubConfig)) {
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
                // Continue if this is NOT the correct data section
                if (!use.equals(dataConfig.getUniqueName())) {
                    continue;
                }

                if (dataConfig.getFileSystem().isEmpty()) {
                    continue;
                }
                List<String> files = dataConfig.getFileSystem().get().getFiles();
                for (String file : files) {
                    // Add all files to the list of API definition files
                    apiFiles.add(new File(extractedSourcesFolderPath, file));
                }
            }
        }
        return Collections.unmodifiableList(apiFiles);
    }

    /**
     * This method takes the extracted sources folder path and the SecHub scan
     * configuration (both usually provided by SecHub) and fetches the first client
     * certificate file uploaded for the current scan.
     *
     * @param extractedSourcesFolderPath
     * @param sechubConfig
     * @return
     */
    public File fetchClientCertificateFile(String extractedSourcesFolderPath, SecHubScanConfiguration sechubConfig) {
        if (extractedSourcesFolderPath == null) {
            LOG.info("Extracted sources folder path env variable was not set.");
            return null;
        }
        if (!isValidWebScanConfigWithDataSection(sechubConfig)) {
            return null;
        }

        SecHubWebScanConfiguration secHubWebScanConfiguration = sechubConfig.getWebScan().get();
        if (secHubWebScanConfiguration.getClientCertificate().isEmpty()) {
            LOG.info("No client certificate configuration was configured for the webscan. Continuing without client certificate configuration.");
            return null;
        }

        ClientCertificateConfiguration clientCertificateConfiguration = secHubWebScanConfiguration.getClientCertificate().get();
        Set<String> namesOfUsedDataConfigurationObjects = clientCertificateConfiguration.getNamesOfUsedDataConfigurationObjects();
        List<SecHubSourceDataConfiguration> sourceData = sechubConfig.getData().get().getSources();

        LOG.info("Fetch client certificate file.");
        for (String use : namesOfUsedDataConfigurationObjects) {
            if (use == null) {
                continue;
            }
            for (SecHubSourceDataConfiguration dataConfig : sourceData) {
                // Continue if this is NOT the correct data section
                if (!use.equals(dataConfig.getUniqueName())) {
                    continue;
                }

                if (dataConfig.getFileSystem().isEmpty()) {
                    continue;
                }
                List<String> files = dataConfig.getFileSystem().get().getFiles();
                for (String file : files) {
                    // we can only handle a single client certificate file, so we take the first one
                    // we get
                    return new File(extractedSourcesFolderPath, file);
                }
            }
        }
        return null;
    }

    private boolean isValidWebScanConfigWithDataSection(SecHubScanConfiguration sechubConfig) {
        if (sechubConfig == null) {
            LOG.info("SecHub scan configuration was not set.");
            return false;
        }
        if (sechubConfig.getWebScan().isEmpty()) {
            LOG.info("No webscan was configured inside the SecHub config.");
            return false;
        }
        if (sechubConfig.getData().isEmpty()) {
            LOG.info("No data section was found.");
            return false;
        }
        return true;
    }

}

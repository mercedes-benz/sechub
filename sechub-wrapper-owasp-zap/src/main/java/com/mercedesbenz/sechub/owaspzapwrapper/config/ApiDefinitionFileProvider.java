// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

public class ApiDefinitionFileProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ApiDefinitionFileProvider.class);

    /**
     *
     * This method takes the extracted sources folder path and the SecHub scan
     * configuration (both usually provided by SecHub). It makes sure, that exactly
     * one file is provided to use as file containing the API definition, since we
     * currently allow only a single file.
     *
     * @param extractedSourcesFolderPath
     * @param sechubConfig
     * @return Path to API definition file or <code>null</code> if parameters are
     *         null or no data section is found
     */
    public Path fetchApiDefinitionFile(String extractedSourcesFolderPath, SecHubScanConfiguration sechubConfig) {
        if (extractedSourcesFolderPath == null) {
            LOG.info("Extracted sources folder path env variable was not set.");
            return null;
        }
        if (sechubConfig == null) {
            LOG.info("SecHub scan configuration was not set.");
            return null;
        }

        if (!sechubConfig.getData().isPresent()) {
            LOG.info("No data section was found. Continuing without searching for API definition.");
            return null;
        }

        List<SecHubSourceDataConfiguration> sourceData = sechubConfig.getData().get().getSources();
        if (sourceData.size() != 1) {
            throw new ZapWrapperRuntimeException("Sources must contain exactly 1 entry.", ZapWrapperExitCode.SECHUB_CONFIGURATION_INVALID);
        }

        if (!sourceData.get(0).getFileSystem().isPresent()) {
            throw new ZapWrapperRuntimeException("Sources filesystem part must be set at this stage.", ZapWrapperExitCode.SECHUB_CONFIGURATION_INVALID);
        }

        List<String> files = sourceData.get(0).getFileSystem().get().getFiles();
        if (files.size() != 1) {
            throw new ZapWrapperRuntimeException("Sources filesystem files part must contain exactly 1 entry.",
                    ZapWrapperExitCode.SECHUB_CONFIGURATION_INVALID);
        }

        File result = new File(extractedSourcesFolderPath, files.get(0));
        return result.toPath();
    }

}

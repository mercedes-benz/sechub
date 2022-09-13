// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

public class ApiDefinitionFileProvider {

    /**
     *
     * This method takes the extracted sources folder path and the SecHub scan
     * configuration (both usually provided by SecHub). It makes sure, that exactly
     * one file is provided to use as file containing the API definition, since we
     * currently allow only a single file.
     *
     * @param extractedSourcesFolderPath
     * @param sechubConfig
     * @return Path to API definition file
     */
    public Path fetchApiDefinitionFile(String extractedSourcesFolderPath, SecHubScanConfiguration sechubConfig) {
        if (extractedSourcesFolderPath == null) {
            throw new ZapWrapperRuntimeException("Sources folder must not be null!", ZapWrapperExitCode.EXECUTION_FAILED);
        }
        if (sechubConfig == null) {
            throw new ZapWrapperRuntimeException("SecHub scan config must not be null!", ZapWrapperExitCode.EXECUTION_FAILED);
        }

        if (!sechubConfig.getData().isPresent()) {
            throw new ZapWrapperRuntimeException("Data section should not be empty since a sources folder was found.",
                    ZapWrapperExitCode.SECHUB_CONFIGURATION_INVALID);
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

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import static java.util.Objects.*;

import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.SecHubBinaryDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;

public class RunSecHubJobDefinitionTransformer {

    /**
     * Transforms a given SecHub job definition to a configuration model
     *
     * @param definition
     * @return {@link SecHubConfigurationModel} instance
     */
    public SecHubConfigurationModel transformToSecHubConfiguration(RunSecHubJobDefinition definition) {
        requireNonNull(definition, "The defintion may not be null!");

        SecHubConfigurationModel config = new SecHubConfigurationModel();

        config.setApiVersion("1.0");
        config.setProjectId(definition.getProject());

        /* adopt scans as they are */
        config.setCodeScan(definition.getCodeScan().orElse(null));
        config.setWebScan(definition.getWebScan().orElse(null));
        config.setInfraScan(definition.getInfraScan().orElse(null));
        config.setLicenseScan(definition.getLicenseScan().orElse(null));
        config.setSecretScan(definition.getSecretScan().orElse(null));

        SecHubDataConfiguration data = createDataConfiguration(definition);
        config.setData(data);

        return config;
    }

    private SecHubDataConfiguration createDataConfiguration(RunSecHubJobDefinition definition) {
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        List<UploadDefinition> uploads = definition.getUploads();
        for (UploadDefinition upload : uploads) {

            String referenceId = resolveReferenceIdOrFail(upload);
            assertFolderDefined(upload, referenceId);
            assertNoMixupBetweenSourceAndBinaryUpload(upload, referenceId);

            handleSourceFolder(data, upload);
            handleBinaryFolder(data, upload);
        }
        return data;
    }

    private void handleSourceFolder(SecHubDataConfiguration data, UploadDefinition upload) {
        Optional<String> sourceFolderOpt = upload.getSourceFolder();
        if (sourceFolderOpt.isEmpty()) {
            return;
        }
        SecHubSourceDataConfiguration sourceConfig = new SecHubSourceDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        fileSystem.getFolders().add(sourceFolderOpt.get());
        sourceConfig.setFileSystem(fileSystem);
        sourceConfig.setUniqueName(upload.getReferenceId().orElse(null));
        data.getSources().add(sourceConfig);
    }

    private void handleBinaryFolder(SecHubDataConfiguration data, UploadDefinition upload) {
        Optional<String> binaryFolderOpt = upload.getBinariesFolder();
        if (binaryFolderOpt.isEmpty()) {
            return;
        }
        SecHubBinaryDataConfiguration binaryConfig = new SecHubBinaryDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        fileSystem.getFolders().add(binaryFolderOpt.get());
        binaryConfig.setFileSystem(fileSystem);
        binaryConfig.setUniqueName(upload.getReferenceId().orElse(null));
        data.getBinaries().add(binaryConfig);
    }

    private void assertNoMixupBetweenSourceAndBinaryUpload(UploadDefinition upload, String referenceId) {
        if (upload.getSourceFolder().isPresent() && upload.getBinariesFolder().isPresent()) {
            throw new IllegalStateException("Source folder and binary folder defined for same reference id:" + referenceId);
        }
    }

    private void assertFolderDefined(UploadDefinition upload, String referenceId) {
        if (upload.getSourceFolder().isEmpty() && upload.getBinariesFolder().isEmpty()) {
            throw new IllegalStateException("Neither a source folder nor a binary folder is defined for reference id:" + referenceId);
        }
    }

    private String resolveReferenceIdOrFail(UploadDefinition upload) {
        Optional<String> refIdOpt = upload.getReferenceId();
        if (!refIdOpt.isPresent()) {
            throw new IllegalStateException("At this point we must have a reference id for every upload, but there is none!");
        }
        String referenceId = refIdOpt.get();
        return referenceId;
    }

}

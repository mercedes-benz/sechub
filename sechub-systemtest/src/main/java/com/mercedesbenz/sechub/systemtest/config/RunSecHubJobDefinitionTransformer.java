// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import static java.util.Objects.*;

import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.api.internal.gen.model.*;

public class RunSecHubJobDefinitionTransformer {

    /**
     * Transforms a given SecHub job definition to a configuration model
     *
     * @param definition
     * @return {@link SecHubConfiguration} instance
     */
    public SecHubConfiguration transformToSecHubConfiguration(RunSecHubJobDefinition definition) {
        requireNonNull(definition, "The defintion may not be null!");

        SecHubConfiguration config = new SecHubConfiguration();

        config.setApiVersion("1.0");
        config.setProjectId(definition.getProject());

        /* adopt scans as they are */
        config.setCodeScan(definition.getCodeScan().orElse(null));
        config.setWebScan(definition.getWebScan().orElse(null));
        config.setInfraScan(definition.getInfraScan().orElse(null));
        config.setLicenseScan(definition.getLicenseScan().orElse(null));
        config.setSecretScan(definition.getSecretScan().orElse(null));
        config.setIacScan(definition.getIacScan().orElse(null));

        SecHubDataConfiguration data = createDataConfiguration(definition);
        config.setData(data);

        return config;
    }

    private SecHubDataConfiguration createDataConfiguration(RunSecHubJobDefinition definition) {
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        List<UploadDefinition> uploads = definition.getUploads();
        for (UploadDefinition upload : uploads) {

            String referenceId = resolveReferenceIdOrFail(upload);
            assertFolderOrFileDefined(upload, referenceId);
            assertNoMixupBetweenSourceAndBinaryUpload(upload, referenceId);

            handleSources(data, upload);
            handleBinaries(data, upload);
        }
        return data;
    }

    private void handleSources(SecHubDataConfiguration data, UploadDefinition upload) {
        Optional<String> sourceFolderOpt = upload.getSourceFolder();
        Optional<String> sourceFileOpt = upload.getSourceFile();
        if (sourceFolderOpt.isEmpty() && sourceFileOpt.isEmpty()) {
            return;
        }
        SecHubSourceDataConfiguration sourceConfig = new SecHubSourceDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        if (sourceFolderOpt.isPresent()) {
            fileSystem.getFolders().add(sourceFolderOpt.get());
        }
        if (sourceFileOpt.isPresent()) {
            fileSystem.getFiles().add(sourceFileOpt.get());
        }
        sourceConfig.setFileSystem(fileSystem);
        sourceConfig.setName(upload.getReferenceId().orElse(null));
        data.getSources().add(sourceConfig);
    }

    private void handleBinaries(SecHubDataConfiguration data, UploadDefinition upload) {
        Optional<String> binaryFolderOpt = upload.getBinariesFolder();
        Optional<String> binaryFileOpt = upload.getBinaryFile();
        if (binaryFolderOpt.isEmpty() && binaryFileOpt.isEmpty()) {
            return;
        }
        SecHubBinaryDataConfiguration binaryConfig = new SecHubBinaryDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        if (binaryFolderOpt.isPresent()) {
            fileSystem.getFolders().add(binaryFolderOpt.get());
        }
        if (binaryFileOpt.isPresent()) {
            fileSystem.getFiles().add(binaryFileOpt.get());
        }
        binaryConfig.setFileSystem(fileSystem);
        binaryConfig.setName(upload.getReferenceId().orElse(null));
        data.getBinaries().add(binaryConfig);
    }

    private void assertNoMixupBetweenSourceAndBinaryUpload(UploadDefinition upload, String referenceId) {

        boolean referenceIsForSourcey = upload.getSourceFolder().isPresent() || upload.getSourceFile().isPresent();
        boolean referenceIsForBinary = upload.getBinariesFolder().isPresent() || upload.getBinaryFile().isPresent();

        if (referenceIsForSourcey && referenceIsForBinary) {
            throw new IllegalStateException("Source folder/file and binary folder/file defined for same reference id:" + referenceId);
        }
    }

    private void assertFolderOrFileDefined(UploadDefinition upload, String referenceId) {

        boolean atleastOneDefined = false;

        atleastOneDefined = atleastOneDefined || upload.getSourceFolder().isPresent();
        atleastOneDefined = atleastOneDefined || upload.getSourceFile().isPresent();
        atleastOneDefined = atleastOneDefined || upload.getBinariesFolder().isPresent();
        atleastOneDefined = atleastOneDefined || upload.getBinaryFile().isPresent();

        if (!atleastOneDefined) {
            throw new IllegalStateException("Neither source folder, binary folder, source file or binary file is defined for reference id:" + referenceId);
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

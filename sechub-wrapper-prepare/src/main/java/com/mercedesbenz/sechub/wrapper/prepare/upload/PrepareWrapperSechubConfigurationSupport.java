// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareToolContext;

@Component
public class PrepareWrapperSechubConfigurationSupport {

    private final FileNameSupport fileNameSupport;

    private static Logger LOG = LoggerFactory.getLogger(PrepareWrapperSechubConfigurationSupport.class);

    public PrepareWrapperSechubConfigurationSupport(FileNameSupport fileNameSupport) {
        this.fileNameSupport = fileNameSupport;
    }

    public SecHubConfigurationModel replaceRemoteDataWithFilesystem(PrepareWrapperContext context, PrepareToolContext toolContext) {
        SecHubConfigurationModel modifiedModel = context.getSecHubConfiguration();

        if (modifiedModel == null) {
            throw new IllegalArgumentException("SecHubConfigurationModel cannot be null");
        }

        if (modifiedModel.getData().isEmpty()) {
            return modifiedModel;
        }

        if (!modifiedModel.getData().get().getSources().isEmpty()) {
            return replaceRemoteSourcesWithFileSystem(toolContext, modifiedModel);
        }

        if (!modifiedModel.getData().get().getBinaries().isEmpty()) {
            return replaceRemoteBinariesWithFileSystem(toolContext, modifiedModel);
        }

        LOG.warn("No sources or binaries found in configuration");
        return modifiedModel;
    }

    private SecHubConfigurationModel replaceRemoteBinariesWithFileSystem(PrepareToolContext context, SecHubConfigurationModel modifiedModel) {
        List<Path> tarFiles = fileNameSupport.getTarFilesFromDirectory(context.getToolDownloadDirectory());
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();

        for (Path tarFile : tarFiles) {
            String tarFilename = tarFile.getFileName().toString();
            fileSystemConfiguration.getFolders().add(tarFilename);
        }

        modifiedModel.getData().get().getBinaries().forEach(binary -> {
            binary.setRemote(null);
            binary.setFileSystem(fileSystemConfiguration);
        });
        return modifiedModel;
    }

    private SecHubConfigurationModel replaceRemoteSourcesWithFileSystem(PrepareToolContext context, SecHubConfigurationModel modifiedModel) {
        List<Path> repositories = fileNameSupport.getRepositoriesFromDirectory(context.getToolDownloadDirectory());
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();

        for (Path repository : repositories) {
            String repositoryName = repository.getFileName().toString();
            fileSystemConfiguration.getFolders().add(repositoryName);
        }

        modifiedModel.getData().get().getSources().forEach(source -> {
            source.setRemote(null);
            source.setFileSystem(fileSystemConfiguration);

        });
        return modifiedModel;
    }
}

package com.mercedesbenz.sechub.wrapper.prepare.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Component
public class PrepareWrapperSechubConfigurationSupport {

    @Autowired
    FileSupport fileSupport;

    private static Logger LOG = LoggerFactory.getLogger(PrepareWrapperSechubConfigurationSupport.class);

    public SecHubConfigurationModel replaceRemoteDataWithFilesystem(PrepareWrapperContext context) {
        SecHubConfigurationModel modifiedModel = context.getSecHubConfiguration();

        if (modifiedModel == null) {
            throw new IllegalArgumentException("SecHubConfigurationModel cannot be null");
        }

        if (modifiedModel.getData().isEmpty()) {
            return modifiedModel;
        }

        if (!modifiedModel.getData().get().getSources().isEmpty()) {
            return replaceRemoteSources(context, modifiedModel);
        }

        if (!modifiedModel.getData().get().getBinaries().isEmpty()) {
            return replaceRemoteBinaries(context, modifiedModel);
        }

        LOG.warn("No sources or binaries found in configuration");
        return modifiedModel;
    }

    private SecHubConfigurationModel replaceRemoteBinaries(PrepareWrapperContext context, SecHubConfigurationModel modifiedModel) {
        String tarFilename = fileSupport.getTarFileFromFolder(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();

        tarFilename = getNameFromPath(tarFilename);
        fileSystemConfiguration.getFolders().add(tarFilename);

        modifiedModel.getData().get().getBinaries().forEach(binary -> {
            binary.setRemote(null);
            binary.setFileSystem(fileSystemConfiguration);
        });
        return modifiedModel;
    }

    private SecHubConfigurationModel replaceRemoteSources(PrepareWrapperContext context, SecHubConfigurationModel modifiedModel) {
        String repoName = fileSupport.getSubfolderFromDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        repoName = getNameFromPath(repoName);

        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add(repoName);

        modifiedModel.getData().get().getSources().forEach(source -> {
            source.setRemote(null);
            source.setFileSystem(fileSystemConfiguration);

        });
        return modifiedModel;
    }

    private String getNameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}

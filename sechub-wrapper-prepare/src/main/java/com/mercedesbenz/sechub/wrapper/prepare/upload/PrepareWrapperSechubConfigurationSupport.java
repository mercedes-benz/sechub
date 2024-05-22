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
    FileNameSupport fileNameSupport;

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
            return replaceRemoteSourcesWithFileSystem(context, modifiedModel);
        }

        if (!modifiedModel.getData().get().getBinaries().isEmpty()) {
            return replaceRemoteBinariesWithFileSystem(context, modifiedModel);
        }

        LOG.warn("No sources or binaries found in configuration");
        return modifiedModel;
    }

    private SecHubConfigurationModel replaceRemoteBinariesWithFileSystem(PrepareWrapperContext context, SecHubConfigurationModel modifiedModel) {
        String tarFilename = fileNameSupport.getTarFileNameFromDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();

        tarFilename = getFileNameFromFullPath(tarFilename);
        fileSystemConfiguration.getFolders().add(tarFilename);

        modifiedModel.getData().get().getBinaries().forEach(binary -> {
            binary.setRemote(null);
            binary.setFileSystem(fileSystemConfiguration);
        });
        return modifiedModel;
    }

    private SecHubConfigurationModel replaceRemoteSourcesWithFileSystem(PrepareWrapperContext context, SecHubConfigurationModel modifiedModel) {
        String repoName = fileNameSupport.getSubfolderFileNameFromDirectory(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        repoName = getFileNameFromFullPath(repoName);

        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add(repoName);

        modifiedModel.getData().get().getSources().forEach(source -> {
            source.setRemote(null);
            source.setFileSystem(fileSystemConfiguration);

        });
        return modifiedModel;
    }

    private String getFileNameFromFullPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}

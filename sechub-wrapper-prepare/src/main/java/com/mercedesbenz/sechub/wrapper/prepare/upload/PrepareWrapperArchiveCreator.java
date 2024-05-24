package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public class PrepareWrapperArchiveCreator {

    @Autowired
    ArchiveSupport archiveSupport;

    @Autowired
    PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;

    public void create(PrepareWrapperContext context, ToolContext toolContext) throws IOException {
        // replace remote with filesystem entry in configuration model
        SecHubConfigurationModel model = sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context, toolContext);
        Path uploadDirectory = toolContext.getUploadDirectory();
        Path workingDirectory = toolContext.getToolDownloadDirectory();

        archiveSupport.createArchives(model, workingDirectory, uploadDirectory);
    }

}

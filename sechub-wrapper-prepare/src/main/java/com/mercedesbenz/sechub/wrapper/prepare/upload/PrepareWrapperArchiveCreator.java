package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareToolContext;

@Service
public class PrepareWrapperArchiveCreator {

    @Autowired
    ArchiveSupport archiveSupport;

    @Autowired
    PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;

    public void create(PrepareWrapperContext context, PrepareToolContext abstractToolContext) throws IOException {
        // replace remote with file system entry in configuration model, this
        // is necessary for doing the upload by archive support which uses
        // the file system information
        SecHubConfigurationModel model = sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context, abstractToolContext);

        archiveSupport.createArchives(model, abstractToolContext.getToolDownloadDirectory(), abstractToolContext.getUploadDirectory());
    }

}

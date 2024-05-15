package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public class PrepareWrapperArchiveCreator {

    @Autowired
    ArchiveSupport archiveSupport;

    @Autowired
    PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;

    public void create(PrepareWrapperContext context) throws IOException {
        // Need to replace remote with filesystem entry in model
        SecHubConfigurationModel model = sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context);
        String uploadDirectory = context.getEnvironment().getPdsPrepareUploadFolderDirectory();

        archiveSupport.createArchives(model, Path.of(uploadDirectory), Path.of(uploadDirectory));
    }

}

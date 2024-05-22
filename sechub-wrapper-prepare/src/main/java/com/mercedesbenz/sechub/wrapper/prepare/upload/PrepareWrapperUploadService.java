package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public class PrepareWrapperUploadService {

    @Autowired
    PrepareWrapperArchiveCreator archiveCreator;

    @Autowired
    PrepareWrapperFileUploadService fileUploadService;

    @Autowired
    PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;

    @Autowired
    FileNameSupport fileNameSupport;

    @Autowired
    CheckSumSupport checkSumSupport;

    public void upload(PrepareWrapperContext context) throws IOException {

        // creates archives for sourcecode or binary file
        archiveCreator.create(context);

        String storagePath = context.getEnvironment().getSechubStoragePath();
        UUID sechubJobUUID = UUID.fromString(context.getEnvironment().getSechubJobUUID());

        SecHubConfigurationModel model = sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context);

        if (model.getData().isEmpty()) {
            throw new IllegalArgumentException("SecHubConfigurationModel data is not configured.");
        }

        if (!model.getData().get().getSources().isEmpty()) {
            File file = new File(context.getEnvironment().getPdsPrepareUploadFolderDirectory() + "/sourcecode.zip");
            String checkSum = checkSumSupport.createSha256Checksum(file.getPath());

            fileUploadService.uploadFile(storagePath, sechubJobUUID, file, checkSum);
        }

        if (!model.getData().get().getBinaries().isEmpty()) {
            File file = new File(context.getEnvironment().getPdsPrepareUploadFolderDirectory() + "/binaries.tar");
            String checkSum = checkSumSupport.createSha256Checksum(file.getPath());

            fileUploadService.uploadFile(storagePath, sechubJobUUID, file, checkSum);
        }
    }

}

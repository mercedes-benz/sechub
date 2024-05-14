package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public class PrepareWrapperUploadService {

    @Autowired
    PrepareWrapperArchiveCreator archiveCreator;

    @Autowired
    PrepareWrapperBinaryUploadService binaryUploadService;

    @Autowired
    PrepareWrapperSourceUploadService sourceUploadService;

    @Autowired
    PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;

    @Autowired
    TarFileSupport tarFileSupport;

    public void upload(PrepareWrapperContext context) throws IOException {

        archiveCreator.create(context);
        String storagePath = context.getEnvironment().getSechubStoragePath();
        UUID sechubJobUUID = UUID.fromString(context.getEnvironment().getSechubJobUUID());

        SecHubConfigurationModel model = sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context);

        // TODO: 14.05.24 laura checksum from files
        String checkSum = "checksum";

        if (!model.getData().isPresent()) {
            throw new IllegalArgumentException("SecHubConfigurationModel data is not present");
        }
        if (!model.getData().get().getSources().isEmpty()) {
            File file = new File(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
            sourceUploadService.uploadSourceCode(storagePath, sechubJobUUID, file, checkSum);
        }
        if (!model.getData().get().getBinaries().isEmpty()) {
            File file = new File(tarFileSupport.getTarFileFromFolder(context.getEnvironment().getPdsPrepareUploadFolderDirectory()));
            binaryUploadService.uploadBinaries(storagePath, sechubJobUUID, file, checkSum);
        }
    }

}

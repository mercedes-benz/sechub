package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILENAME_BINARIES_TAR;
import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILENAME_SOURCECODE_ZIP;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;
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

    public void upload(PrepareWrapperContext context, ToolContext toolContext) throws IOException {

        // creates archives for sourcecode or binary file
        archiveCreator.create(context, toolContext);

        String projectId = context.getSecHubConfiguration().getProjectId();
        UUID sechubJobUUID = UUID.fromString(context.getEnvironment().getSechubJobUUID());

        SecHubConfigurationModel model = sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context, toolContext);

        if (model.getData().isEmpty()) {
            throw new IllegalStateException("SecHubConfigurationModel data is not configured.");
        }

        SecHubDataConfiguration data = model.getData().get();

        if (!data.getSources().isEmpty()) {
            File file = new File(toolContext.getUploadDirectory() + "/" + FILENAME_SOURCECODE_ZIP);
            String checkSum = checkSumSupport.createSha256Checksum(file.getPath());

            fileUploadService.uploadFile(projectId, sechubJobUUID, file, checkSum);
        }

        if (!data.getBinaries().isEmpty()) {
            File file = new File(toolContext.getUploadDirectory() + "/" + FILENAME_BINARIES_TAR);
            String checkSum = checkSumSupport.createSha256Checksum(file.getPath());

            fileUploadService.uploadFile(projectId, sechubJobUUID, file, checkSum);
        }
    }

}

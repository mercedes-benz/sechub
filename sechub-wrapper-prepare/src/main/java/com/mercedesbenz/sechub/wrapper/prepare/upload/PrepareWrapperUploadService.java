// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareToolContext;

@Service
public class PrepareWrapperUploadService {

    private final PrepareWrapperArchiveCreator archiveCreator;
    private final PrepareWrapperFileUploadService fileUploadService;
    private final PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;
    private final FileNameSupport fileNameSupport;
    private final CheckSumSupport checkSumSupport;

    public PrepareWrapperUploadService(PrepareWrapperArchiveCreator archiveCreator, PrepareWrapperFileUploadService fileUploadService, PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport, FileNameSupport fileNameSupport, CheckSumSupport checkSumSupport) {
        this.archiveCreator = archiveCreator;
        this.fileUploadService = fileUploadService;
        this.sechubConfigurationSupport = sechubConfigurationSupport;
        this.fileNameSupport = fileNameSupport;
        this.checkSumSupport = checkSumSupport;
    }

    public void upload(PrepareWrapperContext context, PrepareToolContext abstractToolContext) throws IOException {

        // creates archives for sourcecode or binary file
        archiveCreator.create(context, abstractToolContext);

        String projectId = context.getSecHubConfiguration().getProjectId();
        UUID sechubJobUUID = UUID.fromString(context.getEnvironment().getSechubJobUUID());

        SecHubConfigurationModel model = sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context, abstractToolContext);

        if (model.getData().isEmpty()) {
            throw new IllegalStateException("SecHubConfigurationModel data is not configured.");
        }

        SecHubDataConfiguration data = model.getData().get();

        if (!data.getSources().isEmpty()) {
            File file = new File(abstractToolContext.getUploadDirectory() + File.separator + FILENAME_SOURCECODE_ZIP);
            String checkSum = checkSumSupport.createSha256Checksum(file.getPath());

            fileUploadService.uploadFile(projectId, sechubJobUUID, file, checkSum);
        }

        if (!data.getBinaries().isEmpty()) {
            File file = new File(abstractToolContext.getUploadDirectory() + File.separator + FILENAME_BINARIES_TAR);
            String checkSum = checkSumSupport.createSha256Checksum(file.getPath());

            fileUploadService.uploadFile(projectId, sechubJobUUID, file, checkSum);
        }
    }

}

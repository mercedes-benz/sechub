// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.cli;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static com.mercedesbenz.sechub.commons.pds.PDSDefaultRuntimeKeyConstants.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrepareWrapperEnvironment {

    /********************************/
    /* PDS common environment setup */
    /********************************/

    @Value("${" + PARAM_KEY_PDS_SCAN_CONFIGURATION + "}")
    private String sechubConfigurationModelAsJson;

    @Value("${" + RT_KEY_PDS_JOB_RESULT_FILE + "}")
    private String pdsResultFile;

    @Value("${" + RT_KEY_PDS_JOB_USER_MESSAGES_FOLDER + "}")
    private String pdsUserMessagesFolder;

    @Value("${" + PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH + "}")
    private String sechubStoragePath;

    @Value("${" + PARAM_KEY_SECHUB_JOB_UUID + "}")
    private String sechubJobUUID;

    /*********************************/
    /* PDS prepare environment setup */
    /*********************************/

    @Value("${" + KEY_PDS_PREPARE_UPLOAD_DIRECTORY + "}")
    private String pdsPrepareUploadFolderDirectory;

    public String getSechubConfigurationModelAsJson() {
        return sechubConfigurationModelAsJson;
    }

    public String getPdsResultFile() {
        return pdsResultFile;
    }

    public String getPdsUserMessagesFolder() {
        return pdsUserMessagesFolder;
    }

    public String getPdsPrepareUploadFolderDirectory() {
        return pdsPrepareUploadFolderDirectory;
    }

    public String getSechubStoragePath() {
        return sechubStoragePath;
    }

    public String getSechubJobUUID() {
        return sechubJobUUID;
    }
}
